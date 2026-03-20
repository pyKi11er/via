package com.via.model.building;

import com.via.model.map.Map;
import com.via.model.map.Structure;
import com.via.model.map.TileCoord;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A maintenance garage connected to the road network.
 *
 * The garage itself is a building tile (non-road structure). Vehicles access it from adjacent road tiles.
 */
public class Garage extends Building {

    public Garage(Footprint footprint) {
        super(footprint);
    }

    /**
     * Returns road tiles adjacent to any tile covered by the garage footprint.
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

    public boolean isAccessibleFromRoad(Map map) {
        return !getAccessRoadTiles(map).isEmpty();
    }

    public TileCoord getLocation() {
        return footprint.getOrigin();
    }
}

