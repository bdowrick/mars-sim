/*
 * Mars Simulation Project
 * Deliverybot.java
 * @date 2022-09-01
 * @author Manny Kung
 */
package com.mars_sim.core.robot.ai.job;

import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.Delivery;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.function.FunctionType;

public class Deliverybot extends RobotJob {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor.
	 */
	public Deliverybot() {
		// Use Job constructor.
		super();
		
        jobMissionStarts.add(Delivery.class);
	}

	/**
	 * Gets a robot's capability to perform this job.
	 * @param robot the robot to check.
	 * @return capability (min 0.0).
	 */
	@Override
	public double getCapability(Robot robot) {

		double result = 0D;

		int tradingSkill = robot.getSkillManager().getSkillLevel(SkillType.TRADING);
		result = tradingSkill;

		NaturalAttributeManager attributes = robot.getNaturalAttributeManager();

		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result+= result * ((experienceAptitude - 50D) / 100D);

		// Add conversation.
		int conversation = attributes.getAttribute(NaturalAttributeType.CONVERSATION);
		result+= result * ((conversation - 50D) / 100D);

		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	@Override
	public double getOptimalCount(Settlement settlement) {
		// Check there is a garage space otherwise DeliveryRobot cannot load vehicles
		return settlement.getBuildingManager().getBuildingSet(FunctionType.VEHICLE_MAINTENANCE).size();
	}
}
