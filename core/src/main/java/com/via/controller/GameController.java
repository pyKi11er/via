package com.via.controller;

import com.via.model.Constants;
import com.via.model.GameState;
import com.via.model.building.Building;
import com.via.model.building.Garage;
import com.via.model.enums.VehicleType;
import com.via.model.vehicle.Route;
import com.via.model.vehicle.Vehicle;
import com.via.model.vehicle.VehicleFactory;

import java.util.List;

/**
 * Provides high-level game actions related to vehicles and player interaction.
 *
 * <p>This controller acts as a façade for gameplay operations such as purchasing
 * and assigning vehicles. It also exposes a catalog of available vehicle types
 * with their gameplay properties.</p>
 */
public class GameController {

    /**
     * Immutable data record describing a vehicle type available for purchase.
     *
     * @param type the internal {@link VehicleType}
     * @param displayName user-facing name of the vehicle
     * @param capacity maximum cargo or passenger capacity
     * @param speed movement speed multiplier
     * @param maintenanceCost recurring cost per time unit
     * @param purchaseCost upfront purchase price
     */
    public record VehicleCatalogEntry(
        VehicleType type,
        String displayName,
        int capacity,
        float speed,
        long maintenanceCost,
        long purchaseCost) {}

    /**
     * Static catalog of all purchasable vehicle types.
     *
     * <p>This list is used for UI display and purchase logic.</p>
     */
    public static final List<VehicleCatalogEntry> VEHICLE_CATALOG = List.of(
        new VehicleCatalogEntry(VehicleType.MINI_TRUCK,  "Mini Truck",   25, 1.45f, 15,  10_000),
        new VehicleCatalogEntry(VehicleType.TRUCK,       "Small Truck",  40, 1.25f, 20,  12_000),
        new VehicleCatalogEntry(VehicleType.HEAVY_TRUCK, "Heavy Truck", 120, 0.90f, 55,  38_000),
        new VehicleCatalogEntry(VehicleType.BUS,         "Midi Bus",     30, 1.40f, 18,  14_000),
        new VehicleCatalogEntry(VehicleType.CITY_BUS,    "City Bus",     70, 1.00f, 25,  18_000),
        new VehicleCatalogEntry(VehicleType.EXPRESS_BUS, "Express Bus",  40, 1.55f, 32,  28_000)
    );

    /**
     * Attempts to purchase a vehicle of the specified type.
     *
     * <p>The method performs the following steps:</p>
     * <ol>
     *     <li>Finds a {@link Garage} with available capacity</li>
     *     <li>Checks whether the player has sufficient funds</li>
     *     <li>Creates the vehicle and assigns it to the garage</li>
     * </ol>
     *
     * @param state the current game state
     * @param type the type of vehicle to purchase
     * @return {@code null} if the purchase succeeds; otherwise a human-readable
     *         error message describing why the purchase failed
     */
    public static String buyVehicle(GameState state, VehicleType type) {
        // 1. Find a garage that has available capacity
        Garage availableGarage = null;
        for (Building b : state.getBuildings()) {
            if (b instanceof Garage g) {
                long parkedCount = state.getVehicles().stream()
                    .filter(v -> v.getHomeGarageLocation() != null &&
                        v.getHomeGarageLocation().equals(g.getLocation()))
                    .count();

                if (parkedCount < Constants.GARAGE_CAPACITY) {
                    availableGarage = g;
                    break;
                }
            }
        }

        if (availableGarage == null) {
            return "No available garage space! Build another garage.";
        }

        // 2. Check purchase cost
        long cost = VEHICLE_CATALOG.stream()
            .filter(e -> e.type() == type)
            .mapToLong(VehicleCatalogEntry::purchaseCost)
            .findFirst()
            .orElse(0L);

        if (state.getEconomy().getCapital() < cost) {
            return String.format("Insufficient funds — need $%,d.", cost);
        }

        // 3. Create vehicle
        VehicleFactory.buy(state, availableGarage, type);
        return null;
    }

    /**
     * Assigns a vehicle to a route.
     *
     * <p>The assignment takes effect immediately and overrides any existing route.</p>
     *
     * @param vehicle the vehicle to assign
     * @param route the route to assign the vehicle to
     */
    public static void assignVehicleToRoute(Vehicle vehicle, Route route) {
        vehicle.assignRoute(route);
    }

    /**
     * Finds the first available garage in the game state.
     *
     * <p>This method returns the first {@link Garage} encountered in the building list,
     * regardless of capacity.</p>
     *
     * @param state the current game state
     * @return a garage if one exists, otherwise {@code null}
     */
    private static Garage findGarage(GameState state) {
        for (Building b : state.getBuildings()) {
            if (b instanceof Garage g) return g;
        }
        return null;
    }
}
