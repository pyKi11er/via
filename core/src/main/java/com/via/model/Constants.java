package com.via.model;

public class Constants {
    private Constants() {}

    public static final int TILE_SIZE = 32;        // pixels per tile
    public static final int MAP_WIDTH = 40;        // tiles
    public static final int MAP_HEIGHT = 30;       // tiles
    public static final float NORMAL_TICK_RATE = 1f / 60f; // seconds per tick
    public static final float FAST_MULTIPLIER = 2f;
    public static final float VERY_FAST_MULTIPLIER = 4f;
    public static final long STARTING_CAPITAL = 100_000L;

    /** Deduct maintenance once per interval per vehicle. */
    public static final int VEHICLE_MAINTENANCE_INTERVAL_TICKS = 60;

    public static long getBaseIncomePerUnit(GoodType type) {
        if (type == null) return 0L;
        return switch (type) {
            case PASSENGERS -> 10L;
            case WOOD -> 6L;
            case IRON -> 8L;
            case STEEL -> 12L;
        };
    }
}
