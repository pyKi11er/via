package com.via.model.building;

import java.util.List;
import java.util.UUID;

import com.via.model.map.Structure;
import com.via.model.map.TileCoord;

/**
 * Abstract base class for all buildings in the game.
 * <p>
 * Buildings are structures that occupy one or more tiles on the map
 * and serve as the foundation for specialized buildings such as stops,
 * industries, cities, etc.
 */
public abstract class Building implements Structure {

    /** Unique identifier for this building */
    public final UUID id;

    /** The spatial footprint of this building on the map */
    public final Footprint footprint;

    /**
     * Constructs a new Building with the specified footprint.
     *
     * @param footprint the spatial footprint defining which tiles this building occupies
     */
    public Building(Footprint footprint) {
        this.id = UUID.randomUUID();
        this.footprint = footprint;
    }

    /**
     * Checks whether this building occupies the given tile coordinate.
     *
     * @param coord the tile coordinate to check
     * @return {@code true} if this building occupies the tile, {@code false} otherwise
     */
    public boolean occupiesTile(TileCoord coord){
        return footprint.contains(coord);
    }

    /**
     * Returns an unmodifiable list of all tile coordinates occupied by this building.
     *
     * @return list of occupied tile coordinates
     */
    public List<TileCoord> getOccupiedTiles(){
        return footprint.getTiles();
    }

    /**
     * Returns the footprint of this building.
     *
     * @return the {@link Footprint} associated with this building
     */
    public Footprint getFootprint() {
        return footprint;
    }
}
