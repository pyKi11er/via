package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

/** High‑capacity heavy truck, slower and costlier to maintain. */
public class HeavyTruck extends Vehicle {
    public HeavyTruck(VehiclePosition position) {
        super(
            VehicleType.HEAVY_TRUCK,
            position,
            0.9f,    // speed
            120,     // capacity
            55,      // maintenanceCost
            38_000   // purchaseCost
        );
    }
}
