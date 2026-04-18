package com.via.model.building;

import com.via.model.map.TileCoord;

import java.util.UUID;

/**
 * Represents a vehicle parking area, typically attached to a {@link Garage}.
 * <p>
 * Parking provides a limited number of slots where vehicles can be stored
 * during maintenance or when not in use. It handles slot allocation and
 * provides world coordinates for visual rendering of parked vehicles.
 */
public class Parking extends Building {

    /** Maximum number of vehicles that can be parked simultaneously */
    public static final int MAX_SLOTS = 5;

    /** Array holding vehicle UUIDs currently occupying each parking slot */
    private final UUID[] slots = new UUID[MAX_SLOTS];

    /**
     * Constructs a new Parking area.
     *
     * @param footprint the spatial footprint of the parking area
     */
    public Parking(Footprint footprint) {
        super(footprint);
    }

    /**
     * Finds the first available parking slot.
     *
     * @return the index of a free slot, or -1 if all slots are occupied
     */
    public int findFreeSlot() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (slots[i] == null) return i;
        }
        return -1;
    }

    /**
     * Occupies a specific parking slot with a vehicle.
     *
     * @param slot      the slot index to occupy
     * @param vehicleId the UUID of the vehicle being parked
     */
    public void occupySlot(int slot, UUID vehicleId) {
        if (slot >= 0 && slot < MAX_SLOTS) slots[slot] = vehicleId;
    }

    /**
     * Frees a parking slot, making it available for another vehicle.
     *
     * @param slot the slot index to free
     */
    public void freeSlot(int slot) {
        if (slot >= 0 && slot < MAX_SLOTS) slots[slot] = null;
    }

    /**
     * Checks whether the parking area is completely full.
     *
     * @return {@code true} if no free slots are available
     */
    public boolean isFull() {
        return findFreeSlot() == -1;
    }

    /**
     * Returns the world-pixel position for rendering a vehicle in the given slot.
     * <p>
     * Slots are arranged left-to-right across the top tile of the 2x2 parking footprint.
     *
     * @param slot the parking slot index (0 to MAX_SLOTS-1)
     * @return a float array containing [x, y] world coordinates (bottom-left of vehicle)
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
