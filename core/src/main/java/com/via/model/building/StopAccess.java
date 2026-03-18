package com.via.model.building;

import com.via.model.enums.AccessType;
import com.via.model.enums.Direction;
import com.via.model.map.TileCoord;

/** Connection point from a StopOverlay to a tile on the road network. */
public class StopAccess {
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
