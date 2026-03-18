package com.via.model.map;

import java.util.Objects;

public class Terrain {
    public static final int MIN_FOREST_LEVEL = 0;
    public static final int MAX_FOREST_LEVEL = 4;

    private TerrainType type;
    private int terrainLevel;
    private int forestLevel;

    public Terrain(TerrainType type, int terrainLevel, int forestLevel) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.terrainLevel = terrainLevel;
        setForestLevel(forestLevel);
    }

    public TerrainType getType() {
        return type;
    }

    public void setType(TerrainType type) {
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public int getTerrainLevel() {
        return terrainLevel;
    }

    public void setTerrainLevel(int terrainLevel) {
        this.terrainLevel = terrainLevel;
    }

    public int getForestLevel() {
        return forestLevel;
    }

    public void setForestLevel(int forestLevel) {
        if (forestLevel < MIN_FOREST_LEVEL || forestLevel > MAX_FOREST_LEVEL) {
            throw new IllegalArgumentException(
                "forestLevel must be between " + MIN_FOREST_LEVEL + " and " + MAX_FOREST_LEVEL
            );
        }
        this.forestLevel = forestLevel;
    }

    public boolean hasForest() {
        return forestLevel > 0;
    }
}
