package com.via.controller;

import com.via.model.GameState;
import com.via.model.building.Footprint;
import com.via.model.building.Garage;
import com.via.model.building.Road;
import com.via.model.map.Map;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;

/**
 * Validates and places garages on the map as building structures connected to adjacent road tiles.
 */
public class GarageBuilder {

    private final GameState state;
    private final int baseCost;

    public GarageBuilder(GameState state, int baseCost) {
        this.state = state;
        this.baseCost = baseCost;
    }

    /** Attempts to place a 1x1 garage at the provided tile coordinate. */
    public boolean placeGarage(TileCoord coord) {
        Map map = state.getMap();
        Tile tile = map.getTile(coord);

        if (tile == null) throw new IllegalArgumentException("Invalid coordinate: " + coord.x + "," + coord.y);
        if (tile.getStructure() != null || tile.getOverlay() != null) return false;

        boolean adjacentRoad = map.getAdjacentCoords(coord).stream()
            .map(map::getTile)
            .anyMatch(t -> t != null && t.getStructure() instanceof Road);

        if (!adjacentRoad) {
            throw new IllegalArgumentException("Garage must be placed adjacent to a road.");
        }

        if (baseCost > 0) {
            state.getEconomy().deduct(baseCost);
        }

        Garage garage = new Garage(new Footprint(coord, 1, 1));
        tile.setStructure(garage);
        state.addBuilding(garage); // Ensure GameState tracks this new garage

        return true;
    }
}
