package com.via.controller;

import com.via.model.GameState;
import com.via.model.building.Building;
import com.via.model.building.Garage;
import com.via.model.enums.VehicleType;
import com.via.model.vehicle.Route;
import com.via.model.vehicle.Vehicle;
import com.via.model.vehicle.VehicleFactory;

import java.util.List;

public class GameController {

    public record VehicleCatalogEntry(
            VehicleType type,
            String displayName,
            int capacity,
            float speed,
            long maintenanceCost,
            long purchaseCost) {}

    public static final List<VehicleCatalogEntry> VEHICLE_CATALOG = List.of(
            new VehicleCatalogEntry(VehicleType.MINI_TRUCK,  "Mini Truck",   25, 1.45f, 15,  10_000),
            new VehicleCatalogEntry(VehicleType.TRUCK,       "Small Truck",  40, 1.25f, 20,  12_000),
            new VehicleCatalogEntry(VehicleType.HEAVY_TRUCK, "Heavy Truck", 120, 0.90f, 55,  38_000),
            new VehicleCatalogEntry(VehicleType.BUS,         "Midi Bus",     30, 1.40f, 18,  14_000),
            new VehicleCatalogEntry(VehicleType.CITY_BUS,    "City Bus",     70, 1.00f, 25,  18_000),
            new VehicleCatalogEntry(VehicleType.EXPRESS_BUS, "Express Bus",  40, 1.55f, 32,  28_000)
    );

    /**
     * Attempts to purchase a vehicle of the given type, spawning it at the first available garage.
     *
     * @return null on success, or a human-readable error message on failure.
     */
    public static String buyVehicle(GameState state, VehicleType type) {
        Garage garage = findGarage(state);
        if (garage == null)
            return "No garage available — build one first.";

        long cost = VEHICLE_CATALOG.stream()
                .filter(e -> e.type() == type)
                .mapToLong(VehicleCatalogEntry::purchaseCost)
                .findFirst()
                .orElse(0L);

        if (state.getEconomy().getCapital() < cost)
            return String.format("Insufficient funds — need $%,d.", cost);

        VehicleFactory.buy(state, garage, type);
        return null;
    }

    /** Assign a vehicle to a route immediately. */
    public static void assignVehicleToRoute(Vehicle vehicle, Route route) {
        vehicle.assignRoute(route);
    }

    private static Garage findGarage(GameState state) {
        for (Building b : state.getBuildings()) {
            if (b instanceof Garage g) return g;
        }
        return null;
    }
}
