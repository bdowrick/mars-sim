/**
 * Mars Simulation Project
 * MissionListener.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

/**
 * Listener interface for the mission manager.
 */
public interface MissionManagerListener {

	/**
	 * Adds a new mission.
	 * 
	 * @param mission the new mission.
	 */
	public void addMission(Mission mission);

	/**
	 * Removes an old mission.
	 * 
	 * @param mission the old mission.
	 */
	public void removeMission(Mission mission);
}
