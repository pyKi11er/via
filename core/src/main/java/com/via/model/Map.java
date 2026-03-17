package com.via.model;

import java.util.ArrayList;
import java.util.List;

public class Map {
    private static final double UPHILL_ONE_LEVEL_SPEED_MULTIPLIER = 0.75;
    private static final int MAX_ROAD_HEIGHT_DIFFERENCE = 1;
    private static final int MAX_TERRAFORM_ADJACENT_DIFFERENCE = 2;

    public final GridSize size;
    private final Tile[][] tiles;

    public Map(GridSize size) {
        this.size = size;
        this.tiles = new Tile[size.width][size.height];
    }

    public Tile getTile(TileCoord coord) {
        return tiles[coord.x][coord.y];
    }

    public void setTile(Tile tile, TileCoord coord) {
        tiles[coord.x][coord.y] = tile;
    }

    public boolean isWithinBounds(TileCoord coord) {
        return coord != null
            && coord.x >= 0
            && coord.y >= 0
            && coord.x < size.width
            && coord.y < size.height;
    }

    public boolean areAdjacent(TileCoord first, TileCoord second) {
        if (!isWithinBounds(first) || !isWithinBounds(second)) {
            return false;
        }

        int dx = Math.abs(first.x - second.x);
        int dy = Math.abs(first.y - second.y);
        return dx + dy == 1;
    }

    public List<TileCoord> getAdjacentCoords(TileCoord coord) {
        List<TileCoord> adjacent = new ArrayList<>();

        TileCoord north = new TileCoord(coord.x, coord.y - 1);
        TileCoord east = new TileCoord(coord.x + 1, coord.y);
        TileCoord south = new TileCoord(coord.x, coord.y + 1);
        TileCoord west = new TileCoord(coord.x - 1, coord.y);

        if (isWithinBounds(north)) adjacent.add(north);
        if (isWithinBounds(east)) adjacent.add(east);
        if (isWithinBounds(south)) adjacent.add(south);
        if (isWithinBounds(west)) adjacent.add(west);

        return adjacent;
    }

    public int getHeightDifference(TileCoord first, TileCoord second) {
        Tile firstTile = getRequiredTile(first);
        Tile secondTile = getRequiredTile(second);

        return Math.abs(
            firstTile.getTerrain().getTerrainLevel() - secondTile.getTerrain().getTerrainLevel()
        );
    }

    public boolean canBuildRoadBetween(TileCoord first, TileCoord second) {
        if (!areAdjacent(first, second)) {
            return false;
        }

        return getHeightDifference(first, second) <= MAX_ROAD_HEIGHT_DIFFERENCE;
    }

    public void validateRoadBuildBetween(TileCoord first, TileCoord second) {
        if (!areAdjacent(first, second)) {
            throw new IllegalArgumentException("Road can only be built between adjacent tiles");
        }

        if (getHeightDifference(first, second) > MAX_ROAD_HEIGHT_DIFFERENCE) {
            throw new IllegalArgumentException(
                "Road cannot be built when adjacent tiles differ in height by more than "
                    + MAX_ROAD_HEIGHT_DIFFERENCE
            );
        }
    }

    public boolean canTerraform(TileCoord coord, int delta) {
        if (!isWithinBounds(coord)) {
            return false;
        }

        if (delta != -1 && delta != 1) {
            return false;
        }

        Tile tile = getTiles(coord);
        if (tile == null || !tile.isEmpty()) {
            return false;
        }

        int newLevel = tile.getTerrain().getTerrainLevel() + delta;
        for (TileCoord adjacentCoord : getAdjacentCoords(coord)) {
            Tile adjacentTile = getRequiredTile(adjacentCoord);
            int adjacentLevel = adjacentTile.getTerrain().getTerrainLevel();
            if (Math.abs(newLevel - adjacentLevel) > MAX_TERRAFORM_ADJACENT_DIFFERENCE) {
                return false;
            }
        }

        return true;
    }

    public int terraform(TileCoord coord, int delta, int costPerLevel) {
        if (costPerLevel < 0) {
            throw new IllegalArgumentException("costPerLevel must not be negative");
        }

        if (!canTerraform(coord, delta)) {
            throw new IllegalArgumentException("Terraforming is not allowed for the given tile/change");
        }

        Tile tile = getRequiredTile(coord);
        Terrain terrain = tile.getTerrain();
        terrain.setTerrainLevel(terrain.getTerrainLevel() + delta);

        return Math.abs(delta) * costPerLevel;
    }

    public int getRoadBuildCost(TileCoord coord, int baseCost, int clearingCostPerTree) {
        if (baseCost < 0) {
            throw new IllegalArgumentException("baseCost must not be negative");
        }
        if (clearingCostPerTree < 0) {
            throw new IllegalArgumentException("clearingCostPerTree must not be negative");
        }

        Tile tile = getRequiredTile(coord);
        int forestLevel = tile.getTerrain().getForestLevel();
        return baseCost + (forestLevel * clearingCostPerTree);
    }

    public int clearForestForRoad(TileCoord coord, int baseCost, int clearingCostPerTree) {
        Tile tile = getRequiredTile(coord);
        int cost = getRoadBuildCost(coord, baseCost, clearingCostPerTree);
        tile.getTerrain().setForestLevel(Terrain.MIN_FOREST_LEVEL);
        return cost;
    }

    public double getVehicleSpeedMultiplier(TileCoord from, TileCoord to) {
        if (!areAdjacent(from, to)) {
            return 1.0;
        }

        int fromLevel = getRequiredTile(from).getTerrain().getTerrainLevel();
        int toLevel = getRequiredTile(to).getTerrain().getTerrainLevel();

        if (toLevel > fromLevel) {
            return UPHILL_ONE_LEVEL_SPEED_MULTIPLIER;
        }

        return 1.0;
    }

    private Tile getRequiredTile(TileCoord coord) {
        if (!isWithinBounds(coord)) {
            throw new IllegalArgumentException("Tile coordinate is out of bounds");
        }

        Tile tile = getTiles(coord);
        if (tile == null) {
            throw new IllegalStateException(
                "Tile at (" + coord.x + ", " + coord.y + ") has not been initialized"
            );
        }

        return tile;
    }
}
