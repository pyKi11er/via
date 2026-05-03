package com.via.model.vehicle;

import com.via.model.map.TileCoord;

import java.io.Serializable;

public class VehiclePosition implements Serializable {
    private static final long serialVersionUID = 1L;
    private TileCoord origin;
    private TileCoord destination;
    private float progress;

    // Overtaking state
    private boolean overtaking = false;
    private float laneOffset = 0f; // visual offset perpendicular to direction of travel

    public VehiclePosition(TileCoord origin, TileCoord destination) {
        this.origin = origin;
        this.destination = destination;
        this.progress = 0f;
    }

    public TileCoord getOrigin()      { return origin; }
    public void setOrigin(TileCoord origin) { this.origin = origin; }

    public TileCoord getDestination() { return destination; }
    public void setDestination(TileCoord destination) { this.destination = destination; }

    public float getProgress() { return progress; }
    public void setProgress(float progress) {
        if (progress < 0f || progress > 1f)
            throw new IllegalArgumentException("progress must be between 0.0 and 1.0");
        this.progress = progress;
    }

    public boolean isOvertaking()             { return overtaking; }
    public void setOvertaking(boolean overtaking) { this.overtaking = overtaking; }

    public float getLaneOffset()              { return laneOffset; }
    public void setLaneOffset(float laneOffset) { this.laneOffset = laneOffset; }

    public TileCoord getFrom()              { return origin; }
    public void setFrom(TileCoord from)     { this.origin = from; }
    public TileCoord getTo()                { return destination; }
    public void setTo(TileCoord to)         { this.destination = to; }
}
