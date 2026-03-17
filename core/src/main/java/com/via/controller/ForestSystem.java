package com.via.controller;

import com.via.model.GameState;
import com.via.model.Map;
import com.via.model.Terrain;
import com.via.model.Tile;
import com.via.model.TileCoord;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class ForestSystem {
    private static final float DEFAULT_GROWTH_PROBABILITY = 0.02f;
    private static final float DEFAULT_SPREAD_PROBABILITY = 0.01f;

    private final float growthProbability;
    private final float spreadProbability;
    private final Random random;

    public ForestSystem() {
        this(DEFAULT_GROWTH_PROBABILITY, DEFAULT_SPREAD_PROBABILITY, new Random());
    }

    public ForestSystem(float growthProbability, float spreadProbability) {
        this(growthProbability, spreadProbability, new Random());
    }

    public ForestSystem(float growthProbability, float spreadProbability, Random random) {
        validateProbability(growthProbability, "growthProbability");
        validateProbability(spreadProbability, "spreadProbability");

        this.growthProbability = growthProbability;
        this.spreadProbability = spreadProbability;
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    public void tick(GameState state) {
        if (state == null || state.getMap() == null) {
            return;
        }

        Map map = state.getMap();
        Set<TileCoord> spreadTargets = new HashSet<>();

        for (int x = 0; x < map.size.width; x++) {
            for (int y = 0; y < map.size.height; y++) {
                TileCoord sourceCoord = new TileCoord(x, y);
                Tile sourceTile = map.getTiles(sourceCoord);

                if (sourceTile == null || sourceTile.getTerrain() == null) {
                    continue;
                }

                Terrain terrain = sourceTile.getTerrain();
                int forestLevel = terrain.getForestLevel();
                if (forestLevel <= Terrain.MIN_FOREST_LEVEL) {
                    continue;
                }

                if (forestLevel < Terrain.MAX_FOREST_LEVEL && random.nextFloat() < growthProbability) {
                    terrain.setForestLevel(forestLevel + 1);
                }

                for (TileCoord adjacentCoord : map.getAdjacentCoords(sourceCoord)) {
                    Tile adjacentTile = map.getTiles(adjacentCoord);
                    if (!canSpreadInto(adjacentTile)) {
                        continue;
                    }

                    if (random.nextFloat() < spreadProbability) {
                        spreadTargets.add(adjacentCoord);
                    }
                }
            }
        }

        for (TileCoord targetCoord : spreadTargets) {
            Tile targetTile = map.getTiles(targetCoord);
            if (!canSpreadInto(targetTile)) {
                continue;
            }
            targetTile.getTerrain().setForestLevel(1);
        }
    }

    private boolean canSpreadInto(Tile tile) {
        return tile != null
            && tile.getTerrain() != null
            && !tile.getTerrain().hasForest()
            && tile.isEmpty();
    }

    private static void validateProbability(float probability, String name) {
        if (probability < 0f || probability > 1f) {
            throw new IllegalArgumentException(name + " must be between 0 and 1");
        }
    }
}
