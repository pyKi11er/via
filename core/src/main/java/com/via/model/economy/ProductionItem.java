package com.via.model.economy;

import com.via.model.enums.GoodType;

/**
 * Represents a single output item in a {@link ProductionRule}.
 * <p>
 * Defines one type of good and the quantity produced per production cycle.
 */
public class ProductionItem {

    /** The type of good being produced */
    public final GoodType type;

    /** The amount of the good produced per cycle */
    public final int amount;

    /**
     * Constructs a new ProductionItem.
     *
     * @param type   the type of good to produce
     * @param amount the quantity of the good produced per production cycle
     */
    public ProductionItem(GoodType type, int amount) {
        this.type = type;
        this.amount = amount;
    }
}
