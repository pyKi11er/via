package com.via.model.vehicle;

import com.via.model.GameState;
import com.via.model.building.Garage;
import com.via.model.building.Road;
import com.via.model.enums.VehicleType;
import com.via.model.map.Map;
import com.via.model.map.Structure;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;

/**
 * Vehicle creation / purchase logic.
 *
 * Vehicles can only be purchased in a {@link Garage}.
 */
public class VehicleFactory {

    public static Vehicle buy(GameState state, Garage garage, VehicleType type) {
        if (state == null) throw new IllegalArgumentException("state must not be null");
        if (garage == null) throw new IllegalArgumentException("garage must not be null");
        if (type == null) throw new IllegalArgumentException("type must not be null");

        Map map = state.getMap();
        if (map == null) throw new IllegalStateException("state.map must not be null");

        TileCoord origin = garage.getLocation();
        if (origin == null) throw new IllegalArgumentException("garage location must not be null");
        TileCoord destination = chooseAdjacentRoadNeighbor(map, origin);

        VehiclePosition pos = new VehiclePosition(origin, destination);
        Vehicle v = switch (type) {
            case TRUCK        -> new SmallTruck(pos);
            case BUS          -> new MidiBus(pos);
            case MINI_TRUCK   -> new MiniTruck(pos);
            case HEAVY_TRUCK  -> new HeavyTruck(pos);
            case EXPRESS_BUS  -> new ExpressBus(pos);
            case CITY_BUS     -> new CityBus(pos);
        };


        // Charge purchase cost.
        state.getEconomy().deduct(v.getPurchaseCost());

        // Initialize age/maintenance schedule.
        long purchaseTick = state.getTimeState() != null ? state.getTimeState().getTick() : 0;
        v.onPurchased(purchaseTick);

        state.addVehicle(v);
        return v;
    }

    private static TileCoord chooseAdjacentRoadNeighbor(Map map, TileCoord origin) {
        for (TileCoord neighbor : map.getAdjacentCoords(origin)) {
            Tile tile = map.getTile(neighbor);
            if (tile == null) continue;
            Structure s = tile.getStructure();
            if (s instanceof Road) {
                return neighbor;
            }
        }
        // Isolated road tile (rare on properly connected networks) -> stand still.
        return origin;
    }
}

