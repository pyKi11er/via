package com.via.model.map;

import java.util.Objects;

/**
 * Represents the coordinates of a tile on the game map grid.
 * This immutable class provides x and y coordinates with proper equality and hash code support.
 */
public class TileCoord {
    /** The x-coordinate (column index) of the tile. */
    public final int x;

    /** The y-coordinate (row index) of the tile. */
    public final int y;

    /**
     * Constructs a new TileCoord with the specified coordinates.
     *
     * @param x the x-coordinate (column index)
     * @param y the y-coordinate (row index)
     */
    public TileCoord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Compares this TileCoord to another object for equality.
     * Two TileCoord objects are equal if they have the same x and y coordinates.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TileCoord)) return false;
        TileCoord other = (TileCoord) o;
        return x == other.x && y == other.y;
    }

    /**
     * Returns a hash code value for this TileCoord.
     * The hash code is computed from the x and y coordinates.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
