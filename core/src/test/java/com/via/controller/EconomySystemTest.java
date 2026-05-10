package com.via.controller;

import com.via.model.Constants;
import com.via.model.enums.GoodType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EconomySystemTest {

    private EconomySystem economySystem;

    @BeforeEach
    void setUp() {
        economySystem = new EconomySystem();
    }

    // ── calculateIncome ──────────────────────────────────────────────────────

    @Test
    void calculateIncome_zeroAmount_returnsZero() {
        assertEquals(0L, economySystem.calculateIncome(GoodType.WOOD, 0, 5));
    }

    @Test
    void calculateIncome_negativeAmount_returnsZero() {
        assertEquals(0L, economySystem.calculateIncome(GoodType.IRON, -10, 5));
    }

    @Test
    void calculateIncome_lowDemand_multiplierOne() {
        long base = Constants.getBaseIncomePerUnit(GoodType.WOOD);
        long expected = 10 * base * 1L;
        assertEquals(expected, economySystem.calculateIncome(GoodType.WOOD, 10, 9));
    }

    @Test
    void calculateIncome_highDemand_multiplierTwo() {
        long base = Constants.getBaseIncomePerUnit(GoodType.WOOD);
        long expected = 10 * base * 2L;
        assertEquals(expected, economySystem.calculateIncome(GoodType.WOOD, 10, 10));
    }

    @Test
    void calculateIncome_demandExactlyTen_triggersMultiplier() {
        long low  = economySystem.calculateIncome(GoodType.IRON, 5, 9);
        long high = economySystem.calculateIncome(GoodType.IRON, 5, 10);
        assertEquals(high, low * 2);
    }

    @Test
    void calculateIncome_passengers_usesCorrectBasePrice() {
        long base = Constants.getBaseIncomePerUnit(GoodType.PASSENGERS);
        assertEquals(base * 3L, economySystem.calculateIncome(GoodType.PASSENGERS, 3, 1));
    }

    @Test
    void calculateIncome_steel_usesCorrectBasePrice() {
        long base = Constants.getBaseIncomePerUnit(GoodType.STEEL);
        assertEquals(base * 7L, economySystem.calculateIncome(GoodType.STEEL, 7, 1));
    }

    @Test
    void calculateIncome_allGoodTypes_nonNegative() {
        for (GoodType type : GoodType.values()) {
            long income = economySystem.calculateIncome(type, 10, 5);
            assertTrue(income >= 0, "Income should be non-negative for " + type);
        }
    }

    @Test
    void calculateIncome_amountOne_equalsBasePrice() {
        for (GoodType type : GoodType.values()) {
            long base = Constants.getBaseIncomePerUnit(type);
            assertEquals(base, economySystem.calculateIncome(type, 1, 1));
        }
    }

    @Test
    void calculateIncome_highDemand_doubleOfLowDemand() {
        for (GoodType type : GoodType.values()) {
            long low  = economySystem.calculateIncome(type, 5, 9);
            long high = economySystem.calculateIncome(type, 5, 10);
            assertEquals(high, low * 2, "High demand should double income for " + type);
        }
    }

    // ── tick null guard ──────────────────────────────────────────────────────

    @Test
    void tick_nullState_doesNotThrow() {
        assertDoesNotThrow(() -> economySystem.tick(null));
    }

    // ── processVehicleUnload null guards ─────────────────────────────────────

    @Test
    void processVehicleUnload_nullState_doesNotThrow() {
        assertDoesNotThrow(() -> economySystem.processVehicleUnload(null, null, null));
    }
}
