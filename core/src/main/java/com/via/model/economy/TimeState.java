package com.via.model.economy;

public class TimeState {
    private long tick;
    public TimeState() {
        this.tick = 0;
    }

    public long getTick() {
        return tick;
    }
    public void incrementTick() {
        tick++;
    }
}
