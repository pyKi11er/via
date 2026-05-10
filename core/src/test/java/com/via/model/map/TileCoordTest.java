package com.via.model.map;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TileCoordTest {

    @Test
    void fields_storeXY() {
        TileCoord c = new TileCoord(3, 7);
        assertEquals(3, c.x);
        assertEquals(7, c.y);
    }

    @Test
    void equals_sameCoords_true() {
        assertEquals(new TileCoord(1, 2), new TileCoord(1, 2));
    }

    @Test
    void equals_differentX_false() {
        assertNotEquals(new TileCoord(1, 2), new TileCoord(9, 2));
    }

    @Test
    void equals_differentY_false() {
        assertNotEquals(new TileCoord(1, 2), new TileCoord(1, 9));
    }

    @Test
    void equals_null_false() {
        assertNotEquals(new TileCoord(0, 0), null);
    }

    @Test
    void equals_differentType_false() {
        assertNotEquals(new TileCoord(0, 0), "string");
    }

    @Test
    void equals_sameInstance_true() {
        TileCoord c = new TileCoord(5, 5);
        assertEquals(c, c);
    }

    @Test
    void hashCode_equalCoords_equal() {
        assertEquals(new TileCoord(4, 8).hashCode(), new TileCoord(4, 8).hashCode());
    }

    @Test
    void hashCode_differentCoords_typicallyDifferent() {
        assertNotEquals(new TileCoord(1, 2).hashCode(), new TileCoord(2, 1).hashCode());
    }

    @Test
    void usableAsHashMapKey() {
        HashMap<TileCoord, String> map = new HashMap<>();
        TileCoord key = new TileCoord(10, 20);
        map.put(key, "value");
        assertEquals("value", map.get(new TileCoord(10, 20)));
    }

    @Test
    void usableInHashSet() {
        Set<TileCoord> set = new HashSet<>();
        set.add(new TileCoord(3, 4));
        assertTrue(set.contains(new TileCoord(3, 4)));
        assertFalse(set.contains(new TileCoord(4, 3)));
    }

    @Test
    void zero_coords() {
        TileCoord c = new TileCoord(0, 0);
        assertEquals(0, c.x);
        assertEquals(0, c.y);
    }

    @Test
    void negative_coords() {
        TileCoord c = new TileCoord(-5, -10);
        assertEquals(-5, c.x);
        assertEquals(-10, c.y);
    }
}
