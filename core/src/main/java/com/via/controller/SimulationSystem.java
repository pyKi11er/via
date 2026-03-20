package com.via.controller;

import com.via.model.GameState;
import java.util.Objects;


public class SimulationSystem {
    private final ForestSystem forestSystem;
    private final EconomySystem economySystem;
    private final FacilitySystem facilitySystem;
    private final VehicleMovementSystem vehicleMovementSystem;
    private final VehicleMaintenanceSystem vehicleMaintenanceSystem;

    public SimulationSystem() {
        this(
            new ForestSystem(),
            new EconomySystem(),
            new FacilitySystem(),
            new VehicleMovementSystem(),
            new VehicleMaintenanceSystem()
        );
    }

    public SimulationSystem(
        ForestSystem forestSystem,
        EconomySystem economySystem,
        FacilitySystem facilitySystem,
        VehicleMovementSystem vehicleMovementSystem,
        VehicleMaintenanceSystem vehicleMaintenanceSystem
    ) {
        this.forestSystem = Objects.requireNonNull(forestSystem, "forestSystem must not be null");
        this.economySystem = Objects.requireNonNull(economySystem, "economySystem must not be null");
        this.facilitySystem = Objects.requireNonNull(facilitySystem, "facilitySystem must not be null");
        this.vehicleMovementSystem = Objects.requireNonNull(vehicleMovementSystem, "vehicleMovementSystem must not be null");
        this.vehicleMaintenanceSystem = Objects.requireNonNull(vehicleMaintenanceSystem, "vehicleMaintenanceSystem must not be null");
    }

    /**
     * Advances one simulation tick.
     * timeMultiplier is forwarded to VehicleMovementSystem so vehicles
     * move faster at 2x / 4x speed settings.
     */
    public void updateTick(GameState state, float timeMultiplier) {
        if (state == null) return;
        if (state.isGameOver()) return;

        // Advance simulation time
        state.getTimeState().incrementTick();
        forestSystem.tick(state);

        // Maintenance age progression, garage reroutes, and selling happen before economy + movement.
        economySystem.tick(state);
        vehicleMaintenanceSystem.tickBeforeMovement(state);
        facilitySystem.tick(state);
        vehicleMovementSystem.tick(state,timeMultiplier);
        vehicleMaintenanceSystem.tickAfterMovement(state);
        // Restore routes after vehicles reach garage service points.
        // Future expansions:
        // - Move vehicles along routes
        // - Process facility production/consumption
        // - Update economy flows
        // - Adjust city demand
        economySystem.tick(state);
        vehicleMovementSystem.tick(state, timeMultiplier);
    }
}


