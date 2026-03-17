package com.via.model;

import java.util.List;
import java.util.UUID;

public abstract class Building {
    public final UUID id;
    public final Footprint footprint;
    public Building(Footprint footprint) {
        this.id = UUID.randomUUID();
        this.footprint = footprint;
    }

    public boolean occupiesTile(TileCoord coord){
        return footprint.contains(coord);
    }
    public List<TileCoord> getOccupiedTiles(){
        return footprint.getTiles();
    }
}
