package com.via.controller;

import com.via.model.Constants;
import com.via.model.GameState;
import com.via.model.building.City;
import com.via.model.building.Facility;
import com.via.model.building.PassengerDemand;
import com.via.model.building.StopOverlay;
import com.via.model.economy.ConsumptionItem;
import com.via.model.economy.ProductionItem;
import com.via.model.economy.Storage;
import com.via.model.enums.GoodType;
import com.via.model.map.Map;
import com.via.model.map.Structure;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;

public class EconomySystem {

    private static final int FACILITY_TO_STOP_PER_TICK = 24;

    public void tick(GameState state) {
        if (state == null || state.getEconomy() == null || state.getTimeState() == null) return;

        transferFacilityOutputToAdjacentStops(state);

        if (state.getEconomy().getCapital() < 0) {
            state.setGameOver(true);
        }
    }

    /**
     * Income and facility intake when a vehicle unloads cargo at a route stop (not every sim tick).
     */
    public void onVehicleUnloadedCargoAtStop(GameState state, StopOverlay stop, GoodType type, int unloadedAmount) {
        if (state == null || stop == null || type == null || unloadedAmount <= 0) return;
        if (state.getEconomy() == null) return;

        Map map = state.getMap();
        if (map == null) return;

        Storage storage = stop.getStorage();
        if (storage == null) return;

        for (TileCoord adjacent : map.getAdjacentCoords(stop.getLocation())) {
            Tile adjacentTile = map.getTile(adjacent);
            if (adjacentTile == null) continue;

            Structure s = adjacentTile.getStructure();
            if (s instanceof Facility facility) {
                deliverToFacilityFromVehicleUnload(state, storage, facility, type, unloadedAmount);
            } else if (s instanceof City city) {
                deliverPassengersFromVehicleUnload(state, storage, city, type, unloadedAmount);
            }
        }
    }

    private void deliverToFacilityFromVehicleUnload(
        GameState state, Storage storage, Facility facility, GoodType unloadedType, int unloadedAmount
    ) {
        if (facility.getConsumption() == null) return;

        for (ConsumptionItem item : facility.getConsumption().getInputs()) {
            if (item == null || item.type != unloadedType) continue;
            int recipe = Math.max(0, item.amount);
            if (recipe <= 0) continue;

            int budget = unloadedAmount;
            while (budget >= recipe && storage.getAmount(item.type) >= recipe) {
                storage.removeAmount(item.type, recipe);
                facility.getStorage().addContents(item.type, recipe);
                long income = calculateIncome(item.type, recipe, item.amount);
                state.getEconomy().earn(income);
                budget -= recipe;
            }
        }
    }

    private void deliverPassengersFromVehicleUnload(
        GameState state, Storage storage, City city, GoodType unloadedType, int unloadedAmount
    ) {
        if (unloadedType != GoodType.PASSENGERS) return;

        PassengerDemand demand = city.getPassengerDemand();
        if (demand == null) return;

        int available = storage.getAmount(GoodType.PASSENGERS);
        if (available <= 0) return;

        int perTickDemand = (int) Math.ceil(Math.max(0f, demand.getRatePerTick()));
        if (perTickDemand <= 0) return;

        int deliver = Math.min(available, Math.min(unloadedAmount, perTickDemand));
        if (deliver <= 0) return;

        storage.removeAmount(GoodType.PASSENGERS, deliver);
        long income = calculateIncome(GoodType.PASSENGERS, deliver, perTickDemand);
        state.getEconomy().earn(income);
    }

    private void transferFacilityOutputToAdjacentStops(GameState state) {
        Map map = state.getMap();
        if (map == null) return;

        for (int x = 0; x < map.size.width; x++) {
            for (int y = 0; y < map.size.height; y++) {
                TileCoord coord = new TileCoord(x, y);
                Tile tile = map.getTile(coord);
                if (tile == null || !(tile.getOverlay() instanceof StopOverlay stop)) continue;
                Storage stopStorage = stop.getStorage();
                if (stopStorage == null) continue;

                for (TileCoord adj : map.getAdjacentCoords(stop.getLocation())) {
                    Tile adjTile = map.getTile(adj);
                    if (adjTile == null) continue;
                    if (!(adjTile.getStructure() instanceof Facility facility)) continue;
                    if (facility.getProduction() == null) continue;

                    for (ProductionItem out : facility.getProduction().getOutputs()) {
                        if (out == null) continue;
                        int inFacility = facility.getStorage().getAmount(out.type);
                        if (inFacility <= 0) continue;
                        int move = Math.min(inFacility, FACILITY_TO_STOP_PER_TICK);
                        facility.getStorage().removeAmount(out.type, move);
                        stopStorage.addContents(out.type, move);
                    }
                }
            }
        }
    }

    public long calculateIncome(GoodType type, int deliveredAmount, int demandPerTick) {
        if (deliveredAmount <= 0) return 0;
        long base = Constants.getBaseIncomePerUnit(type);
        long multiplier = demandPerTick >= 10 ? 2 : 1;
        return deliveredAmount * base * multiplier;
    }
}