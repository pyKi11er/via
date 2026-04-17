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
import com.via.model.map.Structure;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;
import com.via.model.vehicle.Route;
import com.via.model.vehicle.Vehicle;

import java.util.*;

/**
 * Handles vehicle age, maintenance due ticks, garage rerouting, and over-aged selling.
 *
 * KEY FIX: findNearestGarageAccessTile() now returns the ROAD TILE adjacent
 * to the garage, not the garage tile itself. VehicleMovementSystem BFS can only
 * traverse road tiles so routing to the garage tile directly always fails.
 */
public class VehicleMaintenanceSystem {

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

            // Sell immediately when over-aged
            if (v.isOverAged()) {
                long resale = v.getResaleValue();
                if (resale > 0) state.getEconomy().earn(resale);
                it.remove();
                continue;
            }

            if (v.isReturningToGarage()) {
                // Garage Demolition Safety Check
                TileCoord target = v.getMaintenanceTargetCoord();
                boolean garageExists = false;
                for (TileCoord neighbor : map.getAdjacentCoords(target)) {
                    if (isGarageTile(map, neighbor)) {
                        garageExists = true;
                        break;
                    }
                }

                if (!garageExists) {
                    // Abort maintenance! Garage was demolished.
                    v.restoreAfterGarageReturn();
                    v.setStopWaitTicks(0); // Cancel the pause
                    v.rescheduleNextMaintenanceDueTick(currentTick); // Try again immediately
                }
                continue;
            }

            // ---> THE FIX: Ignore vehicles that are actively in the garage being maintained! <---
            if (v.isMaintaining()) continue;

            // Check if it's time for maintenance
            if (currentTick < v.getMaintenanceDueTick()) continue;

            GarageAccess access = findNearestGarageAccessTile(state, v.getPosition().getOrigin());
            if (access == null) continue;

            StopOverlay garageStop = new StopOverlay(
                "Garage",
                access.accessRoadTile,
                new StopAccess(
                    AccessType.ROAD_TILE,
                    access.garageTile,
                    computeDirection(access.garageTile, access.accessRoadTile)
                ),
                new Storage()
            );

            Route maintenanceRoute = new Route(List.of(garageStop));
            v.enterGarageReturn(maintenanceRoute, access.accessRoadTile);
        }
    }

    public void tickAfterMovement(GameState state) {
        if (state == null || state.isGameOver()) return;
        for (Vehicle v : state.getVehicles()) {
            if (v == null) continue;
            if (!v.isReturningToGarage()) continue;
            if (v.getPosition() == null) continue;
            if (v.getMaintenanceTargetCoord() != null
                && v.getPosition().getOrigin().equals(v.getMaintenanceTargetCoord())) {
                v.restoreAfterGarageReturn();
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Data holder
    // ══════════════════════════════════════════════════════════════════════════

    private static class GarageAccess {
        /** The actual garage tile (non-road). */
        final TileCoord garageTile;
        /** The road tile directly adjacent to the garage — this is the BFS goal. */
        final TileCoord accessRoadTile;

        GarageAccess(TileCoord garageTile, TileCoord accessRoadTile) {
            this.garageTile      = garageTile;
            this.accessRoadTile  = accessRoadTile;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BFS — finds nearest ROAD TILE that is adjacent to a garage
    // ══════════════════════════════════════════════════════════════════════════

    private GarageAccess findNearestGarageAccessTile(GameState state, TileCoord startTile) {
        Map map = state.getMap();
        if (map == null || startTile == null) return null;

        Set<TileCoord> visited = new HashSet<>();
        Queue<TileCoord> q = new LinkedList<>();

        q.add(startTile);
        visited.add(startTile);

        while (!q.isEmpty()) {
            TileCoord cur = q.poll();

            // Check each neighbour of this road tile — if any is a garage we found our target
            for (TileCoord neighbor : map.getAdjacentCoords(cur)) {
                if (isGarageTile(map, neighbor)) {
                    // cur is the road tile next to the garage — this is our access point
                    return new GarageAccess(neighbor, cur);
                }
            }

            // Continue BFS only along road tiles
            for (TileCoord neighbor : map.getAdjacentCoords(cur)) {
                if (!visited.contains(neighbor) && isRoadTile(map, neighbor)) {
                    visited.add(neighbor);
                    q.add(neighbor);
                }
            }
        }

        return null; // no garage reachable from road network
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

    private Direction computeDirection(TileCoord from, TileCoord to) {
        if (from == null || to == null) return Direction.N;
        if (to.x == from.x && to.y == from.y - 1) return Direction.N;
        if (to.x == from.x && to.y == from.y + 1) return Direction.S;
        if (to.x == from.x - 1 && to.y == from.y) return Direction.W;
        if (to.x == from.x + 1 && to.y == from.y) return Direction.E;
        return Direction.N;
    }
}
