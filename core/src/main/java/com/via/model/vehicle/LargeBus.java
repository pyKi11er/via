package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

public class LargeBus extends Vehicle {
    public LargeBus(VehiclePosition position) {
        super(
            VehicleType.BUS,
            position,
            1.15f,
            55,
            28,
            22_000
        );
    }
}
