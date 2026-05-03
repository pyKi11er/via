package com.via.model.map;

import java.io.Serializable;

/**
 * Represents a single tile on the game map.
 * A tile contains terrain properties and may optionally have a structure and/or overlay.
 */
public class Tile implements Serializable {
    private static final long serialVersionUID = 1L;
    /** The coordinates of this tile on the map grid. */
    public final TileCoord coord;

    private Terrain terrain;
    private Structure structure;
    private Overlay overlay;

    /**
     * Constructs a new Tile with the specified properties.
     *
     * @param coord     the coordinates of the tile
     * @param terrain   the terrain properties of the tile
     * @param structure the structure built on the tile (may be null)
     * @param overlay   the overlay on the tile (may be null)
     */
    public Tile(TileCoord coord, Terrain terrain, Structure structure, Overlay overlay) {
        this.coord = coord;
        this.terrain = terrain;
        this.structure = structure;
        this.overlay = overlay;
    }

    /**
     * Returns the terrain properties of this tile.
     *
     * @return the terrain object
     */
    public Terrain getTerrain() {
        return terrain;
    }

    /**
     * Sets the terrain properties of this tile.
     *
     * @param terrain the new terrain object
     */
    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    /**
     * Returns the structure built on this tile.
     *
     * @return the structure, or null if no structure is present
     */
    public Structure getStructure() {
        return structure;
    }

    /**
     * Sets the structure on this tile.
     *
     * @param structure the new structure (may be null to remove)
     */
    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    /**
     * Returns the overlay on this tile.
     *
     * @return the overlay, or null if no overlay is present
     */
    public Overlay getOverlay() {
        return overlay;
    }

    /**
     * Sets the overlay on this tile.
     *
     * @param overlay the new overlay (may be null to remove)
     */
    public void setOverlay(Overlay overlay) {
        this.overlay = overlay;
    }

    /**
     * Checks whether this tile is empty (no structure and no overlay).
     *
     * @return true if both structure and overlay are null, false otherwise
     */
    public boolean isEmpty() {
        return structure == null && overlay == null;
    }
}
