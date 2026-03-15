package com.via.model;

public class Terrain {
    private TerrainType type;
    private int terrainLevel;
    private int forestLevel;

    public Terrain(TerrainType type, int terrainLevel, int forestLevel) {
        this.type = type;
        this.terrainLevel = terrainLevel;
        this.forestLevel = forestLevel;
    }
    public TerrainType getType() {
        return type;
    }
    public void setType(TerrainType type) {
        this.type = type;
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
        this.forestLevel = forestLevel;
    }
}
