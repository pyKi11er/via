package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

/**
 * Light, fast transport truck for small payloads.
 * Ideal for last-mile delivery and routes with limited cargo demand.
 */
public class MiniTruck extends Vehicle {

    /**
     * Constructs a new MiniTruck at the specified initial position.
     *
     * @param position the starting position of the mini truck
     */
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
