package com.via.model.economy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProductionRule implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<ProductionItem> outputs;
    public ProductionRule(List<ProductionItem> outputs) {
        this.outputs = new ArrayList<>(outputs);
    }
    public List<ProductionItem> getOutputs() {
        return outputs;
    }
}
