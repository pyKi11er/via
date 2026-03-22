package com.via.controller;

import com.via.model.Constants;
import com.via.model.GameState;
import com.via.model.building.City;
import com.via.model.building.Facility;
import com.via.model.building.PassengerDemand;
import com.via.model.building.StopOverlay;
import com.via.model.economy.ConsumptionItem;
import com.via.model.economy.ConsumptionRule;
import com.via.model.economy.Storage;
import com.via.model.enums.GoodType;
import com.via.model.map.Map;
import com.via.model.map.Structure;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;
import com.via.model.vehicle.Cargo;

/**
 * Income is credited only when a vehicle unloads cargo at a stop and that cargo
 * satisfies adjacent demand (facility consumption inputs or city passenger demand).
 */
public class EconomySystem {

    public void tick(GameState state) {
        if (state == null || state.getEconomy() == null) return;

        if (state.getEconomy().getCapital() < 0) {
            state.setGameOver(true);
        }
    }

    /**
     * Called from movement when a vehicle unloads at a route stop. Matching demand
     * is delivered into adjacent facility/city storage and earns income; any remainder
     * stays at the stop.
     */
    public void processVehicleUnload(GameState state, StopOverlay stop, Cargo cargo) {
        if (state == null || state.getEconomy() == null || stop == null) return;
        if (cargo == null || cargo.getAmount() <= 0) return;

        Storage stopStorage = stop.getStorage();
        if (stopStorage == null) return;

        Map map = state.getMap();
        if (map == null) return;

        GoodType type = cargo.getType();
        int remaining = cargo.getAmount();

        for (TileCoord adjacent : map.getAdjacentCoords(stop.getLocation())) {
            Tile tile = map.getTile(adjacent);
            if (tile == null) continue;

            Structure s = tile.getStructure();
            if (s instanceof Facility facility) {
                remaining = deliverUnloadToFacility(state, facility, type, remaining);
            } else if (s instanceof City city && type == GoodType.PASSENGERS) {
                remaining = deliverUnloadPassengersToCity(state, city, remaining);
            }
        }

        if (remaining > 0) {
            stopStorage.addContents(type, remaining);
        }
    }

    private int deliverUnloadToFacility(GameState state, Facility facility, GoodType type, int amount) {
        if (amount <= 0) return 0;

        ConsumptionRule rule = facility.getConsumption();
        if (rule == null) return amount;

        ConsumptionItem match = null;
        for (ConsumptionItem item : rule.getInputs()) {
            if (item != null && item.type == type) {
                match = item;
                break;
            }
        }
        if (match == null) return amount;

        Storage facilityStorage = facility.getStorage();
        if (facilityStorage == null) return amount;

        facilityStorage.addContents(type, amount);
        long income = calculateIncome(type, amount, match.amount);
        state.getEconomy().earn(income);
        return 0;
    }

    private int deliverUnloadPassengersToCity(GameState state, City city, int amount) {
        if (amount <= 0) return 0;

        PassengerDemand demand = city.getPassengerDemand();
        if (demand == null) return amount;

        int perTickDemand = (int) Math.ceil(Math.max(0f, demand.getRatePerTick()));
        if (perTickDemand <= 0) return amount;

        long income = calculateIncome(GoodType.PASSENGERS, amount, perTickDemand);
        state.getEconomy().earn(income);
        return 0;
    }

    /**
     * income = deliveredAmount * basePrice(type) * demandMultiplier, where demandMultiplier is:
     * 2 if demandPerTick &gt;= 10, else 1.
     */
    public long calculateIncome(GoodType type, int deliveredAmount, int demandPerTick) {
        if (deliveredAmount <= 0) return 0;
        long base = Constants.getBaseIncomePerUnit(type);
        long multiplier = demandPerTick >= 10 ? 2 : 1;
        return deliveredAmount * base * multiplier;
    }
}
