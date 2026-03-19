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
 * Handles validation and construction of roads on the map.
 */
public class RoadBuilder {

    private final Map map;
    private final PlayerEconomy economy;
    private final int baseCost;
    private final int clearingCostPerTree;

    public RoadBuilder(Map map, PlayerEconomy economy, int baseCost, int clearingCostPerTree) {
        this.map = map;
        this.economy = economy;
        this.baseCost = baseCost;
        this.clearingCostPerTree = clearingCostPerTree;
    }

    /** Attempts to build a road at the specified tile. */
    public boolean buildRoad(TileCoord coord) {
        Tile tile = map.getTile(coord);
        if (tile == null)
            throw new IllegalArgumentException("Invalid coordinate: " + coord.x + "," + coord.y);

        if (tile.getStructure() instanceof Building) {
            throw new IllegalArgumentException("Cannot build roads on city or facility tiles.");
        }
        if (!tile.isEmpty()) return false;

        // Calculate and deduct cost
        int cost = map.getRoadBuildCost(coord, baseCost, clearingCostPerTree);
        economy.deduct(cost);

        // Clear forest
        tile.getTerrain().setForestLevel(Terrain.MIN_FOREST_LEVEL);

        // Place the road
        tile.setStructure(new Road(coord, baseCost));

        return true;
    }
}
