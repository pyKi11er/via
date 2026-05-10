package com.via.model.vehicle;

import com.via.model.map.TileCoord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehiclePositionTest {

    @Test
    void initialProgress_isZero() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        assertEquals(0f, pos.getProgress());
    }

    @Test
    void setProgress_validBounds() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        pos.setProgress(0.0f);
        assertEquals(0.0f, pos.getProgress());
        pos.setProgress(1.0f);
        assertEquals(1.0f, pos.getProgress());
        pos.setProgress(0.5f);
        assertEquals(0.5f, pos.getProgress());
    }

    @Test
    void setProgress_belowZero_throws() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        assertThrows(IllegalArgumentException.class, () -> pos.setProgress(-0.01f));
    }

    @Test
    void setProgress_aboveOne_throws() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        assertThrows(IllegalArgumentException.class, () -> pos.setProgress(1.01f));
    }

    @Test
    void getOriginAndDestination() {
        TileCoord from = new TileCoord(2, 3);
        TileCoord to   = new TileCoord(4, 5);
        VehiclePosition pos = new VehiclePosition(from, to);
        assertEquals(from, pos.getOrigin());
        assertEquals(to, pos.getDestination());
    }

    @Test
    void aliases_matchPrimaryAccessors() {
        TileCoord from = new TileCoord(1, 1);
        TileCoord to   = new TileCoord(2, 2);
        VehiclePosition pos = new VehiclePosition(from, to);
        assertSame(pos.getOrigin(), pos.getFrom());
        assertSame(pos.getDestination(), pos.getTo());
    }

    // ── overtaking state ─────────────────────────────────────────────────────

    @Test
    void initialOvertaking_isFalse() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        assertFalse(pos.isOvertaking());
    }

    @Test
    void setOvertaking_true_reflects() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        pos.setOvertaking(true);
        assertTrue(pos.isOvertaking());
    }

    @Test
    void setOvertaking_falseAfterTrue() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        pos.setOvertaking(true);
        pos.setOvertaking(false);
        assertFalse(pos.isOvertaking());
    }

    @Test
    void initialLaneOffset_isZero() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        assertEquals(0f, pos.getLaneOffset(), 0.0001f);
    }

    @Test
    void setLaneOffset_storesValue() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        pos.setLaneOffset(0.35f);
        assertEquals(0.35f, pos.getLaneOffset(), 0.0001f);
    }

    // ── visualLaneOffset (smooth lane animation) ──────────────────────────────

    @Test
    void initialVisualLaneOffset_isZero() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        assertEquals(0f, pos.getVisualLaneOffset(), 0.0001f);
    }

    @Test
    void setVisualLaneOffset_storesValue() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        pos.setVisualLaneOffset(0.5f);
        assertEquals(0.5f, pos.getVisualLaneOffset(), 0.0001f);
    }

    @Test
    void setVisualLaneOffset_zero_storesZero() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        pos.setVisualLaneOffset(0.8f);
        pos.setVisualLaneOffset(0f);
        assertEquals(0f, pos.getVisualLaneOffset(), 0.0001f);
    }

    @Test
    void setVisualLaneOffset_one_storesOne() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        pos.setVisualLaneOffset(1f);
        assertEquals(1f, pos.getVisualLaneOffset(), 0.0001f);
    }

    @Test
    void visualLaneOffset_independentOfOvertakingFlag() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        pos.setVisualLaneOffset(0.7f);
        assertFalse(pos.isOvertaking()); // flag not set automatically
        assertEquals(0.7f, pos.getVisualLaneOffset(), 0.0001f);
    }
}
