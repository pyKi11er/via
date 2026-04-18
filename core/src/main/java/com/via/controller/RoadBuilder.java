package com.via.controller;

import com.via.model.*;
import com.via.model.building.Building;
import com.via.model.building.Road;
import com.via.model.economy.PlayerEconomy;
import com.via.model.map.Map;
import com.via.model.map.Terrain;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;

/**
 * Handles validation and construction of {@link Road} structures on the map.
 *
 * <p>Road placement enforces terrain, adjacency, and economic constraints.
 * Construction may also clear forest from the target tile at an additional cost.</p>
 */
public class RoadBuilder {

    private final Map map;
    private final PlayerEconomy economy;
    private final int baseCost;
    private final int clearingCostPerTree;

    /**
     * Creates a new {@code RoadBuilder}.
     *
     * @param map the map where roads will be built
     * @param economy the player's economy used for cost validation and deduction
     * @param baseCost base construction cost of a road
     * @param clearingCostPerTree additional cost per unit of forest cleared
     */
    public RoadBuilder(Map map, PlayerEconomy economy, int baseCost, int clearingCostPerTree) {
        this.map = map;
        this.economy = economy;
        this.baseCost = baseCost;
        this.clearingCostPerTree = clearingCostPerTree;
    }

    /**
     * Attempts to build a road at the specified coordinate.
     *
     * <p>Placement rules:</p>
     * <ul>
     *     <li>The tile must exist</li>
     *     <li>The tile must not contain a {@link Building}</li>
     *     <li>The tile must be empty (no structure or overlay)</li>
     *     <li>The height difference with any adjacent {@link Road} must not exceed 1</li>
     * </ul>
     *
     * <p>Cost calculation:</p>
     * <ul>
     *     <li>Base cost plus additional cost for clearing forest</li>
     *     <li>If insufficient funds are available, construction fails</li>
     * </ul>
     *
     * <p>If construction succeeds:</p>
     * <ul>
     *     <li>The total cost is deducted from the player's economy</li>
     *     <li>Forest on the tile is cleared</li>
     *     <li>A new {@link Road} is placed on the tile</li>
     * </ul>
     *
     * @param coord the coordinate where the road should be built
     * @return {@code true} if the road was successfully built;
     *         {@code false} if the tile is not empty or funds are insufficient
     * @throws IllegalArgumentException if the coordinate is invalid,
     *         the tile contains a building, or slope constraints are violated
     */
    public boolean buildRoad(TileCoord coord) {
        Tile tile = map.getTile(coord);

        if (tile == null) {
            throw new IllegalArgumentException(
                "Invalid coordinate: " + coord.x + "," + coord.y);
        }

        if (tile.getStructure() instanceof Building) {
            throw new IllegalArgumentException(
                "Cannot build roads on city or facility tiles.");
        }

        if (!tile.isEmpty()) {
            return false;
        }

        // Validate slope constraints against adjacent roads
        for (TileCoord adj : map.getAdjacentCoords(coord)) {
            Tile adjTile = map.getTile(adj);

            if (adjTile != null && adjTile.getStructure() instanceof Road) {
                if (map.getHeightDifference(coord, adj) > 1) {
                    throw new IllegalArgumentException(
                        "Cannot build road: height difference with adjacent road exceeds 1.");
                }
            }
        }

        // Calculate total construction cost
        int cost = map.getRoadBuildCost(coord, baseCost, clearingCostPerTree);

        // Prevent construction if insufficient funds
        if (economy.getCapital() < cost) {
            return false;
        }

        // Deduct cost and clear forest
        economy.deduct(cost);
        tile.getTerrain().setForestLevel(Terrain.MIN_FOREST_LEVEL);

        // Place the road
        tile.setStructure(new Road(coord, baseCost));

        return true;
    }
}
