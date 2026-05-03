package com.via.model.building;

import java.io.Serializable;
import com.via.model.enums.Direction;
import com.via.model.map.TileCoord;

/**
 * Represents an entry or exit point (gate) of a {@link City}.
 * <p>
 * Each gate defines a specific location and direction, which is used for
 * connecting the city's internal road network to the external road system.
 */
public class CityGate implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The tile coordinate where this gate is located */
    private final TileCoord location;

    /** The direction this gate faces (used for road connections and vehicle routing) */
    private final Direction direction;

    /**
     * Constructs a new CityGate.
     *
     * @param location   the tile position of the gate
     * @param direction  the direction the gate faces
     */
    public CityGate(TileCoord location, Direction direction) {
        this.location = location;
        this.direction = direction;
    }

    /**
     * Returns the location of this gate.
     *
     * @return the {@link TileCoord} where the gate is positioned
     */
    public TileCoord getLocation() {
        return location;
    }

    /**
     * Returns the direction this gate is facing.
     *
     * @return the {@link Direction} of the gate
     */
    public Direction getDirection() {
        return direction;
    }
}
