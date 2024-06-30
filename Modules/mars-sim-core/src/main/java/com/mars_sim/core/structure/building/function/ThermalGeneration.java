/*
 * Mars Simulation Project
 * ThermalGeneration.java
 * @date 2022-06-17
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.function;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingException;
import com.mars_sim.core.structure.building.FunctionSpec;
import com.mars_sim.core.structure.building.SourceSpec;
import com.mars_sim.core.time.ClockPulse;

/**
 * The ThermalGeneration class handles how the buildings of a settlement
 * generate and control temperature by heating .
 */
public class ThermalGeneration extends Function {

	/** default serial  id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private double heatGeneratedCache;
	
	private double powerGeneratedCache;

	private Heating heating;
	
	private List<HeatSource> heatSources;
	
	/**
	 * Constructor
	 */
	public ThermalGeneration(Building building, FunctionSpec spec) {
		// Call Function constructor.
		super(FunctionType.THERMAL_GENERATION, spec, building);
		
//		double area = building.getFloorArea();
		double areaFactor = 1;
		
		// For hallway and tunnel, the length is not known.
		// Thus, the capacity and consumption-rate needs to be moderated by its final area
//		if (building.getCategory() == BuildingCategory.HALLWAY) {
//			areaFactor = area / 4.5;
//		}
		
		heating = new Heating(building, spec);

		// Determine heat sources.
		heatSources = new ArrayList<>();
		
		for (SourceSpec sourceSpec : buildingConfig.getHeatSources(building.getBuildingType())) {
			double heat = sourceSpec.getCapacity();
			HeatSource heatSource = null;
			HeatSourceType sourceType = HeatSourceType.valueOf(sourceSpec.getType().toUpperCase().replace(" ", "_"));
			
			switch (sourceType) {
			case ELECTRIC_HEATING:
				heatSource = new ElectricHeatSource(heat * areaFactor);				
				break;

			case SOLAR_HEATING:
				heatSource = new SolarHeatingSource(building, heat * areaFactor);
				break;
				
			case FUEL_HEATING:
				boolean toggle = Boolean.parseBoolean(sourceSpec.getAttribute(SourceSpec.TOGGLE));
				String fuelType = sourceSpec.getAttribute(SourceSpec.FUEL_TYPE);
				double consumptionSpeed = Double.parseDouble(sourceSpec.getAttribute(SourceSpec.CONSUMPTION_RATE));
				heatSource = new FuelHeatSource(building, heat, toggle, fuelType, consumptionSpeed);
				break;
				
			default:
				throw new IllegalArgumentException("Do not know heat source type :" + sourceSpec.getType());
			}
			heatSources.add(heatSource);
		}
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding true if adding a new building.
	 * @param settlement the settlement.
	 * @return value) (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName,
			boolean newBuilding, Settlement settlement) {

		double demand = settlement.getThermalSystem().getRequiredHeat();
		double supply = 0D;
		boolean removedBuilding = false;

		for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.THERMAL_GENERATION)) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += getHeatSourceSupply(building.getThermalGeneration().heatSources) * wearModifier;
			}
		}

		double existingHeatValue = demand / (supply + 1D);

		double heatSupply = buildingConfig.getHeatSources(buildingName).stream()
								.mapToDouble(SourceSpec::getCapacity).sum();

		return heatSupply * existingHeatValue;
	}

	/**
	 * Gets the supply value of a list of heat sources.
	 * 
	 * @param heatSources list of heat sources.
	 * @param settlement the settlement.
	 * @return supply value.
	 * @throws Exception if error determining supply value.
	 */
	private static double getHeatSourceSupply(List<HeatSource> heatSources) {
		double result = 0D;

		for (HeatSource source : heatSources) {				
			result += source.getMaxHeat();
		}

		return result;
	}

	/**
	 * Gets the total amount of heat that this building is capable of producing (regardless malfunctions).
	 * 
	 * @return heat generated in kW (heat flow rate)
	 */
	public double getHeatGenerationCapacity() {
		double result = 0D;
		
		for (HeatSource source : heatSources) {
			result += source.getMaxHeat();
		}
		return result;
	}

	/**
	 * Gets the total amount of heat that this building is CURRENTLY producing.
	 * 
	 * @return heat generated in kW (heat flow rate)
	 */
	public double getGeneratedHeat() {
		return heatGeneratedCache;
	}

	/**
	 * Gets the total amount of power that this building is CURRENTLY producing.
	 * 
	 * @return power generated in kW ()
	 */
	public double getGeneratedPower() {
		return powerGeneratedCache; 
	}

	/**
	 * Calculates the total amount of heat that this building is CURRENTLY producing
	 * and also the power required to generate the heat.
	 * 
	 * @return heat generated in kW
	 */
	private double calculateGeneratedHeat(double time) {

		double heatGen = 0D;
	
		double percentageHeat = building.getHeatMode().getPercentage();
		
		for (HeatSource heatSource : heatSources) {
			heatSource.setPercentagePower(percentageHeat);
	    	heatSource.setTime(time);
			heatGen += heatSource.getCurrentHeat(building);
		}
			
		return heatGen;
	}


	/**
	 * Calculates the total amount of power that this building is CURRENTLY producing from heat sources.
	 * 
	 * @return power generated in kW
	 */
	private double calculateGeneratedPower() {

		double result = 0D;
		HeatMode heatMode = building.getHeatMode();
//		if (heatMode != HeatMode.FULL_HEAT) {
			boolean sufficientPower = building.getSettlement().getPowerGrid().isSufficientPower();
			
			// Calculate the unused
			double sparePercentage = 100 - heatMode.getPercentage();
			for (HeatSource heatSource : heatSources) {
			    if (heatSource.getType() == HeatSourceType.SOLAR_HEATING) {
			    	heatSource.setPercentagePower(sparePercentage);
			    	result += heatSource.getCurrentPower(getBuilding());
			    }
			   
			    else if (heatSource.getType() == HeatSourceType.FUEL_HEATING) {
			    	 // if there's not enough electrical power
				    if (!sufficientPower) {
				    	heatSource.setPercentagePower(sparePercentage);
				    	// Note: could be cheating if the mechanism of conversion is NOT properly defined
				    	// Convert heat to electricity
				    	result += heatSource.getCurrentPower(getBuilding());
				    }
			    }	
			}
//		}

		return result;
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			heating.timePassing(pulse);

			double heatGenerated = calculateGeneratedHeat(pulse.getElapsed());		
				// Note: could be cheating if the mechanism of conversion is NOT properly defined
		    	// Convert heat to electricity to help out
			double powerGenerated = calculateGeneratedPower();

			// Need to update this cache value in Heating continuously
			building.setHeatGenerated(heatGenerated);

			heatGeneratedCache = heatGenerated;
			powerGeneratedCache = powerGenerated;
		
			// Future: set new efficiency. Needs a new method in HeatSource updateEffeciency 
		}
		return valid;
	}


	public Heating getHeating() {
		return heating;
	}

	/**
	 * Gets a set of malfunction scopes.
	 */
	@Override
	public Set<String> getMalfunctionScopeStrings() {
		Set<String> set = new HashSet<>();
		String n = getFunctionType().getName();
		set.add(n);
		
		for (int x = 0; x < heatSources.size(); x++) {
			set.add(heatSources.get(x).getType().getName());
		}

		return set;
	}

	/**
	 * Gets the heat sources for the building.
	 * 
	 * @return list of heat sources.
	 */
	public List<HeatSource> getHeatSources() {
		return new ArrayList<>(heatSources);
	}

    @Override
    public double getMaintenanceTime() {

        double result = 0D;

        Iterator<HeatSource> i = heatSources.iterator();
        while (i.hasNext()) {
            result += i.next().getMaintenanceTime();
        }

        return result;
    }

	public double getFullPowerRequired() {
		return getElectricPowerRequired();
	}

	/**
	 * Gets the power required for generating electric heat.
	 * 
	 * @return
	 */
	public double getElectricPowerRequired() {
		HeatMode heatMode = building.getHeatMode();
		
		if (heatMode == HeatMode.OFFLINE || heatMode == HeatMode.HEAT_OFF)
			return 0;

		// add the need of electric heat
		double result = 0;

		for (HeatSource source : heatSources) {

	    	if (source.getType() == HeatSourceType.ELECTRIC_HEATING) {
	    		// Electric heating consumes electricity
	    		result += source.getCurrentHeat(building);
	    	}
		}
		
		// Note: Need to set this
//		building.setPowerRequiredForHeating(powerRequired);

		return result;
	}

	
	@Override
	public void destroy() {
		super.destroy();
		heating.destroy();
		heating = null;
		heatSources = null;
	}

}
