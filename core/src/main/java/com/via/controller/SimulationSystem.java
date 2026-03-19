package com.via.controller;

import com.via.model.GameState;
import java.util.Objects;


public class SimulationSystem {
    private final ForestSystem forestSystem;
    private final EconomySystem economySystem;
    private final FacilitySystem facilitySystem;

    public SimulationSystem() {
        this(new ForestSystem(), new EconomySystem(), new FacilitySystem());
    }

    public SimulationSystem(ForestSystem forestSystem, EconomySystem economySystem,  FacilitySystem facilitySystem) {
        this.forestSystem = Objects.requireNonNull(forestSystem, "forestSystem must not be null");
        this.economySystem = Objects.requireNonNull(economySystem, "economySystem must not be null");
        this.facilitySystem = Objects.requireNonNull(facilitySystem, "facilitySystem must not be null");
    }

    /** Advances one simulation tick. */
    public void updateTick(GameState state) {
        if (state == null) return;
        if (state.isGameOver()) return;

        // Advance simulation time
        state.getTimeState().incrementTick();
        forestSystem.tick(state);
        economySystem.tick(state);
        facilitySystem.tick(state);
        // Future expansions:
        // - Move vehicles along routes
        // - Process facility production/consumption
        // - Update economy flows
        // - Adjust city demand
    }
}
