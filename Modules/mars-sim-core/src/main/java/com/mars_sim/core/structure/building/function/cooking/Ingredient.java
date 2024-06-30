/*
 * Mars Simulation Project
 * Ingredient.java
 * @date 2022-08-30
 * @author Manny Kung
 */

package com.mars_sim.core.structure.building.function.cooking;

import java.io.Serializable;

import com.mars_sim.core.resource.ResourceUtil;

public class Ingredient implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private boolean isItAvailable;

	private int id;

	private double proportion;
	private double ingredientDryMass;

	private int amountResource;

	Ingredient(int id, int amountResourceID, double proportion) {
		this.id = id;
		this.amountResource = amountResourceID;
		this.proportion = proportion;
	}

	public String getName() {
		return ResourceUtil.findAmountResourceName(amountResource);
	}

	public int getAmountResourceID() {
		return amountResource;
	}

	public double getProportion() {
		return proportion;
	}

	public int getID() {
		return id;
	}

	public boolean getIsItAvailable() {
		return isItAvailable;
	}

	public void setIsItAvailable(boolean value) {
		isItAvailable = value;
	}

	public void setDryMass(double ingredientDryWeight) {
		this.ingredientDryMass = ingredientDryWeight;
	}

	public double getDryMass() {
		return ingredientDryMass;
	}

	public String toString() {
		return getName();
	}

}
