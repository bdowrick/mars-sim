/**
 * Mars Simulation Project
 * Repair.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.malfunction.Malfunctionable;

/**
 * The Repair interface is a task for repairing malfunction.
 */
public interface Repair {

	/**
	 * Gets the malfunctionable entity the person is currently repairing or null if
	 * none.
	 * 
	 * @return entity
	 */
	public Malfunctionable getEntity();
}
