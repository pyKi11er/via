package com.via.model;

import java.util.UUID;

//base class for map overlays

public abstract class Overlay {
    protected final UUID id = UUID.randomUUID();
    public UUID getId() { return id; }

}
