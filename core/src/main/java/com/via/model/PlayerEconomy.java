package com.via.model;

public class PlayerEconomy {
    private long capital;
    public PlayerEconomy() {
        capital = Constants.STARTING_CAPITAL;
    }
    public long getCapital() {
        return capital;
    }
    public void increaseCapital(long amount) {
        capital += amount;
    }

    public void decreaseCapital(long amount) {
        capital -= amount;
    }
}
