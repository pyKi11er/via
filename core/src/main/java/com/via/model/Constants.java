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
}
