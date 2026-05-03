package com.via.model.building;

import com.via.model.map.TileCoord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Footprint implements Serializable {
    private static final long serialVersionUID = 1L;
    private final TileCoord origin;
    private final int width;
    private final int height;

    public Footprint(TileCoord origin, int width, int height) {
        this.origin = origin;
        this.width = width;
        this.height = height;
    }
    public TileCoord getOrigin() {
        return origin;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public boolean contains(TileCoord coord) {
        return coord.x >= origin.x && coord.x < origin.x + width
            && coord.y >= origin.y && coord.y < origin.y + height;
    }

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
