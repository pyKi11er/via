package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

/**
 * Midi bus for smaller capacity routes and narrow streets.
 * Offers higher speed and lower operating costs for less crowded routes.
 */
public class MidiBus extends Vehicle {

    /**
     * Constructs a new MidiBus at the specified initial position.
     *
     * @param position the starting position of the midi bus
     */
    public MidiBus(VehiclePosition position) {
        super(
            VehicleType.BUS,
            position,
            1.4f,    // speed
            30,      // capacity
            18,      // maintenanceCost
            14_000   // purchaseCost
        );
    }
}
