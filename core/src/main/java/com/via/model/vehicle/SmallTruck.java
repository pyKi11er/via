package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

public class SmallTruck extends Vehicle {
    public SmallTruck(VehiclePosition position) {
        super(
            VehicleType.TRUCK,
            position,
            1.25f,
            40,
            20,
            12_000
        );
    }
}

