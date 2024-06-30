/**
 * Mars Simulation Project
 * Exercise.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function;

import java.util.Iterator;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingException;
import com.mars_sim.core.structure.building.FunctionSpec;

/**
 * The Exercise class is a building function for exercise.
 */
public class Exercise extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private int exercisers;
	private int exerciserCapacity;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @param spec Define the details of the Function
	 * @throws BuildingException if error in constructing function.
	 */
	public Exercise(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.EXERCISE, spec, building);

		this.exerciserCapacity = spec.getCapacity();
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {

		// Demand is one exerciser capacity for every four inhabitants.
		double demand = settlement.getNumCitizens() / 4D;

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildingSet(FunctionType.EXERCISE).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Exercise exerciseFunction = (Exercise) building.getFunction(FunctionType.EXERCISE);
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += exerciseFunction.exerciserCapacity * wearModifier;
			}
		}

		double valueExerciser = demand / (supply + 1D);

		double exerciserCapacity = buildingConfig.getFunctionSpec(buildingName, FunctionType.EXERCISE).getCapacity();

		return exerciserCapacity * valueExerciser;
	}

	/**
	 * Gets the number of people who can use the exercise facility at once.
	 * 
	 * @return number of people.
	 */
	public int getExerciserCapacity() {
		return exerciserCapacity;
	}

	/**
	 * Gets the current number of people using the exercise facility.
	 * 
	 * @return number of people.
	 */
	public int getNumExercisers() {
		return exercisers;
	}

	/**
	 * Adds a person to the exercise facility.
	 * 
	 * @throws BuildingException if person would exceed exercise facility capacity.
	 */
	public void addExerciser() {
		exercisers++;
		if (exercisers > exerciserCapacity) {
			exercisers = exerciserCapacity;
			throw new IllegalStateException("Exercise facility in use.");
		}
	}

	/**
	 * Removes a person from the exercise facility.
	 * 
	 * @throws BuildingException if nobody is using the exercise facility.
	 */
	public void removeExerciser() {
		exercisers--;
		if (exercisers < 0) {
			exercisers = 0;
			throw new IllegalStateException("Exercise facility empty.");
		}
	}

	@Override
	public double getMaintenanceTime() {
		return exerciserCapacity * 2.5;
	}
}
