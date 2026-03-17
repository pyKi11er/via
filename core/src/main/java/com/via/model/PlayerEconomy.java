package com.via.model;

public class PlayerEconomy {
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

    public void increaseCapital(long amount) {
        earn(amount);
    }

    /** Backwards-compatible alias; prefer {@link #deduct(long)}. */
    public void decreaseCapital(long amount) {
        deduct(amount);
    }
}
