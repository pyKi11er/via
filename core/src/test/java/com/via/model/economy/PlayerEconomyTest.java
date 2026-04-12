package com.via.model.economy;

import com.via.model.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerEconomyTest {

    private PlayerEconomy economy;

    @BeforeEach
    void setUp() {
        economy = new PlayerEconomy();
    }

    @Test
    void initialCapital_equalsStartingConstant() {
        assertEquals(Constants.STARTING_CAPITAL, economy.getCapital());
    }

    @Test
    void earn_increasesCapital() {
        economy.earn(500);
        assertEquals(Constants.STARTING_CAPITAL + 500, economy.getCapital());
    }

    @Test
    void earn_zero_noChange() {
        economy.earn(0);
        assertEquals(Constants.STARTING_CAPITAL, economy.getCapital());
    }

    @Test
    void earn_negativeAmount_throws() {
        assertThrows(IllegalArgumentException.class, () -> economy.earn(-1));
    }

    @Test
    void deduct_decreasesCapital() {
        economy.deduct(1000);
        assertEquals(Constants.STARTING_CAPITAL - 1000, economy.getCapital());
    }

    @Test
    void deduct_negativeAmount_throws() {
        assertThrows(IllegalArgumentException.class, () -> economy.deduct(-1));
    }

    @Test
    void deduct_canGoBelowZero() {
        economy.deduct(Constants.STARTING_CAPITAL + 1);
        assertTrue(economy.getCapital() < 0);
    }

    @Test
    void earnAndDeduct_multipleOps() {
        economy.earn(200);
        economy.deduct(50);
        economy.earn(100);
        assertEquals(Constants.STARTING_CAPITAL + 250, economy.getCapital());
    }
}
