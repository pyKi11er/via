package com.via.model.vehicle;

import com.via.model.building.StopOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Route {
    private final UUID id;
    private final List<StopOverlay> stops;

    public Route(List<StopOverlay> stops) {
        if (stops == null) throw new IllegalArgumentException("stops must not be null");
        this.id = UUID.randomUUID();
        this.stops = new ArrayList<>(stops);
    }

    public UUID getId() {
        return id;
    }

    public List<StopOverlay> getStops() {
        return Collections.unmodifiableList(stops);
    }

    public int size() {
        return stops.size();
    }

    public StopOverlay getStop(int index) {
        if (stops.isEmpty()) return null;
        int wrapped = Math.floorMod(index, stops.size());
        return stops.get(wrapped);
    }

    /** Circular next stop after the given stop (by first matching index). */
    public StopOverlay getNextStop(StopOverlay currentStop) {
        if (stops.isEmpty()) return null;
        if (currentStop == null) return stops.get(0);

        int idx = stops.indexOf(currentStop);
        if (idx < 0) return stops.get(0);
        return stops.get((idx + 1) % stops.size());
    }

    /**
     * Next leg destination: {@code routeStopIndex} is the cursor for the upcoming stop (wrapped).
     * After {@code assignRoute}, use 0 to target the first stop in the list.
     */
    public StopOverlay getNextStop(int routeStopIndex) {
        return getStop(routeStopIndex);
    }
}
