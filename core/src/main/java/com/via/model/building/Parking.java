package com.via.model.building;

import com.via.model.map.TileCoord;

import java.util.UUID;

public class Parking extends Building {

    public static final int MAX_SLOTS = 5;
    private final UUID[] slots = new UUID[MAX_SLOTS];

    public Parking(Footprint footprint) {
        super(footprint);
    }

    public int findFreeSlot() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (slots[i] == null) return i;
        }
        return -1;
    }

    public void occupySlot(int slot, UUID vehicleId) {
        if (slot >= 0 && slot < MAX_SLOTS) slots[slot] = vehicleId;
    }

    public void freeSlot(int slot) {
        if (slot >= 0 && slot < MAX_SLOTS) slots[slot] = null;
    }

    public boolean isFull() {
        return findFreeSlot() == -1;
    }

    /**
     * World-pixel position (bottom-left corner) for drawing a vehicle at the given slot.
     * 5 slots spread evenly across the 2x2 parking area (64px wide), each 10px wide.
     * Slots run left-to-right in the top tile row.
     */
    public float[] getSlotWorldPos(int slot) {
        TileCoord origin = footprint.getOrigin();
        float baseX = origin.x * 32f;
        float baseY = (origin.y + 1) * 32f; // top tile of 2x2 parking
        // 5 slots across 64px: spacing = 64/5 = 12.8 → center each at slot*12.8 + 6.4, draw at 10px wide
        float xOffset = slot * 12.8f + 1.4f;
        return new float[]{baseX + xOffset, baseY};
    }
}
