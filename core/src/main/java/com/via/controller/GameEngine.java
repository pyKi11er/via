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
        // When paused, keep resetting the timer so we don't accumulate
        // a huge elapsed time that fires hundreds of ticks on unpause
        if (timeController.isPaused()) {
            lastUpdateTimeMs = TimeUtils.millis();
            return;
        }

        long now = TimeUtils.millis();
        long rawElapsedMs = now - lastUpdateTimeMs;
        float elapsedMs = rawElapsedMs * timeController.getTimeMultiplier();

        // Fire ticks only when enough simulated time has passed (~60 ticks/s)
        // Use IF not WHILE to avoid infinite loop — cap at a sane number of
        // ticks per frame so a long GC pause doesn't cause a spiral
        if (elapsedMs >= 16.6f) {
            int ticks = Math.min((int)(elapsedMs / 16.6f), 10); // cap at 10 per frame
            for (int i = 0; i < ticks; i++) {
                simulationSystem.updateTick(gameState, timeController.getTimeMultiplier());
            }
            lastUpdateTimeMs = now;
        }
    }
}
