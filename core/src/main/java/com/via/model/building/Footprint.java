package com.via.model.building;

import com.via.model.map.TileCoord;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the rectangular spatial footprint of a {@link Building} on the map.
 * <p>
 * A Footprint defines a axis-aligned rectangle of tiles that a building occupies.
 * It is used to determine tile occupancy, collision detection, and rendering.
 */
public class Footprint {

    /** The bottom-left (origin) tile coordinate of the footprint */
    private final TileCoord origin;

    /** Width of the footprint in tiles */
    private final int width;

    /** Height of the footprint in tiles */
    private final int height;

    /**
     * Constructs a new Footprint.
     *
     * @param origin the bottom-left tile coordinate of the rectangle
     * @param width  the width of the footprint in tiles (must be positive)
     * @param height the height of the footprint in tiles (must be positive)
     */
    public Footprint(TileCoord origin, int width, int height) {
        this.origin = origin;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the origin (bottom-left corner) of this footprint.
     *
     * @return the origin {@link TileCoord}
     */
    public TileCoord getOrigin() {
        return origin;
    }

    /**
     * Returns the width of this footprint in tiles.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of this footprint in tiles.
     *
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Checks whether the given coordinate is inside this footprint.
     *
     * @param coord the tile coordinate to test
     * @return {@code true} if the coordinate lies within the footprint bounds
     */
    public boolean contains(TileCoord coord) {
        return coord.x >= origin.x && coord.x < origin.x + width
            && coord.y >= origin.y && coord.y < origin.y + height;
    }

    /**
     * Returns a list of all tile coordinates covered by this footprint.
     *
     * @return a new list containing every {@link TileCoord} in this footprint
     */
    public List<TileCoord> getTiles() {
        List<TileCoord> tiles = new ArrayList<>();
        for(int x = origin.x; x < origin.x + width; x++) {
            for(int y = origin.y; y < origin.y + height; y++) {
                tiles.add(new TileCoord(x, y));
            }
        }
        return tiles;
    }
}
