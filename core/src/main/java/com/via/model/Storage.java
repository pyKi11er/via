package com.via.model;
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
        contents.put(type, getAmount(type) + amount);
    }
    public void removeContents(GoodType type) {
        contents.remove(type);
    }
    public int getAmount(GoodType type) {
        return contents.getOrDefault(type, 0);
    }
}
