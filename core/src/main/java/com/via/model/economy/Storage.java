package com.via.model.economy;
import com.via.model.enums.GoodType;

import java.util.HashMap;
import java.util.Map;
public class Storage {
    private Map<GoodType, Integer> contents;

    public Storage() {
        contents = new HashMap<>();
    }
    public Map<GoodType, Integer> getContents() {
        return contents;
    }
    public void addContents(GoodType type, Integer amount) {
        if (type == null) throw new IllegalArgumentException("type must not be null");
        if (amount == null) throw new IllegalArgumentException("amount must not be null");
        if (amount < 0) throw new IllegalArgumentException("amount must not be negative");
        contents.put(type, getAmount(type) + amount);
    }
    public void removeContents(GoodType type) {
        contents.remove(type);
    }
    public int getAmount(GoodType type) {
        return contents.getOrDefault(type, 0);
    }

    public void removeAmount(GoodType type, int amount) {
        if (type == null) throw new IllegalArgumentException("type must not be null");
        if (amount < 0) throw new IllegalArgumentException("amount must not be negative");
        int current = getAmount(type);
        int next = current - amount;
        if (next < 0) throw new IllegalArgumentException("not enough " + type + " in storage");
        if (next == 0) {
            contents.remove(type);
        } else {
            contents.put(type, next);
        }
    }
}
