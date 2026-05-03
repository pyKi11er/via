package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

/**
 * Standard large bus for city and suburban transit routes.
 * Offers balanced capacity, speed, and operating costs.
 */
public class LargeBus extends Vehicle {

    /**
     * Constructs a new LargeBus at the specified initial position.
     *
     * @param position the starting position of the large bus
     */
    public LargeBus(VehiclePosition position) {
        super(
            VehicleType.BUS,
            position,
            1.15f,   // speed
            55,      // capacity
            28,      // maintenanceCost
            22_000   // purchaseCost
        );
    }
}
