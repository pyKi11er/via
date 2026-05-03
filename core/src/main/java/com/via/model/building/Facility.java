package com.via.model.building;

import com.via.model.economy.ConsumptionRule;
import com.via.model.economy.ProductionRule;
import com.via.model.economy.Storage;

/**
 * Represents a production or industrial Facility in the game.
 * <p>
 * A Facility is a specialized building that can produce goods according to
 * a {@link ProductionRule} and/or consume resources according to a
 * {@link ConsumptionRule}. It maintains its own {@link Storage} for inputs and outputs.
 */
public class Facility extends Building {

    private static final int MIN_WIDTH = 2;
    private static final int MIN_HEIGHT = 2;

    /** The production rule defining what this facility produces */
    private final ProductionRule production;

    /** The consumption rule defining what resources this facility requires */
    private final ConsumptionRule consumption;

    /** Storage for input materials and produced goods */
    private final Storage storage;

    /**
     * Constructs a new Facility.
     *
     * @param footprint     the spatial footprint of the facility
     * @param production    the production rule for this facility
     * @param consumption   the consumption rule for this facility
     * @throws IllegalArgumentException if the footprint is smaller than 2x2 tiles
     */
    public Facility(Footprint footprint, ProductionRule production, ConsumptionRule consumption) {
        super(footprint);
        if(footprint.getWidth() < MIN_WIDTH || footprint.getHeight() < MIN_HEIGHT) {
            throw new IllegalArgumentException(
                "Facility footprint must be at least " + MIN_WIDTH + "x" + MIN_HEIGHT
            );
        }
        this.production = production;
        this.consumption = consumption;
        this.storage = new Storage();
    }

    /**
     * Returns the production rule of this facility.
     *
     * @return the {@link ProductionRule} of this facility
     */
    public ProductionRule getProduction() {
        return production;
    }

    /**
     * Returns the consumption rule of this facility.
     *
     * @return the {@link ConsumptionRule} of this facility
     */
    public ConsumptionRule getConsumption() {
        return consumption;
    }

    /**
     * Returns the storage associated with this facility.
     *
     * @return the {@link Storage} of this facility
     */
    public Storage getStorage() {
        return storage;
    }
}
