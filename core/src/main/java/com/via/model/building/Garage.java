package com.via.model.building;

import com.via.model.map.Map;
import com.via.model.map.Structure;
import com.via.model.map.TileCoord;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a Maintenance Garage in the game.
 * <p>
 * A Garage is a building where vehicles can perform maintenance.
 * It is connected to the road network via adjacent road tiles.
 * The garage itself is a non-road structure, and vehicles access it
 * from neighboring road tiles.
 */
public class Garage extends Building {

    private Parking parking;

    /**
     * Constructs a new Garage.
     *
     * @param footprint the spatial footprint of the garage
     */
    public Garage(Footprint footprint) {
        super(footprint);
    }

    /**
     * Returns the parking area associated with this garage.
     *
     * @return the {@link Parking} instance, or null if none is set
     */
    public Parking getParking() {
        return parking;
    }

    /**
     * Sets the parking area for this garage.
     *
     * @param parking the {@link Parking} to associate with this garage
     */
    public void setParking(Parking parking) {
        this.parking = parking;
    }

    /**
     * Returns all road tiles adjacent to any tile occupied by this garage.
     * These tiles are used as access points for vehicles entering/leaving the garage.
     *
     * @param map the game map
     * @return list of adjacent road tiles
     * @throws IllegalArgumentException if map is null
     */
    public List<TileCoord> getAccessRoadTiles(Map map) {
        if (map == null) throw new IllegalArgumentException("map must not be null");
        Set<TileCoord> access = new HashSet<>();
        for (TileCoord covered : getOccupiedTiles()) {
            for (TileCoord neighbor : map.getAdjacentCoords(covered)) {
                if (!map.isWithinBounds(neighbor)) continue;
                if (map.getTile(neighbor) == null) continue;
                Structure s = map.getTile(neighbor).getStructure();
                if (s instanceof Road) {
                    access.add(neighbor);
                }
            }
        }
        return access.stream().toList();
    }

    /**
     * Checks whether this garage has at least one adjacent road tile
     * that vehicles can use for access.
     *
     * @param map the game map
     * @return {@code true} if the garage is accessible from a road
     */
    public boolean isAccessibleFromRoad(Map map) {
        return !getAccessRoadTiles(map).isEmpty();
    }

    /**
     * Returns the primary location (origin) of this garage.
     *
     * @return the origin {@link TileCoord} of the garage footprint
     */
    public TileCoord getLocation() {
        return footprint.getOrigin();
    }
}
