package com.via.model.building;

import com.via.model.enums.AccessType;
import com.via.model.enums.Direction;
import com.via.model.map.TileCoord;

import java.io.Serializable;

public class StopAccess implements Serializable {
    private static final long serialVersionUID = 1L;
    private final AccessType accessType;
    private final TileCoord anchor;
    private final Direction direction;

    public StopAccess(AccessType accessType, TileCoord anchor, Direction direction) {
        this.accessType = accessType;
        this.anchor = anchor;
        this.direction = direction;
    }

    public AccessType getAccessType() { return accessType; }

    public TileCoord getAnchor() { return anchor; }

    public Direction getDirection() { return direction; }
}
