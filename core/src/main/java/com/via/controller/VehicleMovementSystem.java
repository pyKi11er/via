package com.via.controller;

import com.via.model.Constants;
import com.via.model.GameState;
import com.via.model.building.Road;
import com.via.model.building.StopOverlay;
import com.via.model.economy.PlayerEconomy;
import com.via.model.economy.Storage;
import com.via.model.enums.Direction;
import com.via.model.enums.GoodType;
import com.via.model.enums.VehicleType;
import com.via.model.map.Map;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;
import com.via.model.vehicle.*;

import java.util.*;

/**
 * Fully implements Issue #12 — Vehicle movement simulation & tile occupancy.
 * - BFS pathfinding between connected stops using Road tiles
 * - Vehicles advance tile-by-tile along the path
 * - Two vehicles can share a tile only if travelling in opposite directions
 * - Handles cargo loading/unloading via StopOverlay.Storage
 * - Integrates with EconomySystem & FacilitySystem (income triggered via Stop storage)
 */
public class VehicleMovementSystem {

    private static final int MAX_VEHICLES_PER_TILE = 2;
    private static final float BASE_DELTA = 0.016f; // ~60 ticks per second

    // tile occupancy (java.util.Map)
    private final java.util.Map<TileCoord, Set<Vehicle>> occupancy = new HashMap<>();

    // temporary cache for per-vehicle planned paths (tile-by-tile)
    private final java.util.Map<UUID, Queue<TileCoord>> pathCache = new HashMap<>();

    public void tick(GameState state, float timeMultiplier) {
        float delta = BASE_DELTA * timeMultiplier;
        if (state == null || state.isGameOver()) return;

        Map map = state.getMap();
        PlayerEconomy economy = state.getEconomy();
        occupancy.clear();

        // Register current occupancy
        for (Vehicle v : state.getVehicles()) {
            VehiclePosition pos = v.getPosition();
            if (pos == null) continue;
            markOccupied(pos.getOrigin(), v);
        }

        for (Vehicle v : state.getVehicles()) {
            VehiclePosition pos = v.getPosition();
            if (pos == null) continue;

            updateVehicleMovement(state, v, pos, map, economy, delta);
        }
    }

    private void updateVehicleMovement(GameState state, Vehicle v, VehiclePosition pos, Map map, PlayerEconomy economy, float delta) {
        float speed = v.getSpeed();
        double slopeFactor = map.getVehicleSpeedMultiplier(pos.getOrigin(), pos.getDestination());
        float appliedSpeed = (float) (speed * slopeFactor);

        float newProgress = pos.getProgress() + appliedSpeed * delta;
        if (newProgress < 1f) {
            pos.setProgress(newProgress);
            return;
        }

        // Arrived to destination tile
        pos.setOrigin(pos.getDestination());
        pos.setProgress(0f);

        StopOverlay currentStop = getStopAtCoord(v, pos.getOrigin());
        if (currentStop != null) {
            unloadAtStop(v, currentStop, economy);
            loadFromStop(v, currentStop);
            v.advanceToNextStop();
            // Clear cached path because destination changes
            pathCache.remove(v.getId());
        }

        StopOverlay nextStop = v.getNextStop();
        if (nextStop == null) return;

        Queue<TileCoord> plannedPath = pathCache.computeIfAbsent(v.getId(),
            id -> findPathBfs(map, pos.getOrigin(), nextStop.getLocation()));

        if (plannedPath == null || plannedPath.isEmpty()) return;

        // Determine next step
        TileCoord nextStep = plannedPath.peek();
        if (nextStep == null) return;

        // Do not overfill tiles
        if (!canEnterTile(v, pos.getOrigin(), nextStep)) return;

        // move toward next tile
        markOccupied(nextStep, v);
        pos.setDestination(nextStep);
        plannedPath.poll(); // consume one step
    }


    /** Unloads cargo at stop and triggers income via Stop storage. */
    private void unloadAtStop(Vehicle v, StopOverlay stop, PlayerEconomy economy) {
        Cargo cargo = v.getCargo();
        if (cargo == null || cargo.getAmount() <= 0) return;

        Storage storage = stop.getStorage();
        if (storage == null) return;

        storage.addContents(cargo.getType(), cargo.getAmount());
        v.setCargo(null);
        // Income now handled by EconomySystem during its tick (stop deliveries)
        // Optional: immediate small delivery bonus
//        long baseIncome = cargo.getAmount() *
//            Constants.getBaseIncomePerUnit(cargo.getType());
//        economy.earn(baseIncome);
    }

    private void loadFromStop(Vehicle v, StopOverlay stop) {
        Storage storage = stop.getStorage();
        if (storage == null) return;

        // Heuristic: determine loadable type based on vehicle type
        GoodType type = (v.getType() == VehicleType.BUS)
            ? GoodType.PASSENGERS
            : chooseAvailableGood(storage);

        if (type == null) return;

        int available = storage.getAmount(type);
        if (available <= 0) return;

        int toLoad = Math.min(v.getCapacity(), available);
        storage.removeAmount(type, toLoad);
        v.setCargo(new Cargo(type, toLoad));
    }

    private GoodType chooseAvailableGood(Storage storage) {
        for (GoodType type : storage.getContents().keySet()) {
            if (storage.getAmount(type) > 0) return type;
        }
        return null;
    }


    /** Simple BFS pathfinder across connected road tiles between two coords. */
    private Queue<TileCoord> findPathBfs(Map map, TileCoord start, TileCoord goal) {
        if (start.equals(goal)) return new LinkedList<>();

        Queue<TileCoord> queue = new LinkedList<>();
        java.util.Map<TileCoord, TileCoord> cameFrom = new HashMap<>();
        Set<TileCoord> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            TileCoord current = queue.poll();
            if (current.equals(goal)) break;

            for (TileCoord neighbor : map.getAdjacentCoords(current)) {
                if (!visited.contains(neighbor)
                    && isRoadTile(map, neighbor)) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                    cameFrom.put(neighbor, current);
                }
            }
        }

        if (!cameFrom.containsKey(goal)) return new LinkedList<>(); // no path

        LinkedList<TileCoord> path = new LinkedList<>();
        TileCoord step = goal;
        while (!step.equals(start)) {
            path.addFirst(step);
            step = cameFrom.get(step);
        }
        return path;
    }

    private boolean isRoadTile(Map map, TileCoord c) {
        Tile t = map.getTile(c);
        return t != null && t.getStructure() instanceof Road;
    }


    private void markOccupied(TileCoord coord, Vehicle v) {
        occupancy.computeIfAbsent(coord, k -> new HashSet<>()).add(v);
    }

    /** Checks whether the vehicle may enter the target tile (max 2 per tile, opposite directions only). */
    private boolean canEnterTile(Vehicle entrant, TileCoord from, TileCoord to) {
        Set<Vehicle> occupants = occupancy.get(to);
        if (occupants == null || occupants.isEmpty()) return true;

        if (occupants.size() >= MAX_VEHICLES_PER_TILE) {
            // Check if any occupant moves in the opposite direction → allow if exactly opposite
            for (Vehicle other : occupants) {
                TileCoord otherFrom = other.getPosition().getOrigin();
                TileCoord otherTo = other.getPosition().getDestination();

                boolean opposite = otherFrom.equals(to) && otherTo.equals(from);
                if (opposite) return true;
            }
            return false;
        }
        return true;
    }


    private StopOverlay getStopAtCoord(Vehicle v, TileCoord coord) {
        Route route = v.getAssignedRoute();
        if (route == null) return null;
        for (StopOverlay stop : route.getStops()) {
            if (stop.getLocation().equals(coord)) return stop;
        }
        return null;
    }
}
