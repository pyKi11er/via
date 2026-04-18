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
 * Handles all economic interactions in the game, including income generation
 * and delivery processing.
 *
 * <p>Income is generated only when a vehicle unloads cargo at a stop and that
 * cargo satisfies nearby demand (facility consumption inputs or city passenger demand).
 * Any excess cargo that does not meet demand is stored at the stop.</p>
 */
public class EconomySystem {

    /**
     * Updates the economy system for a single game tick.
     *
     * <p>If the player's capital drops below zero, the game is marked as over.</p>
     *
     * @param state the current {@link GameState}; may be {@code null}
     */
    public void tick(GameState state) {
        if (state == null || state.getEconomy() == null) return;

        if (state.getEconomy().getCapital() < 0) {
            state.setGameOver(true);
        }
    }

    /**
     * Processes cargo unloading at a stop.
     *
     * <p>When a vehicle unloads cargo, adjacent tiles are checked for structures:
     * <ul>
     *     <li>If a {@link Facility} is present, cargo is delivered if it matches
     *     the facility's consumption requirements.</li>
     *     <li>If a {@link City} is present and the cargo type is passengers,
     *     passenger demand is satisfied.</li>
     * </ul>
     * Any remaining cargo that is not consumed is stored at the stop.</p>
     *
     * @param state the current game state
     * @param stop the stop where cargo is unloaded
     * @param cargo the cargo being unloaded
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

    /**
     * Attempts to deliver cargo to a facility based on its consumption rules.
     *
     * <p>If the cargo type matches one of the facility's required inputs,
     * the full amount is delivered and income is generated. Otherwise,
     * the cargo remains undelivered.</p>
     *
     * @param state the current game state
     * @param facility the target facility
     * @param type the type of goods being delivered
     * @param amount the amount of goods available
     * @return the remaining amount that could not be delivered
     */
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

    /**
     * Delivers passenger cargo to a city and generates income based on demand.
     *
     * <p>Passenger demand is calculated per tick. If demand exists, all passengers
     * are considered delivered and income is generated accordingly.</p>
     *
     * @param state the current game state
     * @param city the target city
     * @param amount the number of passengers
     * @return the remaining passengers (always {@code 0} if demand exists)
     */
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
     * Calculates income generated from delivering goods.
     *
     * <p>Formula:
     * <pre>
     * income = deliveredAmount * basePrice(type) * demandMultiplier
     * </pre>
     * where:
     * <ul>
     *     <li>{@code basePrice(type)} is defined in {@link Constants}</li>
     *     <li>{@code demandMultiplier} is:
     *         <ul>
     *             <li>{@code 2} if {@code demandPerTick >= 10}</li>
     *             <li>{@code 1} otherwise</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param type the type of goods delivered
     * @param deliveredAmount the amount of goods delivered
     * @param demandPerTick the demand level influencing pricing
     * @return the calculated income
     */
    public long calculateIncome(GoodType type, int deliveredAmount, int demandPerTick) {
        if (deliveredAmount <= 0) return 0;
        long base = Constants.getBaseIncomePerUnit(type);
        long multiplier = demandPerTick >= 10 ? 2 : 1;
        return deliveredAmount * base * multiplier;
    }
}
