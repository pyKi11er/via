package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

/**
 * High-capacity heavy truck, slower and costlier to maintain.
 * Designed for transporting large volumes of cargo over long distances.
 */
public class HeavyTruck extends Vehicle {

    /**
     * Constructs a new HeavyTruck at the specified initial position.
     *
     * @param position the starting position of the heavy truck
     */
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
