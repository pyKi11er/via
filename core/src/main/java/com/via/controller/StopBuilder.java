package com.via.controller;

import com.via.model.*;
import com.via.model.building.Road;
import com.via.model.building.StopAccess;
import com.via.model.building.StopOverlay;
import com.via.model.economy.PlayerEconomy;
import com.via.model.economy.Storage;
import com.via.model.map.Map;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;

public class StopBuilder {

    private final Map map;
    private final PlayerEconomy economy;
    private final int baseCost;

    public StopBuilder(Map map, PlayerEconomy economy, int baseCost) {
        this.map = map;
        this.economy = economy;
        this.baseCost = baseCost;
    }

    public boolean placeStop(String name, TileCoord coord, StopAccess access, Storage storage) {
        Tile tile = map.getTile(coord);
        if (tile == null || !tile.isEmpty()) return false;

        // Must have at least one adjacent road
        boolean adjacentRoad = map.getAdjacentCoords(coord).stream()
            .map(map::getTile)
            .anyMatch(t -> t != null && t.getStructure() instanceof Road);

        if (!adjacentRoad) throw new IllegalArgumentException("Stop must be adjacent to a road.");

        economy.deduct(baseCost);
        tile.setOverlay(new StopOverlay(name, coord, access, storage));
        return true;
    }
}
