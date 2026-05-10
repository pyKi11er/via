package com.via.model.building;

import com.via.model.map.TileCoord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FootprintTest {

    @Test
    void getOrigin_returnsConstructorValue() {
        TileCoord origin = new TileCoord(3, 5);
        Footprint fp = new Footprint(origin, 2, 3);
        assertEquals(origin, fp.getOrigin());
    }

    @Test
    void getWidth_returnsConstructorValue() {
        assertEquals(4, new Footprint(new TileCoord(0, 0), 4, 2).getWidth());
    }

    @Test
    void getHeight_returnsConstructorValue() {
        assertEquals(6, new Footprint(new TileCoord(0, 0), 2, 6).getHeight());
    }

    @Test
    void contains_originTile_true() {
        Footprint fp = new Footprint(new TileCoord(2, 2), 3, 3);
        assertTrue(fp.contains(new TileCoord(2, 2)));
    }

    @Test
    void contains_tileInsideBounds_true() {
        Footprint fp = new Footprint(new TileCoord(0, 0), 4, 4);
        assertTrue(fp.contains(new TileCoord(2, 3)));
    }

    @Test
    void contains_tileAtMaxBoundary_false() {
        Footprint fp = new Footprint(new TileCoord(0, 0), 3, 3);
        assertFalse(fp.contains(new TileCoord(3, 0))); // x == origin.x + width
        assertFalse(fp.contains(new TileCoord(0, 3))); // y == origin.y + height
    }

    @Test
    void contains_tileOutsideBounds_false() {
        Footprint fp = new Footprint(new TileCoord(5, 5), 2, 2);
        assertFalse(fp.contains(new TileCoord(0, 0)));
        assertFalse(fp.contains(new TileCoord(7, 5)));
        assertFalse(fp.contains(new TileCoord(5, 7)));
    }

    @Test
    void getTiles_1x1_returnsSingleTile() {
        Footprint fp = new Footprint(new TileCoord(3, 4), 1, 1);
        List<TileCoord> tiles = fp.getTiles();
        assertEquals(1, tiles.size());
        assertEquals(new TileCoord(3, 4), tiles.get(0));
    }

    @Test
    void getTiles_countMatchesWidthTimesHeight() {
        Footprint fp = new Footprint(new TileCoord(0, 0), 3, 4);
        assertEquals(12, fp.getTiles().size());
    }

    @Test
    void getTiles_allTilesContainedInFootprint() {
        Footprint fp = new Footprint(new TileCoord(1, 1), 3, 3);
        for (TileCoord t : fp.getTiles()) {
            assertTrue(fp.contains(t), "Tile " + t.x + "," + t.y + " should be inside footprint");
        }
    }

    @Test
    void getTiles_coversExactRange() {
        Footprint fp = new Footprint(new TileCoord(2, 3), 2, 2);
        List<TileCoord> tiles = fp.getTiles();
        assertTrue(tiles.contains(new TileCoord(2, 3)));
        assertTrue(tiles.contains(new TileCoord(2, 4)));
        assertTrue(tiles.contains(new TileCoord(3, 3)));
        assertTrue(tiles.contains(new TileCoord(3, 4)));
    }
}
