package com.via.model.economy;

import java.io.Serializable;

public class TimeState implements Serializable {
    private static final long serialVersionUID = 1L;
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
