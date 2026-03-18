package com.via.model.vehicle;

import com.via.model.enums.GoodType;

public class Cargo {
    private GoodType type;
    private int amount;

    public Cargo(GoodType type, int amount) {
        this.type = type;
        this.amount = amount;
    }
    public GoodType getType() {
        return type;
    }
    public void setType(GoodType type) {
        this.type = type;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
