package com.via.model.vehicle;

import com.via.model.building.StopAccess;
import com.via.model.building.StopOverlay;
import com.via.model.economy.Storage;
import com.via.model.enums.AccessType;
import com.via.model.enums.Direction;
import com.via.model.map.TileCoord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteTest {

    private StopOverlay stopA;
    private StopOverlay stopB;
    private StopOverlay stopC;

    @BeforeEach
    void setUp() {
        stopA = makeStop("A", 0, 0);
        stopB = makeStop("B", 1, 0);
        stopC = makeStop("C", 2, 0);
    }

    private StopOverlay makeStop(String name, int x, int y) {
        return new StopOverlay(name, new TileCoord(x, y),
            new StopAccess(AccessType.ROAD_TILE, new TileCoord(x, y), Direction.N),
            new Storage());
    }

    @Test
    void constructor_nullStops_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Route(null));
    }

    @Test
    void size_returnsStopCount() {
        Route route = new Route(List.of(stopA, stopB, stopC));
        assertEquals(3, route.size());
    }

    @Test
    void getStop_wrapsAround() {
        Route route = new Route(List.of(stopA, stopB, stopC));
        assertSame(stopA, route.getStop(3)); // 3 % 3 == 0
        assertSame(stopB, route.getStop(4)); // 4 % 3 == 1
    }

    @Test
    void getStop_emptyRoute_returnsNull() {
        Route route = new Route(List.of());
        assertNull(route.getStop(0));
    }

    @Test
    void getNextStopByIndex_returnsCorrectStop() {
        Route route = new Route(List.of(stopA, stopB, stopC));
        assertSame(stopA, route.getNextStop(0));
        assertSame(stopB, route.getNextStop(1));
        assertSame(stopC, route.getNextStop(2));
    }

    @Test
    void getNextStopByStop_circular() {
        Route route = new Route(List.of(stopA, stopB, stopC));
        assertSame(stopB, route.getNextStop(stopA));
        assertSame(stopC, route.getNextStop(stopB));
        assertSame(stopA, route.getNextStop(stopC)); // wraps
    }

    @Test
    void getNextStopByStop_unknownStop_returnsFirst() {
        Route route = new Route(List.of(stopA, stopB));
        StopOverlay unknown = makeStop("X", 9, 9);
        assertSame(stopA, route.getNextStop(unknown));
    }

    @Test
    void getNextStopByStop_nullStop_returnsFirst() {
        Route route = new Route(List.of(stopA, stopB));
        assertSame(stopA, route.getNextStop((StopOverlay) null));
    }

    @Test
    void addStop_increasesSize() {
        Route route = new Route(List.of(stopA));
        route.addStop(stopB);
        assertEquals(2, route.size());
    }

    @Test
    void addStop_null_ignored() {
        Route route = new Route(List.of(stopA));
        route.addStop(null);
        assertEquals(1, route.size());
    }
}
