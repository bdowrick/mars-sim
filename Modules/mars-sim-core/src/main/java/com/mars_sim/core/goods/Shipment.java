/*
 * Mars Simulation Project
 * Shipment.java
 * @date 2022-07-30
 * @author Barry Evans
 */
package com.mars_sim.core.goods;

import java.util.Map;

/**
 * Represents a shipment on Goods with an associated revenue value.
 */
public class Shipment {

    private Map<Good, Integer> load;
    private double costValue;
    
    Shipment(Map<Good, Integer> load, double costValue) {
        this.load = load;
        this.costValue = costValue;
    }

    public double getCostValue() {
        return costValue;
    }

    public Map<Good, Integer> getLoad() {
        return load;
    }
}
