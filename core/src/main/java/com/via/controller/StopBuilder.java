package com.via.controller;

import com.via.model.building.Road;
import com.via.model.building.StopAccess;
import com.via.model.building.StopOverlay;
import com.via.model.economy.PlayerEconomy;
import com.via.model.economy.Storage;
import com.via.model.map.Map;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;

/**
 * Handles validation and placement of {@link StopOverlay} structures on road tiles.
 *
 * <p>Stops act as interaction points between vehicles and the map, enabling
 * loading and unloading of goods or passengers.</p>
 *
 * <p>Placement requires adjacency to road infrastructure and sufficient funds.</p>
 */
public class StopBuilder {

    private final Map map;
    private final PlayerEconomy economy;
    private final int baseCost;

    /**
     * Creates a new {@code StopBuilder}.
     *
     * @param map the game map where stops will be placed
     * @param economy the player's economy used for cost validation and deduction
     * @param baseCost the cost of constructing a stop
     */
    public StopBuilder(Map map, PlayerEconomy economy, int baseCost) {
        this.map = map;
        this.economy = economy;
        this.baseCost = baseCost;
    }

    /**
     * Checks whether a stop can be placed at the given coordinate.
     *
     * <p>A valid placement requires:</p>
     * <ul>
     *     <li>The tile must exist</li>
     *     <li>The tile must contain a {@link Road}</li>
     *     <li>The tile must not already have an overlay</li>
     *     <li>At least one adjacent tile must also contain a {@link Road}</li>
     * </ul>
     *
     * <p>This method has no side effects.</p>
     *
     * @param coord the coordinate to check
     * @return {@code true} if a stop can be placed, otherwise {@code false}
     */
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

    /**
     * Places a stop on the map if all placement conditions are satisfied.
     *
     * <p>Placement requires:</p>
     * <ul>
     *     <li>Valid coordinate and access/storage definitions</li>
     *     <li>Sufficient player funds</li>
     *     <li>A valid road-based location as determined by {@link #canPlaceStopAt(TileCoord)}</li>
     * </ul>
     *
     * <p>If successful, the cost is deducted from the player's economy and
     * a new {@link StopOverlay} is placed on the map.</p>
     *
     * @param name display name of the stop
     * @param coord map coordinate where the stop is placed
     * @param access defines how vehicles interact with the stop
     * @param storage storage container for goods/passengers
     * @return {@code true} if the stop was successfully placed,
     *         {@code false} if placement conditions were not met
     */
    public boolean placeStop(String name, TileCoord coord, StopAccess access, Storage storage) {
        if (coord == null || access == null || storage == null || economy == null) return false;

        if (!canPlaceStopAt(coord)) return false;

        if (economy.getCapital() < baseCost) return false;

        economy.deduct(baseCost);

        Tile tile = map.getTile(coord);
        tile.setOverlay(new StopOverlay(name, coord, access, storage));

        return true;
    }
}
