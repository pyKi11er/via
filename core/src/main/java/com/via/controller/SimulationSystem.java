package com.via.controller;

import com.via.model.GameState;

public class SimulationSystem {

    /** Advances one simulation tick. */
    public void updateTick(GameState state) {
        if (state == null) return;

        // Advance simulation time
        state.getTimeState().incrementTick();

        // Future expansions:
        // - Move vehicles along routes
        // - Process facility production/consumption
        // - Update economy flows
        // - Adjust city demand
    }
}
