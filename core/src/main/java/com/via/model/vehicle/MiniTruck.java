package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

/** Light, fast transport truck for small payloads. */
public class MiniTruck extends Vehicle {
    public MiniTruck(VehiclePosition position) {
        super(
            VehicleType.MINI_TRUCK,
            position,
            1.45f,   // speed
            25,      // capacity
            15,      // maintenanceCost
            10_000   // purchaseCost
        );
    }
}
