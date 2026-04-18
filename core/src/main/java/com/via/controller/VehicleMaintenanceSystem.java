package com.via.controller;

import com.via.model.GameState;
import com.via.model.building.Garage;
import com.via.model.building.Road;
import com.via.model.building.StopAccess;
import com.via.model.building.StopOverlay;
import com.via.model.economy.Storage;
import com.via.model.enums.AccessType;
import com.via.model.enums.Direction;
import com.via.model.map.Map;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;
import com.via.model.vehicle.Route;
import com.via.model.vehicle.Vehicle;

import java.util.*;

/**
 * Manages vehicle lifecycle, including aging, scheduled maintenance,
 * garage return routing, and over-aged removal.
 *
 * <p>This system ensures vehicles:</p>
 * <ul>
 *     <li>Age over time and are eventually retired</li>
 *     <li>Return to their assigned garage for maintenance</li>
 *     <li>Are safely removed when over-aged</li>
 *     <li>Recover correctly if their garage is destroyed</li>
 * </ul>
 *
 * <p>Maintenance routing is based on road connectivity. Vehicles only traverse
 * {@link Road} tiles and cannot directly target garage tiles. Instead, they
 * navigate to a road tile adjacent to a garage.</p>
 */
public class VehicleMaintenanceSystem {

    /**
     * Executes pre-movement maintenance logic.
     *
     * <p>Responsibilities:</p>
     * <ul>
     *     <li>Increment vehicle age</li>
     *     <li>Remove over-aged vehicles and apply resale value</li>
     *     <li>Validate ongoing garage-return state</li>
     *     <li>Trigger maintenance routing when due</li>
     * </ul>
     *
     * <p>If a vehicle is due for maintenance, it is routed to the nearest
     * accessible road tile adjacent to its home garage.</p>
     *
     * @param state the current game state
     */
    public void tickBeforeMovement(GameState state) {
        if (state == null || state.isGameOver()) return;
        if (state.getEconomy() == null || state.getTimeState() == null) return;

        long currentTick = state.getTimeState().getTick();
        Map map = state.getMap();
        if (map == null) return;

        Iterator<Vehicle> it = state.getVehicles().iterator();

        while (it.hasNext()) {
            Vehicle v = it.next();
            if (v == null) continue;

            v.incrementAgeTicks(1);

            // Remove vehicles that exceed lifespan
            if (v.isOverAged()) {
                long resale = v.getResaleValue();
                if (resale > 0) state.getEconomy().earn(resale);
                it.remove();
                continue;
            }

            // Validate ongoing garage return state
            if (v.isReturningToGarage()) {
                TileCoord target = v.getMaintenanceTargetCoord();
                boolean garageExists = false;

                for (TileCoord neighbor : map.getAdjacentCoords(target)) {
                    if (isGarageTile(map, neighbor)) {
                        garageExists = true;
                        break;
                    }
                }

                // Abort maintenance if garage was destroyed
                if (!garageExists) {
                    v.restoreAfterGarageReturn();
                    v.setStopWaitTicks(0);
                    v.rescheduleNextMaintenanceDueTick(currentTick);
                }

                continue;
            }

            if (v.isMaintaining()) continue;

            // Not yet time for maintenance
            if (currentTick < v.getMaintenanceDueTick()) continue;

            TileCoord homeLoc = v.getHomeGarageLocation();
            if (homeLoc == null) continue;

            Tile garageTile = map.getTile(homeLoc);

            // Skip maintenance if garage no longer exists
            if (garageTile == null || !(garageTile.getStructure() instanceof Garage)) {
                continue;
            }

            Garage homeGarage = (Garage) garageTile.getStructure();
            List<TileCoord> accessRoads = homeGarage.getAccessRoadTiles(map);
            if (accessRoads.isEmpty()) continue;

            // Choose a road tile adjacent to the garage as entry point
            TileCoord accessRoadTile = accessRoads.get(0);

            StopOverlay garageStop = new StopOverlay(
                "Garage",
                accessRoadTile,
                new StopAccess(
                    AccessType.ROAD_TILE,
                    homeLoc,
                    computeDirection(homeLoc, accessRoadTile)
                ),
                new Storage()
            );

            Route maintenanceRoute = new Route(List.of(garageStop));
            v.enterGarageReturn(maintenanceRoute, accessRoadTile);
        }
    }

    /**
     * Finalizes garage return state after movement has been processed.
     *
     * <p>If a vehicle has reached its maintenance target, it is restored
     * to normal operation state.</p>
     *
     * @param state the current game state
     */
    public void tickAfterMovement(GameState state) {
        if (state == null || state.isGameOver()) return;

        for (Vehicle v : state.getVehicles()) {
            if (v == null) continue;
            if (!v.isReturningToGarage()) continue;
            if (v.getPosition() == null) continue;

            if (v.getMaintenanceTargetCoord() != null &&
                v.getPosition().getOrigin().equals(v.getMaintenanceTargetCoord())) {
                v.restoreAfterGarageReturn();
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Internal data structure
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Represents a discovered garage connection during BFS search.
     *
     * <p>Vehicles cannot route directly to garages; instead they target
     * the adjacent road tile used as the access point.</p>
     */
    private static class GarageAccess {

        /** The actual garage tile. */
        final TileCoord garageTile;

        /** Road tile adjacent to the garage used as routing target. */
        final TileCoord accessRoadTile;

        GarageAccess(TileCoord garageTile, TileCoord accessRoadTile) {
            this.garageTile = garageTile;
            this.accessRoadTile = accessRoadTile;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BFS logic for finding nearest garage access point
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Finds the nearest road tile that is adjacent to a garage using BFS.
     *
     * <p>Search behavior:</p>
     * <ul>
     *     <li>Traversal is restricted to {@link Road} tiles only</li>
     *     <li>Target is a road tile adjacent to a {@link Garage}</li>
     *     <li>Returns the road tile as the navigation endpoint</li>
     * </ul>
     *
     * @param state game state
     * @param startTile starting road tile
     * @return garage access information, or {@code null} if none found
     */
    private GarageAccess findNearestGarageAccessTile(GameState state, TileCoord startTile) {
        Map map = state.getMap();
        if (map == null || startTile == null) return null;

        Set<TileCoord> visited = new HashSet<>();
        Queue<TileCoord> q = new LinkedList<>();

        q.add(startTile);
        visited.add(startTile);

        while (!q.isEmpty()) {
            TileCoord cur = q.poll();

            // Check if this road tile borders a garage
            for (TileCoord neighbor : map.getAdjacentCoords(cur)) {
                if (isGarageTile(map, neighbor)) {
                    return new GarageAccess(neighbor, cur);
                }
            }

            // Continue BFS through road network only
            for (TileCoord neighbor : map.getAdjacentCoords(cur)) {
                if (!visited.contains(neighbor) && isRoadTile(map, neighbor)) {
                    visited.add(neighbor);
                    q.add(neighbor);
                }
            }
        }

        return null;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════════

    private boolean isRoadTile(Map map, TileCoord c) {
        if (map == null || c == null) return false;
        Tile t = map.getTile(c);
        return t != null && t.getStructure() instanceof Road;
    }

    private boolean isGarageTile(Map map, TileCoord c) {
        if (map == null || c == null) return false;
        Tile t = map.getTile(c);
        return t != null && t.getStructure() instanceof Garage;
    }

    /**
     * Computes direction from one tile to an adjacent tile.
     *
     * @param from origin tile
     * @param to destination tile
     * @return cardinal direction
     */
    private Direction computeDirection(TileCoord from, TileCoord to) {
        if (from == null || to == null) return Direction.N;

        if (to.x == from.x && to.y == from.y - 1) return Direction.N;
        if (to.x == from.x && to.y == from.y + 1) return Direction.S;
        if (to.x == from.x - 1 && to.y == from.y) return Direction.W;
        if (to.x == from.x + 1 && to.y == from.y) return Direction.E;

        return Direction.N;
    }
}
