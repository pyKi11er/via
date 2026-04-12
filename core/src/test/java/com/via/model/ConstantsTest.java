package com.via.model;

import com.via.model.enums.GoodType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstantsTest {

    @Test
    void maintenanceInterval_atAgeZero_returnsBaseInterval() {
        long interval = Constants.getMaintenanceIntervalTicks(0);
        assertEquals(Constants.VEHICLE_MAINTENANCE_INTERVAL_TICKS, interval);
    }

    @Test
    void maintenanceInterval_negativeAge_treatedAsZero() {
        long atZero = Constants.getMaintenanceIntervalTicks(0);
        long atNeg  = Constants.getMaintenanceIntervalTicks(-100);
        assertEquals(atZero, atNeg);
    }

    @Test
    void maintenanceInterval_neverBelowMinimum() {
        long interval = Constants.getMaintenanceIntervalTicks(Long.MAX_VALUE / 2);
        assertTrue(interval >= Constants.VEHICLE_MIN_MAINTENANCE_INTERVAL_TICKS);
    }

    @Test
    void resaleValue_atAgeZero_equalsPurchaseCost() {
        long resale = Constants.getResaleValue(10_000, 0);
        assertEquals(10_000, resale);
    }

    @Test
    void resaleValue_negativePurchaseCost_returnsZero() {
        assertEquals(0, Constants.getResaleValue(-1, 0));
    }

    @Test
    void resaleValue_negativeAge_treatedAsZero() {
        long pos = Constants.getResaleValue(10_000, 0);
        long neg = Constants.getResaleValue(10_000, -500);
        assertEquals(pos, neg);
    }

    @Test
    void resaleValue_neverBelowMinFraction() {
        long purchase = 10_000;
        long floor = Math.round(purchase * Constants.VEHICLE_MIN_RESALE_FRACTION);
        long resale = Constants.getResaleValue(purchase, Long.MAX_VALUE / 2);
        assertTrue(resale >= floor);
    }

    @Test
    void baseIncomePerUnit_knownTypes() {
        assertEquals(10L, Constants.getBaseIncomePerUnit(GoodType.PASSENGERS));
        assertEquals(6L,  Constants.getBaseIncomePerUnit(GoodType.WOOD));
        assertEquals(8L,  Constants.getBaseIncomePerUnit(GoodType.IRON));
        assertEquals(12L, Constants.getBaseIncomePerUnit(GoodType.STEEL));
    }

    @Test
    void baseIncomePerUnit_null_returnsZero() {
        assertEquals(0L, Constants.getBaseIncomePerUnit(null));
    }
}
