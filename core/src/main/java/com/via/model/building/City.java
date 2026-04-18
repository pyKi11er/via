package com.via.model.building;

import com.via.model.map.TileCoord;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a City in the game world.
 * <p>
 * A City is a large building that generates passenger demand and contains
 * an internal road network with entry/exit gates. Cities serve as major
 * hubs for passenger transportation.
 */
public class City extends Building {

    private static final int MIN_WIDTH = 3;
    private static final int MIN_HEIGHT = 3;

    private PassengerDemand passengerDemand;

    /** Internal road network logic for pathfinding within the city */
    private final InternalRoadNetwork internalRoads;

    /** Entry/exit points of the city */
    private final List<CityGate> gates;

    /**
     * Constructs a new City.
     *
     * @param footprint        the spatial footprint of the city
     * @param passengerDemand  the passenger generation profile for this city
     * @param gates            list of city gates (entry/exit points)
     * @throws IllegalArgumentException if the footprint is too small or no gates are provided
     */
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
        initializeInternalRoadNetwork();
    }

    /**
     * Initializes the internal road network by marking all tiles within
     * the city footprint as drivable internal roads.
     */
    private void initializeInternalRoadNetwork() {
        for (TileCoord c : getOccupiedTiles()) {
            if (!internalRoads.isInternalRoad(c)) {
                internalRoads.addRoadTile(c);
            }
        }
    }

    /**
     * Returns the passenger demand profile of this city.
     *
     * @return the current {@link PassengerDemand}
     */
    public PassengerDemand getPassengerDemand() {
        return passengerDemand;
    }

    /**
     * Sets the passenger demand profile for this city.
     *
     * @param passengerDemand the new passenger demand
     */
    public void  setPassengerDemand(PassengerDemand passengerDemand) {
        this.passengerDemand = passengerDemand;
    }

    /**
     * Returns the internal road network of the city.
     *
     * @return the {@link InternalRoadNetwork} used for intra-city pathfinding
     */
    public InternalRoadNetwork getInternalRoadNetwork() {
        return internalRoads;
    }

    /**
     * Returns the list of city gates (entry/exit points).
     *
     * @return list of {@link CityGate} objects
     */
    public List<CityGate> getGates() {
        return gates;
    }
}
