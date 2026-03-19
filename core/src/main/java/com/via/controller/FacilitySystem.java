package com.via.controller;

import com.via.model.GameState;
import com.via.model.building.Building;
import com.via.model.building.City;
import com.via.model.building.Facility;
import com.via.model.building.PassengerDemand;
import com.via.model.economy.*;

import java.util.Random;

public class FacilitySystem {
    private static final int PRODUCTION_INTERVAL_TICKS = 30;
    private static final float MAX_DEMAND_CHANGE_PER_DAY = 0.05f;
    private static final int TICKS_PER_DAY = 240;

    public void tick(GameState state) {
        if(state == null) {
            return;
        }
        long tick = state.getTimeState().getTick();
        for(Building building : state.getBuildings()){
            if(building instanceof Facility facility){
                processFacility(facility, tick);
            }
            else if(building instanceof City city){
                fluctuateDemand(city, tick);
            }
        }
    }

    private void processFacility(Facility facility, long tick) {
        if (tick % PRODUCTION_INTERVAL_TICKS != 0) return;

        Storage storage = facility.getStorage();
        ConsumptionRule consumption = facility.getConsumption();
        ProductionRule production = facility.getProduction();

        if (consumption != null) {
            for (ConsumptionItem item : consumption.getInputs()) {
                if (storage.getAmount(item.type) < item.amount) {
                    return; // not enough input — don't produce
                }
            }

            for (ConsumptionItem item : consumption.getInputs()) {
                storage.removeAmount(item.type, item.amount);
            }
        }

        if (production != null) {
            for (ProductionItem item : production.getOutputs()) {
                storage.addContents(item.type, item.amount);
            }
        }
    }

    private final Random random = new Random();

    private void fluctuateDemand(City city, long tick) {
        if (tick % TICKS_PER_DAY != 0) return;

        PassengerDemand demand = city.getPassengerDemand();
        float current = demand.getRatePerTick();
        float maxChange = current * MAX_DEMAND_CHANGE_PER_DAY;
        float change = (random.nextFloat() * 2 * maxChange) - maxChange;
        float newRate = Math.max(0.1f, current + change);
        demand.setRatePerTick(newRate);
    }
}
