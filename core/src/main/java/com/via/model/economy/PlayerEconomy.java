package com.via.model.economy;

import com.via.model.Constants;

/**
 * Manages the player's financial resources (capital) in the game.
 * <p>
 * This class handles earning, spending, and tracking the player's money.
 * It enforces non-negative transaction amounts and provides both modern
 * and backwards-compatible methods for capital changes.
 */
public class PlayerEconomy {

    /** The player's current capital / money */
    private long capital;

    /**
     * Constructs a new PlayerEconomy with the default starting capital.
     *
     * @see Constants#STARTING_CAPITAL
     */
    public PlayerEconomy() {
        capital = Constants.STARTING_CAPITAL;
    }

    /**
     * Returns the player's current capital.
     *
     * @return the amount of capital the player currently has
     */
    public long getCapital() {
        return capital;
    }

    /**
     * Adds money to the player's capital.
     *
     * @param amount the positive amount to earn
     * @throws IllegalArgumentException if amount is negative
     */
    public void earn(long amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must not be negative");
        capital += amount;
    }

    /**
     * Deducts money from the player's capital.
     *
     * @param amount the positive amount to spend
     * @throws IllegalArgumentException if amount is negative
     */
    public void deduct(long amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must not be negative");
        capital -= amount;
    }

    /**
     * Backwards-compatible alias for {@link #earn(long)}.
     * Prefer using {@code earn(long)} in new code.
     *
     * @param amount the positive amount to add
     */
    public void increaseCapital(long amount) {
        earn(amount);
    }

    /**
     * Backwards-compatible alias for {@link #deduct(long)}.
     * Prefer using {@code deduct(long)} in new code.
     *
     * @param amount the positive amount to subtract
     */
    public void decreaseCapital(long amount) {
        deduct(amount);
    }
}
