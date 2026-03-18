package com.via.model.economy;

import java.util.ArrayList;
import java.util.List;

public class ConsumptionRule {
    private final List<ConsumptionItem> inputs;

    public ConsumptionRule(List<ConsumptionItem> inputs) {
        this.inputs = new ArrayList<>(inputs);
    }

    public List<ConsumptionItem> getInputs() {
        return inputs;
    }
}
