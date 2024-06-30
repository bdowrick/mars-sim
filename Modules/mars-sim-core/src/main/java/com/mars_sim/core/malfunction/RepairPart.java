/*
 * Mars Simulation Project
 * RepairPart.java
 * @date 2021-11-16
 * @author Barry Evans
 */

package com.mars_sim.core.malfunction;

import java.io.Serializable;

/**
 * Private inner class for repair part information.
 */
class RepairPart implements Serializable {

	private static final long serialVersionUID = 1L;

	// Data members
	private String name;
	private int partID;
	private int number;
	private double repairProbability;

	/**
	 * Constructor.
	 *
	 * @param name        the name of the part.
	 * @param partID      the ID of the part.
	 * @param number      the maximum number of parts.
	 * @param repairProbability the probability of the part being needed.
	 */
	RepairPart(String name, int partID, int number, double repairProbability) {
		this.partID = partID;
		this.name = name;
		this.number = number;
		this.repairProbability = repairProbability;
	}

	protected String getName() {
		return name;
	}

	protected int getPartID() {
		return partID;
	}

	protected int getNumber() {
		return number;
	}

	protected double getRepairProbability() {
		return repairProbability;
	}
	
	protected void setRepairProbability(double value) {
		repairProbability = value;
	}
}
