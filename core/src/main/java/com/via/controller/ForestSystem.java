package com.via.controller;

import com.via.model.GameState;
import com.via.model.map.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Simulates forest growth and spreading across the map over time.
 *
 * <p>The system operates on each game tick and applies two main processes:</p>
 * <ul>
 *     <li><b>Growth:</b> Existing forest tiles may increase their forest level
 *     up to {@link Terrain#MAX_FOREST_LEVEL}.</li>
 *     <li><b>Spread:</b> Forest tiles may spread to adjacent empty tiles,
 *     creating new forest with a minimal level.</li>
 * </ul>
 *
 * <p>Both processes are probabilistic and controlled by configurable parameters.</p>
 */
public class ForestSystem {

    /** Default probability that a forest tile increases its level on a tick. */
    private static final float DEFAULT_GROWTH_PROBABILITY = 0.0002f;

    /** Default probability that a forest tile spreads to an adjacent tile on a tick. */
    private static final float DEFAULT_SPREAD_PROBABILITY = 0.0001f;

    private final float growthProbability;
    private final float spreadProbability;
    private final Random random;

    /**
     * Creates a {@code ForestSystem} with default growth and spread probabilities.
     */
    public ForestSystem() {
        this(DEFAULT_GROWTH_PROBABILITY, DEFAULT_SPREAD_PROBABILITY, new Random());
    }

    /**
     * Creates a {@code ForestSystem} with custom growth and spread probabilities.
     *
     * @param growthProbability probability of forest level increase per tick (0–1)
     * @param spreadProbability probability of forest spreading per adjacent tile (0–1)
     * @throws IllegalArgumentException if any probability is outside [0, 1]
     */
    public ForestSystem(float growthProbability, float spreadProbability) {
        this(growthProbability, spreadProbability, new Random());
    }

    /**
     * Creates a {@code ForestSystem} with full control over probabilities and randomness.
     *
     * @param growthProbability probability of forest level increase per tick (0–1)
     * @param spreadProbability probability of forest spreading per adjacent tile (0–1)
     * @param random random number generator used for simulation
     * @throws IllegalArgumentException if any probability is outside [0, 1]
     * @throws NullPointerException if {@code random} is {@code null}
     */
    public ForestSystem(float growthProbability, float spreadProbability, Random random) {
        validateProbability(growthProbability, "growthProbability");
        validateProbability(spreadProbability, "spreadProbability");

        this.growthProbability = growthProbability;
        this.spreadProbability = spreadProbability;
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    /**
     * Advances the forest simulation by one game tick.
     *
     * <p>For each tile on the map:</p>
     * <ul>
     *     <li>If it contains forest, it may grow in level.</li>
     *     <li>It may attempt to spread forest to adjacent valid tiles.</li>
     * </ul>
     *
     * <p>Spreading is applied after scanning all tiles to avoid bias from
     * iteration order.</p>
     *
     * @param state the current game state; may be {@code null}
     */
    public void tick(GameState state) {
        if (state == null || state.getMap() == null) {
            return;
        }

        Map map = state.getMap();
        Set<TileCoord> spreadTargets = new HashSet<>();

        for (int x = 0; x < map.size.width; x++) {
            for (int y = 0; y < map.size.height; y++) {
                TileCoord sourceCoord = new TileCoord(x, y);
                Tile sourceTile = map.getTile(sourceCoord);

                if (sourceTile == null || sourceTile.getTerrain() == null) {
                    continue;
                }

                Terrain terrain = sourceTile.getTerrain();
                int forestLevel = terrain.getForestLevel();

                if (forestLevel <= Terrain.MIN_FOREST_LEVEL) {
                    continue;
                }

                // Growth step
                if (forestLevel < Terrain.MAX_FOREST_LEVEL
                    && random.nextFloat() < growthProbability) {
                    terrain.setForestLevel(forestLevel + 1);
                }

                // Spread step
                for (TileCoord adjacentCoord : map.getAdjacentCoords(sourceCoord)) {
                    Tile adjacentTile = map.getTile(adjacentCoord);

                    if (!canSpreadInto(adjacentTile)) {
                        continue;
                    }

                    if (random.nextFloat() < spreadProbability) {
                        spreadTargets.add(adjacentCoord);
                    }
                }
            }
        }

        // Apply spreading after evaluation
        for (TileCoord targetCoord : spreadTargets) {
            Tile targetTile = map.getTile(targetCoord);

            if (!canSpreadInto(targetTile)) {
                continue;
            }

            targetTile.getTerrain().setForestLevel(1);
        }
    }

    /**
     * Determines whether forest can spread into a given tile.
     *
     * <p>A tile is valid if it:</p>
     * <ul>
     *     <li>Exists and has terrain</li>
     *     <li>Does not already contain forest</li>
     *     <li>Is empty (no structures or overlays)</li>
     * </ul>
     *
     * @param tile the tile to evaluate
     * @return {@code true} if forest can spread into the tile, otherwise {@code false}
     */
    private boolean canSpreadInto(Tile tile) {
        return tile != null
            && tile.getTerrain() != null
            && !tile.getTerrain().hasForest()
            && tile.isEmpty();
    }

    /**
     * Validates that a probability value is within the range [0, 1].
     *
     * @param probability the value to validate
     * @param name the name of the parameter (for error messages)
     * @throws IllegalArgumentException if the value is outside the valid range
     */
    private static void validateProbability(float probability, String name) {
        if (probability < 0f || probability > 1f) {
            throw new IllegalArgumentException(name + " must be between 0 and 1");
        }
    }
}
