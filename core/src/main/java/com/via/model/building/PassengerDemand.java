package com.via.model.building;

/**
 * Represents the passenger generation rate of a {@link City}.
 * <p>
 * This class defines how many passengers are generated per game tick
 * at a city. It can be adjusted dynamically to simulate population
 * growth, events, or varying demand levels.
 */
public class PassengerDemand {

    /** Passengers generated per tick by this city */
    private float ratePerTick;

    /**
     * Constructs a new PassengerDemand with the specified generation rate.
     *
     * @param ratePerTick the number of passengers generated per game tick
     */
    public PassengerDemand(float ratePerTick) {
        this.ratePerTick = ratePerTick;
    }

    /**
     * Returns the current passenger generation rate.
     *
     * @return passengers per tick
     */
    public float getRatePerTick() {
        return ratePerTick;
    }

    /**
     * Sets a new passenger generation rate.
     *
     * @param ratePerTick the new number of passengers per tick
     */
    public void setRatePerTick(float ratePerTick) {
        this.ratePerTick = ratePerTick;
    }
}
