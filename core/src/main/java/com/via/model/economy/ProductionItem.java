package com.via.model.economy;

import com.via.model.enums.GoodType;

public class ProductionItem {
    public final GoodType type;
    public final int amount;

    public ProductionItem(GoodType type, int amount) {
        this.type = type;
        this.amount = amount;
    }
}
