package com.via.controller;

import com.via.model.GameState;
import com.via.model.building.Road;
import com.via.model.building.StopOverlay;
import com.via.model.economy.Storage;
import com.via.model.enums.GoodType;
import com.via.model.enums.VehicleType;
import com.via.model.map.Map;
import com.via.model.map.Structure;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;
import com.via.model.vehicle.*;

import java.util.*;

public class VehicleMovementSystem {

    private static final int   MAX_VEHICLES_PER_TILE  = 2;
    private static final float BASE_DELTA              = 0.016f;
    private static final int   STOP_WAIT_TICKS         = 60;
    private static final int   OVERTAKE_LOOK_AHEAD     = 3;
    private static final float OVERTAKE_LANE_OFFSET    = 0.35f;
    private static final float BLOCKED_SPEED_FACTOR    = 0.3f; // Enforces traffic pacing

    private final java.util.Map<UUID, Float> overtakeRamp = new HashMap<>();
    private final EconomySystem economySystem;
    private final java.util.Map<TileCoord, Set<Vehicle>> occupancy = new HashMap<>();
    private final java.util.Map<UUID, Queue<TileCoord>>  pathCache = new HashMap<>();

    public VehicleMovementSystem(EconomySystem economySystem) {
        this.economySystem = Objects.requireNonNull(economySystem, "economySystem");
    }

    public void tick(GameState state, float timeMultiplier) {
        if (state == null || state.isGameOver()) return;

        float delta = BASE_DELTA * timeMultiplier;
        Map map     = state.getMap();

        occupancy.clear();
        for (Vehicle v : state.getVehicles()) {
            if (v.isMaintaining()) continue;

            VehiclePosition pos = v.getPosition();
            if (pos == null) continue;

            // Mark both origin and destination to claim the path
            markOccupied(pos.getOrigin(), v);
            if (!pos.getOrigin().equals(pos.getDestination())) {
                markOccupied(pos.getDestination(), v);
            }
        }

        for (Vehicle v : state.getVehicles()) {
            VehiclePosition pos = v.getPosition();
            if (pos == null) continue;
            updateVehicleMovement(state, v, pos, map, delta);
        }
    }

    private void updateVehicleMovement(GameState state, Vehicle v, VehiclePosition pos, Map map, float delta) {
        // 1. Process Waiting State
        if (v.getStopWaitTicks() > 0) {
            if (v.isMaintaining() && v.getStopWaitTicks() == 1) {
                if (state.getEconomy().getCapital() >= v.getMaintenanceCost()) {
                    state.getEconomy().deduct(v.getMaintenanceCost());
                    v.rescheduleNextMaintenanceDueTick(state.getTimeState().getTick());
                    v.setStopWaitTicks(0);
                } else {
                    return; // Bankrupt! Freeze at 1 tick left.
                }
            } else {
                v.setStopWaitTicks(v.getStopWaitTicks() - 1);
            }
            return;
        }

        // 2. Transit State: Currently moving between tiles
        if (!pos.getOrigin().equals(pos.getDestination())) {
            float baseSpeed   = v.getSpeed();
            double slope      = map.getVehicleSpeedMultiplier(pos.getOrigin(), pos.getDestination());
            float appliedSpeed = (float) (baseSpeed * slope);

            // Tailgating Logic
            Vehicle ahead = getVehicleAheadInSameTile(v, pos);
            if (ahead != null && !pos.isOvertaking()) {
                appliedSpeed *= BLOCKED_SPEED_FACTOR;
            }

            // Overtaking Acceleration
            if (pos.isOvertaking()) {
                float ramp = overtakeRamp.getOrDefault(v.getId(), 1.0f);
                ramp = ramp + delta * 4.0f;
                overtakeRamp.put(v.getId(), ramp);
                appliedSpeed *= ramp;
            } else {
                overtakeRamp.remove(v.getId());
            }

            // Safeguard against absolutely zero speed mathematically halting the vehicle
            if (appliedSpeed <= 0.001f) appliedSpeed = 0.01f;

            float newProgress = pos.getProgress() + appliedSpeed * delta;
            if (newProgress < 1f) {
                pos.setProgress(newProgress);
                return;
            }

            // Arrived at destination tile boundary!
            pos.setOvertaking(false);
            overtakeRamp.remove(v.getId());
            pos.setOrigin(pos.getDestination());
            pos.setProgress(0f);

            // Did we arrive at a valid route stop?
            StopOverlay nextStop = v.getNextStop();
            if (nextStop != null && nextStop.getLocation().equals(pos.getOrigin())) {
                unloadAtStop(state, v, nextStop);
                loadFromStop(v, nextStop);
                v.advanceToNextStop();
                pathCache.remove(v.getId());
                v.setStopWaitTicks(STOP_WAIT_TICKS);
                return; // Entering Wait State
            }
        }

        // 3. Idle State: We have finished our previous move and need a new target tile
        StopOverlay nextStop = v.getNextStop();
        if (nextStop == null) return;

        Queue<TileCoord> plannedPath = pathCache.get(v.getId());
        if (plannedPath == null || plannedPath.isEmpty()) {
            plannedPath = findPathBfs(map, pos.getOrigin(), nextStop.getLocation());
            if (!plannedPath.isEmpty()) {
                pathCache.put(v.getId(), plannedPath);
            } else {
                return; // No path found, stay idle
            }
        }

        if (plannedPath.isEmpty()) return;
        TileCoord nextStep = plannedPath.peek();
        if (nextStep == null) return;

        // Overtaking Check: Is there a vehicle blocking our way?
        Vehicle blocker = getBlockingVehicle(v, pos.getOrigin(), nextStep);
        boolean isBlockerStopped = (blocker != null) &&
            blocker.getPosition().getOrigin().equals(blocker.getPosition().getDestination());

        if (blocker != null && !pos.isOvertaking()) {
            // ALWAYS allow overtaking if the blocker is stopped, regardless of top speed!
            if (blocker.getSpeed() < v.getSpeed() || isBlockerStopped) {
                if (canOvertake(state, v, pos, map, nextStep)) {
                    startOvertake(v, pos, nextStep);
                    overtakeRamp.put(v.getId(), 1.0f);
                    markOccupied(nextStep, v);
                    pos.setDestination(nextStep);
                    plannedPath.poll();
                    return;
                }
            }
        }

        // Normal Entry Check
        if (!canEnterTile(v, pos.getOrigin(), nextStep)) {
            return; // Blocked! We stay idle and wait for traffic to clear.
        }

        // Success! Acquire the next step and start moving.
        pos.setOvertaking(false);
        overtakeRamp.remove(v.getId());
        markOccupied(nextStep, v);
        pos.setDestination(nextStep);
        plannedPath.poll();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Overtaking & Traffic Logic
    // ══════════════════════════════════════════════════════════════════════════

    private Vehicle getVehicleAheadInSameTile(Vehicle mover, VehiclePosition pos) {
        Set<Vehicle> occupants = occupancy.get(pos.getOrigin());
        if (occupants == null) return null;

        for (Vehicle other : occupants) {
            if (other == mover) continue;
            VehiclePosition op = other.getPosition();
            if (op == null) continue;

            if (op.getDestination().equals(pos.getDestination())) {
                if (op.getProgress() >= pos.getProgress()) {
                    return other;
                }
            }
        }
        return null;
    }

    private boolean canOvertake(GameState state, Vehicle v, VehiclePosition pos, Map map, TileCoord nextStep) {
        int dx = nextStep.x - pos.getOrigin().x;
        int dy = nextStep.y - pos.getOrigin().y;

        TileCoord scan = pos.getOrigin();
        for (int i = 0; i < OVERTAKE_LOOK_AHEAD; i++) {
            scan = new TileCoord(scan.x + dx, scan.y + dy);
            if (!map.isWithinBounds(scan)) break;
            if (hasVehicleMovingOpposite(state, scan, dx, dy)) return false;
        }
        return true;
    }

    private void startOvertake(Vehicle v, VehiclePosition pos, TileCoord nextStep) {
        pos.setOvertaking(true);
        pos.setLaneOffset(OVERTAKE_LANE_OFFSET);
    }

    private Vehicle getBlockingVehicle(Vehicle mover, TileCoord from, TileCoord to) {
        Set<Vehicle> occupants = occupancy.get(to);
        if (occupants == null) return null;

        int myDx = to.x - from.x;
        int myDy = to.y - from.y;

        for (Vehicle other : occupants) {
            if (other == mover) continue;
            VehiclePosition op = other.getPosition();
            if (op == null) continue;

            int opDx = op.getDestination().x - op.getOrigin().x;
            int opDy = op.getDestination().y - op.getOrigin().y;

            boolean sameDir = (myDx == opDx && myDy == opDy) || (opDx == 0 && opDy == 0);
            if (sameDir) return other;
        }
        return null;
    }

    private boolean hasVehicleMovingOpposite(GameState state, TileCoord coord, int myDx, int myDy) {
        Set<Vehicle> occupants = occupancy.get(coord);
        if (occupants == null || occupants.isEmpty()) return false;
        for (Vehicle other : occupants) {
            VehiclePosition op = other.getPosition();
            if (op == null) continue;
            int vdx = op.getDestination().x - op.getOrigin().x;
            int vdy = op.getDestination().y - op.getOrigin().y;
            if (vdx == -myDx && vdy == -myDy && (vdx != 0 || vdy != 0)) return true;
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Cargo & Pathfinding
    // ══════════════════════════════════════════════════════════════════════════

    private void unloadAtStop(GameState state, Vehicle v, StopOverlay stop) {
        Cargo cargo = v.getCargo();
        if (cargo == null || cargo.getAmount() <= 0) return;
        economySystem.processVehicleUnload(state, stop, cargo);
        v.setCargo(null);
    }

    private void loadFromStop(Vehicle v, StopOverlay stop) {
        Storage storage = stop.getStorage();
        if (storage == null) return;
        GoodType type = (v.getType() == VehicleType.BUS) ? GoodType.PASSENGERS : chooseAvailableGood(storage);
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
                if (!visited.contains(neighbor) && (isTraversable(map, neighbor) || neighbor.equals(goal))) {
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

    private boolean isTraversable(Map map, TileCoord c) {
        Tile t = map.getTile(c);
        if (t == null) return false;
        Structure s = t.getStructure();
        return s instanceof Road || s instanceof com.via.model.building.City;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Tile Occupancy
    // ══════════════════════════════════════════════════════════════════════════

    private void markOccupied(TileCoord coord, Vehicle v) {
        occupancy.computeIfAbsent(coord, k -> new HashSet<>()).add(v);
    }

    private boolean canEnterTile(Vehicle entrant, TileCoord from, TileCoord to) {
        Set<Vehicle> occupants = occupancy.get(to);
        if (occupants == null || occupants.isEmpty()) return true;

        if (occupants.size() >= MAX_VEHICLES_PER_TILE) return false;

        for (Vehicle other : occupants) {
            if (other == entrant) continue;
            VehiclePosition op = other.getPosition();
            if (op == null) continue;

            int myDx  = to.x - from.x;
            int myDy  = to.y - from.y;
            int opDx  = op.getDestination().x - op.getOrigin().x;
            int opDy  = op.getDestination().y - op.getOrigin().y;

            // Allow head-on passing safely in the other lane
            boolean headOn = (myDx == -opDx && myDy == -opDy) && (myDx != 0 || myDy != 0);
            if (headOn) continue;

            // Allow tailgating!
            boolean sameDir = (myDx == opDx && myDy == opDy) && (myDx != 0 || myDy != 0);
            if (sameDir) continue;

            // If the occupant is currently vacating the tile, we can tail them
            if (op.getOrigin().equals(to) && !op.getDestination().equals(to)) {
                continue;
            }

            return false;
        }
        return true;
    }
}
