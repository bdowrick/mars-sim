/*
 * Mars Simulation Project
 * PowerGrid.java
 * @date 2024-06-28
 * @author Scott Davis
 */
package com.mars_sim.core.structure;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingException;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.AdjustablePowerSource;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.PowerMode;
import com.mars_sim.core.structure.building.function.PowerSource;
import com.mars_sim.core.structure.building.function.PowerSourceType;
import com.mars_sim.core.structure.building.function.PowerStorage;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * This class is a settlement's building power grid.
 */
public class PowerGrid implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(PowerGrid.class.getName());

	private static final double R_LOAD = 1000D; // assume constant load resistance

	private static final double ROLLING_FACTOR = 1.1D; 
	
	private static final double PERC_AVG_VOLT_DROP = 98D;

	public static final double HOURS_PER_MILLISOL = MarsTime.HOURS_PER_MILLISOL; 

	private boolean sufficientPower;
	
	private double degradationRatePerSol = .0004D;
	private double systemEfficiency = 1D;
	private double powerGenerated;
	private double totalEnergyStored;
	private double energyStorageCapacity;
	private double powerRequired;
	private double powerValue;

	private Settlement settlement;
	private BuildingManager manager;
	private PowerMode powerMode;

	/**
	 * Constructor.
	 */
	public PowerGrid(Settlement settlement) {
		this.settlement = settlement;
		manager = settlement.getBuildingManager();
		powerMode = PowerMode.FULL_POWER;
		powerGenerated = 0D;
		totalEnergyStored = 0D;
		energyStorageCapacity = 0D;
		powerRequired = 0D;
		sufficientPower = true;
	}

	/**
	 * Gets the power grid mode.
	 * 
	 * @return power grid mode
	 */
	public PowerMode getPowerMode() {
		return powerMode;
	}

	/**
	 * Sets the power grid power mode.
	 * 
	 * @param newPowerMode the new power grid power mode.
	 */
	public void setPowerMode(PowerMode newPowerMode) {
		if (powerMode != newPowerMode) {
			powerMode = newPowerMode;
			settlement.fireUnitUpdate(UnitEventType.POWER_MODE_EVENT);
		}
	}

	/**
	 * Gets the generated power in the grid.
	 * 
	 * @return power in kW
	 */
	public double getGeneratedPower() {
		return powerGenerated;
	}

	/**
	 * Sets the generated power in the grid.
	 * 
	 * @param newGeneratedPower the new generated power (kW).
	 */
	private void setGeneratedPower(double newGeneratedPower) {
		double p = Math.round(newGeneratedPower*1000.0)/1000.0;
		if (powerGenerated != p) {
			powerGenerated = p;
			settlement.fireUnitUpdate(UnitEventType.GENERATED_POWER_EVENT);
		}
	}

	/**
	 * Gets the stored energy in the grid.
	 * 
	 * @return stored energy in kWh.
	 */
	public double getStoredEnergy() {
		return totalEnergyStored;
	}

	public String getDisplayStoredEnergy() {
		double stored = totalEnergyStored;
		if (stored < 0D || Double.isNaN(stored) || Double.isInfinite(stored))
			return "";
		
		double percent = stored / energyStorageCapacity * 100;
		
		StringBuilder sb = new StringBuilder();
		sb.append(Math.round(stored *10.0)/10.0)
		.append(" (")
		.append(Math.round(percent *10.0)/10.0)
		.append(" %)");
		
		return sb.toString();
	}
	
	
	/**
	 * Sets the stored energy in the grid.
	 * 
	 * @param newEnergyStored the new stored energy (kWh).
	 */
	public void setStoredEnergy(double newEnergyStored) {
		if (totalEnergyStored != newEnergyStored) {
			totalEnergyStored = newEnergyStored;
			settlement.fireUnitUpdate(UnitEventType.STORED_ENERGY_EVENT);
		}
	}

	/**
	 * Gets the stored energy capacity in the grid.
	 * 
	 * @return stored energy capacity in kWh.
	 */
	public double getStoredEnergyCapacity() {
		return energyStorageCapacity;
	}

	/**
	 * Sets the total stored energy capacity in the grid.
	 * 
	 * @param newCap the new stored energy capacity (kWh).
	 */
	public void setStoredEnergyCapacity(double newCap) {
		if (energyStorageCapacity != newCap) {
			energyStorageCapacity = newCap;
			settlement.fireUnitUpdate(UnitEventType.STORED_ENERGY_CAPACITY_EVENT);
		}
	} 

	/**
	 * Gets the power required from the grid.
	 * 
	 * @return power in kW
	 */
	public double getRequiredPower() {
		return powerRequired;
	}

	/**
	 * Sets the required power in the grid.
	 * 
	 * @param newRequiredPower the new required power (kW).
	 */
	private void setRequiredPower(double newRequiredPower) {
		if (powerRequired != newRequiredPower) {
			powerRequired = newRequiredPower;
			settlement.fireUnitUpdate(UnitEventType.REQUIRED_POWER_EVENT);
		}
	}

	/**
	 * Checks if there is enough power in the grid for all buildings to be set to
	 * full power.
	 * 
	 * @return true if sufficient power
	 */
	public boolean isSufficientPower() {
		return sufficientPower;
	}

	/**
	 * Time passing for power grid.
	 * 
	 * @param time amount of time passing (in millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {

		logger.log(settlement, Level.FINEST, 0, Msg.getString("PowerGrid.log.settlementPowerSituation", settlement.getName()));

		// update the total power generated in the grid.
		updateTotalPowerGenerated();

		// Determine total power required in the grid.
		updateTotalRequiredPower();

		// Update overall grid efficiency.
		updateEfficiency(pulse.getElapsed());

		// Update the power flow.
		double neededPower = powerRequired * ROLLING_FACTOR - powerGenerated;
		sufficientPower = (neededPower < 0);
		if (neededPower < 0) {
			handleExcessPower(pulse.getElapsed(), neededPower);
		}
		else {
			handleLackOfPower(pulse.getElapsed(), neededPower);
		}

		// Update the total power storage capacity in the grid.
		updateTotalEnergyStorageCapacity();

		// Update the total power stored in the grid.
		updateTotalStoredEnergy();

		// Update power value.
		determinePowerValue();
		
		return true;
	}

	/**
	 * Updates the system efficiency factor.
	 * 
	 * @param time
	 */
	private void updateEfficiency(double time) {
		double dFactor = degradationRatePerSol * time / 1000D;
		systemEfficiency = systemEfficiency - systemEfficiency * dFactor;
	}


	/**
	 * Calculates the amount of electrical power generated.
	 * 
	 * @param increaseLoad
	 * @param neededPower
	 * @return net power change in kW
	 */
	public double stepUpDownPower(boolean increaseLoad, double neededPower) {
		double netPower = 0D;

		for(Building b : manager.getBuildingSet(FunctionType.POWER_GENERATION)) {
			for(PowerSource powerSource : b.getPowerGeneration().getPowerSources()) {
				double previous = powerSource.getCurrentPower(b);
				if (powerSource instanceof AdjustablePowerSource fps) {
					if (increaseLoad) {
						fps.increaseLoadCapacity();
					}
					else {
						fps.decreaseLoadCapacity();
					}

					double net = powerSource.getCurrentPower(b) - previous;
					if (Double.isFinite(net)) {
						netPower += net;
						neededPower -= netPower;
						if (neededPower <= 0) {
							return netPower;
						}
					}
				}
			}
		}
		return netPower;
	}
	
		
	/**
	 * Handles excess power.
	 * 
	 * @param time 
	 * @param neededPower
	 */
	private void handleExcessPower(double time, double neededPower) {
		double excess = -neededPower;

		sufficientPower = true;
		
		Set<Building> buildings = manager.getBuildingSet();

		// A. Turn on low power in inhabitable buildings
		// building until required power reduction is met.
		double netPower1 = adjustPowerLevel(false, excess, buildings, 
				true, PowerMode.NO_POWER, PowerMode.LOW_POWER);
		
		excess -= netPower1;
		if (excess < 0) {
			// Update the total generated power
			setGeneratedPower(powerGenerated + netPower1);
			sufficientPower = false;
			return;
		}
		

		// B. Turn on full power mode on inhabitable buildings
		
		// If power needs are still not met, turn on full power in each inhabitable
		// building until required power reduction is met.
		double netPower2 = adjustPowerLevel(false, neededPower, buildings, 
				true, PowerMode.LOW_POWER, PowerMode.FULL_POWER);
		
		excess -= netPower2;
		if (excess < 0) {
			// Update the total generated power
			setGeneratedPower(powerGenerated + netPower2);
			sufficientPower = false;
			return;
		}
		
		// C. Turn off emergency power generators 
		
		// C1. Turn off methane power generators 
		double methanePower = adjustPowerLevelFunctionType(false, excess, buildings, 
				FunctionType.POWER_GENERATION, PowerSourceType.FUEL_POWER);
		
		excess -= methanePower;
		if (excess < 0) {
			// Update the total generated power
			setGeneratedPower(powerGenerated + methanePower);
			sufficientPower = false;
			return;
		}
		
		// D. Turn on low power mode on non-inhabitable buildings
		
		// If power needs are still not met, turn on low power in each non-inhabitable
		// building until required power reduction is met.
		double netPower3 = adjustPowerLevel(false, excess, buildings, 
				false, PowerMode.NO_POWER, PowerMode.LOW_POWER);
		
		excess -= netPower3;
		if (excess < 0) {
			// Update the total generated power
			setGeneratedPower(powerGenerated + netPower3);
			sufficientPower = false;
			return;
		}
		

		// E. Turn on full power mode on non-inhabitable buildings
		
		// If power needs are still not met, turn on full power in each non-inhabitable
		// building until required power reduction is met.
		double netPower4 = adjustPowerLevel(false, neededPower, buildings, 
				false, PowerMode.LOW_POWER, PowerMode.FULL_POWER);
		
		excess -= netPower4;
		if (excess < 0) {
			// Update the total generated power
			setGeneratedPower(powerGenerated + netPower4);
			sufficientPower = false;
			return;
		}
		
		// F. Step down the capacity of the fission power plant by a small percent
		int rand = RandomUtil.getRandomInt(9);
		if (rand == 9) {
			double netPower02 = stepUpDownPower(false, excess);
			excess -= netPower02;
			if (excess < 0) {
				// Update the total generated power
				setGeneratedPower(powerGenerated + netPower02);
				sufficientPower = false;
				return;
			}
		}
		
		// Turn on full power mode on non-inhabitable building
		// Store excess power in power storage buildings.

		double timeHr = time * HOURS_PER_MILLISOL;
		double excessEnergy = excess * timeHr * systemEfficiency;
		double unableToStoreEnergy = storeExcessPower(excessEnergy, time);
		double excessPower = unableToStoreEnergy / timeHr / systemEfficiency;

		if (excess < 0) {
			// Update the total generated power
			setGeneratedPower(powerGenerated + excessPower);
			sufficientPower = false;
			return;
		}
		
		// Step down the capacity of the fission power plant by a small percent
		double netPower02 = stepUpDownPower(false, excess);
		excess -= netPower02;
		if (excess < 0) {
			// Update the total generated power
			setGeneratedPower(powerGenerated + netPower02);
			sufficientPower = false;
			return;
		}
	}

	/**
	 * Generates more power.
	 * 
	 * @param time
	 * @param neededPower
	 */
	private void handleLackOfPower(double time, double neededPower) {

		// insufficient power produced, need to pull energy from batteries to meet the
		// demand
		sufficientPower = false;
		
		// Increases the load capacity of fission reactors if available
		double fissionPower0 = stepUpDownPower(true, neededPower);

		neededPower -= fissionPower0;
		// if the fission reactors produces more than enough
		if (neededPower < 0) {
			// Update the total generated power with contribution from increased power load capacity of fission reactors
			setGeneratedPower(powerGenerated + fissionPower0);
			sufficientPower = true;
			return;
		}

		double timeInHour = time * HOURS_PER_MILLISOL; 
		
		// Assume the gauge of the cable is uniformly low, as represented by percentAverageVoltageDrop
		// TODO: account for the distance of the separation between endpoints
		double neededEnergy = neededPower * timeInHour / PERC_AVG_VOLT_DROP * 100D;

		// Assume the energy flow is instantaneous and
		// subtract powerHr from the battery reserve
		double retrieved = retrieveStoredEnergy(neededEnergy, time);

		double batteryPower = retrieved / timeInHour;
		
		neededPower -= batteryPower;
		// if the grid batteries has more than enough
		if (neededPower < 0) {
			// Update the total generated power with contribution from batteries
			setGeneratedPower(powerGenerated + batteryPower);
			sufficientPower = true;
			return;
		}

		Set<Building> buildings = manager.getBuildingSet();
		
		// Turn on emergency power generators to supplement power
		
		// If still not having sufficient power,
		// turn on methane generators to low power mode if available
		double methanePower0 = adjustPowerLevelFunctionType(true, neededPower, buildings, 
				FunctionType.POWER_GENERATION, PowerSourceType.FUEL_POWER);

		neededPower -= methanePower0;		
		if (neededPower < 0) {
		// Update the total generated power with contribution from methane generators
		setGeneratedPower(powerGenerated + methanePower0);
			sufficientPower = true;
			return;
		}
		
		// Increases the load capacity of fission reactors if available
		double fissionPower1 = stepUpDownPower(true, neededPower);

		neededPower -= fissionPower1;
		// if the fission reactors produces more than enough
		if (neededPower < 0) {
			// Update the total generated power with contribution from increased power load capacity of fission reactors
			setGeneratedPower(powerGenerated + fissionPower1);
			sufficientPower = true;
			return;
		}
		
		// If still not having sufficient power, reduce power to some buildings

		// Reduce each non-inhabitable building's full power mode to low power until
		// required power reduction is met.
		double savedPower0 = adjustPowerLevel(true, neededPower, buildings, 
				false, PowerMode.FULL_POWER, PowerMode.LOW_POWER);
		
		neededPower -= savedPower0;
		if (neededPower < 0) {
			// Update the total generated power
			setGeneratedPower(powerGenerated + savedPower0);
			sufficientPower = true;
			return;
		}
		
		// If power needs are still not met, turn off the power in each
		// uninhabitable building until required power reduction is met.
		double savedPower1 = adjustPowerLevel(true, neededPower, buildings, 
				false, PowerMode.LOW_POWER, PowerMode.NO_POWER);
			
		neededPower -= savedPower1;
		if (neededPower < 0) {
			// Update the total generated power
			setGeneratedPower(powerGenerated + savedPower1);
			sufficientPower = true;
			return;
		}

		// If still not having sufficient power,
		// turn on methane generators to full power mode if available
		double methanePower1 = adjustPowerLevelFunctionType(true, neededPower, buildings, 
				FunctionType.POWER_GENERATION, PowerSourceType.FUEL_POWER);

		neededPower -= methanePower1;		
		if (neededPower < 0) {
		// Update the total generated power with contribution from methane generators
		setGeneratedPower(powerGenerated + methanePower1);
			sufficientPower = true;
			return;
		}
		
		
		// Increases the load capacity of fission reactors if available
		double fissionPower2 = stepUpDownPower(true, neededPower);

		neededPower -= fissionPower2;
		// if the fission reactors produces more than enough
		if (neededPower < 0) {
			// Update the total generated power with contribution from increased power load capacity of fission reactors
			setGeneratedPower(powerGenerated + fissionPower2);
			sufficientPower = true;
			return;
		}
		
		// If power needs are still not met, turn on the low power in each inhabitable
		// building until required power reduction is met.
		double savedPower2 = adjustPowerLevel(true, neededPower, buildings, 
				true, PowerMode.FULL_POWER, PowerMode.LOW_POWER);
		
		neededPower -= savedPower2;
		if (neededPower < 0) {
			// Update the total generated power
			setGeneratedPower(powerGenerated + savedPower2);
			sufficientPower = true;
		}
		
		// If power needs are still not met, turn off the power in each inhabitable
		// building until required power reduction is met.
		double savedPower3 = adjustPowerLevel(true, neededPower, buildings, 
				true, PowerMode.LOW_POWER, PowerMode.NO_POWER);
		
		neededPower -= savedPower3;
		if (neededPower < 0) {
			// Update the total generated power
			setGeneratedPower(powerGenerated + savedPower3);
			sufficientPower = true;
		}
	}
		
	/**
	 * Adjust the power level in inhabitable and non-inhabitable buildings.
	 * 
	 * @param gridLackPower the power grid has insufficient power
	 * @param neededPower
	 * @param buildings
	 * @param lifeSupport
	 * @param oldPowerMode
	 * @param newPowerMode
	 * @return
	 */
	private double adjustPowerLevel(boolean gridLackPower, double neededPower, Set<Building> buildings, 
			boolean lifeSupport, PowerMode oldPowerMode, PowerMode newPowerMode) {
		double netPower = 0;
	
		Iterator<Building> i = buildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			boolean life = building.hasFunction(FunctionType.LIFE_SUPPORT);
			PowerMode thisOldPM = building.getPowerMode();
			
			if (lifeSupport == life
//					&& (!gridLackPower || !canGenMoreThanLoad(building, newPowerMode))
					&& thisOldPM == oldPowerMode) {
						
				// For stepping down power
				if (oldPowerMode == PowerMode.FULL_POWER
					&& newPowerMode == PowerMode.LOW_POWER) {
					netPower += building.getFullPowerRequired()
							- building.getLowPowerRequired();
				}
				// For stepping down power
				else if (oldPowerMode == PowerMode.LOW_POWER
					&& newPowerMode == PowerMode.NO_POWER) {
					netPower += building.getLowPowerRequired();
				}
				// For stepping up power
				else if (oldPowerMode == PowerMode.LOW_POWER
					&& newPowerMode == PowerMode.FULL_POWER) {
					netPower += building.getFullPowerRequired()
							- building.getLowPowerRequired();
				}
				// For stepping up power
				else if (oldPowerMode == PowerMode.NO_POWER
					&& newPowerMode == PowerMode.LOW_POWER) {
					netPower += building.getLowPowerRequired();
				}
				else {
					continue;
				}
							
				neededPower -= netPower;
				if (neededPower > 0) {
					// Switch from one power mode to another
					building.setPowerMode(newPowerMode);
				}
				
				else {
					// When lacking power and needing to step up power level
					if (!gridLackPower) {
						// Switch from one power mode to another
						building.setPowerMode(newPowerMode);	
					}

					return netPower;
				}
			}
		}
		return netPower;
	}
	
	/**
	 * Adjust the power level in inhabitable and non-inhabitable buildings.
	 * 
	 * @param stepUp turning up power level
	 * @param neededPower
	 * @param buildings
	 * @param functionType
	 * @param powerSourceType
	 * @return
	 */
	private double adjustPowerLevelFunctionType(boolean stepUp, double neededPower, 
			Set<Building> buildings, FunctionType functionType,
			PowerSourceType powerSourceType) {
		double netPower = 0;
	
		Iterator<Building> i = buildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!building.hasFunction(functionType))
				continue;
			
			List<PowerSource> sources = building.getPowerGeneration().getPowerSources();
			if (sources.isEmpty())
				continue;
			
			Iterator<PowerSource> j = sources.iterator();
			while (j.hasNext()) {
				PowerSource source = j.next();			
				if (source.getType() != powerSourceType)
					continue;
				
				PowerMode oldPowerMode = building.getPowerMode();
				PowerMode newPowerMode = null;
				if (stepUp && oldPowerMode == PowerMode.NO_POWER) {
					netPower += building.getFullPowerRequired();
					newPowerMode = PowerMode.FULL_POWER;
				}
				else if (stepUp && oldPowerMode == PowerMode.LOW_POWER) {
					netPower += building.getFullPowerRequired()
						- building.getLowPowerRequired();
					newPowerMode = PowerMode.LOW_POWER;
				}
				else if (!stepUp && oldPowerMode == PowerMode.LOW_POWER) {
					netPower += building.getLowPowerRequired();	
					newPowerMode = PowerMode.NO_POWER;
				}
				else if (!stepUp && oldPowerMode == PowerMode.FULL_POWER) {
					netPower += building.getFullPowerRequired()
							- building.getLowPowerRequired();
					newPowerMode = PowerMode.LOW_POWER;
				}
				else {
					continue;
				}
				
				neededPower -= netPower;
				if (neededPower > 0) {
					building.setPowerMode(newPowerMode);
				}
				
				else {
					// In case of stepping up power level
					if (stepUp) {
						building.setPowerMode(newPowerMode);
					}
					return netPower;
				}
			}
		}
		return netPower;
	}
	
	/**
	 * Updates the total power generated in the grid.
	 * 
	 * @throws BuildingException if error determining total power generated.
	 */
	private void updateTotalPowerGenerated() {
		// Add the power generated by all power generation buildings.
		double power = manager.getBuildingSet(FunctionType.POWER_GENERATION).stream()
								.mapToDouble(b -> b.getPowerGeneration().getGeneratedPower())
								.sum();
		setGeneratedPower(power);
	}

	/**
	 * Updates the total energy stored in the grid.
	 * 
	 * @throws BuildingException if error determining total energy stored.
	 */
	private void updateTotalStoredEnergy() {
		double store = manager.getBuildingSet(FunctionType.POWER_STORAGE).stream()
								.mapToDouble(b -> b.getPowerStorage().getkWattHourStored())
								.sum();
		setStoredEnergy(store);
	}

	/**
	 * Updates the total power required in the grid.
	 * 
	 * @throws BuildingException if error determining total power required.
	 */
	private void updateTotalRequiredPower() {
		double power = 0D;
		// Gets all buildings, not just power producers
		Iterator<Building> iUsed = manager.getBuildingSet().iterator();
		while (iUsed.hasNext()) {
			Building building = iUsed.next();
			if (building.getPowerMode() == PowerMode.FULL_POWER) {
				power += building.getFullPowerRequired();
			}
			else if (building.getPowerMode() == PowerMode.LOW_POWER) {
				power += building.getLowPowerRequired();
			}
		}

		setRequiredPower(power);
	}

	/**
	 * Updates the total energy storage capacity in the grid.
	 * 
	 * @throws BuildingException if error determining total energy storage capacity.
	 */
	private void updateTotalEnergyStorageCapacity() {
		double capacity = manager.getBuildingSet(FunctionType.POWER_STORAGE).stream()
									.mapToDouble(b -> b.getPowerStorage().getCurrentMaxCapacity())
									.sum();
		setStoredEnergyCapacity(capacity);
	}

	/**
	 * Checks if building can generate more power than it uses in a given power mode.
	 *
	 * @param building the building
	 * @param mode     {@link PowerMode} the building's power mode to check.
	 * @return true if building supplies more power than it uses. throws
	 *         BuildingException if error in power generation.
	 */
	private boolean canGenMoreThanLoad(Building b, PowerMode mode) {
		double generated = 0D;
		if (b.hasFunction(FunctionType.POWER_GENERATION)) {
			// The power that it can generate at this moment
			// e.g. Solar power is dependent upon the sunlight
			// e.g. Wind power is dependent upon the wind speed
			generated = b.getPowerGeneration().getGeneratedPower();
		}

		double powerLoad = 0D;
		if (mode == PowerMode.FULL_POWER)
			powerLoad = b.getFullPowerRequired();
		else if (mode == PowerMode.LOW_POWER)
			powerLoad = b.getLowPowerRequired();

		return generated > powerLoad;
	}

	/**
	 * Stores any excess energy into the power grid via battery storage systems in buildings if possible.
	 * 
	 * @param excessEnergy excess grid energy (in kW hr).
	 * @return energy unable to store
	 */
	private double storeExcessPower(double excessEnergy, double time) {
		double excess = excessEnergy;
		Iterator<Building> i = manager.getBuildingSet(FunctionType.POWER_STORAGE).iterator();
		while (i.hasNext()) {
			PowerStorage storage = i.next().getPowerStorage();
			double stored = storage.getkWattHourStored();
			double max = storage.getCurrentMaxCapacity();
			double gap = max - stored;
			double onePercent = max * .01D;

			if (gap > onePercent && excess > 0) {
				// TODO: need to come up with a better battery model with charge capacity
				// parameters from
				// https://www.mathworks.com/help/physmod/elec/ref/genericbattery.html?requestedDomain=www.mathworks.com

				// Note: Tesla runs its batteries up to 4C charging rate
				// see https://teslamotorsclub.com/tmc/threads/limits-of-model-s-charging.36185/

				double accept = computeStorableEnergy(storage, excess, time);

				if (accept > 0 && accept <= excess) {

					// update the resultant energy stored in battery
					stored = stored + accept;
					// update excess energy
					excess = excess - accept;
					// update the energy stored in this battery
					storage.reconditionBattery(stored);
				}
			}
		}
		
		return excess;
	}

	/**
	 * Computes how much stored energy can be taken in during charging.
	 * Receives energy from the grid to charge up a single battery storage system.
	 * 
	 * @param storage PowerStorage
	 * @param excess  energy
	 * @param time    in millisols
	 * @return energy to be delivered
	 */
	public double computeStorableEnergy(PowerStorage storage, double excess, double time) {
		if (excess <= 0)
			return 0;
		
		double stored = storage.getkWattHourStored();
		double maxCap = storage.getCurrentMaxCapacity();
		
		if (stored >= maxCap)
			return 0;
		
		double needed = maxCap - stored;

		double vTerminal = storage.getTerminalVoltage();
		// Assume the internal resistance of the battery is constant
		double rInt = storage.getTotalResistance();

		double stateOfCharge = stored / maxCap;
		// Use fudge_factor to improve the charging but decreases 
		// when the battery is getting full
		double fudgeFactor = 5 * (1 - stateOfCharge);
		// The output voltage
		double vOut = vTerminal * R_LOAD / (R_LOAD + rInt);

		if (vOut <= 0)
			return 0;

		double ampHr = storage.getAmpHourRating();
//		double hr = time * HOURS_PER_MILLISOL;

		double ampHrRating = ampHr; 
				
		// Note: Tesla runs its batteries up to 4C charging rate
		// see https://teslamotorsclub.com/tmc/threads/limits-of-model-s-charging.36185/

		double cRating = storage.getMaxCRating();
		double nowAmpHr = cRating * ampHrRating * (1 - stateOfCharge);
		double possiblekWh = nowAmpHr / 1000D * vOut * fudgeFactor ;

		double smallestkWh = Math.min(excess, Math.min(possiblekWh, needed));

//		logger.info(storage.getBuilding(), "kWh: " + Math.round(stored * 100.0)/100.0
//				+ "  smallestkWh: " + Math.round(smallestkWh * 10000.0)/10000.0 
//				+ "  needed: " + Math.round(needed * 10000.0)/10000.0 
//				+ "  possiblekWh: " + Math.round(possiblekWh * 10000.0)/10000.0
//				+ "  ampHrRating: " + Math.round(ampHrRating * 100.0)/100.0
//				+ "  nowAmpHr: " + Math.round(nowAmpHr * 100.0)/100.0);
		
		return smallestkWh;
	}

	/**
	 * Retrieves stored energy from grid-connected batteries.
	 * 
	 * @param needed the energy needed (kWh)
	 * @param time the hours
	 * @return energy to be retrieved (kWh)
	 */
	public double retrieveStoredEnergy(double totalEnergyNeeded, double time) {
		double retrieved = 0;
		double remainingNeed = totalEnergyNeeded;
		double totalAvailable = 0;
		
		Set<Building> storages = manager.getBuildingSet(FunctionType.POWER_STORAGE);
		if (!storages.isEmpty()) {
			
			for (Building b : storages) {
				PowerStorage storage = b.getPowerStorage();
				totalAvailable += storage.computeAvailableEnergy(
						remainingNeed - totalAvailable, R_LOAD, time);
			}
		
			double neededPerStorage = totalAvailable / storages.size();
			
			for (Building b : storages) {
				PowerStorage storage = b.getPowerStorage();
				
				if (remainingNeed <= 0) {
					break;
				}

				double available = storage.computeAvailableEnergy(
						RandomUtil.getRandomDouble(neededPerStorage, neededPerStorage * 2), R_LOAD, time);
				double stored = storage.getkWattHourStored();

				if (available > 0) {
					// update the resultant energy stored in battery
					stored = stored - available;
					// update energy needed
					remainingNeed = remainingNeed - available;
					// update the energy stored in this battery
					storage.reconditionBattery(stored);
					// update the total retrieved energy
					retrieved = retrieved + available;
				}
			}
		}
		return retrieved;
	}



	/**
	 * Gets the value of electrical power at the settlement.
	 * 
	 * @return value of power (VP per kw h).
	 */
	public double getPowerValue() {
		return powerValue;
	}

	/**
	 * Determines the value of electrical power at the settlement.
	 */
	private void determinePowerValue() {
		double demand = powerRequired;
		double supply = powerGenerated + (totalEnergyStored / 2D);

		double newPowerValue = demand / (supply + 1.0D);

		if (newPowerValue != powerValue) {
			powerValue = newPowerValue;
			settlement.fireUnitUpdate(UnitEventType.POWER_VALUE_EVENT);
		}
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		powerMode = null;
		settlement = null;
		manager = null;
	}
}
