package com.via.controller;

import com.via.model.map.*;

import java.util.Random;

/**
 * Generates varied terrain heights for the game map and derives slope tile types
 * from the resulting height differences.
 *
 * Height convention:
 *   - Level 0 = flat / sea-level
 *   - Level 3 = maximum hill height
 *
 * Slope convention:
 *   - SLOPE_X means this tile is higher than its neighbour in direction X
 *     (i.e. the terrain descends toward direction X).
 */
public class TerrainGenerator {

    private static final int MAX_LEVEL = 3;

    /**
     * Y range that must stay flat so the pre-built road network and buildings
     * remain playable without terraforming.
     */
    private static final int FLAT_Y_START = 13;
    private static final int FLAT_Y_END   = 24;

    // Public API

    /**
     * Fills every tile's {@code terrainLevel} with a plausible hill landscape.
     * The road/building corridor (rows {@value #FLAT_Y_START}–{@value #FLAT_Y_END})
     * is kept at level 0 so it remains immediately buildable.
     */
    public static void generate(Map map, long seed) {
        Random rng = new Random(seed);
        int w = map.size.width;
        int h = map.size.height;

        float[][] heightMap = new float[w][h];

        // ---- place random Gaussian-falloff hills --------------------------------
        int numHills = 12;
        for (int i = 0; i < numHills; i++) {
            int cx = rng.nextInt(w);

            // Bias centers above/below the flat corridor
            int cy;
            if (rng.nextBoolean()) {
                cy = rng.nextInt(Math.max(1, FLAT_Y_START - 2));    // upper wilderness
            } else {
                int bottom = FLAT_Y_END + 2;
                cy = bottom + rng.nextInt(Math.max(1, h - bottom)); // lower wilderness
            }

            int radius       = 4 + rng.nextInt(6);            // 4–9 tiles
            float hillHeight = 1.0f + rng.nextInt(MAX_LEVEL);  // 1–3

            int x0 = Math.max(0, cx - radius);
            int x1 = Math.min(w, cx + radius);
            int y0 = Math.max(0, cy - radius);
            int y1 = Math.min(h, cy + radius);

            for (int x = x0; x < x1; x++) {
                for (int y = y0; y < y1; y++) {
                    float dist = (float) Math.sqrt((double)(x - cx) * (x - cx)
                                                 + (double)(y - cy) * (y - cy));
                    if (dist < radius) {
                        heightMap[x][y] += hillHeight * (1.0f - dist / radius);
                    }
                }
            }
        }

        // ---- apply heights to tiles, keeping the flat corridor at 0 -----------
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

        // ---- smooth: no two adjacent tiles may differ by more than 2 ----------
        smoothHeights(map);
    }

    /**
     * Recomputes every tile's {@link TerrainType} from its current height
     * relative to its four neighbours.  Call this after {@link #generate} and
     * after every terraform action.
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
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Iteratively clamps height differences so no adjacent pair differs by more
     * than 2.  Multiple passes are needed because one clamp may shift a tile
     * relative to its other neighbours.
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
     * Returns the {@link TerrainType} for {@code coord} based on which adjacent
     * tile (if any) has the lowest height.  The slope type points toward the
     * steepest downhill direction.
     *
     * <p>North in this game is the +Y direction (matching {@link
     * com.via.model.enums.Direction#N}).
     */
    private static TerrainType computeSlopeType(Map map, TileCoord coord) {
        int centerLevel = map.getTile(coord).getTerrain().getTerrainLevel();

        int maxDescent = 0;
        TerrainType result = TerrainType.FLAT;

        // North = y + 1
        TileCoord north = new TileCoord(coord.x, coord.y + 1);
        if (map.isWithinBounds(north)) {
            int descent = centerLevel - map.getTile(north).getTerrain().getTerrainLevel();
            if (descent > maxDescent) { maxDescent = descent; result = TerrainType.SLOPE_NORTH; }
        }

        // South = y - 1
        TileCoord south = new TileCoord(coord.x, coord.y - 1);
        if (map.isWithinBounds(south)) {
            int descent = centerLevel - map.getTile(south).getTerrain().getTerrainLevel();
            if (descent > maxDescent) { maxDescent = descent; result = TerrainType.SLOPE_SOUTH; }
        }

        // East = x + 1
        TileCoord east = new TileCoord(coord.x + 1, coord.y);
        if (map.isWithinBounds(east)) {
            int descent = centerLevel - map.getTile(east).getTerrain().getTerrainLevel();
            if (descent > maxDescent) { maxDescent = descent; result = TerrainType.SLOPE_EAST; }
        }

        // West = x - 1
        TileCoord west = new TileCoord(coord.x - 1, coord.y);
        if (map.isWithinBounds(west)) {
            int descent = centerLevel - map.getTile(west).getTerrain().getTerrainLevel();
            if (descent > maxDescent) { maxDescent = descent; result = TerrainType.SLOPE_WEST; }
        }

        return result;
    }
}
