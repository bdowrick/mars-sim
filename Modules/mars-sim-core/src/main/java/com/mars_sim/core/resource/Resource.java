/*
 * Mars Simulation Project
 * Resource.java
 * @date 2021-11-16
 * @author Scott Davis
 */

package com.mars_sim.core.resource;

/**
 * A resource used in the simulation.
 */
public interface Resource extends Comparable<Resource> {

	/**
	 * Gets the resource's id.
	 *
	 * @return resource id.
	 */
	public int getID();

	/**
	 * Gets the resource's name.
	 *
	 * @return name
	 */
	public String getName();

	/**
	 * Gets the resource's description.
	 *
	 * @return {@link String}
	 */
	public String getDescription();
}
