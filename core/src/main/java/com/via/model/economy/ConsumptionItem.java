package com.via.model.economy;

import com.via.model.enums.GoodType;

import java.io.Serializable;

public class ConsumptionItem implements Serializable {
    private static final long serialVersionUID = 1L;
    public final GoodType type;
    public final int amount;

    public ConsumptionItem(GoodType type, int amount) {
        this.type = type;
        this.amount = amount;
    }
}
