package com.via.controller;

import com.via.model.GameState;
import com.via.model.building.Road;
import com.via.model.building.StopOverlay;
import com.via.model.economy.Storage;
import com.via.model.enums.GoodType;
import com.via.model.enums.VehicleType;
import com.via.model.map.Map;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;
import com.via.model.vehicle.*;

import java.util.*;

public class VehicleMovementSystem {

    private static final int   MAX_VEHICLES_PER_TILE = 2;
    private static final float BASE_DELTA             = 0.016f;

    private final java.util.Map<TileCoord, Set<Vehicle>> occupancy = new HashMap<>();
    private final java.util.Map<UUID, Queue<TileCoord>>  pathCache = new HashMap<>();

    public void tick(GameState state, float timeMultiplier, EconomySystem economySystem) {
        if (state == null || state.isGameOver()) return;

        float delta = BASE_DELTA * timeMultiplier;
        Map map = state.getMap();

        occupancy.clear();

        for (Vehicle v : state.getVehicles()) {
            VehiclePosition pos = v.getPosition();
            if (pos == null) continue;
            markOccupied(pos.getOrigin(), v);
        }

        for (Vehicle v : state.getVehicles()) {
            VehiclePosition pos = v.getPosition();
            if (pos == null) continue;
            updateVehicleMovement(state, v, pos, map, economySystem, delta);
        }
    }

    private void updateVehicleMovement(GameState state, Vehicle v,
                                       VehiclePosition pos, Map map,
                                       EconomySystem economySystem, float delta) {
        float speed       = v.getSpeed();
        double slopeFactor = map.getVehicleSpeedMultiplier(pos.getOrigin(), pos.getDestination());
        float appliedSpeed = (float)(speed * slopeFactor);

        float newProgress = pos.getProgress() + appliedSpeed * delta;
        if (newProgress < 1f) {
            pos.setProgress(newProgress);
            return;
        }

        // Arrived at destination tile
        pos.setOrigin(pos.getDestination());
        pos.setProgress(0f);

        StopOverlay currentStop = getStopAtCoord(v, pos.getOrigin());
        if (currentStop != null) {
            unloadAtStop(state, v, currentStop, economySystem);
            loadFromStop(v, currentStop);
            v.advanceToNextStop();
            pathCache.remove(v.getId()); // destination changed — clear cached path
        }

        StopOverlay nextStop = v.getNextStop();
        if (nextStop == null) return;

        // Only cache successful paths — never cache empty (failed) paths
        Queue<TileCoord> plannedPath = pathCache.get(v.getId());
        if (plannedPath == null || plannedPath.isEmpty()) {
            plannedPath = findPathBfs(map, pos.getOrigin(), nextStop.getLocation());
            if (!plannedPath.isEmpty()) {
                pathCache.put(v.getId(), plannedPath);
            } else {
                return; // no path found — retry next tick
            }
        }

        TileCoord nextStep = plannedPath.peek();
        if (nextStep == null) return;

        if (!canEnterTile(v, pos.getOrigin(), nextStep)) return;

        markOccupied(nextStep, v);
        pos.setDestination(nextStep);
        plannedPath.poll();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Cargo
    // ══════════════════════════════════════════════════════════════════════════

    private void unloadAtStop(GameState state, Vehicle v, StopOverlay stop, EconomySystem economySystem) {
        Cargo cargo = v.getCargo();
        if (cargo == null || cargo.getAmount() <= 0) return;
        Storage storage = stop.getStorage();
        if (storage == null) return;
        GoodType type = cargo.getType();
        int amount = cargo.getAmount();
        storage.addContents(type, amount);
        v.setCargo(null);
        if (economySystem != null) {
            economySystem.onVehicleUnloadedCargoAtStop(state, stop, type, amount);
        }
    }

    private void loadFromStop(Vehicle v, StopOverlay stop) {
        Storage storage = stop.getStorage();
        if (storage == null) return;
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

    // ══════════════════════════════════════════════════════════════════════════
    // BFS pathfinding — road tiles only
    // ══════════════════════════════════════════════════════════════════════════

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
                    && (isRoadTile(map, neighbor) || neighbor.equals(goal))) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                    cameFrom.put(neighbor, current);
                }
            }
        }

        if (!cameFrom.containsKey(goal)) return new LinkedList<>();

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

    // ══════════════════════════════════════════════════════════════════════════
    // Tile occupancy
    // ══════════════════════════════════════════════════════════════════════════

    private void markOccupied(TileCoord coord, Vehicle v) {
        occupancy.computeIfAbsent(coord, k -> new HashSet<>()).add(v);
    }

    private boolean canEnterTile(Vehicle entrant, TileCoord from, TileCoord to) {
        Set<Vehicle> occupants = occupancy.get(to);
        if (occupants == null || occupants.isEmpty()) return true;
        if (occupants.size() >= MAX_VEHICLES_PER_TILE) {
            for (Vehicle other : occupants) {
                TileCoord otherFrom = other.getPosition().getOrigin();
                TileCoord otherTo   = other.getPosition().getDestination();
                if (otherFrom.equals(to) && otherTo.equals(from)) return true;
            }
            return false;
        }
        return true;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Stop lookup
    // ══════════════════════════════════════════════════════════════════════════

    private StopOverlay getStopAtCoord(Vehicle v, TileCoord coord) {
        Route route = v.getAssignedRoute();
        if (route == null) return null;
        for (StopOverlay stop : route.getStops()) {
            if (stop.getLocation().equals(coord)) return stop;
        }
        return null;
    }
}
