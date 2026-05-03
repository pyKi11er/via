package com.via.model.building;

import java.io.Serializable;
import com.via.model.map.TileCoord;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the internal road network within a {@link City}.
 * <p>
 * This class tracks which tiles inside a city are considered drivable roads
 * and maintains the list of entry/exit points (gates) that connect the city
 * to the external road network.
 */
public class InternalRoadNetwork implements Serializable {
    private static final long serialVersionUID = 1L;

    /** All tiles that are part of the city's internal road network */
    private final List<TileCoord> roadTiles;

    /** Entry and exit points connecting the internal network to external roads */
    private final List<CityGate> entryPoints;

    /**
     * Constructs a new empty InternalRoadNetwork.
     */
    public InternalRoadNetwork() {
        roadTiles = new ArrayList<>();
        entryPoints = new ArrayList<>();
    }

    /**
     * Returns all road tiles in this internal network.
     *
     * @return list of {@link TileCoord} that are internal roads
     */
    public List<TileCoord> getRoadTiles() {
        return roadTiles;
    }

    /**
     * Returns all city gates (entry/exit points) for this network.
     *
     * @return list of {@link CityGate} objects
     */
    public List<CityGate> getEntryPoints() {
        return entryPoints;
    }

    /**
     * Adds a tile to the internal road network.
     *
     * @param coord the tile coordinate to mark as an internal road
     */
    public void addRoadTile(TileCoord coord) {
        roadTiles.add(coord);
    }

    /**
     * Adds a city gate as an entry/exit point to the internal road network.
     *
     * @param gate the {@link CityGate} to add
     */
    public void addEntryPoint(CityGate gate) {
        entryPoints.add(gate);
    }

    /**
     * Checks whether the given coordinate is part of the internal road network.
     *
     * @param coord the tile coordinate to check
     * @return {@code true} if the tile is an internal road, {@code false} otherwise
     */
    public boolean isInternalRoad(TileCoord coord) {
        return roadTiles.contains(coord);
    }
}
