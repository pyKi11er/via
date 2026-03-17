package com.via.model;

public class ConsumptionItem {
    public final GoodType type;
    public final int amount;

    public ConsumptionItem(GoodType type, int amount) {
        this.type = type;
        this.amount = amount;
    }
}
