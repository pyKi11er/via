package com.via.model.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the game map consisting of a grid of tiles.
 * Handles tile management, terrain operations, road construction,
 and vehicle movement logic.
 */
public class Map implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final double UPHILL_ONE_LEVEL_SPEED_MULTIPLIER = 0.75;
    private static final int MAX_ROAD_HEIGHT_DIFFERENCE = 1;
    private static final int MAX_TERRAFORM_ADJACENT_DIFFERENCE = 2;

    /** The dimensions of the map grid. */
    public final GridSize size;
    private final Tile[][] tiles;

    /**
     * Constructs a new Map with the specified size.
     *
     * @param size the dimensions of the map (width and height)
     */
    public Map(GridSize size) {
        this.size = size;
        this.tiles = new Tile[size.width][size.height];
    }

    /**
     * Returns the tile at the specified coordinates.
     *
     * @param coord the coordinates of the tile
     * @return the tile at the given coordinates, or null if not set
     */
    public Tile getTile(TileCoord coord) {
        return tiles[coord.x][coord.y];
    }

    /**
     * Sets a tile at the specified coordinates.
     *
     * @param tile  the tile to place
     * @param coord the coordinates where to place the tile
     */
    public void setTile(Tile tile, TileCoord coord) {
        tiles[coord.x][coord.y] = tile;
    }

    /**
     * Checks whether the given coordinates are within map bounds.
     *
     * @param coord the coordinates to check
     * @return true if the coordinates are within bounds, false otherwise
     */
    public boolean isWithinBounds(TileCoord coord) {
        return coord != null
            && coord.x >= 0
            && coord.y >= 0
            && coord.x < size.width
            && coord.y < size.height;
    }

    /**
     * Checks whether two tiles are adjacent (sharing a side, not diagonal).
     *
     * @param first  the first tile coordinates
     * @param second the second tile coordinates
     * @return true if the tiles are adjacent, false otherwise
     */
    public boolean areAdjacent(TileCoord first, TileCoord second) {
        if (!isWithinBounds(first) || !isWithinBounds(second)) {
            return false;
        }

        int dx = Math.abs(first.x - second.x);
        int dy = Math.abs(first.y - second.y);
        return dx + dy == 1;
    }

    /**
     * Returns a list of all adjacent tile coordinates (north, east, south, west).
     *
     * @param coord the reference tile coordinates
     * @return a list of valid adjacent coordinates within map bounds
     */
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

    /**
     * Calculates the absolute terrain level difference between two tiles.
     *
     * @param first  the first tile coordinates
     * @param second the second tile coordinates
     * @return the absolute height difference
     * @throws IllegalArgumentException if either coordinate is out of bounds or tile uninitialized
     */
    public int getHeightDifference(TileCoord first, TileCoord second) {
        Tile firstTile = getRequiredTile(first);
        Tile secondTile = getRequiredTile(second);

        return Math.abs(
            firstTile.getTerrain().getTerrainLevel() - secondTile.getTerrain().getTerrainLevel()
        );
    }

    /**
     * Checks whether a road can be built between two adjacent tiles.
     *
     * @param first  the first tile coordinates
     * @param second the second tile coordinates
     * @return true if the tiles are adjacent and height difference is within limits
     */
    public boolean canBuildRoadBetween(TileCoord first, TileCoord second) {
        if (!areAdjacent(first, second)) {
            return false;
        }

        return getHeightDifference(first, second) <= MAX_ROAD_HEIGHT_DIFFERENCE;
    }

    /**
     * Validates road construction between two tiles, throwing exceptions if invalid.
     *
     * @param first  the first tile coordinates
     * @param second the second tile coordinates
     * @throws IllegalArgumentException if tiles are not adjacent or height difference exceeds limit
     */
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

    /**
     * Checks whether a tile can be terraformed by the specified delta.
     *
     * @param coord the tile coordinates
     * @param delta the terrain level change (-1 for lower, +1 for raise)
     * @return true if terraforming is allowed, false otherwise
     */
    public boolean canTerraform(TileCoord coord, int delta) {
        if (!isWithinBounds(coord)) {
            return false;
        }

        if (delta != -1 && delta != 1) {
            return false;
        }

        Tile tile = getTile(coord);
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

    /**
     * Performs terraforming on a tile, changing its terrain level.
     *
     * @param coord        the tile coordinates
     * @param delta        the terrain level change (-1 or +1)
     * @param costPerLevel the cost per level of terraforming
     * @return the total cost of the operation
     * @throws IllegalArgumentException if costPerLevel is negative or terraforming is not allowed
     */
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

    /**
     * Calculates the cost to build a road on a tile, including forest clearing costs.
     *
     * @param coord              the tile coordinates
     * @param baseCost           the base road construction cost
     * @param clearingCostPerTree the cost to clear each tree
     * @return the total road building cost
     * @throws IllegalArgumentException if any cost parameter is negative
     */
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

    /**
     * Clears forest from a tile for road construction and returns the total cost.
     *
     * @param coord              the tile coordinates
     * @param baseCost           the base road construction cost
     * @param clearingCostPerTree the cost to clear each tree
     * @return the total cost for clearing and road construction
     */
    public int clearForestForRoad(TileCoord coord, int baseCost, int clearingCostPerTree) {
        Tile tile = getRequiredTile(coord);
        int cost = getRoadBuildCost(coord, baseCost, clearingCostPerTree);
        tile.getTerrain().setForestLevel(Terrain.MIN_FOREST_LEVEL);
        return cost;
    }

    /**
     * Returns the speed multiplier for a vehicle moving between two adjacent tiles.
     * Moving uphill reduces speed; downhill and flat movement have normal speed.
     *
     * @param from the starting tile coordinates
     * @param to   the destination tile coordinates
     * @return the speed multiplier (1.0 for flat/downhill, 0.75 for uphill)
     */
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

    /**
     * Retrieves a tile, throwing an exception if it doesn't exist or is out of bounds.
     *
     * @param coord the tile coordinates
     * @return the tile at the specified coordinates
     * @throws IllegalArgumentException if coordinates are out of bounds
     * @throws IllegalStateException    if the tile has not been initialized
     */
    private Tile getRequiredTile(TileCoord coord) {
        if (!isWithinBounds(coord)) {
            throw new IllegalArgumentException("Tile coordinate is out of bounds");
        }

        Tile tile = getTile(coord);
        if (tile == null) {
            throw new IllegalStateException(
                "Tile at (" + coord.x + ", " + coord.y + ") has not been initialized"
            );
        }

        return tile;
    }
}
