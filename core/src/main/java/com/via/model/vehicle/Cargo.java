package com.via.model.vehicle;

import java.io.Serializable;
import com.via.model.enums.GoodType;

/**
 * Represents a cargo load consisting of a specific good type and quantity.
 * Used by vehicles to transport goods between stations.
 */
public class Cargo implements Serializable {
    private static final long serialVersionUID = 1L;
    private GoodType type;
    private int amount;

    /**
     * Constructs a new Cargo with the specified type and amount.
     *
     * @param type   the type of good being transported
     * @param amount the quantity of the good (must be non-negative)
     */
    public Cargo(GoodType type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    /**
     * Returns the type of good in this cargo.
     *
     * @return the good type
     */
    public GoodType getType() {
        return type;
    }

    /**
     * Sets the type of good in this cargo.
     *
     * @param type the new good type
     */
    public void setType(GoodType type) {
        this.type = type;
    }

    /**
     * Returns the quantity of goods in this cargo.
     *
     * @return the amount of goods
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the quantity of goods in this cargo.
     *
     * @param amount the new amount (must be non-negative)
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
