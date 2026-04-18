package com.via.controller;

import com.via.model.GameState;
import com.via.model.building.*;
import com.via.model.economy.*;
import com.via.model.enums.GoodType;
import com.via.model.map.Map;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;

import java.util.ArrayList;
import java.util.Random;

/**
 * Handles all facility- and city-related simulation logic.
 *
 * <p>This system is responsible for:</p>
 * <ul>
 *     <li>Processing facility production and consumption cycles</li>
 *     <li>Fluctuating city passenger demand over time</li>
 *     <li>Replenishing passengers at stops near cities</li>
 *     <li>Transferring produced goods from facilities to adjacent stops</li>
 * </ul>
 *
 * <p>All updates are performed incrementally on each game tick.</p>
 */
public class FacilitySystem {

    /** Number of ticks between production cycles for facilities. */
    private static final int PRODUCTION_INTERVAL_TICKS = 120;

    /** Maximum relative daily change in city passenger demand (±5%). */
    private static final float MAX_DEMAND_CHANGE_PER_DAY = 0.05f;

    /** Number of ticks representing one in-game day. */
    private static final int TICKS_PER_DAY = 240;

    private final Random random = new Random();

    /**
     * Updates all facilities and cities for the current game tick.
     *
     * <p>This includes:</p>
     * <ul>
     *     <li>Running production cycles for facilities</li>
     *     <li>Adjusting city passenger demand</li>
     *     <li>Replenishing passengers at nearby stops once per day</li>
     *     <li>Transferring goods from facilities to adjacent stops</li>
     * </ul>
     *
     * @param state the current game state; may be {@code null}
     */
    public void tick(GameState state) {
        if (state == null) {
            return;
        }

        long tick = state.getTimeState().getTick();

        for (Building building : state.getBuildings()) {
            if (building instanceof Facility facility) {
                processFacility(facility, tick);
            } else if (building instanceof City city) {
                fluctuateDemand(city, tick);

                if (tick % TICKS_PER_DAY == 0) {
                    replenishPassengersAtCityStops(state, city);
                }
            }
        }

        transferToAdjacentStops(state);
    }

    /**
     * Processes a single facility's production cycle.
     *
     * <p>Production occurs only at fixed intervals. If the facility has a
     * {@link ConsumptionRule}, all required inputs must be available before
     * production can proceed. Inputs are consumed, and outputs defined by the
     * {@link ProductionRule} are added to the facility's storage.</p>
     *
     * @param facility the facility to process
     * @param tick the current game tick
     */
    private void processFacility(Facility facility, long tick) {
        if (tick % PRODUCTION_INTERVAL_TICKS != 0) return;

        Storage storage = facility.getStorage();
        ConsumptionRule consumption = facility.getConsumption();
        ProductionRule production = facility.getProduction();

        if (consumption != null) {
            // Check if all required inputs are available
            for (ConsumptionItem item : consumption.getInputs()) {
                if (storage.getAmount(item.type) < item.amount) {
                    return; // insufficient resources
                }
            }

            // Consume inputs
            for (ConsumptionItem item : consumption.getInputs()) {
                storage.removeAmount(item.type, item.amount);
            }
        }

        if (production != null) {
            // Produce outputs
            for (ProductionItem item : production.getOutputs()) {
                storage.addContents(item.type, item.amount);
            }
        }
    }

    /**
     * Applies daily fluctuation to a city's passenger demand.
     *
     * <p>The demand changes randomly within a bounded percentage of the current
     * rate, ensuring gradual and non-negative variation.</p>
     *
     * @param city the city whose demand is updated
     * @param tick the current game tick
     */
    private void fluctuateDemand(City city, long tick) {
        if (tick % TICKS_PER_DAY != 0) return;

        PassengerDemand demand = city.getPassengerDemand();
        float current = demand.getRatePerTick();

        float maxChange = current * MAX_DEMAND_CHANGE_PER_DAY;
        float change = (random.nextFloat() * 2 * maxChange) - maxChange;

        float newRate = Math.max(0.1f, current + change);
        demand.setRatePerTick(newRate);
    }

    /**
     * Replenishes passenger supply at a stop adjacent to the given city.
     *
     * <p>This does not generate income. It only ensures that nearby stops
     * have passengers available for pickup.</p>
     *
     * <p>The number of passengers added is based on the city's current
     * demand level.</p>
     *
     * @param state the current game state
     * @param city the city providing passengers
     */
    private void replenishPassengersAtCityStops(GameState state, City city) {
        PassengerDemand demand = city.getPassengerDemand();
        if (demand == null) return;

        Map map = state.getMap();
        if (map == null) return;

        int batch = Math.max(4, (int) Math.ceil(demand.getRatePerTick() * 8f));

        for (TileCoord c : city.getOccupiedTiles()) {
            for (TileCoord adj : map.getAdjacentCoords(c)) {
                Tile tile = map.getTile(adj);
                if (tile == null || !(tile.getOverlay() instanceof StopOverlay stop)) continue;
                if (stop.getStorage() == null) continue;

                stop.getStorage().addContents(GoodType.PASSENGERS, batch);
                return; // only replenish one adjacent stop
            }
        }
    }

    /**
     * Transfers all goods from facility storage to adjacent stops.
     *
     * <p>For each facility, all stored goods are moved to any adjacent
     * {@link StopOverlay}. This enables vehicles to pick up produced goods.</p>
     *
     * <p>All available quantities of each {@link GoodType} are transferred.</p>
     *
     * @param state the current game state
     */
    private void transferToAdjacentStops(GameState state) {
        Map map = state.getMap();

        for (Building building : state.getBuildings()) {
            if (!(building instanceof Facility facility)) continue;

            Storage facilityStorage = facility.getStorage();

            for (TileCoord coord : facility.getOccupiedTiles()) {
                for (TileCoord adj : map.getAdjacentCoords(coord)) {
                    Tile tile = map.getTile(adj);
                    if (tile == null || !(tile.getOverlay() instanceof StopOverlay stop)) continue;
                    if (stop.getStorage() == null) continue;

                    for (GoodType type : new ArrayList<>(facilityStorage.getContents().keySet())) {
                        int amount = facilityStorage.getAmount(type);
                        if (amount <= 0) continue;

                        facilityStorage.removeAmount(type, amount);
                        stop.getStorage().addContents(type, amount);
                    }
                }
            }
        }
    }
}
