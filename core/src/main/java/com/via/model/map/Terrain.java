package com.via.model.map;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the terrain properties of a tile in the game map.
 * Encapsulates terrain type, elevation level, and forest density.
 */
public class Terrain implements Serializable {
    private static final long serialVersionUID = 1L;
    /** The minimum forest level (no forest). */
    public static final int MIN_FOREST_LEVEL = 0;

    /** The maximum forest level (dense forest). */
    public static final int MAX_FOREST_LEVEL = 4;

    private TerrainType type;
    private int terrainLevel;
    private int forestLevel;

    /**
     * Constructs a new Terrain with the specified properties.
     *
     * @param type         the terrain type (must not be null)
     * @param terrainLevel the elevation level of the terrain
     * @param forestLevel  the forest density level (must be between MIN_FOREST_LEVEL and MAX_FOREST_LEVEL)
     * @throws NullPointerException     if type is null
     * @throws IllegalArgumentException if forestLevel is out of valid range
     */
    public Terrain(TerrainType type, int terrainLevel, int forestLevel) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.terrainLevel = terrainLevel;
        setForestLevel(forestLevel);
    }

    /**
     * Returns the terrain type.
     *
     * @return the terrain type
     */
    public TerrainType getType() {
        return type;
    }

    /**
     * Sets the terrain type.
     *
     * @param type the new terrain type (must not be null)
     * @throws NullPointerException if type is null
     */
    public void setType(TerrainType type) {
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    /**
     * Returns the terrain elevation level.
     *
     * @return the terrain level
     */
    public int getTerrainLevel() {
        return terrainLevel;
    }

    /**
     * Sets the terrain elevation level.
     *
     * @param terrainLevel the new elevation level
     */
    public void setTerrainLevel(int terrainLevel) {
        this.terrainLevel = terrainLevel;
    }

    /**
     * Returns the forest density level.
     *
     * @return the forest level (0 = no forest, 4 = dense forest)
     */
    public int getForestLevel() {
        return forestLevel;
    }

    /**
     * Sets the forest density level.
     *
     * @param forestLevel the new forest level (must be between MIN_FOREST_LEVEL and MAX_FOREST_LEVEL)
     * @throws IllegalArgumentException if forestLevel is out of valid range
     */
    public void setForestLevel(int forestLevel) {
        if (forestLevel < MIN_FOREST_LEVEL || forestLevel > MAX_FOREST_LEVEL) {
            throw new IllegalArgumentException(
                "forestLevel must be between " + MIN_FOREST_LEVEL + " and " + MAX_FOREST_LEVEL
            );
        }
        this.forestLevel = forestLevel;
    }

    /**
     * Checks whether this terrain has any forest cover.
     *
     * @return true if forest level is greater than 0, false otherwise
     */
    public boolean hasForest() {
        return forestLevel > 0;
    }
}
