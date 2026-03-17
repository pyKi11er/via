package com.via.model;

public class CityGate {
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
