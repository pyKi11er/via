package com.via.controller;

import com.via.model.Constants;
import com.via.model.GameState;
import com.via.model.building.*;
import com.via.model.economy.ConsumptionItem;
import com.via.model.economy.ConsumptionRule;
import com.via.model.economy.ProductionItem;
import com.via.model.economy.ProductionRule;
import com.via.model.enums.Direction;
import com.via.model.enums.GoodType;
import com.via.model.map.*;
import com.via.model.map.Map;
import com.via.model.map.TileCoord;

import java.util.List;

public class MapInitializer {
    public static GameState createNewGame() {
        Map map = new Map(new GridSize(40, 30));
        for(int x = 0; x < 40; x++) {
            for(int y = 0; y < 30; y++) {
                Terrain terrain = new Terrain(TerrainType.FLAT, 0, 0);
                Tile tile = new Tile(new TileCoord(x, y), terrain, null, null);
                map.setTile(tile, new TileCoord(x, y));
            }
        }
        Tile roadTile = new Tile(new TileCoord(5, 5),
            new Terrain(TerrainType.FLAT, 0, 0),
            new Road(new TileCoord(5, 5), 100),
            null);
        map.setTile(roadTile, new TileCoord(5, 5));
        // Add a few road tiles in a row to test bitmask
        for (int i = 3; i < 8; i++) {
            Tile road = new Tile(new TileCoord(i, 5),
                new Terrain(TerrainType.FLAT, 0, 0),
                new Road(new TileCoord(i, 5), 100), null);
            map.setTile(road, new TileCoord(i, 5));
        }

        // Add a test garage connected to the road network.
        // Vehicle access should be via the adjacent road tile (6,5) for a garage at (6,6).
        Tile garageTile = new Tile(new TileCoord(6, 6),
            new Terrain(TerrainType.FLAT, 0, 0),
            new com.via.model.building.Garage(new Footprint(new TileCoord(6, 6), 1, 1)),
            null);
        map.setTile(garageTile, new TileCoord(6, 6));

        // Add a test city block
        for (int x = 10; x < 13; x++) {
            for (int y = 10; y < 13; y++) {
                Tile city = new Tile(new TileCoord(x, y),
                    new Terrain(TerrainType.FLAT, 0, 0),
                    new Road(new TileCoord(x, y), 0), null);
                map.setTile(city, new TileCoord(x, y));
            }
        }

        GameState state = new GameState(map);

        // Add a test truck
        com.via.model.vehicle.VehiclePosition pos = new com.via.model.vehicle.VehiclePosition(
            new TileCoord(5, 5), new TileCoord(6, 5));
        com.via.model.vehicle.Vehicle truck = new com.via.model.vehicle.SmallTruck(pos);
        truck.onPurchased(state.getTimeState().getTick());
        state.addVehicle(truck);

        // Register the garage building in the game state (used for nearest-garage maintenance reroutes).
            state.addBuilding(new com.via.model.building.Garage(new Footprint(new TileCoord(6, 6), 1, 1)));

        // Facility logic (from original createNewGame)
        //Mine
        ProductionRule mineProduction = new ProductionRule(
            List.of(new ProductionItem(GoodType.IRON, 10))
        );
        ConsumptionRule mineConsumption = new ConsumptionRule(List.of());
        Facility mine = new Facility(
            new Footprint(new TileCoord(5, 5), 2, 2),
            mineProduction,
            mineConsumption
        );
        state.addBuilding(mine);

        //Forest camp
        ProductionRule forestProduction = new ProductionRule(
            List.of(new ProductionItem(GoodType.WOOD, 8))
        );
        Facility forestCamp = new Facility(
            new Footprint(new TileCoord(10, 5), 2, 2),
            forestProduction,
            new ConsumptionRule(List.of())
        );
        state.addBuilding(forestCamp);

        //Steel Mill
        ProductionRule steelProduction = new ProductionRule(
            List.of(new ProductionItem(GoodType.STEEL, 5))
        );
        ConsumptionRule steelConsumption = new ConsumptionRule(
            List.of(new ConsumptionItem(GoodType.IRON, 8))
        );
        Facility steelMill = new Facility(
            new Footprint(new TileCoord(15, 5), 2, 2),
            steelProduction,
            steelConsumption
        );
        state.addBuilding(steelMill);

        // City
        List<CityGate> gates = List.of(
            new CityGate(new TileCoord(20, 8), Direction.S)
        );
        City city = new City(
            new Footprint(new TileCoord(18, 5), 3, 3),
            new PassengerDemand(5.0f),
            gates
        );
        state.addBuilding(city);

        return state;
    }
}
