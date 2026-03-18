package com.via.model.building;

public class PassengerDemand {
    private  float ratePerTick;

    public PassengerDemand(float ratePerTick) {
        this.ratePerTick = ratePerTick;
    }
    public float getRatePerTick() {
        return ratePerTick;
    }
    public void setRatePerTick(float ratePerTick) {
        this.ratePerTick = ratePerTick;
    }
}
