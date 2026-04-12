package com.via.model.vehicle;

import com.via.model.Constants;
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

class VehicleTest {

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        VehiclePosition pos = new VehiclePosition(new TileCoord(0, 0), new TileCoord(1, 0));
        vehicle = new CityBus(pos);
    }

    private StopOverlay makeStop(String name, int x, int y) {
        return new StopOverlay(name, new TileCoord(x, y),
            new StopAccess(AccessType.ROAD_TILE, new TileCoord(x, y), Direction.N),
            new Storage());
    }

    // --- basic properties ---

    @Test
    void type_isCorrect() {
        assertEquals(com.via.model.enums.VehicleType.CITY_BUS, vehicle.getType());
    }

    @Test
    void id_isNotNull() {
        assertNotNull(vehicle.getId());
    }

    @Test
    void initialRoute_isNull() {
        assertNull(vehicle.getAssignedRoute());
    }

    @Test
    void getNextStop_noRoute_returnsNull() {
        assertNull(vehicle.getNextStop());
    }

    // --- assignRoute / advanceToNextStop ---

    @Test
    void assignRoute_resetsIndex() {
        StopOverlay a = makeStop("A", 0, 0);
        StopOverlay b = makeStop("B", 1, 0);
        Route route = new Route(List.of(a, b));
        vehicle.assignRoute(route);
        vehicle.advanceToNextStop();
        vehicle.assignRoute(route); // re-assign resets to 0
        assertSame(a, vehicle.getNextStop());
    }

    @Test
    void advanceToNextStop_wrapsAround() {
        StopOverlay a = makeStop("A", 0, 0);
        StopOverlay b = makeStop("B", 1, 0);
        Route route = new Route(List.of(a, b));
        vehicle.assignRoute(route);

        assertSame(a, vehicle.getNextStop());
        vehicle.advanceToNextStop();
        assertSame(b, vehicle.getNextStop());
        vehicle.advanceToNextStop();
        assertSame(a, vehicle.getNextStop()); // wraps
    }

    @Test
    void advanceToNextStop_noRoute_doesNotThrow() {
        assertDoesNotThrow(() -> vehicle.advanceToNextStop());
    }

    // --- age / maintenance ---

    @Test
    void initialAgeTicks_isZero() {
        assertEquals(0, vehicle.getAgeTicks());
    }

    @Test
    void incrementAgeTicks_increases() {
        vehicle.incrementAgeTicks(100);
        assertEquals(100, vehicle.getAgeTicks());
    }

    @Test
    void incrementAgeTicks_zeroOrNegative_ignored() {
        vehicle.incrementAgeTicks(0);
        vehicle.incrementAgeTicks(-50);
        assertEquals(0, vehicle.getAgeTicks());
    }

    @Test
    void onPurchased_resetsAge() {
        vehicle.incrementAgeTicks(500);
        vehicle.onPurchased(0);
        assertEquals(0, vehicle.getAgeTicks());
    }

    @Test
    void onPurchased_setsMaintenanceDueTick() {
        vehicle.onPurchased(1000);
        long expected = 1000 + Constants.getMaintenanceIntervalTicks(0);
        assertEquals(expected, vehicle.getMaintenanceDueTick());
    }

    @Test
    void isOverAged_belowThreshold_returnsFalse() {
        vehicle.incrementAgeTicks(Constants.VEHICLE_MAX_AGE_TICKS - 1);
        assertFalse(vehicle.isOverAged());
    }

    @Test
    void isOverAged_atThreshold_returnsTrue() {
        vehicle.incrementAgeTicks(Constants.VEHICLE_MAX_AGE_TICKS);
        assertTrue(vehicle.isOverAged());
    }

    @Test
    void getResaleValue_atAgeZero_equalsPurchaseCost() {
        assertEquals(vehicle.getPurchaseCost(), vehicle.getResaleValue());
    }

    // --- garage return ---

    @Test
    void initialReturningToGarage_isFalse() {
        assertFalse(vehicle.isReturningToGarage());
    }

    @Test
    void enterGarageReturn_setsFlag() {
        Route garageRoute = new Route(List.of(makeStop("G", 5, 5)));
        TileCoord target = new TileCoord(5, 5);
        vehicle.enterGarageReturn(garageRoute, target);
        assertTrue(vehicle.isReturningToGarage());
    }

    @Test
    void restoreAfterGarageReturn_restoresPreviousRoute() {
        StopOverlay a = makeStop("A", 0, 0);
        Route original = new Route(List.of(a));
        vehicle.assignRoute(original);
        vehicle.advanceToNextStop();

        Route garageRoute = new Route(List.of(makeStop("G", 5, 5)));
        vehicle.enterGarageReturn(garageRoute, new TileCoord(5, 5));
        vehicle.restoreAfterGarageReturn();

        assertFalse(vehicle.isReturningToGarage());
        assertSame(original, vehicle.getAssignedRoute());
    }

    @Test
    void enterGarageReturn_nullRoute_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> vehicle.enterGarageReturn(null, new TileCoord(0, 0)));
    }

    @Test
    void enterGarageReturn_nullTarget_throws() {
        Route r = new Route(List.of(makeStop("G", 0, 0)));
        assertThrows(IllegalArgumentException.class,
            () -> vehicle.enterGarageReturn(r, null));
    }

    // --- stopWaitTicks ---

    @Test
    void stopWaitTicks_defaultZero() {
        assertEquals(0, vehicle.getStopWaitTicks());
    }

    @Test
    void stopWaitTicks_setAndGet() {
        vehicle.setStopWaitTicks(42);
        assertEquals(42, vehicle.getStopWaitTicks());
    }
}
