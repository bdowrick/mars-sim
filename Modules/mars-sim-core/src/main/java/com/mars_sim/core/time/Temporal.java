/**
 * Mars Simulation Project
 * Temporal.java
 * @date 2021-12-09
 * @author Barry Evans
 */

package com.mars_sim.core.time;

/**
 * Represents an instance that is influenced by passing time.
 *
 */
public interface Temporal {

	/**
	 * Advances the time.
	 * 
	 * @param pulse The advancement of time.
	 * @return Was the pulse applied.
	 */
	boolean timePassing(ClockPulse pulse);
}
