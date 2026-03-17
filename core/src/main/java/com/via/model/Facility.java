package com.via.model;

public class Facility extends Building {
    private static final int MIN_WIDTH = 2;
    private static final int MIN_HEIGHT = 2;

    private final ProductionRule production;
    private final ConsumptionRule consumption;
    private final Storage storage;
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

    public ProductionRule getProduction() {
        return production;
    }

    public ConsumptionRule getConsumption() {
        return consumption;
    }

    public Storage getStorage() {
        return storage;
    }
}
