package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

/**
 * Fast long-distance passenger express bus.
 * Optimized for inter-city routes with fewer stops and higher speeds.
 */
public class ExpressBus extends Vehicle {

    /**
     * Constructs a new ExpressBus at the specified initial position.
     *
     * @param position the starting position of the express bus
     */
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
