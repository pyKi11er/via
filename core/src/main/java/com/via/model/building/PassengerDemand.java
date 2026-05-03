package com.via.model.building;

import java.io.Serializable;

public class PassengerDemand implements Serializable {
    private static final long serialVersionUID = 1L;
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
