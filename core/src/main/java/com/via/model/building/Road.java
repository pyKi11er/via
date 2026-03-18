package com.via.model.building;

import com.via.model.enums.Direction;
import com.via.model.map.Map;
import com.via.model.map.Structure;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a road tile connecting to adjacent road tiles.
 */
public class Road implements Structure {

    private final long baseBuildCost;
    private final TileCoord location;

    public Road(TileCoord location, long baseBuildCost) {
        this.location = location;
        this.baseBuildCost = baseBuildCost;
    }

    public TileCoord getLocation() {
        return location;
    }

    public long getBaseBuildCost() {
        return baseBuildCost;
    }

    /**
     * Returns the directions where other road tiles are directly connected.
     */
    public List<Direction> getConnections(Map map) {
        List<Direction> connections = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            TileCoord adj = getAdjacent(location, dir);
            if (map.isWithinBounds(adj)) {
                Tile adjacentTile = map.getTile(adj);
                if (adjacentTile != null && adjacentTile.getStructure() instanceof Road) {
                    connections.add(dir);
                }
            }
        }

        return connections;
    }

    public boolean isJunction(Map map) {
        return getConnections(map).size() >= 3;
    }

    private TileCoord getAdjacent(TileCoord coord, Direction dir) {
        return switch (dir) {
            case N -> new TileCoord(coord.x, coord.y - 1);
            case S -> new TileCoord(coord.x, coord.y + 1);
            case E -> new TileCoord(coord.x + 1, coord.y);
            case W -> new TileCoord(coord.x - 1, coord.y);
        };
    }
}
