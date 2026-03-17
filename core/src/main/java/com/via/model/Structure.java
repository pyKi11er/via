package com.via.model;

import java.util.UUID;

//base class for map structures

public abstract class Structure {
    protected final UUID id = UUID.randomUUID();
    public UUID getId() { return id; }
}
