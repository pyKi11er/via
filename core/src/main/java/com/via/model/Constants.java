package com.via.model;

import com.via.model.enums.GoodType;

public class Constants {
    private Constants() {}

    public static final int TILE_SIZE = 32;        // pixels per tile
    public static final int MAP_WIDTH = 40;        // tiles
    public static final int MAP_HEIGHT = 30;       // tiles
    public static final float NORMAL_TICK_RATE = 1f / 60f; // seconds per tick
    public static final float FAST_MULTIPLIER = 2f;
    public static final float VERY_FAST_MULTIPLIER = 4f;
    public static final long STARTING_CAPITAL = 100_000L;
    public static final int GARAGE_CAPACITY = 5;

    /** Cost to place one stop (used by {@link com.via.controller.StopBuilder}). */
    public static final int STOP_PLACEMENT_COST = 2_000;

    /** Deduct maintenance once per interval per vehicle. */
    public static final int VEHICLE_MAINTENANCE_INTERVAL_TICKS = 18000; // 1 minute in game

    /**
     * Vehicle age (in simulation ticks) at which it becomes "over-aged" and can be sold.
     * When the vehicle is sold, its resale value is returned to capital.
     */
    // public static final int VEHICLE_MAX_AGE_TICKS = 216000; // IRL 1 hour
    public static final int VEHICLE_MAX_AGE_TICKS = 99999999; // temp: effectively never over-aged

    /**
     * Minimum maintenance interval as the vehicle ages.
     * The maintenance interval decreases with age but will never go below this value.
     */
    // public static final int VEHICLE_MIN_MAINTENANCE_INTERVAL_TICKS = 15;
    public static final int VEHICLE_MIN_MAINTENANCE_INTERVAL_TICKS = 18000; // temp: no degradation

    /**
     * Controls how quickly the maintenance interval decreases with age.
     * Interval(age) is computed as:
     * baseInterval / (1 + age/scale), clamped by VEHICLE_MIN_MAINTENANCE_INTERVAL_TICKS.
     */
    // public static final int VEHICLE_MAINTENANCE_INTERVAL_AGE_SCALE_TICKS = 300;
    public static final int VEHICLE_MAINTENANCE_INTERVAL_AGE_SCALE_TICKS = 99999999; // temp: no degradation

    /**
     * Resale value floor as a fraction of purchaseCost (used when the vehicle is over-aged).
     */
    public static final double VEHICLE_MIN_RESALE_FRACTION = 0.10;

    /** Returns the maintenance interval in ticks based on current vehicle age. */
    public static long getMaintenanceIntervalTicks(long ageTicks) {
        if (ageTicks < 0) ageTicks = 0;
        double base = VEHICLE_MAINTENANCE_INTERVAL_TICKS;
        double scale = Math.max(1.0, VEHICLE_MAINTENANCE_INTERVAL_AGE_SCALE_TICKS);
        long interval = Math.round(base / (1.0 + (ageTicks / scale)));
        return Math.max(VEHICLE_MIN_MAINTENANCE_INTERVAL_TICKS, interval);
    }

    /** Computes resale value as a deterministic function of age. */
    public static long getResaleValue(long purchaseCost, long ageTicks) {
        if (purchaseCost <= 0) return 0;
        if (ageTicks < 0) ageTicks = 0;

        // Resale fraction declines linearly with age, but never below VEHICLE_MIN_RESALE_FRACTION.
        double frac = 1.0 - ((double) ageTicks / (double) VEHICLE_MAX_AGE_TICKS);
        frac = Math.max(VEHICLE_MIN_RESALE_FRACTION, Math.min(1.0, frac));
        return Math.round(purchaseCost * frac);
    }

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
