package com.via.model.building;

import com.via.model.map.TileCoord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ParkingTest {

    private Parking parking;

    @BeforeEach
    void setUp() {
        parking = new Parking(new Footprint(new TileCoord(0, 0), 2, 2));
    }

    @Test
    void findFreeSlot_allFree_returnsZero() {
        assertEquals(0, parking.findFreeSlot());
    }

    @Test
    void findFreeSlot_firstSlotOccupied_returnsOne() {
        parking.occupySlot(0, UUID.randomUUID());
        assertEquals(1, parking.findFreeSlot());
    }

    @Test
    void findFreeSlot_allFull_returnsMinusOne() {
        for (int i = 0; i < Parking.MAX_SLOTS; i++) {
            parking.occupySlot(i, UUID.randomUUID());
        }
        assertEquals(-1, parking.findFreeSlot());
    }

    @Test
    void occupySlot_storesVehicleId() {
        UUID id = UUID.randomUUID();
        parking.occupySlot(0, id);
        assertEquals(1, parking.findFreeSlot());
    }

    @Test
    void freeSlot_makesSlotAvailable() {
        UUID id = UUID.randomUUID();
        parking.occupySlot(0, id);
        parking.occupySlot(1, id);
        parking.freeSlot(0);
        assertEquals(0, parking.findFreeSlot());
    }

    @Test
    void freeSlot_outOfBounds_noException() {
        assertDoesNotThrow(() -> parking.freeSlot(-1));
        assertDoesNotThrow(() -> parking.freeSlot(Parking.MAX_SLOTS));
    }

    @Test
    void occupySlot_outOfBounds_noException() {
        assertDoesNotThrow(() -> parking.occupySlot(-1, UUID.randomUUID()));
        assertDoesNotThrow(() -> parking.occupySlot(Parking.MAX_SLOTS, UUID.randomUUID()));
    }

    @Test
    void isFull_emptyParking_false() {
        assertFalse(parking.isFull());
    }

    @Test
    void isFull_partiallyFull_false() {
        parking.occupySlot(0, UUID.randomUUID());
        assertFalse(parking.isFull());
    }

    @Test
    void isFull_completelyFull_true() {
        for (int i = 0; i < Parking.MAX_SLOTS; i++) {
            parking.occupySlot(i, UUID.randomUUID());
        }
        assertTrue(parking.isFull());
    }

    @Test
    void isFull_afterFreeingOne_false() {
        for (int i = 0; i < Parking.MAX_SLOTS; i++) {
            parking.occupySlot(i, UUID.randomUUID());
        }
        parking.freeSlot(2);
        assertFalse(parking.isFull());
    }

    @Test
    void maxSlots_isFive() {
        assertEquals(5, Parking.MAX_SLOTS);
    }

    @Test
    void getSlotWorldPos_returnsNonNullArray() {
        float[] pos = parking.getSlotWorldPos(0);
        assertNotNull(pos);
        assertEquals(2, pos.length);
    }

    @Test
    void getSlotWorldPos_slotsHaveIncreasingX() {
        float x0 = parking.getSlotWorldPos(0)[0];
        float x1 = parking.getSlotWorldPos(1)[0];
        float x2 = parking.getSlotWorldPos(2)[0];
        assertTrue(x0 < x1, "Slot 1 should be to the right of slot 0");
        assertTrue(x1 < x2, "Slot 2 should be to the right of slot 1");
    }

    @Test
    void getSlotWorldPos_sameYForAllSlots() {
        float y0 = parking.getSlotWorldPos(0)[1];
        for (int i = 1; i < Parking.MAX_SLOTS; i++) {
            assertEquals(y0, parking.getSlotWorldPos(i)[1], 0.001f);
        }
    }

    @Test
    void fillAndClearCycle_worksRepeatedly() {
        UUID id = UUID.randomUUID();
        for (int cycle = 0; cycle < 3; cycle++) {
            for (int i = 0; i < Parking.MAX_SLOTS; i++) parking.occupySlot(i, id);
            assertTrue(parking.isFull());
            for (int i = 0; i < Parking.MAX_SLOTS; i++) parking.freeSlot(i);
            assertFalse(parking.isFull());
        }
    }
}
