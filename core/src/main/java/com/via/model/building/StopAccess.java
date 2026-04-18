package com.via.model.building;

import com.via.model.enums.AccessType;
import com.via.model.enums.Direction;
import com.via.model.map.TileCoord;

/**
 * Represents a connection point between a {@link StopOverlay} and the road network.
 * <p>
 * Each StopAccess defines how vehicles can enter or exit a stop from a specific
 * road tile, including the type of access and the direction of movement.
 */
public class StopAccess {

    /** The type of access (e.g., entry, exit, bidirectional) */
    private final AccessType accessType;

    /** The road tile coordinate where this access connects */
    private final TileCoord anchor;

    /** The direction vehicles face when using this access point */
    private final Direction direction;

    /**
     * Constructs a new StopAccess.
     *
     * @param accessType the type of access this point provides
     * @param anchor     the road tile coordinate acting as the connection point
     * @param direction  the direction vehicles use when entering/exiting
     */
    public StopAccess(AccessType accessType, TileCoord anchor, Direction direction) {
        this.accessType = accessType;
        this.anchor = anchor;
        this.direction = direction;
    }

    /**
     * Returns the type of access.
     *
     * @return the {@link AccessType} of this connection
     */
    public AccessType getAccessType() {
        return accessType;
    }

    /**
     * Returns the anchor tile on the road network.
     *
     * @return the {@link TileCoord} where this access connects
     */
    public TileCoord getAnchor() {
        return anchor;
    }

    /**
     * Returns the direction of vehicle movement for this access point.
     *
     * @return the {@link Direction} of this access
     */
    public Direction getDirection() {
        return direction;
    }
}
