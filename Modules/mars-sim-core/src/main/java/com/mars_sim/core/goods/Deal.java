/*
 * Mars Simulation Project
 * Deal.java
 * @date 2022-07-21
 * @author Barry Evans
 */
package com.mars_sim.core.goods;

import java.util.Map;

import com.mars_sim.core.structure.Settlement;

/**
 * Represents a potential deal with another Settlement.
 */
public class Deal implements Comparable<Deal> {
	private Settlement buyer;
    private Shipment buyingLoad;
    private Shipment sellingLoad;
    private double tradeCost;
	private double profit;


	Deal(Settlement buyer, Shipment sellLoad, Shipment buyLoad, double cost) {
        this.buyer = buyer;
        this.sellingLoad = sellLoad;
        this.buyingLoad = buyLoad;
        this.tradeCost = cost;

        // Profit is the money earn from the sell minus the money used in the return buy plus the delivery cost
        this.profit = sellLoad.getCostValue() - (buyLoad.getCostValue() + cost);
    }

    public Settlement getBuyer() {
        return buyer;
    }

    public double getTradeCost() {
        return tradeCost;
    }

    public double getProfit() {
        return profit;
    }

    public double getBuyingRevenue() {
        return buyingLoad.getCostValue();
    }

    public Map<Good, Integer> getBuyingLoad() {
        return buyingLoad.getLoad();
    }

    public double getSellingRevenue() {
        return sellingLoad.getCostValue();
    }

    public Map<Good, Integer> getSellingLoad() {
        return sellingLoad.getLoad();
    }

    /**
     * Order Deal according to increasing profit
     */
    @Override
    public int compareTo(Deal other) {
        return Double.compare(profit, other.profit);
    }
}