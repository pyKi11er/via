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
import com.via.model.map.GridSize;
import com.via.model.map.Map;
import com.via.model.map.TileCoord;

import java.util.List;

public class MapInitializer {
    public static GameState createNewGame() {
        Map map = new Map(new GridSize(Constants.MAP_WIDTH, Constants.MAP_HEIGHT));
        GameState state = new GameState(map);

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
