package com.via.model.economy;

import com.via.model.enums.GoodType;

public class ConsumptionItem {
    public final GoodType type;
    public final int amount;

    public ConsumptionItem(GoodType type, int amount) {
        this.type = type;
        this.amount = amount;
    }
}
