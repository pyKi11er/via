package com.via.model.economy;

/**
 * Represents the time state of the economy system.
 * Tracks the current tick count, which advances over time.
 */
public class TimeState {
    private long tick;

    /**
     * Constructs a new TimeState with an initial tick count of zero.
     */
    public TimeState() {
        this.tick = 0;
    }

    /**
     * Returns the current tick count.
     *
     * @return the current tick value
     */
    public long getTick() {
        return tick;
    }

    /**
     * Increments the tick counter by one.
     * This method should be called each time the economy time advances.
     */
    public void incrementTick() {
        tick++;
    }
}
