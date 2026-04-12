package com.via.model.economy;

import com.via.model.enums.GoodType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StorageTest {

    private Storage storage;

    @BeforeEach
    void setUp() {
        storage = new Storage();
    }

    @Test
    void getAmount_missingType_returnsZero() {
        assertEquals(0, storage.getAmount(GoodType.WOOD));
    }

    @Test
    void addContents_increasesAmount() {
        storage.addContents(GoodType.WOOD, 5);
        assertEquals(5, storage.getAmount(GoodType.WOOD));
    }

    @Test
    void addContents_accumulates() {
        storage.addContents(GoodType.IRON, 3);
        storage.addContents(GoodType.IRON, 7);
        assertEquals(10, storage.getAmount(GoodType.IRON));
    }

    @Test
    void addContents_nullType_throws() {
        assertThrows(IllegalArgumentException.class, () -> storage.addContents(null, 1));
    }

    @Test
    void addContents_nullAmount_throws() {
        assertThrows(IllegalArgumentException.class, () -> storage.addContents(GoodType.WOOD, null));
    }

    @Test
    void addContents_negativeAmount_throws() {
        assertThrows(IllegalArgumentException.class, () -> storage.addContents(GoodType.WOOD, -1));
    }

    @Test
    void removeAmount_decreasesAmount() {
        storage.addContents(GoodType.STEEL, 10);
        storage.removeAmount(GoodType.STEEL, 4);
        assertEquals(6, storage.getAmount(GoodType.STEEL));
    }

    @Test
    void removeAmount_toZero_removesKey() {
        storage.addContents(GoodType.PASSENGERS, 5);
        storage.removeAmount(GoodType.PASSENGERS, 5);
        assertEquals(0, storage.getAmount(GoodType.PASSENGERS));
        assertFalse(storage.getContents().containsKey(GoodType.PASSENGERS));
    }

    @Test
    void removeAmount_tooMuch_throws() {
        storage.addContents(GoodType.WOOD, 3);
        assertThrows(IllegalArgumentException.class, () -> storage.removeAmount(GoodType.WOOD, 10));
    }

    @Test
    void removeAmount_nullType_throws() {
        assertThrows(IllegalArgumentException.class, () -> storage.removeAmount(null, 1));
    }

    @Test
    void removeAmount_negativeAmount_throws() {
        assertThrows(IllegalArgumentException.class, () -> storage.removeAmount(GoodType.WOOD, -1));
    }

    @Test
    void removeContents_removesKey() {
        storage.addContents(GoodType.IRON, 5);
        storage.removeContents(GoodType.IRON);
        assertEquals(0, storage.getAmount(GoodType.IRON));
    }
}
