package com.via.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectionTest {

    @Test
    void values_hasFourDirections() {
        assertEquals(4, Direction.values().length);
    }

    @Test
    void ordinal_north_isZero() {
        assertEquals(0, Direction.N.ordinal());
    }

    @Test
    void ordinal_east_isOne() {
        assertEquals(1, Direction.E.ordinal());
    }

    @Test
    void ordinal_south_isTwo() {
        assertEquals(2, Direction.S.ordinal());
    }

    @Test
    void ordinal_west_isThree() {
        assertEquals(3, Direction.W.ordinal());
    }

    @Test
    void valueOf_N_returnsNorth() {
        assertSame(Direction.N, Direction.valueOf("N"));
    }

    @Test
    void valueOf_W_returnsWest() {
        assertSame(Direction.W, Direction.valueOf("W"));
    }

    @Test
    void allValues_nameNonNull() {
        for (Direction d : Direction.values()) {
            assertNotNull(d.name());
        }
    }
}
