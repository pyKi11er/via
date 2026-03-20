package com.via.controller;

import com.via.model.GameState;

import java.util.Objects;

public class SimulationSystem {

    private final ForestSystem forestSystem;
    private final FacilitySystem facilitySystem;
    private final EconomySystem economySystem;
    private final VehicleMovementSystem vehicleMovementSystem;

    public SimulationSystem() {
        this(new ForestSystem(), new FacilitySystem(),
            new EconomySystem(), new VehicleMovementSystem());
    }

    public SimulationSystem(ForestSystem forestSystem,
                            FacilitySystem facilitySystem,
                            EconomySystem economySystem,
                            VehicleMovementSystem vehicleMovementSystem) {
        this.forestSystem          = Objects.requireNonNull(forestSystem);
        this.facilitySystem        = Objects.requireNonNull(facilitySystem);
        this.economySystem         = Objects.requireNonNull(economySystem);
        this.vehicleMovementSystem = Objects.requireNonNull(vehicleMovementSystem);
    }

    /**
     * Advances one simulation tick.
     * timeMultiplier is forwarded to VehicleMovementSystem so vehicles
     * move faster at 2x / 4x speed settings.
     */
    public void updateTick(GameState state, float timeMultiplier) {
        if (state == null) return;

        state.getTimeState().incrementTick();

        forestSystem.tick(state);
        facilitySystem.tick(state);
        economySystem.tick(state);
        vehicleMovementSystem.tick(state, timeMultiplier);
    }
}
