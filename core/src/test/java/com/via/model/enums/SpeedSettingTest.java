package com.via.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpeedSettingTest {

    @Test
    void values_hasFourSettings() {
        assertEquals(4, SpeedSetting.values().length);
    }

    @Test
    void paused_isFirst() {
        assertEquals(0, SpeedSetting.PAUSED.ordinal());
    }

    @Test
    void normal_isSecond() {
        assertEquals(1, SpeedSetting.NORMAL.ordinal());
    }

    @Test
    void fast_isThird() {
        assertEquals(2, SpeedSetting.FAST.ordinal());
    }

    @Test
    void veryFast_isFourth() {
        assertEquals(3, SpeedSetting.VERY_FAST.ordinal());
    }

    @Test
    void valueOf_PAUSED_returnsPaused() {
        assertSame(SpeedSetting.PAUSED, SpeedSetting.valueOf("PAUSED"));
    }

    @Test
    void valueOf_VERY_FAST_returnsVeryFast() {
        assertSame(SpeedSetting.VERY_FAST, SpeedSetting.valueOf("VERY_FAST"));
    }

    @Test
    void allValues_nameNonNull() {
        for (SpeedSetting s : SpeedSetting.values()) {
            assertNotNull(s.name());
        }
    }
}
