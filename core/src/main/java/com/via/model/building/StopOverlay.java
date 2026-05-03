package com.via.model.building;

import com.via.model.economy.Storage;
import com.via.model.map.Overlay;
import com.via.model.map.TileCoord;

import java.util.UUID;

/**
 * Represents a stop used for loading and unloading passengers or goods.
 * <p>
 * A StopOverlay is placed on top of existing structures (typically roads or cities)
 * and serves as a point where vehicles can stop to transfer cargo or passengers.
 * It includes access logic to the road network and its own storage.
 */
public class StopOverlay implements Overlay {

    /** Unique identifier for this stop */
    private final UUID id;

    /** Display name of the stop */
    private final String name;

    /** The tile coordinate where this stop is located */
    private final TileCoord location;

    /** Defines how vehicles can access this stop from the road network */
    private final StopAccess access;

    /** Storage for goods or passengers at this stop */
    private final Storage storage;

    /**
     * Constructs a new StopOverlay.
     *
     * @param name     the display name of the stop
     * @param location the tile coordinate of the stop
     * @param access   the road access configuration for this stop
     * @param storage  the storage used for loading/unloading cargo or passengers
     */
    public StopOverlay(String name, TileCoord location, StopAccess access, Storage storage) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location;
        this.access = access;
        this.storage = storage;
    }

    /**
     * Returns the display name of this stop.
     *
     * @return the name of the stop
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the location of this stop on the map.
     *
     * @return the {@link TileCoord} where the stop is placed
     */
    public TileCoord getLocation() {
        return location;
    }

    /**
     * Returns the road access configuration for this stop.
     *
     * @return the {@link StopAccess} defining vehicle entry/exit
     */
    public StopAccess getAccess() {
        return access;
    }

    /**
     * Returns the storage associated with this stop.
     *
     * @return the {@link Storage} for cargo/passengers
     */
    public Storage getStorage() {
        return storage;
    }
}
