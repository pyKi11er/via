package com.via.model.economy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimeStateTest {

    private TimeState timeState;

    @BeforeEach
    void setUp() {
        timeState = new TimeState();
    }

    @Test
    void initialTick_isZero() {
        assertEquals(0L, timeState.getTick());
    }

    @Test
    void incrementTick_advancesBy1() {
        timeState.incrementTick();
        assertEquals(1L, timeState.getTick());
    }

    @Test
    void incrementTick_multiple_accumulates() {
        for (int i = 0; i < 100; i++) timeState.incrementTick();
        assertEquals(100L, timeState.getTick());
    }

    @Test
    void incrementTick_doesNotWrapUnexpectedly() {
        for (int i = 0; i < 1000; i++) timeState.incrementTick();
        assertEquals(1000L, timeState.getTick());
    }

    @Test
    void getTick_independentInstances() {
        TimeState a = new TimeState();
        TimeState b = new TimeState();
        a.incrementTick();
        a.incrementTick();
        b.incrementTick();
        assertEquals(2L, a.getTick());
        assertEquals(1L, b.getTick());
    }
}
