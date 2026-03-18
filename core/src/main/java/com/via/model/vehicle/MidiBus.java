package com.via.model.vehicle;

public class MidiBus extends Vehicle {
    public MidiBus(VehiclePosition position) {
        super(
            VehicleType.BUS,
            position,
            1.4f,
            30,
            18,
            14_000
        );
    }
}

