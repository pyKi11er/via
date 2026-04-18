package com.via.model.economy;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the input requirements for a production cycle in a Facility.
 * <p>
 * A ConsumptionRule specifies all the goods (and their quantities) that must
 * be consumed to produce output according to a {@link ProductionRule}.
 */
public class ConsumptionRule {

    /** List of goods and quantities required as inputs */
    private final List<ConsumptionItem> inputs;

    /**
     * Constructs a new ConsumptionRule.
     *
     * @param inputs list of {@link ConsumptionItem} defining required inputs
     */
    public ConsumptionRule(List<ConsumptionItem> inputs) {
        this.inputs = new ArrayList<>(inputs);
    }

    /**
     * Returns the list of input items required for production.
     *
     * @return an unmodifiable view of the input items (defensive copy was made at construction)
     */
    public List<ConsumptionItem> getInputs() {
        return inputs;
    }
}
