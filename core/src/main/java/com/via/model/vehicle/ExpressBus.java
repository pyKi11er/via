package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

/** Fast long‑distance passenger express bus. */
public class ExpressBus extends Vehicle {
    public ExpressBus(VehiclePosition position) {
        super(
            VehicleType.EXPRESS_BUS,
            position,
            1.55f,   // speed
            40,      // capacity
            32,      // maintenanceCost
            28_000   // purchaseCost
        );
    }
}
