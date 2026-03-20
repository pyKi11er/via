package com.via.controller;

import com.via.model.*;
import com.via.model.building.City;
import com.via.model.building.Facility;
import com.via.model.building.PassengerDemand;
import com.via.model.building.StopOverlay;
import com.via.model.economy.ConsumptionItem;
import com.via.model.economy.Storage;
import com.via.model.enums.GoodType;
import com.via.model.map.Map;
import com.via.model.map.Structure;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;
import com.via.model.vehicle.Vehicle;

public class EconomySystem {

    public void tick(GameState state) {
        if (state == null || state.getEconomy() == null || state.getTimeState() == null) return;

        processStopDeliveries(state);

        if (state.getEconomy().getCapital() < 0) {
            state.setGameOver(true);
        }
    }

    private void processStopDeliveries(GameState state) {
        Map map = state.getMap();
        if (map == null) return;

        for (int x = 0; x < map.size.width; x++) {
            for (int y = 0; y < map.size.height; y++) {
                TileCoord coord = new TileCoord(x, y);
                Tile tile = map.getTile(coord);
                if (tile == null) continue;
                if (!(tile.getOverlay() instanceof StopOverlay stop)) continue;

                deliverToAdjacentDemand(state, map, stop);
            }
        }
    }

    private void deliverToAdjacentDemand(GameState state, Map map, StopOverlay stop) {
        Storage storage = stop.getStorage();
        if (storage == null) return;

        for (TileCoord adjacent : map.getAdjacentCoords(stop.getLocation())) {
            Tile adjacentTile = map.getTile(adjacent);
            if (adjacentTile == null) continue;

            Structure s = adjacentTile.getStructure();
            if (s instanceof Facility facility) {
                deliverToFacility(state, storage, facility);
            } else if (s instanceof City city) {
                deliverPassengersToCity(state, storage, city);
            }
        }
    }

    private void deliverToFacility(GameState state, Storage storage, Facility facility) {
        if (facility.getConsumption() == null) return;

        for (ConsumptionItem item : facility.getConsumption().getInputs()) {
            if (item == null) continue;
            int available = storage.getAmount(item.type);
            if (available <= 0) continue;

            int deliver = Math.min(available, Math.max(0, item.amount));
            if (deliver <= 0) continue;

            storage.removeAmount(item.type, deliver);
            long income = calculateIncome(item.type, deliver, item.amount);
            state.getEconomy().earn(income);
        }
    }

    private void deliverPassengersToCity(GameState state, Storage storage, City city) {
        PassengerDemand demand = city.getPassengerDemand();
        if (demand == null) return;

        int available = storage.getAmount(GoodType.PASSENGERS);
        if (available <= 0) return;

        int perTickDemand = (int) Math.ceil(Math.max(0f, demand.getRatePerTick()));
        if (perTickDemand <= 0) return;

        int deliver = Math.min(available, perTickDemand);
        storage.removeAmount(GoodType.PASSENGERS, deliver);

        long income = calculateIncome(GoodType.PASSENGERS, deliver, perTickDemand);
        state.getEconomy().earn(income);
    }

    /**
     * Current pricing model (simple + deterministic for tests):
     * income = deliveredAmount * basePrice(type) * demandMultiplier, where demandMultiplier is:
     * - 2 if demandPerTick >= 10
     * - 1 otherwise
     */
    public long calculateIncome(GoodType type, int deliveredAmount, int demandPerTick) {
        if (deliveredAmount <= 0) return 0;
        long base = Constants.getBaseIncomePerUnit(type);
        long multiplier = demandPerTick >= 10 ? 2 : 1;
        return deliveredAmount * base * multiplier;
    }
}

