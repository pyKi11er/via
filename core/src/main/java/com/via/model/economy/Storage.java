package com.via.model.economy;

import com.via.model.enums.GoodType;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a storage container for goods and resources.
 * <p>
 * Used by Facility, StopOverlay, and other entities to hold
 * inventories of different {@link GoodType}s. This class enforces valid
 * operations (non-negative amounts, sufficient stock for removals).
 */
public class Storage {

    /** Maps each good type to its current stored quantity */
    private Map<GoodType, Integer> contents;

    /**
     * Constructs a new empty Storage.
     */
    public Storage() {
        contents = new HashMap<>();
    }

    /**
     * Returns the internal map of all stored goods.
     *
     * @return the contents map (direct reference — be careful with modification)
     */
    public Map<GoodType, Integer> getContents() {
        return contents;
    }

    /**
     * Adds a specified amount of a good to the storage.
     *
     * @param type   the type of good to add
     * @param amount the amount to add (must be non-negative)
     * @throws IllegalArgumentException if type or amount is null, or amount is negative
     */
    public void addContents(GoodType type, Integer amount) {
        if (type == null) throw new IllegalArgumentException("type must not be null");
        if (amount == null) throw new IllegalArgumentException("amount must not be null");
        if (amount < 0) throw new IllegalArgumentException("amount must not be negative");
        contents.put(type, getAmount(type) + amount);
    }

    /**
     * Completely removes a good type from storage.
     *
     * @param type the good type to remove
     */
    public void removeContents(GoodType type) {
        contents.remove(type);
    }

    /**
     * Returns the current amount of a specific good in storage.
     *
     * @param type the good type to query
     * @return the stored amount, or 0 if the good is not present
     */
    public int getAmount(GoodType type) {
        return contents.getOrDefault(type, 0);
    }

    /**
     * Removes a specified amount of a good from storage.
     *
     * @param type   the good type to remove
     * @param amount the amount to remove (must be non-negative)
     * @throws IllegalArgumentException if type is null, amount is negative, or there is insufficient stock
     */
    public void removeAmount(GoodType type, int amount) {
        if (type == null) throw new IllegalArgumentException("type must not be null");
        if (amount < 0) throw new IllegalArgumentException("amount must not be negative");
        int current = getAmount(type);
        int next = current - amount;
        if (next < 0) throw new IllegalArgumentException("not enough " + type + " in storage");
        if (next == 0) {
            contents.remove(type);
        } else {
            contents.put(type, next);
        }
    }
}
