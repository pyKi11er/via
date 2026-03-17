package com.via.controller;

import com.via.model.SpeedSetting;

public class TimeController {

    private SpeedSetting speedSetting;
    private boolean paused;

    public TimeController() {
        this.speedSetting = SpeedSetting.NORMAL;
        this.paused = false;
    }

    public void setSpeed(SpeedSetting newSpeed) {
        this.speedSetting = newSpeed;
        this.paused = (newSpeed == SpeedSetting.PAUSED);
    }

    public SpeedSetting getSpeedSetting() {
        return speedSetting;
    }

    public boolean isPaused() {
        return paused;
    }

    public float getTimeMultiplier() {
        switch (speedSetting) {
            case VERY_FAST: return 4.0f;
            case FAST: return 2.0f;
            case NORMAL: return 1.0f;
            case PAUSED:
            default: return 0.0f;
        }
    }

    public void togglePause() {
        this.paused = !paused;
        if (paused) this.speedSetting = SpeedSetting.PAUSED;
        else if (this.speedSetting == SpeedSetting.PAUSED) this.speedSetting = SpeedSetting.NORMAL;
    }
}
