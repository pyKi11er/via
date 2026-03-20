package com.via.controller;

import com.via.model.GameState;
import com.via.model.building.Garage;
import com.via.model.building.Road;
import com.via.model.building.StopAccess;
import com.via.model.building.StopOverlay;
import com.via.model.enums.AccessType;
import com.via.model.enums.Direction;
import com.via.model.map.Map;
import com.via.model.map.Structure;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;
import com.via.model.vehicle.Route;
import com.via.model.vehicle.Vehicle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Handles vehicle age, maintenance due ticks, garage rerouting, and over-aged selling.
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

            // Age progression (1 simulation tick per update).
            v.incrementAgeTicks(1);

            // Sell immediately when over-aged.
            if (v.isOverAged()) {
                long resale = v.getResaleValue();
                if (resale > 0) state.getEconomy().earn(resale);
                it.remove();
                continue;
            }

            if (v.isReturningToGarage()) continue;

            if (currentTick < v.getMaintenanceDueTick()) continue;

            // Pay maintenance and schedule the next due tick (maintenance interval becomes more frequent with age).
            state.getEconomy().deduct(v.getMaintenanceCost());
            v.rescheduleNextMaintenanceDueTick(currentTick);

            GarageAccess nearest = findNearestGarageTile(state, v.getPosition().getOrigin());
            if (nearest == null) continue;

            StopOverlay garageStop = new StopOverlay(
                "Garage",
                nearest.garageTile,
                new StopAccess(AccessType.ROAD_TILE, nearest.garage.getLocation(), computeDirection(nearest.garage.getLocation(), nearest.garageTile)),
                null
            );
            Route maintenanceRoute = new Route(List.of(garageStop));
            v.enterGarageReturn(maintenanceRoute, nearest.garageTile);
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

    private static class GarageAccess {
        final Garage garage;
        final TileCoord garageTile;

        GarageAccess(Garage garage, TileCoord garageTile) {
            this.garage = garage;
            this.garageTile = garageTile;
        }
    }

    private GarageAccess findNearestGarageTile(GameState state, TileCoord startTile) {
        Map map = state.getMap();
        if (map == null || startTile == null) return null;

        // Index all tiles occupied by garages directly from the map structure.
        // This keeps maintenance routing working even if building lists aren't populated.
        HashMap<TileCoord, Garage> garageIndex = new HashMap<>();
        for (int x = 0; x < map.size.width; x++) {
            for (int y = 0; y < map.size.height; y++) {
                TileCoord coord = new TileCoord(x, y);
                Tile t = map.getTile(coord);
                if (t == null) continue;
                if (t.getStructure() instanceof Garage g) {
                    garageIndex.putIfAbsent(coord, g);
                }
            }
        }

        if (garageIndex.isEmpty()) return null;

        Set<TileCoord> visited = new HashSet<>();
        Queue<TileCoord> q = new LinkedList<>();

        q.add(startTile);
        visited.add(startTile);

        while (!q.isEmpty()) {
            TileCoord cur = q.poll();
            if (garageIndex.containsKey(cur)) {
                return new GarageAccess(garageIndex.get(cur), cur);
            }

            for (TileCoord neighbor : map.getAdjacentCoords(cur)) {
                if (visited.contains(neighbor)) continue;
                if (!isRoadTile(map, neighbor)) continue;
                visited.add(neighbor);
                q.add(neighbor);
            }
        }

        return null;
    }

    private boolean isRoadTile(Map map, TileCoord c) {
        if (map == null || c == null) return false;
        Tile t = map.getTile(c);
        if (t == null) return false;
        Structure s = t.getStructure();
        return s instanceof Road || s instanceof Garage;
    }

    private Direction computeDirection(TileCoord anchor, TileCoord roadTile) {
        if (anchor == null || roadTile == null) return Direction.N;
        if (roadTile.x == anchor.x && roadTile.y == anchor.y - 1) return Direction.N;
        if (roadTile.x == anchor.x && roadTile.y == anchor.y + 1) return Direction.S;
        if (roadTile.x == anchor.x - 1 && roadTile.y == anchor.y) return Direction.W;
        if (roadTile.x == anchor.x + 1 && roadTile.y == anchor.y) return Direction.E;
        return Direction.N;
    }
}

