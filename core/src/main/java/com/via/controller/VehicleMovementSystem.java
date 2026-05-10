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
    private static final float LANE_LERP_SPEED         = 6.0f;
    private static final int   STUCK_THRESHOLD         = 45;  // ticks before forcing path recalc
    private static final int   CONGESTION_WEIGHT       = 4;   // extra cost per vehicle on a tile

    private final java.util.Map<UUID, Float>   overtakeRamp = new HashMap<>();
    private final java.util.Map<UUID, Integer> stuckTicks   = new HashMap<>();
    private final EconomySystem economySystem;
    private final java.util.Map<TileCoord, Set<Vehicle>> occupancy = new HashMap<>();
    private final java.util.Map<UUID, Queue<TileCoord>>  pathCache = new HashMap<>();

    private static final class DNode implements Comparable<DNode> {
        final TileCoord coord;
        final int cost;
        DNode(TileCoord c, int cost) { this.coord = c; this.cost = cost; }
        @Override public int compareTo(DNode o) { return Integer.compare(cost, o.cost); }
    }

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

            // Mark both origin and destination to claim the full path segment
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
        // Smooth lane transition — runs every tick regardless of movement state
        float targetLane = pos.isOvertaking() ? 1.0f : 0.0f;
        float vis = pos.getVisualLaneOffset();
        float rate = delta * LANE_LERP_SPEED;
        if (vis < targetLane) pos.setVisualLaneOffset(Math.min(vis + rate, targetLane));
        else                  pos.setVisualLaneOffset(Math.max(vis - rate, targetLane));

        // 1. Process Waiting State
        if (v.getStopWaitTicks() > 0) {
            if (v.isMaintaining() && v.getStopWaitTicks() == 1) {
                if (state.getEconomy().getCapital() >= v.getMaintenanceCost()) {
                    state.getEconomy().deduct(v.getMaintenanceCost());
                    v.rescheduleNextMaintenanceDueTick(state.getTimeState().getTick());
                    v.setStopWaitTicks(0);
                } else {
                    return; // Bankrupt freeze
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

            // ---> Continuous Radar & Queue Pacing <---
            AheadData ahead = getVehicleAhead(v, pos);
            if (ahead != null && !pos.isOvertaking()) {
                float leaderSpeed = (ahead.v.getStopWaitTicks() > 0 || ahead.v.isMaintaining()) ? 0f : ahead.v.getSpeed();

                if (ahead.gap < 0.20f) {
                    // Hard Brake! Do not clip!
                    appliedSpeed = 0f;
                } else if (ahead.gap < 0.60f) {
                    // Match speed to gently pull up behind them
                    if (appliedSpeed > leaderSpeed) {
                        appliedSpeed = leaderSpeed;
                    }
                }
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

            // Safeguard against absolute zero math locks
            if (appliedSpeed <= 0.001f && appliedSpeed > 0f) appliedSpeed = 0.01f;

            float newProgress = pos.getProgress() + appliedSpeed * delta;
            if (newProgress < 1f) {
                pos.setProgress(newProgress);
                return;
            }

            // Arrived at destination tile boundary!
            pos.setOrigin(pos.getDestination());
            pos.setProgress(0f);

            // Stubborn Overtaker (Don't merge into slow vehicles)
            if (!sharesTileWithSlowerVehicle(v, pos.getOrigin())) {
                pos.setOvertaking(false);
                overtakeRamp.remove(v.getId());
            }

            StopOverlay nextStop = v.getNextStop();
            if (nextStop != null && nextStop.getLocation().equals(pos.getOrigin())) {
                unloadAtStop(state, v, nextStop);
                loadFromStop(v, nextStop);
                v.advanceToNextStop();
                pathCache.remove(v.getId());
                v.setStopWaitTicks(STOP_WAIT_TICKS);
                return;
            }
        }

        // 3. Idle State: Acquire next tile
        StopOverlay nextStop = v.getNextStop();
        if (nextStop == null) return;

        Queue<TileCoord> plannedPath = pathCache.get(v.getId());
        if (plannedPath == null || plannedPath.isEmpty()) {
            plannedPath = findPathDijkstra(map, pos.getOrigin(), nextStop.getLocation());
            if (!plannedPath.isEmpty()) {
                pathCache.put(v.getId(), plannedPath);
            } else {
                return;
            }
        }

        if (plannedPath.isEmpty()) return;
        TileCoord nextStep = plannedPath.peek();
        if (nextStep == null) return;

        Vehicle blocker = getBlockingVehicle(v, pos.getOrigin(), nextStep);
        boolean isBlockerStopped = (blocker != null) &&
            (blocker.getPosition().getOrigin().equals(blocker.getPosition().getDestination()) || blocker.getStopWaitTicks() > 0);

        if (blocker != null && !pos.isOvertaking()) {
            // ALWAYS allow overtaking if the blocker is stopped
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

        // T-Bone Blocker & Tailgater
        if (!canEnterTile(v, pos.getOrigin(), nextStep)) {
            int stuck = stuckTicks.merge(v.getId(), 1, Integer::sum);
            if (stuck >= STUCK_THRESHOLD) {
                stuckTicks.put(v.getId(), 0);
                pathCache.remove(v.getId()); // recalc via Dijkstra next tick
            }
            return; // Blocked at intersection! Wait here.
        }

        // Safely entered next tile
        stuckTicks.remove(v.getId());
        if (!pos.isOvertaking()) overtakeRamp.remove(v.getId());
        markOccupied(nextStep, v);
        pos.setDestination(nextStep);
        plannedPath.poll();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Radar & Gap Calculation
    // ══════════════════════════════════════════════════════════════════════════

    private static class AheadData {
        Vehicle v;
        float gap;
        AheadData(Vehicle v, float gap) { this.v = v; this.gap = gap; }
    }

    private AheadData getVehicleAhead(Vehicle mover, VehiclePosition myPos) {
        Vehicle closest = null;
        float minGap = Float.MAX_VALUE;

        // 1. Scan the Current Segment
        Set<Vehicle> inOrigin = occupancy.get(myPos.getOrigin());
        if (inOrigin != null) {
            for (Vehicle other : inOrigin) {
                if (other == mover) continue;
                VehiclePosition op = other.getPosition();
                if (op == null) continue;

                if (op.getOrigin().equals(myPos.getOrigin()) && op.getDestination().equals(myPos.getDestination())) {
                    if (op.getProgress() >= myPos.getProgress()) {
                        float gap = op.getProgress() - myPos.getProgress();
                        // Tie breaker
                        if (gap == 0f && other.getId().compareTo(mover.getId()) < 0) continue;

                        if (gap < minGap) { minGap = gap; closest = other; }
                    }
                }
            }
        }

        // 2. Scan the Next Segment (Destinations)
        Set<Vehicle> inDest = occupancy.get(myPos.getDestination());
        if (inDest != null && !myPos.getOrigin().equals(myPos.getDestination())) {
            for (Vehicle other : inDest) {
                if (other == mover) continue;
                VehiclePosition op = other.getPosition();
                if (op == null) continue;

                // They are vacating our destination or stopped at it
                if (op.getOrigin().equals(myPos.getDestination())) {
                    if (op.getDestination().equals(myPos.getOrigin())) continue; // Ignore head-on

                    float gap = (1.0f - myPos.getProgress()) + op.getProgress();
                    if (gap < minGap) { minGap = gap; closest = other; }
                }

                // They are merging into our destination from elsewhere
                if (op.getDestination().equals(myPos.getDestination()) && !op.getOrigin().equals(myPos.getOrigin())) {
                    if (op.getProgress() > myPos.getProgress()) {
                        float gap = 1.0f - myPos.getProgress();
                        if (gap < minGap) { minGap = gap; closest = other; }
                    }
                }
            }
        }

        if (closest != null && minGap <= 1.0f) return new AheadData(closest, minGap);
        return null;
    }

    private boolean sharesTileWithSlowerVehicle(Vehicle mover, TileCoord currentTile) {
        Set<Vehicle> occupants = occupancy.get(currentTile);
        if (occupants == null) return false;

        VehiclePosition myPos = mover.getPosition();
        for (Vehicle other : occupants) {
            if (other == mover) continue;

            VehiclePosition op = other.getPosition();
            if (op == null) continue;

            int myDx = myPos.getDestination().x - myPos.getOrigin().x;
            int myDy = myPos.getDestination().y - myPos.getOrigin().y;
            int opDx = op.getDestination().x - op.getOrigin().x;
            int opDy = op.getDestination().y - op.getOrigin().y;

            if (myDx == -opDx && myDy == -opDy && (myDx != 0 || myDy != 0)) continue;

            // Keep overtaking flag until slower vehicle is entirely cleared
            if (other.getSpeed() < mover.getSpeed() || other.getStopWaitTicks() > 0 || other.isMaintaining()) {
                return true;
            }
        }
        return false;
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

            // Allow tailgating! (Our new AheadData radar will prevent them from crashing)
            boolean sameDir = (myDx == opDx && myDy == opDy) && (myDx != 0 || myDy != 0);
            if (sameDir) continue;

            // Allow entry if occupant is currently driving out of the tile
            if (op.getOrigin().equals(to) && !op.getDestination().equals(to)) {
                continue;
            }

            // ---> NEW FIX: Deadlock Swap Breaker <---
            // If the blocking vehicle is parked/idle, check if it wants to swap places with us
            if (opDx == 0 && opDy == 0 && other.getId() != null) {
                Queue<TileCoord> opPath = pathCache.get(other.getId());
                if (opPath != null && !opPath.isEmpty() && opPath.peek().equals(from)) {
                    continue; // Let us enter so they can leave!
                }
            }

            // BLOCKED! This prevents vehicles from T-Boning each other at intersections
            return false;
        }
        return true;
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

    private Queue<TileCoord> findPathDijkstra(Map map, TileCoord start, TileCoord goal) {
        if (start.equals(goal)) return new LinkedList<>();

        PriorityQueue<DNode> open = new PriorityQueue<>();
        java.util.Map<TileCoord, TileCoord> cameFrom = new HashMap<>();
        java.util.Map<TileCoord, Integer>   bestCost  = new HashMap<>();

        open.add(new DNode(start, 0));
        bestCost.put(start, 0);

        while (!open.isEmpty()) {
            DNode cur = open.poll();
            if (cur.coord.equals(goal)) break;
            if (cur.cost > bestCost.getOrDefault(cur.coord, Integer.MAX_VALUE)) continue;

            for (TileCoord nb : map.getAdjacentCoords(cur.coord)) {
                if (!isTraversable(map, nb) && !nb.equals(goal)) continue;

                // Base cost 1 + penalty for each vehicle currently on that tile
                Set<Vehicle> occ = occupancy.get(nb);
                int tileCost = 1 + (occ != null ? occ.size() * CONGESTION_WEIGHT : 0);
                int newCost  = cur.cost + tileCost;

                if (newCost < bestCost.getOrDefault(nb, Integer.MAX_VALUE)) {
                    bestCost.put(nb, newCost);
                    cameFrom.put(nb, cur.coord);
                    open.add(new DNode(nb, newCost));
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
}
