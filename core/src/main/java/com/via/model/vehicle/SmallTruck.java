package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

/**
 * Small truck for light cargo transport and urban deliveries.
 * Offers good maneuverability with moderate capacity and operating costs.
 */
public class SmallTruck extends Vehicle {

    /**
     * Constructs a new SmallTruck at the specified initial position.
     *
     * @param position the starting position of the small truck
     */
    public SmallTruck(VehiclePosition position) {
        super(
            VehicleType.TRUCK,
            position,
            1.25f,   // speed
            40,      // capacity
            20,      // maintenanceCost
            12_000   // purchaseCost
        );
    }
}
