/*
 * Mars Simulation Project
 * ResourceHolder.java
 * @date 2021-10-14
 * @author Barry Evans
 */
package com.mars_sim.core.equipment;

import java.util.Set;

import com.mars_sim.core.Unit;

/**
 * Represents an entity that can hold resources.
 *
 */
public interface ResourceHolder {

	/**
	 * Gets the amount resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	double getAmountResourceStored(int resource);

	
	/**
	 * Gets all the amount resource resource stored, including inside equipment
	 *
	 * @param resource
	 * @return quantity
	 */
	double getAllAmountResourceStored(int resource);
	
	/**
	 * Stores the amount resource
	 *
	 * @param resource the amount resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	double storeAmountResource(int resource, double quantity);


	/**
	 * Retrieves the resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	double retrieveAmountResource(int resource, double quantity);

	/**
	 * Gets the capacity of a particular amount resource
	 *
	 * @param resource
	 * @return capacity
	 */
	double getAmountResourceCapacity(int resource);

	/**
	 * Obtains the remaining storage space of a particular amount resource
	 *
	 * @param resource
	 * @return quantity
	 */
	double getAmountResourceRemainingCapacity(int resource);

	/**
	 * Gets the total capacity of resource that this container can hold.
	 *
	 * @return total capacity (kg).
	 */
	double getCargoCapacity();

	/**
	 * Gets a collection of supported resources
	 *
	 * @return a collection of resource ids
	 */
	Set<Integer> getAmountResourceIDs();

	/**
	 * Gets a collection of all supported resources, including inside equipment
	 *
	 * @return a collection of resource ids
	 */
	Set<Integer> getAllAmountResourceIDs();
	
	/**
	 * Gets the holder's unit instance
	 *
	 * @return the holder's unit instance
	 */
	public Unit getHolder();
	
	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public boolean hasAmountResourceRemainingCapacity(int resource);
}
