package com.via.model.vehicle;

import com.via.model.building.StopOverlay;
import com.via.model.Constants;
import com.via.model.enums.VehicleType;
import com.via.model.map.TileCoord;

import java.util.UUID;

public abstract class Vehicle {
    private final UUID id;
    private final VehicleType type;
    private VehiclePosition position;
    private final float speed;
    private final int capacity;
    private final long maintenanceCost;
    private final long purchaseCost;

    private Route assignedRoute;
    private int routeStopIndex;
    private Cargo cargo;

    private long ageTicks;
    private long maintenanceDueTick;

    private int stopWaitTicks;

    private boolean returningToGarage;
    private Route routeBeforeGarage;
    private int routeStopIndexBeforeGarage;
    private TileCoord maintenanceTargetCoord;

    protected Vehicle(
        VehicleType type,
        VehiclePosition position,
        float speed,
        int capacity,
        long maintenanceCost,
        long purchaseCost
    ) {
        if (type == null) throw new IllegalArgumentException("type must not be null");
        if (position == null) throw new IllegalArgumentException("position must not be null");
        if (speed <= 0f) throw new IllegalArgumentException("speed must be positive");
        if (capacity < 0) throw new IllegalArgumentException("capacity must not be negative");
        if (maintenanceCost < 0) throw new IllegalArgumentException("maintenanceCost must not be negative");
        if (purchaseCost < 0) throw new IllegalArgumentException("purchaseCost must not be negative");
        this.id = UUID.randomUUID();
        this.type = type;
        this.position = position;
        this.speed = speed;
        this.capacity = capacity;
        this.maintenanceCost = maintenanceCost;
        this.purchaseCost = purchaseCost;
        this.assignedRoute = null;
        this.routeStopIndex = 0;
        this.cargo = null;

        // Runtime state (set up via onPurchased()).
        this.ageTicks = 0;
        this.maintenanceDueTick = 0;
        this.returningToGarage = false;
        this.routeBeforeGarage = null;
        this.routeStopIndexBeforeGarage = 0;
        this.maintenanceTargetCoord = null;
    }

    public int getStopWaitTicks() { return stopWaitTicks; }
    public void setStopWaitTicks(int ticks) { this.stopWaitTicks = ticks; }

    public UUID getId() {
        return id;
    }

    public VehicleType getType() {
        return type;
    }

    public VehiclePosition getPosition() {
        return position;
    }

    public void setPosition(VehiclePosition position) {
        if (position == null) throw new IllegalArgumentException("position must not be null");
        this.position = position;
    }

    public float getSpeed() {
        return speed;
    }

    public int getCapacity() {
        return capacity;
    }

    public long getMaintenanceCost() {
        return maintenanceCost;
    }

    public long getPurchaseCost() {
        return purchaseCost;
    }

    public Route getAssignedRoute() {
        return assignedRoute;
    }

    public void assignRoute(Route route) {
        this.assignedRoute = route;
        this.routeStopIndex = 0;
    }

    public int getRouteStopIndex() {
        return routeStopIndex;
    }

    public void setRouteStopIndex(int routeStopIndex) {
        this.routeStopIndex = routeStopIndex;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    /** Returns the next stop on the assigned route (circular). */
    public StopOverlay getNextStop() {
        if (assignedRoute == null) return null;
        return assignedRoute.getNextStop(routeStopIndex);
    }

    /** Advances the internal route index (circular). */
    public void advanceToNextStop() {
        if (assignedRoute == null || assignedRoute.size() == 0) return;
        routeStopIndex = Math.floorMod(routeStopIndex + 1, assignedRoute.size());
    }

    public long getAgeTicks() {
        return ageTicks;
    }

    public long getMaintenanceDueTick() {
        return maintenanceDueTick;
    }

    public boolean isReturningToGarage() {
        return returningToGarage;
    }

    public TileCoord getMaintenanceTargetCoord() {
        return maintenanceTargetCoord;
    }

    /**
     * Initializes age/maintenance schedule at purchase time.
     * Should be called by {@code VehicleFactory.buy()} and any test/setup code that creates vehicles directly.
     */
    public void onPurchased(long purchaseTick) {
        this.ageTicks = 0;
        this.maintenanceDueTick = purchaseTick + Constants.getMaintenanceIntervalTicks(0);
        this.returningToGarage = false;
        this.routeBeforeGarage = null;
        this.routeStopIndexBeforeGarage = 0;
        this.maintenanceTargetCoord = null;
    }

    /** Increments vehicle age by the number of simulation ticks elapsed. */
    public void incrementAgeTicks(long deltaTicks) {
        if (deltaTicks <= 0) return;
        this.ageTicks += deltaTicks;
    }

    /** Reschedules the next maintenance due tick based on current age. */
    public void rescheduleNextMaintenanceDueTick(long currentTick) {
        this.maintenanceDueTick = currentTick + Constants.getMaintenanceIntervalTicks(ageTicks);
    }

    /** Returns true once vehicle age reaches the over-aged threshold. */
    public boolean isOverAged() {
        return ageTicks >= Constants.VEHICLE_MAX_AGE_TICKS;
    }

    /** Resale value when the vehicle is sold. */
    public long getResaleValue() {
        return Constants.getResaleValue(purchaseCost, ageTicks);
    }

    /**
     * Overrides the current route to return the vehicle to a garage service point.
     * The previous route (and stop index) are restored after the vehicle arrives.
     */
    public void enterGarageReturn(Route garageMaintenanceRoute, TileCoord maintenanceTargetCoord) {
        if (garageMaintenanceRoute == null) throw new IllegalArgumentException("garageMaintenanceRoute must not be null");
        if (maintenanceTargetCoord == null) throw new IllegalArgumentException("maintenanceTargetCoord must not be null");

        if (!returningToGarage) {
            this.routeBeforeGarage = this.assignedRoute;
            this.routeStopIndexBeforeGarage = this.routeStopIndex;
        }

        this.assignedRoute = garageMaintenanceRoute;
        this.routeStopIndex = 0;
        this.returningToGarage = true;
        this.maintenanceTargetCoord = maintenanceTargetCoord;
    }

    /** Restores the route overridden by {@link #enterGarageReturn(Route, TileCoord)}. */
    public void restoreAfterGarageReturn() {
        this.assignedRoute = this.routeBeforeGarage;
        this.routeStopIndex = this.routeStopIndexBeforeGarage;
        this.returningToGarage = false;
        this.routeBeforeGarage = null;
        this.maintenanceTargetCoord = null;
        this.routeStopIndexBeforeGarage = 0;
    }
}
