package com.via.model.building;

import com.via.model.enums.Direction;
import com.via.model.map.Map;
import com.via.model.map.Structure;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single road tile in the game world.
 * <p>
 * A Road is a traversable {@link Structure} that connects to adjacent roads,
 * enabling vehicle movement. It also stores the base construction cost
 * for building or upgrading roads.
 */
public class Road implements Structure {

    /** Base cost to build this road tile */
    private final long baseBuildCost;

    /** The tile coordinate where this road is located */
    private final TileCoord location;

    /**
     * Constructs a new Road at the specified location.
     *
     * @param location       the tile coordinate of this road
     * @param baseBuildCost  the base cost to construct this road
     */
    public Road(TileCoord location, long baseBuildCost) {
        this.location = location;
        this.baseBuildCost = baseBuildCost;
    }

    /**
     * Returns the location of this road tile.
     *
     * @return the {@link TileCoord} of this road
     */
    public TileCoord getLocation() {
        return location;
    }

    /**
     * Returns the base build cost of this road tile.
     *
     * @return the base construction cost
     */
    public long getBaseBuildCost() {
        return baseBuildCost;
    }

    /**
     * Returns all directions in which this road is directly connected
     * to another road tile.
     *
     * @param map the game map used to check adjacent tiles
     * @return list of {@link Direction}s where connected roads exist
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

    /**
     * Checks whether this road tile is a junction (connected to 3 or more other roads).
     *
     * @param map the game map
     * @return {@code true} if this is a junction, {@code false} otherwise
     */
    public boolean isJunction(Map map) {
        return getConnections(map).size() >= 3;
    }

    /**
     * Returns the adjacent tile coordinate in the given direction.
     */
    private TileCoord getAdjacent(TileCoord coord, Direction dir) {
        return switch (dir) {
            case N -> new TileCoord(coord.x, coord.y - 1);
            case S -> new TileCoord(coord.x, coord.y + 1);
            case E -> new TileCoord(coord.x + 1, coord.y);
            case W -> new TileCoord(coord.x - 1, coord.y);
        };
    }
}
