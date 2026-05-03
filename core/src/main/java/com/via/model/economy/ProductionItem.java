package com.via.model.economy;

import com.via.model.enums.GoodType;

import java.io.Serializable;

public class ProductionItem implements Serializable {
    private static final long serialVersionUID = 1L;
    public final GoodType type;
    public final int amount;

    public ProductionItem(GoodType type, int amount) {
        this.type = type;
        this.amount = amount;
    }
}
