package com.via.controller;

import com.via.model.enums.SpeedSetting;

/**
 * Controls global simulation speed and pause state for the game loop.
 *
 * <p>The {@code TimeController} determines how fast simulation ticks are processed
 * by providing a time multiplier used by {@link GameEngine} and related systems.</p>
 *
 * <p>It supports normal speed scaling as well as a paused state, which halts
 * simulation progression.</p>
 */
public class TimeController {

    private SpeedSetting speedSetting;
    private boolean paused;

    /**
     * Creates a new {@code TimeController} with default settings.
     *
     * <p>Default state is {@link SpeedSetting#NORMAL} and not paused.</p>
     */
    public TimeController() {
        this.speedSetting = SpeedSetting.NORMAL;
        this.paused = false;
    }

    /**
     * Sets the simulation speed.
     *
     * <p>If the speed is set to {@link SpeedSetting#PAUSED}, the controller
     * will also enter a paused state. Any other speed unpauses simulation.</p>
     *
     * @param newSpeed the new speed setting
     */
    public void setSpeed(SpeedSetting newSpeed) {
        this.speedSetting = newSpeed;
        this.paused = (newSpeed == SpeedSetting.PAUSED);
    }

    /**
     * Returns the current speed setting.
     *
     * @return the active {@link SpeedSetting}
     */
    public SpeedSetting getSpeedSetting() {
        return speedSetting;
    }

    /**
     * Indicates whether the simulation is currently paused.
     *
     * @return {@code true} if paused, otherwise {@code false}
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Returns the multiplier applied to simulation time progression.
     *
     * <p>Typical values:</p>
     * <ul>
     *     <li>{@code 0.0} = paused</li>
     *     <li>{@code 1.0} = normal speed</li>
     *     <li>{@code 2.0} = fast forward</li>
     *     <li>{@code 4.0} = very fast forward</li>
     * </ul>
     *
     * @return the time scaling multiplier
     */
    public float getTimeMultiplier() {
        switch (speedSetting) {
            case VERY_FAST: return 4.0f;
            case FAST: return 2.0f;
            case NORMAL: return 1.0f;
            case PAUSED:
            default: return 0.0f;
        }
    }

    /**
     * Toggles paused state.
     *
     * <p>If the simulation is paused, it resumes at {@link SpeedSetting#NORMAL}.
     * If it is running, it becomes paused.</p>
     */
    public void togglePause() {
        this.paused = !paused;

        if (paused) {
            this.speedSetting = SpeedSetting.PAUSED;
        } else if (this.speedSetting == SpeedSetting.PAUSED) {
            this.speedSetting = SpeedSetting.NORMAL;
        }
    }
}
