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

/**
 * Utility class responsible for creating and initializing a complete game map.
 *
 * <p>This includes:</p>
 * <ul>
 *     <li>Generating terrain and base tiles</li>
 *     <li>Placing initial buildings (facilities, city, garage, parking)</li>
 *     <li>Constructing a road network</li>
 *     <li>Adding transport stops and routes</li>
 *     <li>Spawning initial vehicles</li>
 *     <li>Seeding environmental features such as forests</li>
 * </ul>
 *
 * <p>The generated map represents a predefined starting scenario designed
 * for testing or demo gameplay.</p>
 */

public class MapInitializer {

    private static final TileCoord MINE_ORIGIN       = new TileCoord(2,  20);
    private static final TileCoord FOREST_ORIGIN     = new TileCoord(10, 20);
    private static final TileCoord STEEL_MILL_ORIGIN = new TileCoord(18, 20);
    private static final TileCoord CITY_A_ORIGIN = new TileCoord(28, 19);
    private static final TileCoord GARAGE_ORIGIN     = new TileCoord(6,  16);
    private static final TileCoord PARKING_ORIGIN    = new TileCoord(4,  14);

    private static final int ROAD_Y = 17;
    /**
     * Creates and returns a fully initialized {@link GameState}.
     *
     * <p>The initialization process performs the following steps:</p>
     * <ol>
     *     <li>Create a blank map with default terrain</li>
     *     <li>Generate terrain variation</li>
     *     <li>Seed initial forest areas</li>
     *     <li>Place buildings (facilities, city, garage, parking)</li>
     *     <li>Construct road network</li>
     *     <li>Place stops and create a transport route</li>
     *     <li>Spawn initial vehicles and assign routes</li>
     *     <li>Finalize terrain slope types</li>
     * </ol>
     *
     * @return a new, fully initialized game state
     */
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

        // Generate varied terrain heights before placing anything
        TerrainGenerator.generate(map, 42L);

        GameState state = new GameState(map);

        seedForests(map);

        // 2. Place buildings
        buildMine(map, state);
        buildForestCamp(map, state);
        buildSteelMill(map, state);
        buildCityA(map, state);
        buildGarage(map, state);

        // 3. Build road network
        buildRoads(map);

        // Pre-built parking lot next to the garage
        buildParking(map, state);

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

        // Derive slope tile types from final heights (after all buildings are placed)
        TerrainGenerator.updateSlopeTypes(map);

        // 6. Add vehicles
        VehiclePosition truckPos = new VehiclePosition(
            new TileCoord(3, ROAD_Y), new TileCoord(4, ROAD_Y));
        SmallTruck truck = new SmallTruck(truckPos);
        truck.onPurchased(0);
        truck.assignRoute(route);
        state.addVehicle(truck);

        VehiclePosition busPos = new VehiclePosition(
            new TileCoord(11, ROAD_Y), new TileCoord(12, ROAD_Y));
        MidiBus bus = new MidiBus(busPos);
        bus.onPurchased(0);
        bus.assignRoute(route);
        state.addVehicle(bus);

        return state;
    }
    /**
     * Builds a mining facility producing iron.
     */
    private static void buildMine(Map map, GameState state) {
        ProductionRule  prod = new ProductionRule(List.of(new ProductionItem(GoodType.IRON, 10)));
        ConsumptionRule cons = new ConsumptionRule(List.of());
        Facility mine = new Facility(new Footprint(MINE_ORIGIN, 2, 2), prod, cons);
        stampBuilding(map, mine);
        state.addBuilding(mine);
    }
    /**
     * Builds a forest camp producing wood.
     */
    private static void buildForestCamp(Map map, GameState state) {
        ProductionRule  prod = new ProductionRule(List.of(new ProductionItem(GoodType.WOOD, 8)));
        ConsumptionRule cons = new ConsumptionRule(List.of());
        Facility forestCamp = new Facility(new Footprint(FOREST_ORIGIN, 2, 2), prod, cons);
        stampBuilding(map, forestCamp);
        state.addBuilding(forestCamp);
    }
    /**
     * Builds a steel mill that consumes iron and produces steel.
     */
    private static void buildSteelMill(Map map, GameState state) {
        ProductionRule  prod = new ProductionRule(List.of(new ProductionItem(GoodType.STEEL, 5)));
        ConsumptionRule cons = new ConsumptionRule(List.of(new ConsumptionItem(GoodType.IRON, 8)));
        Facility steelMill = new Facility(new Footprint(STEEL_MILL_ORIGIN, 2, 2), prod, cons);
        stampBuilding(map, steelMill);
        state.addBuilding(steelMill);
    }
    /**
     * Builds a city with passenger demand and a defined access gate.
     */
    private static void buildCityA(Map map, GameState state) {
        Footprint fp = new Footprint(CITY_A_ORIGIN, 3, 3);
        List<CityGate> gates = List.of(
            new CityGate(new TileCoord(CITY_A_ORIGIN.x, CITY_A_ORIGIN.y - 1), Direction.S)
        );
        City city = new City(fp, new PassengerDemand(5.0f), gates);
        stampBuilding(map, city);
        state.addBuilding(city);
    }
    /**
     * Builds a garage for vehicle storage and spawning.
     */

    private static void buildGarage(Map map, GameState state) {
        Garage garage = new Garage(new Footprint(GARAGE_ORIGIN, 1, 1));
        stampBuilding(map, garage);
        state.addBuilding(garage);
    }
    /**
     * Applies a building's footprint onto the map tiles.
     *
     * @param map the map
     * @param building the building to place
     */

    private static void stampBuilding(Map map, Building building) {
        for (TileCoord coord : building.getOccupiedTiles()) {
            if (!map.isWithinBounds(coord)) continue;
            Tile tile = map.getTile(coord);
            if (tile == null) continue;
            tile.setStructure((Structure) building);
        }
    }
    /**
     * Constructs the predefined road network.
     */
    private static void buildRoads(Map map) {
        for (int x = 2; x <= 32; x++) placeRoad(map, x, ROAD_Y);
        for (int y = ROAD_Y + 1; y <= 19; y++) placeRoad(map, 3,  y);
        for (int y = ROAD_Y + 1; y <= 19; y++) placeRoad(map, 11, y);
        for (int y = ROAD_Y + 1; y <= 19; y++) placeRoad(map, 19, y);
        placeRoad(map, 29, ROAD_Y);
        placeRoad(map, 29, ROAD_Y + 1);

        // Parking loop roads: rectangle around the parking area at (4,14)-(5,15)
        for (int y = 13; y <= 16; y++) placeRoad(map, 3, y); // left side
        for (int y = 13; y <= 15; y++) placeRoad(map, 6, y); // right side (below garage)
        placeRoad(map, 4, 13);                                // bottom connector
        placeRoad(map, 5, 13);
    }

    private static void buildParking(Map map, GameState state) {
        com.via.model.building.Parking parking =
            new com.via.model.building.Parking(
                new com.via.model.building.Footprint(PARKING_ORIGIN, 2, 2));
        stampBuilding(map, parking);
        state.addBuilding(parking);

        for (Building b : state.getBuildings()) {
            if (b instanceof com.via.model.building.Garage g) {
                g.setParking(parking);
                break;
            }
        }
    }
    /**
     * Places a road tile if the location is valid and empty.
     */

    private static void placeRoad(Map map, int x, int y) {
        TileCoord coord = new TileCoord(x, y);
        if (!map.isWithinBounds(coord)) return;
        Tile tile = map.getTile(coord);
        if (tile == null || tile.getStructure() != null) return;
        tile.setStructure(new Road(coord, 100));
    }

    /**
     * Places a stop overlay on a road tile.
     *
     * @param map the map
     * @param name display name of the stop
     * @param coord location of the stop
     * @return the created {@link StopOverlay}, or {@code null} if placement fails
     */

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
    /**
     * Seeds predefined forest patches on the map.
     *
     * @param map the map to modify
     */
    private static void seedForests(Map map) {
        int[][] seeds = {
            {1, 25, 2}, {2, 25, 1}, {1, 26, 1}, {2, 26, 3}, {3, 26, 1},
            {14, 27, 3}, {15, 27, 2}, {15, 28, 1}, {16, 27, 1}, {14, 28, 2},
            {35, 22, 2}, {36, 22, 1}, {35, 23, 3}, {36, 23, 2}, {37, 23, 1},
            {39, 2, 1}, {39, 1, 1}, {38, 2, 2}, {38, 1, 1}, {38, 2, 1},
            {9, 5, 1}, {9, 4, 1}, {8, 5, 2}, {8, 4, 1}, {8, 3, 1},
        };

        for (int[] seed : seeds) {
            TileCoord coord = new TileCoord(seed[0], seed[1]);
            if (!map.isWithinBounds(coord)) continue;
            Tile tile = map.getTile(coord);
            if (tile == null || tile.getTerrain() == null) continue;
            tile.getTerrain().setForestLevel(seed[2]);
        }
    }
}
