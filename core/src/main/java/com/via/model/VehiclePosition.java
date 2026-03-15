package com.via.model;

public class VehiclePosition {
    private TileCoord from;
    private TileCoord to;
    private float progress;
    public VehiclePosition(TileCoord from, TileCoord to) {
        this.from = from;
        this.to = to;
        this.progress = 0;
    }
    public TileCoord getFrom() {
        return from;
    }
    public void setFrom(TileCoord from) {
        this.from = from;
    }
    public TileCoord getTo() {
        return to;
    }
    public void setTo(TileCoord to) {
        this.to = to;
    }
    public float getProgress() {
        return progress;
    }
    public void setProgress(float progress) {
        this.progress = progress;
    }
}
