package com.via.model.vehicle;

import com.via.model.map.TileCoord;

public class VehiclePosition {
    private TileCoord origin;
    private TileCoord destination;
    private float progress;

    public VehiclePosition(TileCoord origin, TileCoord destination) {
        this.origin = origin;
        this.destination = destination;
        this.progress = 0f;
    }

    public TileCoord getOrigin() {
        return origin;
    }

    public void setOrigin(TileCoord origin) {
        this.origin = origin;
    }

    public TileCoord getDestination() {
        return destination;
    }

    public void setDestination(TileCoord destination) {
        this.destination = destination;
    }
    public float getProgress() {
        return progress;
    }
    public void setProgress(float progress) {
        if (progress < 0f || progress > 1f) {
            throw new IllegalArgumentException("progress must be between 0.0 and 1.0");
        }
        this.progress = progress;
    }

    /** Backwards-compatible alias; prefer {@link #getOrigin()}. */
    public TileCoord getFrom() {
        return origin;
    }

    /** Backwards-compatible alias; prefer {@link #setOrigin(TileCoord)}. */
    public void setFrom(TileCoord from) {
        this.origin = from;
    }

    /** Backwards-compatible alias; prefer {@link #getDestination()}. */
    public TileCoord getTo() {
        return destination;
    }

    /** Backwards-compatible alias; prefer {@link #setDestination(TileCoord)}. */
    public void setTo(TileCoord to) {
        this.destination = to;
    }
}
