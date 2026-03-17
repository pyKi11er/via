package com.via.model;

import java.util.ArrayList;
import java.util.List;

public class City extends Building {
    private static final int MIN_WIDTH = 3;
    private static final int MIN_HEIGHT = 3;

    private PassengerDemand passengerDemand;
    private final InternalRoadNetwork internalRoads;
    private final List<CityGate> gates;
    public City(Footprint footprint, PassengerDemand passengerDemand, List<CityGate> gates) {
        super(footprint);
        if(footprint.getWidth() < MIN_WIDTH || footprint.getHeight() < MIN_HEIGHT) {
            throw new IllegalArgumentException(
                "City footprint must be at least " + MIN_WIDTH + "x" + MIN_HEIGHT
            );
        }
        if (gates == null || gates.isEmpty()) {
            throw new IllegalArgumentException("City must have at least one CityGate");
        }
        this.passengerDemand = passengerDemand;
        this.internalRoads = new InternalRoadNetwork();
        this.gates = new ArrayList<>(gates);
        gates.forEach(internalRoads::addEntryPoint);
    }
    public PassengerDemand getPassengerDemand() {
        return passengerDemand;
    }
    public void  setPassengerDemand(PassengerDemand passengerDemand) {
        this.passengerDemand = passengerDemand;
    }
    public InternalRoadNetwork getInternalRoadNetwork() {
        return internalRoads;
    }
    public List<CityGate> getGates() {
        return gates;
    }
}
