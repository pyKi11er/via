package com.via.controller;

import com.via.model.map.*;

import java.util.Random;

/**
 * Generates procedural terrain for the game map and derives slope information
 * from height differences between tiles.
 *
 * <p>This system is responsible for:</p>
 * <ul>
 *     <li>Generating randomized hill-based heightmaps</li>
 *     <li>Preserving a flat corridor for roads and infrastructure</li>
 *     <li>Smoothing terrain to avoid extreme height differences</li>
 *     <li>Converting height differences into directional slope types</li>
 * </ul>
 *
 * <p>Height values are discrete levels:</p>
 * <ul>
 *     <li>{@code 0} = flat / sea level</li>
 *     <li>{@code 3} = maximum elevation</li>
 * </ul>
 *
 * <p>Slope types indicate the steepest downhill direction from each tile.</p>
 */
public class TerrainGenerator {

    /** Maximum terrain height level. */
    private static final int MAX_LEVEL = 3;

    /**
     * Y-range reserved as a flat corridor for roads and buildings.
     * This ensures infrastructure can be placed without terrain conflicts.
     */
    private static final int FLAT_Y_START = 13;
    private static final int FLAT_Y_END   = 24;

    /**
     * Generates terrain heights across the map using procedural hill generation.
     *
     * <p>Generation steps:</p>
     * <ol>
     *     <li>Create random Gaussian-style hills across the map</li>
     *     <li>Preserve a flat corridor for infrastructure</li>
     *     <li>Clamp heights to valid range</li>
     *     <li>Smooth terrain to reduce extreme height differences</li>
     * </ol>
     *
     * <p>The provided seed ensures deterministic generation.</p>
     *
     * @param map the map to generate terrain for
     * @param seed random seed for deterministic generation
     */
    public static void generate(Map map, long seed) {
        Random rng = new Random(seed);
        int w = map.size.width;
        int h = map.size.height;

        float[][] heightMap = new float[w][h];

        // ---- Generate hill formations -----------------------------------------
        int numHills = 12;
        for (int i = 0; i < numHills; i++) {
            int cx = rng.nextInt(w);

            // Bias hill placement outside the flat corridor
            int cy;
            if (rng.nextBoolean()) {
                cy = rng.nextInt(Math.max(1, FLAT_Y_START - 2));
            } else {
                int bottom = FLAT_Y_END + 2;
                cy = bottom + rng.nextInt(Math.max(1, h - bottom));
            }

            int radius = 4 + rng.nextInt(6);              // 4–9 tiles
            float height = 1.0f + rng.nextInt(MAX_LEVEL); // 1–3

            int x0 = Math.max(0, cx - radius);
            int x1 = Math.min(w, cx + radius);
            int y0 = Math.max(0, cy - radius);
            int y1 = Math.min(h, cy + radius);

            for (int x = x0; x < x1; x++) {
                for (int y = y0; y < y1; y++) {
                    float dist = (float) Math.sqrt(
                        (x - cx) * (x - cx) + (y - cy) * (y - cy));

                    if (dist < radius) {
                        heightMap[x][y] += height * (1.0f - dist / radius);
                    }
                }
            }
        }

        // ---- Apply heights to terrain -----------------------------------------
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Tile tile = map.getTile(new TileCoord(x, y));
                if (tile == null) continue;

                int level;
                if (y >= FLAT_Y_START && y <= FLAT_Y_END) {
                    level = 0;
                } else {
                    level = Math.min(MAX_LEVEL, Math.round(heightMap[x][y]));
                }

                tile.getTerrain().setTerrainLevel(level);
            }
        }

        // ---- Smooth height transitions ----------------------------------------
        smoothHeights(map);
    }

    /**
     * Updates slope types for all tiles based on height differences with neighbors.
     *
     * <p>This should be called after terrain generation or any terrain modification.</p>
     *
     * @param map the map to update
     */
    public static void updateSlopeTypes(Map map) {
        for (int x = 0; x < map.size.width; x++) {
            for (int y = 0; y < map.size.height; y++) {
                TileCoord coord = new TileCoord(x, y);
                Tile tile = map.getTile(coord);
                if (tile == null) continue;

                tile.getTerrain().setType(computeSlopeType(map, coord));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Smooths terrain so adjacent tiles do not differ in height by more than 2 levels.
     *
     * <p>Multiple passes are required because adjustments propagate to neighbors.</p>
     *
     * @param map the map to smooth
     */
    private static void smoothHeights(Map map) {
        for (int pass = 0; pass < 4; pass++) {
            for (int x = 0; x < map.size.width; x++) {
                for (int y = 0; y < map.size.height; y++) {
                    Tile tile = map.getTile(new TileCoord(x, y));
                    if (tile == null) continue;

                    int level = tile.getTerrain().getTerrainLevel();

                    for (TileCoord adj : map.getAdjacentCoords(new TileCoord(x, y))) {
                        Tile adjTile = map.getTile(adj);
                        if (adjTile == null) continue;

                        int adjLevel = adjTile.getTerrain().getTerrainLevel();

                        if (Math.abs(level - adjLevel) > 2) {
                            int clamped = adjLevel > level ? level + 2 : level - 2;
                            adjTile.getTerrain().setTerrainLevel(clamped);
                        }
                    }
                }
            }
        }
    }

    /**
     * Computes the slope type for a tile based on the steepest descent to a neighbor.
     *
     * <p>The returned slope indicates the direction water/traffic would naturally flow
     * downhill from this tile.</p>
     *
     * @param map the map
     * @param coord tile coordinate
     * @return the computed {@link TerrainType}
     */
    private static TerrainType computeSlopeType(Map map, TileCoord coord) {
        int center = map.getTile(coord).getTerrain().getTerrainLevel();

        int maxDescent = 0;
        TerrainType result = TerrainType.FLAT;

        TileCoord north = new TileCoord(coord.x, coord.y + 1);
        if (map.isWithinBounds(north)) {
            int d = center - map.getTile(north).getTerrain().getTerrainLevel();
            if (d > maxDescent) { maxDescent = d; result = TerrainType.SLOPE_NORTH; }
        }

        TileCoord south = new TileCoord(coord.x, coord.y - 1);
        if (map.isWithinBounds(south)) {
            int d = center - map.getTile(south).getTerrain().getTerrainLevel();
            if (d > maxDescent) { maxDescent = d; result = TerrainType.SLOPE_SOUTH; }
        }

        TileCoord east = new TileCoord(coord.x + 1, coord.y);
        if (map.isWithinBounds(east)) {
            int d = center - map.getTile(east).getTerrain().getTerrainLevel();
            if (d > maxDescent) { maxDescent = d; result = TerrainType.SLOPE_EAST; }
        }

        TileCoord west = new TileCoord(coord.x - 1, coord.y);
        if (map.isWithinBounds(west)) {
            int d = center - map.getTile(west).getTerrain().getTerrainLevel();
            if (d > maxDescent) { maxDescent = d; result = TerrainType.SLOPE_WEST; }
        }

        return result;
    }
}
