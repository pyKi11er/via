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
import com.via.model.vehicle.*;

import java.util.ArrayList;
import java.util.List;

public class MapInitializer {

    private static final TileCoord MINE_ORIGIN       = new TileCoord(2,  20);
    private static final TileCoord FOREST_ORIGIN     = new TileCoord(10, 20);
    private static final TileCoord STEEL_MILL_ORIGIN = new TileCoord(18, 20);
    private static final TileCoord CITY_A_ORIGIN = new TileCoord(28, 19);
    private static final TileCoord GARAGE_ORIGIN     = new TileCoord(6,  16);

    private static final int ROAD_Y = 17;

    public static GameState createNewGame() {

        // 1. Blank map
        Map map = new Map(new GridSize(Constants.MAP_WIDTH, Constants.MAP_HEIGHT));
        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 30; y++) {
                Terrain terrain = new Terrain(TerrainType.FLAT, 0, 0);
                map.setTile(new Tile(new TileCoord(x, y), terrain, null, null),
                    new TileCoord(x, y));
            }
        }

        GameState state = new GameState(map);

        // 2. Place buildings
        buildMine(map, state);
        buildForestCamp(map, state);
        buildSteelMill(map, state);
        buildCityA(map, state);
        buildGarage(map, state);

        // 3. Build road network
        buildRoads(map);

        // 4. Place stops
        StopOverlay mineStop   = placeStop(map, "Mine Stop",       new TileCoord(3,  19));
        StopOverlay forestStop = placeStop(map, "Forest Stop",     new TileCoord(11, 19));
        StopOverlay steelStop  = placeStop(map, "Steel Mill Stop", new TileCoord(19, 19));
        StopOverlay cityStop   = placeStop(map, "City A Stop",     new TileCoord(29, 18));

        // 5. Build route from non-null stops only
        List<StopOverlay> validStops = new ArrayList<>();
        if (mineStop   != null) validStops.add(mineStop);
        if (forestStop != null) validStops.add(forestStop);
        if (steelStop  != null) validStops.add(steelStop);
        if (cityStop   != null) validStops.add(cityStop);

        if (cityStop != null && cityStop.getStorage() != null) {
            cityStop.getStorage().addContents(GoodType.PASSENGERS, 48);
        }

        Route route = new Route(validStops);
        state.addRoute(route);

        // 6. Add vehicles
        VehiclePosition truckPos = new VehiclePosition(
            new TileCoord(3, ROAD_Y), new TileCoord(4, ROAD_Y));
        SmallTruck truck = new SmallTruck(truckPos);
        truck.assignRoute(route);
        state.addVehicle(truck);

        VehiclePosition busPos = new VehiclePosition(
            new TileCoord(11, ROAD_Y), new TileCoord(12, ROAD_Y));
        MidiBus bus = new MidiBus(busPos);
        bus.assignRoute(route);
        state.addVehicle(bus);

        return state;
    }

    private static void buildMine(Map map, GameState state) {
        ProductionRule  prod = new ProductionRule(List.of(new ProductionItem(GoodType.IRON, 10)));
        ConsumptionRule cons = new ConsumptionRule(List.of());
        Facility mine = new Facility(new Footprint(MINE_ORIGIN, 2, 2), prod, cons);
        stampBuilding(map, mine);
        state.addBuilding(mine);
    }

    private static void buildForestCamp(Map map, GameState state) {
        ProductionRule  prod = new ProductionRule(List.of(new ProductionItem(GoodType.WOOD, 8)));
        ConsumptionRule cons = new ConsumptionRule(List.of());
        Facility forestCamp = new Facility(new Footprint(FOREST_ORIGIN, 2, 2), prod, cons);
        stampBuilding(map, forestCamp);
        state.addBuilding(forestCamp);
    }

    private static void buildSteelMill(Map map, GameState state) {
        ProductionRule  prod = new ProductionRule(List.of(new ProductionItem(GoodType.STEEL, 5)));
        ConsumptionRule cons = new ConsumptionRule(List.of(new ConsumptionItem(GoodType.IRON, 8)));
        Facility steelMill = new Facility(new Footprint(STEEL_MILL_ORIGIN, 2, 2), prod, cons);
        stampBuilding(map, steelMill);
        state.addBuilding(steelMill);
    }

    private static void buildCityA(Map map, GameState state) {
        Footprint fp = new Footprint(CITY_A_ORIGIN, 3, 3);
        List<CityGate> gates = List.of(
            new CityGate(new TileCoord(CITY_A_ORIGIN.x, CITY_A_ORIGIN.y - 1), Direction.S)
        );
        City city = new City(fp, new PassengerDemand(5.0f), gates);
        stampBuilding(map, city);
        state.addBuilding(city);
    }

    private static void buildGarage(Map map, GameState state) {
        Garage garage = new Garage(new Footprint(GARAGE_ORIGIN, 1, 1));
        stampBuilding(map, garage);
        state.addBuilding(garage);
    }

    private static void stampBuilding(Map map, Building building) {
        for (TileCoord coord : building.getOccupiedTiles()) {
            if (!map.isWithinBounds(coord)) continue;
            Tile tile = map.getTile(coord);
            if (tile == null) continue;
            tile.setStructure((Structure) building);
        }
    }

    private static void buildRoads(Map map) {
        for (int x = 2; x <= 32; x++) placeRoad(map, x, ROAD_Y);
        for (int y = ROAD_Y + 1; y <= 19; y++) placeRoad(map, 3,  y);
        for (int y = ROAD_Y + 1; y <= 19; y++) placeRoad(map, 11, y);
        for (int y = ROAD_Y + 1; y <= 19; y++) placeRoad(map, 19, y);
        placeRoad(map, 29, ROAD_Y);
        placeRoad(map, 29, ROAD_Y + 1);
    }

    private static void placeRoad(Map map, int x, int y) {
        TileCoord coord = new TileCoord(x, y);
        if (!map.isWithinBounds(coord)) return;
        Tile tile = map.getTile(coord);
        if (tile == null || tile.getStructure() != null) return;
        tile.setStructure(new Road(coord, 100));
    }

    private static StopOverlay placeStop(Map map, String name, TileCoord coord) {
        if (!map.isWithinBounds(coord)) return null;
        Tile tile = map.getTile(coord);
        if (tile == null || !(tile.getStructure() instanceof Road)) return null;
        StopAccess access = new StopAccess(
            com.via.model.enums.AccessType.ROAD_TILE, coord, Direction.N);
        com.via.model.economy.Storage storage = new com.via.model.economy.Storage();
        StopOverlay stop = new StopOverlay(name, coord, access, storage);
        tile.setOverlay(stop);
        return stop;
    }
}
