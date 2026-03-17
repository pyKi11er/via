package com.via.model;

import java.util.UUID;

/**
 * Represents a stop used for loading/unloading passengers or goods.
 */
public class StopOverlay extends Overlay {

    private final UUID id;
    private final String name;
    private final TileCoord location;
    private final StopAccess access;
    private final Storage storage;

    public StopOverlay(String name, TileCoord location, StopAccess access, Storage storage) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location;
        this.access = access;
        this.storage = storage;
    }

    public String getName() { return name; }

    public TileCoord getLocation() { return location; }

    public StopAccess getAccess() { return access; }

    public Storage getStorage() { return storage; }
}
