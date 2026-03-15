package com.via.model;

public class Map {
    public final GridSize size;
    private final Tile[][] tiles;

    public Map(GridSize size) {
        this.size = size;
        this.tiles = new Tile[size.width][size.height];
    }

    public Tile getTiles(TileCoord coord) {
        return tiles[coord.x][coord.y];
    }

    public void setTile(Tile tile, TileCoord coord) {
        tiles[coord.x][coord.y] = tile;
    }
}
