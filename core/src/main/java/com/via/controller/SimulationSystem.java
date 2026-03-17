package com.via.controller;

import com.via.model.GameState;
import java.util.Objects;

public class SimulationSystem {
    private final ForestSystem forestSystem;

    public SimulationSystem() {
        this(new ForestSystem());
    }

    public SimulationSystem(ForestSystem forestSystem) {
        this.forestSystem = Objects.requireNonNull(forestSystem, "forestSystem must not be null");
    }

    /** Advances one simulation tick. */
    public void updateTick(GameState state) {
        if (state == null) return;

        // Advance simulation time
        state.getTimeState().incrementTick();
        forestSystem.tick(state);

        // Future expansions:
        // - Move vehicles along routes
        // - Process facility production/consumption
        // - Update economy flows
        // - Adjust city demand
    }
}
