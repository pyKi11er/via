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
        EconomySystem economySystem = new EconomySystem();
        this.forestSystem = new ForestSystem();
        this.economySystem = economySystem;
        this.facilitySystem = new FacilitySystem();
        this.vehicleMovementSystem = new VehicleMovementSystem(economySystem);
        this.vehicleMaintenanceSystem = new VehicleMaintenanceSystem();
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
        if (state == null || state.isGameOver()) return;

        state.getTimeState().incrementTick();
        forestSystem.tick(state);
        facilitySystem.tick(state);           // 1. produce goods + transfer to stops
        vehicleMaintenanceSystem.tickBeforeMovement(state); // 2. maintenance checks
        vehicleMovementSystem.tick(state, timeMultiplier);  // 3. vehicles move + load/unload (income on unload)
        vehicleMaintenanceSystem.tickAfterMovement(state);  // 4. restore after garage
        economySystem.tick(state);            // 5. game-over / capital checks only
    }


}


