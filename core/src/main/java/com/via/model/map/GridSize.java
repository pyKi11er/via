package com.via.model.map;

import java.io.Serializable;

public class GridSize implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int width;
    public final int height;
    public GridSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
