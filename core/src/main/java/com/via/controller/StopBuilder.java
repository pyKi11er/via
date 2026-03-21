package com.via.controller;

import com.via.model.building.Road;
import com.via.model.building.StopAccess;
import com.via.model.building.StopOverlay;
import com.via.model.economy.PlayerEconomy;
import com.via.model.economy.Storage;
import com.via.model.enums.AccessType;
import com.via.model.enums.Direction;
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

    public int getBaseCost() {
        return baseCost;
    }

    /**
     * Whether a bus/truck stop can be placed on this tile (road, free overlay, connected network, funds).
     */
    public boolean canPlaceStopAt(TileCoord coord) {
        if (map == null || coord == null || !map.isWithinBounds(coord)) return false;
        if (economy != null && economy.getCapital() < baseCost) return false;

        Tile tile = map.getTile(coord);
        if (tile == null) return false;
        if (!(tile.getStructure() instanceof Road)) return false;
        if (tile.getOverlay() != null) return false;
        return hasAdjacentRoadNeighbor(coord);
    }

    /**
     * Places a stop on a road tile. Charges {@link #baseCost} on success.
     *
     * @return false if invalid location, insufficient funds, or tile blocked (no throw for normal play)
     */
    public boolean placeStop(String name, TileCoord coord) {
        if (!canPlaceStopAt(coord)) return false;

        Tile tile = map.getTile(coord);
        if (tile == null || !(tile.getStructure() instanceof Road) || tile.getOverlay() != null) {
            return false;
        }

        Direction accessDir = inferAccessDirection(coord);
        StopAccess access = new StopAccess(AccessType.ROAD_TILE, coord, accessDir);
        Storage storage = new Storage();

        economy.deduct(baseCost);
        tile.setOverlay(new StopOverlay(name, coord, access, storage));
        return true;
    }

    private boolean hasAdjacentRoadNeighbor(TileCoord coord) {
        return map.getAdjacentCoords(coord).stream()
            .anyMatch(n -> {
                Tile t = map.getTile(n);
                return t != null && t.getStructure() instanceof Road && !n.equals(coord);
            });
    }

    /** Prefer the first adjacent road tile; direction faces along the link toward that neighbor. */
    private Direction inferAccessDirection(TileCoord coord) {
        for (TileCoord n : map.getAdjacentCoords(coord)) {
            Tile t = map.getTile(n);
            if (t != null && t.getStructure() instanceof Road) {
                return directionFromTo(coord, n);
            }
        }
        return Direction.N;
    }

    private static Direction directionFromTo(TileCoord from, TileCoord to) {
        int dx = Integer.compare(to.x, from.x);
        int dy = Integer.compare(to.y, from.y);
        if (dx > 0) return Direction.E;
        if (dx < 0) return Direction.W;
        if (dy > 0) return Direction.S;
        if (dy < 0) return Direction.N;
        return Direction.N;
    }
}
