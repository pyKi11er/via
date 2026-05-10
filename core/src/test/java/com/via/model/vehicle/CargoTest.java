package com.via.model.vehicle;

import com.via.model.enums.GoodType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CargoTest {

    @Test
    void constructor_storesTypeAndAmount() {
        Cargo c = new Cargo(GoodType.WOOD, 42);
        assertEquals(GoodType.WOOD, c.getType());
        assertEquals(42, c.getAmount());
    }

    @Test
    void constructor_nullType_allowedAtModel() {
        Cargo c = new Cargo(null, 5);
        assertNull(c.getType());
    }

    @Test
    void constructor_zeroAmount() {
        Cargo c = new Cargo(GoodType.IRON, 0);
        assertEquals(0, c.getAmount());
    }

    @Test
    void setType_updatesType() {
        Cargo c = new Cargo(GoodType.WOOD, 10);
        c.setType(GoodType.STEEL);
        assertEquals(GoodType.STEEL, c.getType());
    }

    @Test
    void setType_toNull() {
        Cargo c = new Cargo(GoodType.IRON, 5);
        c.setType(null);
        assertNull(c.getType());
    }

    @Test
    void setAmount_updatesAmount() {
        Cargo c = new Cargo(GoodType.PASSENGERS, 10);
        c.setAmount(99);
        assertEquals(99, c.getAmount());
    }

    @Test
    void setAmount_toZero() {
        Cargo c = new Cargo(GoodType.STEEL, 50);
        c.setAmount(0);
        assertEquals(0, c.getAmount());
    }

    @Test
    void allGoodTypes_storeCorrectly() {
        for (GoodType type : GoodType.values()) {
            Cargo c = new Cargo(type, 1);
            assertEquals(type, c.getType());
        }
    }
}
