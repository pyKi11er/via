package com.via.model.map;

public class Tile {
    public final TileCoord coord;
    private Terrain terrain;
    private Structure structure;
    private Overlay overlay;

    public Tile(TileCoord coord, Terrain terrain, Structure structure, Overlay overlay) {
        this.coord = coord;
        this.terrain = terrain;
        this.structure = structure;
        this.overlay = overlay;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    public Overlay getOverlay() {
        return overlay;
    }

    public void setOverlay(Overlay overlay) {
        this.overlay = overlay;
    }

    public boolean isEmpty() {
        return structure == null && overlay == null;
    }
}
