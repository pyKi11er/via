package com.via.model.vehicle;

import com.via.model.enums.VehicleType;

/** Large, slower city transit bus with high seat capacity. */
public class CityBus extends Vehicle {
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
