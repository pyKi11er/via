package com.via.model.map;

/**
 * Represents the dimensions of a grid-based map.
 * Encapsulates the width and height as immutable values.
 */
public class GridSize {
    /** The width of the grid (number of columns). */
    public final int width;

    /** The height of the grid (number of rows). */
    public final int height;

    /**
     * Constructs a new GridSize with the specified dimensions.
     *
     * @param width  the width of the grid (must be non-negative)
     * @param height the height of the grid (must be non-negative)
     */
    public GridSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
