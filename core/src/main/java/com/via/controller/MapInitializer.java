package com.via.controller;

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
 * Creates and fully initialises a new game world.
 *
 * Map layout (40 × 30 tiles):
 *
 *   Mine        (2×2) @ (2,  20)   — produces IRON
 *   Forest Camp (2×2) @ (10, 20)   — produces WOOD
 *   Steel Mill  (2×2) @ (18, 20)   — consumes IRON, produces STEEL
 *   City A      (3×3) @ (28, 18)   — demands PASSENGERS + STEEL
 *
 * Road corridor runs along y=17 connecting all buildings.
 * A vertical spur connects each building to the corridor.
 */
public class MapInitializer {

    // ── Building anchor coords ─────────────────────────────────────────────
    private static final TileCoord MINE_ORIGIN        = new TileCoord(2,  20);
    private static final TileCoord FOREST_ORIGIN      = new TileCoord(10, 20);
    private static final TileCoord STEEL_MILL_ORIGIN  = new TileCoord(18, 20);
    private static final TileCoord CITY_A_ORIGIN      = new TileCoord(28, 18);

    // ── Road corridor y-level ──────────────────────────────────────────────
    private static final int ROAD_Y = 17;

    // ══════════════════════════════════════════════════════════════════════════
    public static GameState createNewGame() {

        // 1. Blank map — every tile gets flat terrain
        Map map = new Map(new GridSize(40, 30));
        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 30; y++) {
                Terrain terrain = new Terrain(TerrainType.FLAT, 0, 0);
                map.setTile(new Tile(new TileCoord(x, y), terrain, null, null),
                    new TileCoord(x, y));
            }
        }

        GameState state = new GameState(map);

        // 2. Place buildings (sets structure on every tile of their footprint)
        Facility mine       = buildMine(map, state);
        Facility forestCamp = buildForestCamp(map, state);
        Facility steelMill  = buildSteelMill(map, state);
        City     cityA      = buildCityA(map, state);

        // 3. Build road network
        buildRoads(map);

// 4. Place stops
        StopOverlay mineStop   = placeStop(map, "Mine Stop",       new TileCoord(3,  19));
        StopOverlay forestStop = placeStop(map, "Forest Stop",     new TileCoord(11, 19));
        StopOverlay steelStop  = placeStop(map, "Steel Mill Stop", new TileCoord(19, 19));
        StopOverlay cityStop   = placeStop(map, "City A Stop",     new TileCoord(29, 18));

// 5. Only add non-null stops to route
        List<StopOverlay> validStops = new ArrayList<>();
        if (mineStop   != null) validStops.add(mineStop);
        if (forestStop != null) validStops.add(forestStop);
        if (steelStop  != null) validStops.add(steelStop);
        if (cityStop   != null) validStops.add(cityStop);

        Route route = new Route(validStops);
        state.addRoute(route);

        // 6. Add vehicles and assign routes
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

    // ══════════════════════════════════════════════════════════════════════════
    // Building helpers — each method creates the domain object AND stamps the
    // structure onto every tile in its footprint.
    // ══════════════════════════════════════════════════════════════════════════

    private static Facility buildMine(Map map, GameState state) {
        ProductionRule  prod = new ProductionRule(List.of(new ProductionItem(GoodType.IRON, 10)));
        ConsumptionRule cons = new ConsumptionRule(List.of());
        Footprint fp = new Footprint(MINE_ORIGIN, 2, 2);
        Facility mine = new Facility(fp, prod, cons);

        stampBuilding(map, mine);
        state.addBuilding(mine);
        return mine;
    }

    private static Facility buildForestCamp(Map map, GameState state) {
        ProductionRule  prod = new ProductionRule(List.of(new ProductionItem(GoodType.WOOD, 8)));
        ConsumptionRule cons = new ConsumptionRule(List.of());
        Footprint fp = new Footprint(FOREST_ORIGIN, 2, 2);
        Facility forestCamp = new Facility(fp, prod, cons);

        stampBuilding(map, forestCamp);
        state.addBuilding(forestCamp);
        return forestCamp;
    }

    private static Facility buildSteelMill(Map map, GameState state) {
        ProductionRule  prod = new ProductionRule(List.of(new ProductionItem(GoodType.STEEL, 5)));
        ConsumptionRule cons = new ConsumptionRule(List.of(new ConsumptionItem(GoodType.IRON, 8)));
        Footprint fp = new Footprint(STEEL_MILL_ORIGIN, 2, 2);
        Facility steelMill = new Facility(fp, prod, cons);

        stampBuilding(map, steelMill);
        state.addBuilding(steelMill);
        return steelMill;
    }

    private static City buildCityA(Map map, GameState state) {
        Footprint fp = new Footprint(CITY_A_ORIGIN, 3, 3);
        List<CityGate> gates = List.of(
            new CityGate(new TileCoord(CITY_A_ORIGIN.x, CITY_A_ORIGIN.y - 1), Direction.S)
        );
        City city = new City(fp, new PassengerDemand(5.0f), gates);

        stampBuilding(map, city);
        state.addBuilding(city);
        return city;
    }

    /**
     * Writes the building reference as the Structure on every tile it occupies.
     * This is the critical step that was missing before — without this the
     * renderer never sees a City/Facility on any tile.
     */
    private static void stampBuilding(Map map, Building building) {
        for (TileCoord coord : building.getOccupiedTiles()) {
            if (!map.isWithinBounds(coord)) continue;
            Tile tile = map.getTile(coord);
            if (tile == null) continue;
            tile.setStructure(building instanceof com.via.model.map.Structure
                ? (com.via.model.map.Structure) building
                : null);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Road network
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Builds:
     *  - A horizontal corridor from x=2 to x=32 at y=ROAD_Y
     *  - Vertical spurs from each building down to the corridor
     */
    private static void buildRoads(Map map) {
        // Horizontal corridor
        for (int x = 2; x <= 32; x++) {
            placeRoad(map, x, ROAD_Y);
        }

        // Vertical spur — Mine (x=3, from y=19 down to ROAD_Y)
        for (int y = ROAD_Y + 1; y <= 19; y++) {
            placeRoad(map, 3, y);
        }

        // Vertical spur — Forest Camp (x=11)
        for (int y = ROAD_Y + 1; y <= 19; y++) {
            placeRoad(map, 11, y);
        }

        // Vertical spur — Steel Mill (x=19)
        for (int y = ROAD_Y + 1; y <= 19; y++) {
            placeRoad(map, 19, y);
        }

        // Vertical spur — City A (x=29, from ROAD_Y up to city edge at y=18)
        placeRoad(map, 29, ROAD_Y);
        placeRoad(map, 29, ROAD_Y + 1);
    }

    private static void placeRoad(Map map, int x, int y) {
        TileCoord coord = new TileCoord(x, y);
        if (!map.isWithinBounds(coord)) return;
        Tile tile = map.getTile(coord);
        if (tile == null) return;
        // Only place road on empty tiles (don't overwrite buildings)
        if (tile.getStructure() != null) return;
        tile.setStructure(new Road(coord, 100));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Stop helper
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Places a StopOverlay on the given tile as long as it has a Road structure.
     * Returns the created StopOverlay so it can be added to routes.
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
}
