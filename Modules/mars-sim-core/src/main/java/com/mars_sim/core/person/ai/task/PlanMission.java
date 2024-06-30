/*
 * Mars Simulation Project
 * PlanMission.java
 * @date 2023-06-30
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.task;

import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.PlanType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.Administration;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;


/**
 * This class is a task for reviewing mission plans
 */
public class PlanMission extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(PlanMission.class.getName());
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.planMission"); //$NON-NLS-1$

	/** Task phases. */
//	private static final TaskPhase GATHERING = new TaskPhase(Msg.getString("Task.phase.planMission.gatheringData")); //$NON-NLS-1$

	private static final TaskPhase SELECTING = new TaskPhase(Msg.getString("Task.phase.planMission.selectingMission")); //$NON-NLS-1$

	private static final TaskPhase SUBMITTING = new TaskPhase(Msg.getString("Task.phase.planMission.submittingMission")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.1D;

	// Data members
	/** The administration building the person is using. */
	private Administration office;

	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public PlanMission(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, RandomUtil.getRandomInt(20, 50));

		boolean canDo = person.getMind().canStartNewMission();
		if (!canDo) {
			endTask();
		}
			
		if (person.isInSettlement()) {

			// If person is in a settlement, try to find an office building.
			Building officeBuilding = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.ADMINISTRATION);

			// Note: office building is optional
			if (officeBuilding != null) {
				office = officeBuilding.getAdministration();	
				if (!office.isFull()) {
					office.addStaff();
					// Walk to the office building.
					walkToTaskSpecificActivitySpotInBuilding(officeBuilding, FunctionType.ADMINISTRATION, true);
				}
			}
			else {
				Building dining = BuildingManager.getAvailableDiningBuilding(person, false);
				// Note: dining building is optional
				if (dining != null) {
					// Walk to the dining building.
					walkToTaskSpecificActivitySpotInBuilding(dining, FunctionType.DINING, true);
				}
				// work anywhere		
			}
			// Note: add other workplace if administration building is not available

		} // end of roleType
		else {
			logger.warning(person, "Not in a Settlement");
			endTask();
		}

		// Initialize phase
		addPhase(SELECTING);
		addPhase(SUBMITTING);
		
		setPhase(SELECTING);
	}	

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (SELECTING.equals(getPhase())) {
			return selectingPhase(time);			
		} else if (SUBMITTING.equals(getPhase())) {
			return submittingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the selecting mission phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double selectingPhase(double time) {
		double remainingTime = 0;
		
		boolean canDo = person.getMind().canStartNewMission();
		if (!canDo) {
			endTask();
		}
		else {
			// Start a new mission
			person.getMind().getNewMission();
			
			Mission mission = person.getMind().getMission();
			if (mission != null)
				setPhase(SUBMITTING);
			else {
				// No mission found so stop planning for now
				endTask();
			}
		}
		
        return remainingTime;
	}
	
	/**
	 * Performs the submitting the mission phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double submittingPhase(double time) {
		double remainingTime = 0;
		
		Mission mission = person.getMind().getMission();
		MissionPlanning plan = mission.getPlan();
		
		if ((plan != null) && !mission.isDone()) {
			logger.log(worker, Level.INFO, 30_000, "Submitted a mission plan for " 
					+ mission.getName() + ".");
			
			// Set the plan pending and add to approval list
			plan.setStatus(PlanType.PENDING);
			missionManager.requestMissionApproving(plan);
		}
		
		// Add experience
		addExperience(time); 
		
		endTask();
		
		return remainingTime;
	}
	
	@Override
	protected void addExperience(double time) {
        double newPoints = time / 20D;
        int experienceAptitude = worker.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.EXPERIENCE_APTITUDE);
        int leadershipAptitude = worker.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.LEADERSHIP);
        newPoints += newPoints * (experienceAptitude + leadershipAptitude- 100D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        worker.getSkillManager().addExperience(SkillType.MANAGEMENT, newPoints, time);

	}

	/**
	 * Releases office space.
	 */
	@Override
	protected void clearDown() {
		// Remove person from administration function so others can use it.
		if (office != null && office.getNumStaff() > 0) {
			office.removeStaff();
		}
	}

}
