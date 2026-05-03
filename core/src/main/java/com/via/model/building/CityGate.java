package com.via.model.building;

import com.via.model.enums.Direction;
import com.via.model.map.TileCoord;

import java.io.Serializable;

public class CityGate implements Serializable {
    private static final long serialVersionUID = 1L;
    private final TileCoord location;
    private final Direction direction;

    public CityGate(TileCoord location, Direction direction) {
        this.location = location;
        this.direction = direction;
    }
    public TileCoord getLocation() {
        return location;
    }
    public Direction getDirection() {
        return direction;
    }
}
