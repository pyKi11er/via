package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

/**
 * Large truck for medium to heavy cargo transport.
 * Provides good balance between capacity, speed, and operating costs.
 */
public class LargeTruck extends Vehicle {

    /**
     * Constructs a new LargeTruck at the specified initial position.
     *
     * @param position the starting position of the large truck
     */
    public LargeTruck(VehiclePosition position) {
        super(
            VehicleType.TRUCK,
            position,
            1.0f,    // speed
            80,      // capacity
            35,      // maintenanceCost
            24_000   // purchaseCost
        );
    }
}
