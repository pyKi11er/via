package com.via.model.economy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConsumptionRule implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<ConsumptionItem> inputs;

    public ConsumptionRule(List<ConsumptionItem> inputs) {
        this.inputs = new ArrayList<>(inputs);
    }

    public List<ConsumptionItem> getInputs() {
        return inputs;
    }
}
