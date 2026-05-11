package com.via.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GridSizeTest {

    @Test
    void width_storesConstructorValue() {
        GridSize gs = new GridSize(10, 5);
        assertEquals(10, gs.width);
    }

    @Test
    void height_storesConstructorValue() {
        GridSize gs = new GridSize(10, 5);
        assertEquals(5, gs.height);
    }

    @Test
    void zeroWidth_allowed() {
        GridSize gs = new GridSize(0, 3);
        assertEquals(0, gs.width);
    }

    @Test
    void zeroHeight_allowed() {
        GridSize gs = new GridSize(4, 0);
        assertEquals(0, gs.height);
    }

    @Test
    void bothZero_allowed() {
        GridSize gs = new GridSize(0, 0);
        assertEquals(0, gs.width);
        assertEquals(0, gs.height);
    }

    @Test
    void largeValues_stored() {
        GridSize gs = new GridSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, gs.width);
        assertEquals(Integer.MAX_VALUE, gs.height);
    }

    @Test
    void widthAndHeight_areIndependent() {
        GridSize gs = new GridSize(7, 13);
        assertNotEquals(gs.width, gs.height);
    }

    @Test
    void squareGrid_widthEqualsHeight() {
        GridSize gs = new GridSize(8, 8);
        assertEquals(gs.width, gs.height);
    }

    @Test
    void area_computedCorrectly() {
        GridSize gs = new GridSize(6, 4);
        assertEquals(24, (long) gs.width * gs.height);
    }
}
