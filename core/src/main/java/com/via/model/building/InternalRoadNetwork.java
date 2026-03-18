package com.via.model.building;

import com.via.model.map.TileCoord;

import java.util.ArrayList;
import java.util.List;

public class InternalRoadNetwork {
    private final List<TileCoord> roadTiles;
    private final List<CityGate> entryPoints;

    public InternalRoadNetwork() {
        roadTiles = new ArrayList<>();
        entryPoints = new ArrayList<>();
    }
    public List<TileCoord> getRoadTiles() {
        return roadTiles;
    }
    public List<CityGate> getEntryPoints() {
        return entryPoints;
    }
    public void addRoadTile(TileCoord coord) {
        roadTiles.add(coord);
    }
    public void addEntryPoint(CityGate gate) {
        entryPoints.add(gate);
    }

    public boolean isInternalRoad(TileCoord coord) {
        return roadTiles.contains(coord);
    }
}
