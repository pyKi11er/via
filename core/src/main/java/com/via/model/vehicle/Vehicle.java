package com.via.model.vehicle;

import com.via.model.building.StopOverlay;
import com.via.model.enums.VehicleType;

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
    }

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
}
