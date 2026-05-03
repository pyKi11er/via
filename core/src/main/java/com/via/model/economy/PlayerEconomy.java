package com.via.model.economy;

import com.via.model.Constants;

import java.io.Serializable;

public class PlayerEconomy implements Serializable {
    private static final long serialVersionUID = 1L;
    private long capital;
    public PlayerEconomy() {
        capital = Constants.STARTING_CAPITAL;
    }
    public long getCapital() {
        return capital;
    }
    public void earn(long amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must not be negative");
        capital += amount;
    }

    public void deduct(long amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must not be negative");
        capital -= amount;
    }

    /** Backwards-compatible alias; prefer {@link #earn(long)}. */
    public void increaseCapital(long amount) {
        earn(amount);
    }

    /** Backwards-compatible alias; prefer {@link #deduct(long)}. */
    public void decreaseCapital(long amount) {
        deduct(amount);
    }
}
