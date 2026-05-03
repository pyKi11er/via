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

/**
 * Manages the movement, pathfinding, overtaking, and stop behavior of all vehicles in the game.
 * <p>
 * This system handles:
 * <ul>
 *   <li>Tile-based movement with progress interpolation</li>
 *   <li>Real-time pathfinding using BFS</li>
 *   <li>Overtaking logic with oncoming traffic detection</li>
 *   <li>Stop waiting behavior (loading/unloading and maintenance)</li>
 *   <li>Tile occupancy and collision prevention</li>
 * </ul>
 */

public class VehicleMovementSystem {

    private static final int   MAX_VEHICLES_PER_TILE  = 2;
    private static final float BASE_DELTA              = 0.016f;
    private static final int   STOP_WAIT_TICKS         = 60;
    private static final int   OVERTAKE_LOOK_AHEAD     = 3;
    private static final float OVERTAKE_LANE_OFFSET    = 0.35f;
    private static final float BLOCKED_SPEED_FACTOR = 0.3f; // 30% speed when stuck behind slower vehicle
    // track progressive overtake acceleration per vehicle
    private final java.util.Map<UUID, Float> overtakeRamp = new HashMap<>();


    private final EconomySystem economySystem;

    private final java.util.Map<TileCoord, Set<Vehicle>> occupancy = new HashMap<>();
    private final java.util.Map<UUID, Queue<TileCoord>>  pathCache = new HashMap<>();
    /**
     * Creates a new VehicleMovementSystem.
     *
     * @param economySystem the economy system used for processing cargo transactions
     */
    public VehicleMovementSystem(EconomySystem economySystem) {
        this.economySystem = Objects.requireNonNull(economySystem, "economySystem");
    }
    /**
     * Updates movement for all vehicles in the game.
     * <p>
     * This is the main update method called every game tick.
     *
     * @param state          current game state
     * @param timeMultiplier multiplier for variable timestep support
     */
    public void tick(GameState state, float timeMultiplier) {
        if (state == null || state.isGameOver()) return;

        float delta = BASE_DELTA * timeMultiplier;
        Map map     = state.getMap();

        occupancy.clear();
        for (Vehicle v : state.getVehicles()) {
            // This prevents the parked vehicle from causing a massive traffic jam.
            if (v.isMaintaining()) continue;

            VehiclePosition pos = v.getPosition();
            if (pos == null) continue;
            // fix: Mark both origin and destination to prevent vehicles piling into the same tile
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
    /**
     * Updates movement logic for a single vehicle.
     */
    private void updateVehicleMovement(GameState state, Vehicle v,
                                       VehiclePosition pos, Map map, float delta) {
        if (v.getStopWaitTicks() > 0) {
            if (v.isMaintaining() && v.getStopWaitTicks() == 1) {
                // Try to pay right before maintenance finishes
                if (state.getEconomy().getCapital() >= v.getMaintenanceCost()) {
                    state.getEconomy().deduct(v.getMaintenanceCost());
                    v.rescheduleNextMaintenanceDueTick(state.getTimeState().getTick());
                    v.setStopWaitTicks(0); // Automatically clears maintaining flag
                } else {
                    // Bankrupt! Freeze at 1 tick left. Vehicle stays off the road.
                    return;
                }
            } else {
                v.setStopWaitTicks(v.getStopWaitTicks() - 1);
            }
            return;
        }

        float baseSpeed   = v.getSpeed();
        double slope      = map.getVehicleSpeedMultiplier(pos.getOrigin(), pos.getDestination());
        float appliedSpeed = (float) (baseSpeed * slope);

        // ---> NEW: Traffic / Tailgating Logic <---
        Vehicle ahead = getVehicleAheadInSameTile(v, pos);
        if (ahead != null && !pos.isOvertaking()) {
            // We are stuck behind someone in the same tile. Hit the brakes!
            appliedSpeed *= BLOCKED_SPEED_FACTOR;
        }
        // -----------------------------------------
        // progressively ramp up speed if currently overtaking
        if (pos.isOvertaking()) {
            float ramp = overtakeRamp.getOrDefault(v.getId(), 1.0f);
            ramp = ramp + delta * 4.0f; // smoothly increase up to 4×
            overtakeRamp.put(v.getId(), ramp);
            appliedSpeed *= ramp;
        } else {
            // reset ramp when not overtaking
            overtakeRamp.remove(v.getId());
        }

        float newProgress = pos.getProgress() + appliedSpeed * delta;
        if (newProgress < 1f) {
            pos.setProgress(newProgress);
            return;
        }

        // Arrived — clear overtaking state at tile end
        pos.setOvertaking(false);
        overtakeRamp.remove(v.getId());

        pos.setOrigin(pos.getDestination());
        pos.setProgress(0f);

        StopOverlay nextStop = v.getNextStop();
        if (nextStop != null && nextStop.getLocation().equals(pos.getOrigin())) {
            unloadAtStop(state, v, nextStop);
            loadFromStop(v, nextStop);
            v.advanceToNextStop();
            pathCache.remove(v.getId());
            v.setStopWaitTicks(STOP_WAIT_TICKS);
        }

        nextStop = v.getNextStop();
        if (nextStop == null) return;

        Queue<TileCoord> plannedPath = pathCache.get(v.getId());
        if (plannedPath == null || plannedPath.isEmpty()) {
            plannedPath = findPathBfs(map, pos.getOrigin(), nextStop.getLocation());
            if (!plannedPath.isEmpty()) {
                pathCache.put(v.getId(), plannedPath);
            }else{
                return;
            }
        }

        if (plannedPath.isEmpty()) return;

        TileCoord nextStep = plannedPath.peek();
        if (nextStep == null) return;

        Vehicle slowerVehicleAhead = getSlowerVehicleAhead(v, pos, nextStep);

        if (slowerVehicleAhead != null && !pos.isOvertaking()) {
            if (canOvertake(state, v, pos, map, nextStep)) {
                startOvertake(v, pos, nextStep);
                overtakeRamp.put(v.getId(), 1.0f);
                markOccupied(nextStep, v);
                pos.setDestination(nextStep);
                plannedPath.poll();
                pos.setProgress(Math.min(1f, pos.getProgress() + appliedSpeed * delta));
                return;
            }
        }

        // Normal movement or Tailgating into the next tile
        if (!canEnterTile(v, pos.getOrigin(), nextStep)) {
            return; // Blocked! We must wait here.
        }

        pos.setOvertaking(false);
        overtakeRamp.remove(v.getId());
        markOccupied(nextStep, v);
        pos.setDestination(nextStep);
        plannedPath.poll();
    }


    // ══════════════════════════════════════════════════════════════════════════
    // Overtaking logic
    // ══════════════════════════════════════════════════════════════════════════

    private Vehicle getSlowerVehicleAhead(Vehicle mover, VehiclePosition pos, TileCoord nextStep) {
        // Check if we are tailgating in the exact same tile
        Vehicle aheadInSame = getVehicleAheadInSameTile(mover, pos);
        if (aheadInSame != null && aheadInSame.getSpeed() < mover.getSpeed()) return aheadInSame;

        // Check if there is a slower vehicle in the next tile
        Vehicle blocker = getBlockingVehicle(mover, pos.getOrigin(), nextStep);
        if (blocker != null && blocker.getSpeed() < mover.getSpeed()) return blocker;

        return null;
    }


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

    /**
     * Returns true if vehicle v is allowed to overtake right now.
     * Conditions:
     *   1. There is a slower vehicle immediately ahead blocking us.
     *   2. The oncoming lane (opposite direction) is clear for OVERTAKE_LOOK_AHEAD tiles.
     */
    private boolean canOvertake(GameState state, Vehicle v, VehiclePosition pos, Map map, TileCoord nextStep) {
        int dx = nextStep.x - pos.getOrigin().x;
        int dy = nextStep.y - pos.getOrigin().y;

        TileCoord scan = pos.getOrigin();
        for (int i = 0; i < OVERTAKE_LOOK_AHEAD; i++) {
            // FIX: scan.x + dx looks AHEAD. The old code used -dx which looked backwards!
            scan = new TileCoord(scan.x + dx, scan.y + dy);
            if (!map.isWithinBounds(scan)) break;
            if (hasVehicleMovingOpposite(state, scan, dx, dy)) return false;
        }
        return true;
    }

    /**
     * Marks the vehicle as overtaking and applies the visual lane offset
     * perpendicular to its direction of travel.
     */
    private void startOvertake(Vehicle v, VehiclePosition pos, TileCoord nextStep) {
        pos.setOvertaking(true);
        pos.setLaneOffset(OVERTAKE_LANE_OFFSET);
    }

    /**
     * Returns the vehicle blocking entry into {@code to} from {@code from},
     * or null if no blocker is found.
     */
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

            // Only block if moving in the same direction (not head-on)
            boolean sameDir = (myDx == opDx && myDy == opDy)
                || (opDx == 0 && opDy == 0); // stopped vehicle also blocks
            if (sameDir) return other;
        }
        return null;
    }

    /**
     * Returns true if any vehicle in the given tile is moving in direction (dx, dy)
     * — i.e. it's oncoming traffic relative to the overtaker.
     */
    private boolean hasVehicleMovingOpposite(GameState state, TileCoord coord, int myDx, int myDy) {
        Set<Vehicle> occupants = occupancy.get(coord);
        if (occupants == null || occupants.isEmpty()) return false;
        for (Vehicle other : occupants) {
            VehiclePosition op = other.getPosition();
            if (op == null) continue;
            int vdx = op.getDestination().x - op.getOrigin().x;
            int vdy = op.getDestination().y - op.getOrigin().y;
            // If they are moving opposite to our vector, the lane is NOT safe
            if (vdx == -myDx && vdy == -myDy && (vdx != 0 || vdy != 0)) return true;
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Cargo
    // ══════════════════════════════════════════════════════════════════════════
    /**
     * Unloads cargo at a stop and processes economy transactions.
     */
    private void unloadAtStop(GameState state, Vehicle v, StopOverlay stop) {
        Cargo cargo = v.getCargo();
        if (cargo == null || cargo.getAmount() <= 0) return;
        economySystem.processVehicleUnload(state, stop, cargo);
        v.setCargo(null);
    }
    /**
     * Loads appropriate cargo from a stop (passengers for buses, goods otherwise).
     */
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
    // BFS pathfinding
    // ══════════════════════════════════════════════════════════════════════════
    /**
     * Finds a path from start to goal using Breadth-First Search.
     *
     * @return a queue containing the path (excluding the start tile), or empty queue if no path exists
     */
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
                    && (isTraversable(map, neighbor) || neighbor.equals(goal))) {
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
    // Tile occupancy
    // ══════════════════════════════════════════════════════════════════════════

    private void markOccupied(TileCoord coord, Vehicle v) {
        occupancy.computeIfAbsent(coord, k -> new HashSet<>()).add(v);
    }
    /**
     * Checks whether a vehicle can enter a target tile.
     * <p>
     * Allows head-on passing but blocks same-direction or stationary vehicles.
     */
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

            // allow head-on passing safely in the other lane
            boolean headOn = (myDx == -opDx && myDy == -opDy) && (myDx != 0 || myDy != 0);
            if (headOn) continue;

            // fix: allow tailgating, slower vehicles can enter the tile if following the same direction
            boolean sameDir = (myDx == opDx && myDy == opDy) && (myDx != 0 || myDy != 0);
            if (sameDir) continue;

            // if the occupant is currently vacating the tile, we can tail them
            if (op.getOrigin().equals(to) && !op.getDestination().equals(to)) {
                continue;
            }

            return false;
        }
        return true;
    }

}
