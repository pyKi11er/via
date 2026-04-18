package com.via.controller;

import com.badlogic.gdx.utils.TimeUtils;
import com.via.model.GameState;

/**
 * Core runtime engine responsible for advancing the game simulation over time.
 *
 * <p>The {@code GameEngine} coordinates:</p>
 * <ul>
 *     <li>Time progression via {@link TimeController}</li>
 *     <li>Simulation updates via {@link SimulationSystem}</li>
 * </ul>
 *
 * <p>It converts real-world elapsed time into simulation ticks, applying a
 * configurable time multiplier and ensuring stable update behavior even under
 * varying frame rates.</p>
 */
public class GameEngine {

    private final GameState gameState;
    private final TimeController timeController;
    private final SimulationSystem simulationSystem;

    /** Timestamp of the last update in milliseconds. */
    private long lastUpdateTimeMs;

    /**
     * Creates a new {@code GameEngine} instance.
     *
     * @param gameState the game state to be updated by the engine
     */
    public GameEngine(GameState gameState) {
        this.gameState = gameState;
        this.timeController = new TimeController();
        this.simulationSystem = new SimulationSystem();
        this.lastUpdateTimeMs = TimeUtils.millis();
    }

    /**
     * Returns the current game state.
     *
     * @return the active {@link GameState}
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Returns the time controller managing pause state and time scaling.
     *
     * @return the {@link TimeController}
     */
    public TimeController getTimeController() {
        return timeController;
    }

    /**
     * Updates the game simulation based on elapsed real time.
     *
     * <p>Behavior:</p>
     * <ul>
     *     <li>If the game is paused, the internal timer is reset to prevent
     *     accumulation of elapsed time.</li>
     *     <li>Otherwise, elapsed real time is converted into simulated time
     *     using the time multiplier.</li>
     *     <li>Simulation ticks are executed when enough simulated time has
     *     accumulated (approximately 60 ticks per second).</li>
     *     <li>The number of ticks per frame is capped to prevent large update
     *     spikes (e.g., after pauses or frame drops).</li>
     * </ul>
     *
     * <p>This approach ensures smooth and stable simulation without runaway
     * update loops.</p>
     */
    public void update() {
        // When paused, reset timer to avoid large accumulated delta on resume
        if (timeController.isPaused()) {
            lastUpdateTimeMs = TimeUtils.millis();
            return;
        }

        long now = TimeUtils.millis();
        long rawElapsedMs = now - lastUpdateTimeMs;
        float elapsedMs = rawElapsedMs * timeController.getTimeMultiplier();

        // Trigger simulation ticks at ~60 updates per second
        if (elapsedMs >= 16.6f) {
            // Cap ticks per frame to avoid spiral of death
            int ticks = Math.min((int) (elapsedMs / 16.6f), 10);

            for (int i = 0; i < ticks; i++) {
                simulationSystem.updateTick(
                    gameState,
                    timeController.getTimeMultiplier()
                );
            }

            lastUpdateTimeMs = now;
        }
    }
}
