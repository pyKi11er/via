package com.via.model.vehicle;

import com.via.model.building.StopOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents a vehicle route consisting of an ordered list of stops.
 * Routes are immutable in terms of stop list order but allow adding new stops.
 * Supports circular traversal of stops for continuous routes.
 */
public class Route {
    private final UUID id;
    private final List<StopOverlay> stops;

    /**
     * Constructs a new Route with the specified list of stops.
     *
     * @param stops the list of stops that make up this route (must not be null)
     * @throws IllegalArgumentException if stops is null
     */
    public Route(List<StopOverlay> stops) {
        if (stops == null) throw new IllegalArgumentException("stops must not be null");
        this.id = UUID.randomUUID();
        this.stops = new ArrayList<>(stops);
    }

    /**
     * Returns the unique identifier of this route.
     *
     * @return the route ID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns an unmodifiable list of stops on this route.
     *
     * @return the list of stops (read-only)
     */
    public List<StopOverlay> getStops() {
        return Collections.unmodifiableList(stops);
    }

    /**
     * Adds a stop to the end of this route.
     *
     * @param stop the stop to add (ignored if null)
     */
    public void addStop(StopOverlay stop) {
        if (stop != null) stops.add(stop);
    }

    /**
     * Returns the number of stops on this route.
     *
     * @return the stop count
     */
    public int size() {
        return stops.size();
    }

    /**
     * Returns the stop at the specified index, with circular wrapping.
     *
     * @param index the index (supports negative values via modulo)
     * @return the stop at the wrapped index, or null if the route is empty
     */
    public StopOverlay getStop(int index) {
        if (stops.isEmpty()) return null;
        int wrapped = Math.floorMod(index, stops.size());
        return stops.get(wrapped);
    }

    /**
     * Returns the next stop after the given stop in the circular route.
     *
     * @param currentStop the current stop (if null, returns the first stop)
     * @return the next stop in the route, or null if the route is empty
     */
    public StopOverlay getNextStop(StopOverlay currentStop) {
        if (stops.isEmpty()) return null;
        if (currentStop == null) return stops.get(0);

        int idx = stops.indexOf(currentStop);
        if (idx < 0) return stops.get(0);
        return stops.get((idx + 1) % stops.size());
    }

    /**
     * Returns the stop at the specified route index for the next leg destination.
     * After calling assignRoute, use index 0 to target the first stop in the list.
     *
     * @param routeStopIndex the cursor index for the upcoming stop (will be wrapped)
     * @return the stop at the wrapped index, or null if the route is empty
     */
    public StopOverlay getNextStop(int routeStopIndex) {
        return getStop(routeStopIndex);
    }
}
