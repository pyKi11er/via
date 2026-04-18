package com.via.model.vehicle;

import com.via.model.building.StopOverlay;
import com.via.model.Constants;
import com.via.model.enums.VehicleType;
import com.via.model.map.TileCoord;

import java.util.UUID;

/**
 * Abstract base class representing a vehicle in the transport simulation.
 * Manages vehicle state including position, route assignment, cargo, maintenance,
 * and garage operations. All vehicle types extend this class.
 */
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
    private boolean maintaining; // Tracks if the vehicle is currently paused in the garage

    private boolean returningToGarage;
    private Route routeBeforeGarage;
    private int routeStopIndexBeforeGarage;
    private TileCoord maintenanceTargetCoord;
    private TileCoord homeGarageLocation;

    private com.via.model.building.Parking parking;
    private int parkedSlot = -1;

    /**
     * Constructs a new Vehicle with the specified parameters.
     *
     * @param type            the vehicle type (must not be null)
     * @param position        the initial position (must not be null)
     * @param speed           the vehicle speed (must be positive)
     * @param capacity        the cargo/passenger capacity (must be non-negative)
     * @param maintenanceCost the cost per maintenance interval (must be non-negative)
     * @param purchaseCost    the purchase cost (must be non-negative)
     * @throws IllegalArgumentException if any validation fails
     */
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

        this.ageTicks = 0;
        this.maintenanceDueTick = 0;
        this.returningToGarage = false;
        this.maintaining = false;
        this.routeBeforeGarage = null;
        this.routeStopIndexBeforeGarage = 0;
        this.maintenanceTargetCoord = null;
    }

    /**
     * Returns the number of ticks the vehicle will wait at a stop.
     *
     * @return the wait ticks
     */
    public int getStopWaitTicks() { return stopWaitTicks; }

    /**
     * Sets the number of ticks the vehicle should wait at a stop.
     * When ticks reach zero, maintenance status is automatically cleared.
     *
     * @param ticks the wait duration in ticks
     */
    public void setStopWaitTicks(int ticks) {
        this.stopWaitTicks = ticks;
        if (this.stopWaitTicks <= 0) {
            this.maintaining = false; // Automatically clears maintenance status when wait is over
        }
    }

    /**
     * Returns the unique identifier of this vehicle.
     *
     * @return the vehicle ID
     */
    public UUID getId() { return id; }

    /**
     * Returns the type of this vehicle.
     *
     * @return the vehicle type
     */
    public VehicleType getType() { return type; }

    /**
     * Returns the current position of this vehicle.
     *
     * @return the vehicle position
     */
    public VehiclePosition getPosition() { return position; }

    /**
     * Sets the position of this vehicle.
     *
     * @param position the new position (must not be null)
     * @throws IllegalArgumentException if position is null
     */
    public void setPosition(VehiclePosition position) {
        if (position == null) throw new IllegalArgumentException("position must not be null");
        this.position = position;
    }

    /**
     * Returns the speed of this vehicle.
     *
     * @return the speed value
     */
    public float getSpeed() { return speed; }

    /**
     * Returns the cargo/passenger capacity of this vehicle.
     *
     * @return the capacity
     */
    public int getCapacity() { return capacity; }

    /**
     * Returns the maintenance cost of this vehicle.
     *
     * @return the maintenance cost
     */
    public long getMaintenanceCost() { return maintenanceCost; }

    /**
     * Returns the purchase cost of this vehicle.
     *
     * @return the purchase cost
     */
    public long getPurchaseCost() { return purchaseCost; }

    /**
     * Returns the route currently assigned to this vehicle.
     *
     * @return the assigned route, or null if none
     */
    public Route getAssignedRoute() { return assignedRoute; }

    /**
     * Returns the parking facility where this vehicle is parked.
     *
     * @return the parking facility, or null if not parked
     */
    public com.via.model.building.Parking getParking() { return parking; }

    /**
     * Returns the parked slot index.
     *
     * @return the slot index, or -1 if not parked
     */
    public int getParkedSlot() { return parkedSlot; }

    /**
     * Sets the parking slot for this vehicle.
     *
     * @param parking the parking facility
     * @param slot    the slot index
     */
    public void setParkingSlot(com.via.model.building.Parking parking, int slot) {
        this.parking = parking;
        this.parkedSlot = slot;
    }

    /**
     * Assigns a route to this vehicle and frees any occupied parking slot.
     *
     * @param route the route to assign
     */
    public void assignRoute(Route route) {
        this.assignedRoute = route;
        this.routeStopIndex = 0;
        if (this.parking != null && this.parkedSlot >= 0) {
            this.parking.freeSlot(this.parkedSlot);
            this.parking = null;
            this.parkedSlot = -1;
        }
    }

    /**
     * Returns the current route stop index.
     *
     * @return the stop index
     */
    public int getRouteStopIndex() { return routeStopIndex; }

    /**
     * Sets the current route stop index.
     *
     * @param routeStopIndex the new stop index
     */
    public void setRouteStopIndex(int routeStopIndex) { this.routeStopIndex = routeStopIndex; }

    /**
     * Returns the cargo currently carried by this vehicle.
     *
     * @return the cargo, or null if empty
     */
    public Cargo getCargo() { return cargo; }

    /**
     * Sets the cargo for this vehicle.
     *
     * @param cargo the cargo to carry (may be null)
     */
    public void setCargo(Cargo cargo) { this.cargo = cargo; }

    /**
     * Returns the next stop on the assigned route.
     *
     * @return the next stop, or null if no route is assigned
     */
    public StopOverlay getNextStop() {
        if (assignedRoute == null) return null;
        return assignedRoute.getNextStop(routeStopIndex);
    }

    /**
     * Advances the route index to the next stop (circular).
     */
    public void advanceToNextStop() {
        if (assignedRoute == null || assignedRoute.size() == 0) return;
        routeStopIndex = Math.floorMod(routeStopIndex + 1, assignedRoute.size());
    }

    /**
     * Returns the age of this vehicle in ticks.
     *
     * @return the age ticks
     */
    public long getAgeTicks() { return ageTicks; }

    /**
     * Returns the tick when maintenance is next due.
     *
     * @return the maintenance due tick
     */
    public long getMaintenanceDueTick() { return maintenanceDueTick; }

    /**
     * Returns whether the vehicle is returning to garage.
     *
     * @return true if returning to garage, false otherwise
     */
    public boolean isReturningToGarage() { return returningToGarage; }

    /**
     * Returns whether the vehicle is currently in maintenance.
     *
     * @return true if maintaining, false otherwise
     */
    public boolean isMaintaining() { return maintaining; }

    /**
     * Returns the maintenance target coordinates.
     *
     * @return the target coordinates, or null if none
     */
    public TileCoord getMaintenanceTargetCoord() { return maintenanceTargetCoord; }

    /**
     * Initializes the vehicle after purchase.
     *
     * @param purchaseTick the tick when the vehicle was purchased
     */
    public void onPurchased(long purchaseTick) {
        this.ageTicks = 0;
        this.maintenanceDueTick = purchaseTick + Constants.getMaintenanceIntervalTicks(0);
        this.returningToGarage = false;
        this.maintaining = false;
        this.routeBeforeGarage = null;
        this.routeStopIndexBeforeGarage = 0;
        this.maintenanceTargetCoord = null;
    }

    /**
     * Increments the vehicle's age by the specified number of ticks.
     *
     * @param deltaTicks the number of ticks to add (must be positive)
     */
    public void incrementAgeTicks(long deltaTicks) {
        if (deltaTicks <= 0) return;
        this.ageTicks += deltaTicks;
    }

    /**
     * Reschedules the next maintenance due tick based on current age.
     *
     * @param currentTick the current game tick
     */
    public void rescheduleNextMaintenanceDueTick(long currentTick) {
        this.maintenanceDueTick = currentTick + Constants.getMaintenanceIntervalTicks(ageTicks);
    }

    /**
     * Checks whether the vehicle has exceeded its maximum age.
     *
     * @return true if over-aged, false otherwise
     */
    public boolean isOverAged() {
        return ageTicks >= Constants.VEHICLE_MAX_AGE_TICKS;
    }

    /**
     * Calculates the resale value of this vehicle based on purchase cost and age.
     *
     * @return the resale value
     */
    public long getResaleValue() {
        return Constants.getResaleValue(purchaseCost, ageTicks);
    }

    /**
     * Sends the vehicle to the garage for maintenance.
     * The vehicle will follow the provided maintenance route to the target.
     *
     * @param garageMaintenanceRoute the route to the garage
     * @param maintenanceTargetCoord the coordinates of the garage
     * @throws IllegalArgumentException if either parameter is null
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

    /**
     * Restores the vehicle's original route after garage maintenance.
     * The vehicle will pause for maintenance time before resuming operations.
     */
    public void restoreAfterGarageReturn() {
        this.assignedRoute = this.routeBeforeGarage;
        this.routeStopIndex = this.routeStopIndexBeforeGarage;
        this.returningToGarage = false;
        this.routeBeforeGarage = null;
        this.maintenanceTargetCoord = null;
        this.routeStopIndexBeforeGarage = 0;

        // Pause for 180 ticks (approx 3 seconds) to simulate maintenance time
        this.setStopWaitTicks(180);
        this.maintaining = true;
    }

    /**
     * Returns the home garage location of this vehicle.
     *
     * @return the home garage coordinates, or null if not set
     */
    public TileCoord getHomeGarageLocation() {
        return homeGarageLocation;
    }

    /**
     * Sets the home garage location for this vehicle.
     *
     * @param homeGarageLocation the home garage coordinates
     */
    public void setHomeGarageLocation(TileCoord homeGarageLocation) {
        this.homeGarageLocation = homeGarageLocation;
    }
}
