/**
 * Mars Simulation Project
 * Role.java
 * @date 2023-11-14
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.role;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.activities.GroupActivity;
import com.mars_sim.core.data.History;
import com.mars_sim.core.data.History.HistoryItem;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.GroupActivityType;

public class Role implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private Person person;

	private RoleType roleType;

	private History<RoleType> roleHistory = new History<>();

	public Role(Person person) {
		this.person = person;
	}

	/**
	 * Gets the type of role.
	 * 
	 * @return role type
	 */
	public RoleType getType() {
		return roleType;
	}

	/**
	 * Releases the old role type.
	 * 
	 * @param role type
	 */
	public void relinquishOldRoleType() {

		if (roleType != null) {
			if (person.getAssociatedSettlement() != null)		
				person.getAssociatedSettlement().getChainOfCommand().releaseRole(roleType);
			else 
				person.getBuriedSettlement().getChainOfCommand().releaseRole(roleType);
		}
	}

	/**
	 * Changes the role type.
	 *                                                                                                                                                                                                                
	 * @param newType the new role
	 */
	public void changeRoleType(RoleType newType) {
		RoleType oldType = roleType;

		if (newType == null) {
			throw new IllegalArgumentException("New roletype cannot be null.");
		}

		if (newType != oldType) {
			var home = person.getAssociatedSettlement();

			// Note : if this is a leadership role, only one person should occupy this position 
			List<Person> predecessors = Collections.emptyList();
			if (newType.isChief() || newType.isCouncil()) {
				// Find a list of predecessors who are occupying this role
				predecessors = home.getChainOfCommand().findPeopleWithRole(newType);
				if (!predecessors.isEmpty()) {
					// Predecessors to seek for a new role to fill
					predecessors.get(0).getRole().obtainNewRole();
				}
			}
			
			// Turn in the old role
			relinquishOldRoleType();

			// Set the role type of this person to the new role type
			roleType = newType;
			roleHistory.add(roleType);
			
			// Save the role in the settlement Registry
			home.getChainOfCommand().registerRole(roleType);

			// Records the role change and fire unit update
			person.fireUnitUpdate(UnitEventType.ROLE_EVENT, roleType);

			// For Council members being changed have a meeting
			if (roleType.isCouncil() && !predecessors.isEmpty()) {
				GroupActivity.createPersonActivity("Council Announcement for " + roleType.getName(),
									GroupActivityType.ANNOUNCEMENT, home, person, 0, 
									Simulation.instance().getMasterClock().getMarsTime());
			}
		}
	}

	/**
	 * Gets how has this person's role assignment has changed over time.
	 * 
	 * @return
	 */
	public List<HistoryItem<RoleType>> getChanges() {
		return roleHistory.getChanges();
	}
	
	/**
	 * Obtains a new role.
	 * 
	 * @param s
	 */
	public void obtainNewRole() {
		// Find the best role
		RoleType roleType = RoleUtil.findBestRole(person);	
		// Finalize setting a person's new role
		changeRoleType(roleType);
	}
	
	/**
	 * Override {@link Object#toString()} method.
	 */
	@Override
	public String toString() {
		return roleType.getName();
	}
}
