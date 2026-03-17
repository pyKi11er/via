package com.via.controller;

import com.badlogic.gdx.utils.TimeUtils;
import com.via.model.GameState;

public class GameEngine {

    private final GameState gameState;
    private final TimeController timeController;
    private final SimulationSystem simulationSystem;

    private long lastUpdateTimeMs;

    public GameEngine(GameState gameState) {
        this.gameState = gameState;
        this.timeController = new TimeController();
        this.simulationSystem = new SimulationSystem();
        this.lastUpdateTimeMs = TimeUtils.millis();
    }

    public GameState getGameState() {
        return gameState;
    }

    public TimeController getTimeController() {
        return timeController;
    }

    public void update() {
        if (timeController.isPaused()) return;

        long now = TimeUtils.millis();
        float elapsedMs = (now - lastUpdateTimeMs) * timeController.getTimeMultiplier();

        while (elapsedMs >= 16.6f) {
            simulationSystem.updateTick(gameState);
            elapsedMs -= 16.6f;
        }

        lastUpdateTimeMs = now;
    }
}
