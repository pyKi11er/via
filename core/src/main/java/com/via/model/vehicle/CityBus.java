package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

/**
 * Large, slower city transit bus with high seat capacity.
 * Suitable for urban routes with frequent stops and high passenger demand.
 */
public class CityBus extends Vehicle {

    /**
     * Constructs a new CityBus at the specified initial position.
     *
     * @param position the starting position of the city bus
     */
    public CityBus(VehiclePosition position) {
        super(
            VehicleType.CITY_BUS,
            position,
            1.0f,    // speed
            70,      // capacity
            25,      // maintenanceCost
            18_000   // purchaseCost
        );
    }
}
