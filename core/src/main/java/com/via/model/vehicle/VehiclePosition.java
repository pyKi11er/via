package com.via.model.vehicle;

import com.via.model.map.TileCoord;

/**
 * Represents the position of a vehicle during movement between two tiles.
 * Tracks the origin and destination tiles, progress along the path, and visual
 * state for overtaking maneuvers including lane offset.
 */
public class VehiclePosition {
    private TileCoord origin;
    private TileCoord destination;
    private float progress;

    // Overtaking state
    private boolean overtaking = false;
    private float laneOffset = 0f; // visual offset perpendicular to direction of travel

    /**
     * Constructs a new VehiclePosition with the specified origin and destination.
     * Progress starts at 0 (at the origin).
     *
     * @param origin      the starting tile coordinate
     * @param destination the destination tile coordinate
     */
    public VehiclePosition(TileCoord origin, TileCoord destination) {
        this.origin = origin;
        this.destination = destination;
        this.progress = 0f;
    }

    /**
     * Returns the origin (starting) tile coordinate.
     *
     * @return the origin tile
     */
    public TileCoord getOrigin() { return origin; }

    /**
     * Sets the origin (starting) tile coordinate.
     *
     * @param origin the new origin tile
     */
    public void setOrigin(TileCoord origin) { this.origin = origin; }

    /**
     * Returns the destination (target) tile coordinate.
     *
     * @return the destination tile
     */
    public TileCoord getDestination() { return destination; }

    /**
     * Sets the destination (target) tile coordinate.
     *
     * @param destination the new destination tile
     */
    public void setDestination(TileCoord destination) { this.destination = destination; }

    /**
     * Returns the progress along the path from origin to destination.
     *
     * @return progress value between 0.0 (at origin) and 1.0 (at destination)
     */
    public float getProgress() { return progress; }

    /**
     * Sets the progress along the path from origin to destination.
     *
     * @param progress the new progress value (must be between 0.0 and 1.0)
     * @throws IllegalArgumentException if progress is outside the valid range
     */
    public void setProgress(float progress) {
        if (progress < 0f || progress > 1f)
            throw new IllegalArgumentException("progress must be between 0.0 and 1.0");
        this.progress = progress;
    }

    /**
     * Returns whether the vehicle is currently overtaking another vehicle.
     *
     * @return true if overtaking, false otherwise
     */
    public boolean isOvertaking() { return overtaking; }

    /**
     * Sets whether the vehicle is currently overtaking another vehicle.
     *
     * @param overtaking the overtaking state
     */
    public void setOvertaking(boolean overtaking) { this.overtaking = overtaking; }

    /**
     * Returns the visual lane offset for overtaking maneuvers.
     *
     * @return the lane offset value
     */
    public float getLaneOffset() { return laneOffset; }

    /**
     * Sets the visual lane offset for overtaking maneuvers.
     *
     * @param laneOffset the new lane offset value
     */
    public void setLaneOffset(float laneOffset) { this.laneOffset = laneOffset; }

    /**
     * Returns the origin tile (alias for {@link #getOrigin()}).
     *
     * @return the origin tile
     */
    public TileCoord getFrom() { return origin; }

    /**
     * Sets the origin tile (alias for {@link #setOrigin(TileCoord)}).
     *
     * @param from the new origin tile
     */
    public void setFrom(TileCoord from) { this.origin = from; }

    /**
     * Returns the destination tile (alias for {@link #getDestination()}).
     *
     * @return the destination tile
     */
    public TileCoord getTo() { return destination; }

    /**
     * Sets the destination tile (alias for {@link #setDestination(TileCoord)}).
     *
     * @param to the new destination tile
     */
    public void setTo(TileCoord to) { this.destination = to; }
}
