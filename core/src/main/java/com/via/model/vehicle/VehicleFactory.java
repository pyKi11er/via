package com.via.model.vehicle;

import com.via.model.GameState;
import com.via.model.building.Garage;
import com.via.model.enums.VehicleType;
import com.via.model.map.Map;
import com.via.model.map.TileCoord;

import java.util.List;

/**
 * Vehicle creation and purchase logic factory.
 * Provides functionality to purchase new vehicles, which can only be done at a {@link Garage}.
 * Handles vehicle instantiation, positioning, parking slot assignment, and economic transactions.
 */
public class VehicleFactory {

    /**
     * Purchases a new vehicle of the specified type from a garage.
     * The vehicle spawns on an adjacent road tile, deducts the purchase cost from the economy,
     * and assigns it a parking slot if available.
     *
     * @param state   the current game state (must not be null)
     * @param garage  the garage from which to purchase the vehicle (must not be null)
     * @param type    the type of vehicle to purchase (must not be null)
     * @return the newly created and initialized vehicle
     * @throws IllegalArgumentException if any parameter is null
     * @throws IllegalStateException    if the map is null or the garage has no adjacent road tile
     */
    public static Vehicle buy(GameState state, Garage garage, VehicleType type) {
        if (state == null) throw new IllegalArgumentException("state must not be null");
        if (garage == null) throw new IllegalArgumentException("garage must not be null");
        if (type == null) throw new IllegalArgumentException("type must not be null");

        Map map = state.getMap();
        if (map == null) throw new IllegalStateException("state.map must not be null");

        com.via.model.building.Parking parking = garage.getParking();
        int slot = parking != null ? parking.findFreeSlot() : -1;

        // Always spawn on a road tile so the movement system can navigate
        List<TileCoord> accessRoads = garage.getAccessRoadTiles(map);
        if (accessRoads.isEmpty())
            throw new IllegalStateException("Garage has no adjacent road to spawn vehicle on.");
        TileCoord spawnTile = accessRoads.get(0);

        VehiclePosition pos = new VehiclePosition(spawnTile, spawnTile);
        Vehicle v = switch (type) {
            case TRUCK        -> new SmallTruck(pos);
            case BUS          -> new MidiBus(pos);
            case MINI_TRUCK   -> new MiniTruck(pos);
            case HEAVY_TRUCK  -> new HeavyTruck(pos);
            case EXPRESS_BUS  -> new ExpressBus(pos);
            case CITY_BUS     -> new CityBus(pos);
        };

        // Charge purchase cost
        state.getEconomy().deduct(v.getPurchaseCost());

        // Initialize age/maintenance schedule
        long purchaseTick = state.getTimeState() != null ? state.getTimeState().getTick() : 0;
        v.onPurchased(purchaseTick);

        v.setHomeGarageLocation(garage.getLocation());

        if (parking != null && slot >= 0) {
            parking.occupySlot(slot, v.getId());
            v.setParkingSlot(parking, slot);
        }

        state.addVehicle(v);
        return v;
    }
}
