package com.via.model.economy;

import java.io.Serializable;
import com.via.model.enums.GoodType;

/**
 * Represents a single item in a {@link ConsumptionRule}.
 * <p>
 * Defines one type of good and the quantity consumed per production cycle.
 */
public class ConsumptionItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The type of good being consumed */
    public final GoodType type;

    /** The amount of the good consumed per cycle */
    public final int amount;

    /**
     * Constructs a new ConsumptionItem.
     *
     * @param type   the type of good to consume
     * @param amount the quantity of the good consumed per production cycle
     */
    public ConsumptionItem(GoodType type, int amount) {
        this.type = type;
        this.amount = amount;
    }
}
