package com.via.model.map;

import java.util.Objects;

public class TileCoord {
    public final int x;
    public final int y;
    public TileCoord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TileCoord)) return false;
        TileCoord other = (TileCoord) o;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
