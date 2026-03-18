package com.via.model.vehicle;

public class LargeTruck extends Vehicle {
    public LargeTruck(VehiclePosition position) {
        super(
            VehicleType.TRUCK,
            position,
            1.0f,
            80,
            35,
            24_000
        );
    }
}

