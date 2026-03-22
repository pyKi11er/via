package com.via.controller;

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

    /** True if a stop could be placed here (no side effects). */
    public boolean canPlaceStopAt(TileCoord coord) {
        if (coord == null || map == null) return false;
        Tile tile = map.getTile(coord);
        if (tile == null || !(tile.getStructure() instanceof Road) || tile.getOverlay() != null) {
            return false;
        }
        return map.getAdjacentCoords(coord).stream()
            .map(map::getTile)
            .anyMatch(t -> t != null && t.getStructure() instanceof Road);
    }

    public StopOverlay placeStop(String name, TileCoord coord, StopAccess access, Storage storage) {
        if (coord == null || access == null || storage == null || economy == null) return null;

        if (!canPlaceStopAt(coord)) return null;

        if (economy.getCapital() < baseCost) return null;

        economy.deduct(baseCost);
        StopOverlay stop = new StopOverlay(name, coord, access, storage);
        map.getTile(coord).setOverlay(stop);
        return stop;
    }
}
