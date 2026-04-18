package com.via.controller;

import com.via.model.GameState;
import com.via.model.building.Footprint;
import com.via.model.building.Garage;
import com.via.model.building.Road;
import com.via.model.map.Map;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;

/**
 * Responsible for validating and placing {@link Garage} structures on the map.
 *
 * <p>A garage can only be placed on an empty tile that is adjacent to at least
 * one {@link Road}. When successfully placed, the garage is added to the
 * {@link GameState} and optionally deducts construction cost from the player's
 * economy.</p>
 */
public class GarageBuilder {

    private final GameState state;
    private final int baseCost;

    /**
     * Creates a new {@code GarageBuilder}.
     *
     * @param state the current game state
     * @param baseCost the cost to place a garage; may be {@code 0} for free placement
     */
    public GarageBuilder(GameState state, int baseCost) {
        this.state = state;
        this.baseCost = baseCost;
    }

    /**
     * Attempts to place a 1×1 garage at the specified coordinate.
     *
     * <p>Placement rules:</p>
     * <ul>
     *     <li>The target tile must exist</li>
     *     <li>The tile must not already contain a structure or overlay</li>
     *     <li>The tile must be adjacent to at least one {@link Road}</li>
     * </ul>
     *
     * <p>If placement succeeds:</p>
     * <ul>
     *     <li>The construction cost is deducted (if {@code baseCost > 0})</li>
     *     <li>A new {@link Garage} is created and placed on the map</li>
     *     <li>The garage is registered in the {@link GameState}</li>
     * </ul>
     *
     * @param coord the map coordinate where the garage should be placed
     * @return {@code true} if the garage was successfully placed;
     *         {@code false} if the tile is occupied
     * @throws IllegalArgumentException if the coordinate is invalid or
     *         the placement is not adjacent to a road
     */
    public boolean placeGarage(TileCoord coord) {
        Map map = state.getMap();
        Tile tile = map.getTile(coord);

        if (tile == null) {
            throw new IllegalArgumentException(
                "Invalid coordinate: " + coord.x + "," + coord.y);
        }

        if (tile.getStructure() != null || tile.getOverlay() != null) {
            return false;
        }

        boolean adjacentRoad = map.getAdjacentCoords(coord).stream()
            .map(map::getTile)
            .anyMatch(t -> t != null && t.getStructure() instanceof Road);

        if (!adjacentRoad) {
            throw new IllegalArgumentException(
                "Garage must be placed adjacent to a road.");
        }

        if (baseCost > 0) {
            state.getEconomy().deduct(baseCost);
        }

        Garage garage = new Garage(new Footprint(coord, 1, 1));
        tile.setStructure(garage);
        state.addBuilding(garage);

        return true;
    }
}
