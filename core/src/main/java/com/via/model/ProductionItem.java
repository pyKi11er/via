package com.via.model;

public class ProductionItem {
    public final GoodType type;
    public final int amount;

    public ProductionItem(GoodType type, int amount) {
        this.type = type;
        this.amount = amount;
    }
}
