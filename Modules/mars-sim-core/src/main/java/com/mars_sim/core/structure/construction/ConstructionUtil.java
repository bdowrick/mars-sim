/*
 * Mars Simulation Project
 * ConstructionUtil.java
 * @date 2021-12-15
 * @author Scott Davis
 */
package com.mars_sim.core.structure.construction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.structure.Settlement;

/**
 * Utility class for construction.
 */
public class ConstructionUtil {

	private static ConstructionConfig config = SimulationConfig.instance().getConstructionConfiguration();
	
	/**
	 * Private constructor.
	 */
	private ConstructionUtil() {
	}

	/**
	 * Gets a construction stage info matching a given name.
	 * 
	 * @param stageName the stage info name.
	 * @return construction stage info or null if none found.
	 * @throws Exception if error finding construction stage info.
	 */
	// TODO: reduce the utilization on this method. 3.5% of total cpu
	public static ConstructionStageInfo getConstructionStageInfo(String stageName) {
		ConstructionStageInfo result = null;

		Iterator<ConstructionStageInfo> i = getAllConstructionStageInfoList().iterator(); 
		while (i.hasNext()) {
			ConstructionStageInfo stageInfo = i.next();
			if (stageInfo.getName().equalsIgnoreCase(stageName.trim())) {
				result = stageInfo;
				break;
			}
		}

		return result;
	}

	/**
	 * Gets a list of all construction stage info of a given type.
	 * 
	 * @param stageType the type of stage.
	 * @return list of construction stage info.
	 * @throws Exception if error getting list.
	 */
	public static List<ConstructionStageInfo> getConstructionStageInfoList(String stageType) {
		return getConstructionStageInfoList(stageType, Integer.MAX_VALUE);
	}

	/**
	 * Gets a list of all construction stage info of a given type.
	 * 
	 * @param stageType the type of stage.
	 * @param constructionSkill the architect's construction skill.
	 * @return list of construction stage info.
	 * @throws Exception if error getting list.
	 */
	public static List<ConstructionStageInfo> getConstructionStageInfoList(String stageType,
			int constructionSkill) {

		List<ConstructionStageInfo> result = 
				new ArrayList<>(config.getConstructionStageInfoList(stageType));
		Iterator<ConstructionStageInfo> i = result.iterator();
		while (i.hasNext()) {
			if (i.next().getArchitectConstructionSkill() > constructionSkill) i.remove();
		}
		return result;
	}

	/**
	 * Gets a list of all foundation construction stage info.
	 * 
	 * @return list of construction stage info.
	 * @throws Exception if error getting list.
	 */
	public static List<ConstructionStageInfo> getFoundationConstructionStageInfoList() { 
		return getFoundationConstructionStageInfoList(Integer.MAX_VALUE);
	}

	/**
	 * Gets a list of all foundation construction stage info.
	 * 
	 * @param constructionSkill the architect's construction skill.
	 * @return list of construction stage info.
	 * @throws Exception if error getting list.
	 */
	public static List<ConstructionStageInfo> getFoundationConstructionStageInfoList(
			int constructionSkill) { 
		return getConstructionStageInfoList(ConstructionStageInfo.FOUNDATION, constructionSkill);
	}

	/**
	 * Gets a list of all frame construction stage info.
	 * 
	 * @return list of construction stage info.
	 * @throws Exception if error getting list.
	 */
	public static List<ConstructionStageInfo> getFrameConstructionStageInfoList() {
		return getFrameConstructionStageInfoList(Integer.MAX_VALUE);
	}

	/**
	 * Gets a list of all frame construction stage info.
	 * 
	 * @param constructionSkill the architect's construction skill.
	 * @return list of construction stage info.
	 * @throws Exception if error getting list.
	 */
	public static List<ConstructionStageInfo> getFrameConstructionStageInfoList(
			int constructionSkill) {
		return getConstructionStageInfoList(ConstructionStageInfo.FRAME, constructionSkill);
	}

	/**
	 * Gets a list of all building construction stage info.
	 * 
	 * @return list of construction stage info.
	 * @throws Exception if error getting list.
	 */
	public static List<ConstructionStageInfo> getBuildingConstructionStageInfoList() {
		return getBuildingConstructionStageInfoList(Integer.MAX_VALUE);
	}

	/**
	 * Gets a list of all building construction stage info.
	 * 
	 * @param constructionSkill the architect's construction skill.
	 * @return list of construction stage info.
	 * @throws Exception if error getting list.
	 */
	public static List<ConstructionStageInfo> getBuildingConstructionStageInfoList(
			int constructionSkill) {
		return getConstructionStageInfoList(ConstructionStageInfo.BUILDING, constructionSkill);
	}

	/**
	 * Gets a list of all construction stage info available.
	 * 
	 * @return list of construction stage info.
	 * @throws Exception if error getting list.
	 */
	public static List<ConstructionStageInfo> getAllConstructionStageInfoList() {
		return config.getAllConstructionStageInfoList();
	}

	/**
	 * Gets a list of names of buildings that are constructable from a given construction stage info.
	 * 
	 * @param stageInfo the construction stage info.
	 * @return list of building names.
	 * @throws Exception if error getting list.
	 */
	public static List<String> getConstructableBuildingNames(ConstructionStageInfo stageInfo) {

		List<String> result = new ArrayList<String>();

		if (ConstructionStageInfo.FOUNDATION.equals(stageInfo.getType())) {
			Iterator<ConstructionStageInfo> i = getNextPossibleStages(stageInfo).iterator();
			while (i.hasNext()) result.addAll(getConstructableBuildingNames(i.next()));
		}
		else if (ConstructionStageInfo.FRAME.equals(stageInfo.getType())) {
			Iterator<ConstructionStageInfo> i = getNextPossibleStages(stageInfo).iterator();
			while (i.hasNext()) result.add(i.next().getName());
		}
		else if (ConstructionStageInfo.BUILDING.equals(stageInfo.getType())) {
			result.add(stageInfo.getName());
		}
		else throw new IllegalStateException("Unknown stage type: " + stageInfo.getType());

		return result;
	}

	/**
	 * Gets a list of the next possible construction stages from a given construction stage info.
	 * 
	 * @param stageInfo the construction stage info.
	 * @return list of construction stage info.
	 * @throws Exception if error getting list.
	 */
	public static List<ConstructionStageInfo> getNextPossibleStages(ConstructionStageInfo stageInfo) {

		List<ConstructionStageInfo> result = new ArrayList<>();

		String nextStageName = null;
		if (ConstructionStageInfo.FOUNDATION.equals(stageInfo.getType())) 
			nextStageName = ConstructionStageInfo.FRAME;
		else if (ConstructionStageInfo.FRAME.equals(stageInfo.getType())) 
			nextStageName = ConstructionStageInfo.BUILDING;

		if (nextStageName != null) {
	
			Iterator<ConstructionStageInfo> i = config.getConstructionStageInfoList(nextStageName).iterator();
			while (i.hasNext()) {
				ConstructionStageInfo buildingStage = i.next();
				if (stageInfo.getName().equals(buildingStage.getPrerequisiteStage()) && buildingStage.isConstructable()) {
					result.add(buildingStage);
				}
			}
		}

		return result;
	}

	/**
	 * Gets the prerequisite construction stage info for a given stage info.
	 * 
	 * @param stageInfo the construction stage info.
	 * @return the prerequisite stage info or null if none.
	 * @throws Exception if error finding prerequisite stage info.
	 */
	public static ConstructionStageInfo getPrerequisiteStage(ConstructionStageInfo stageInfo) {
		ConstructionStageInfo result = null;

		String prerequisiteStageName = stageInfo.getPrerequisiteStage();
		if (prerequisiteStageName != null) {
			Iterator<ConstructionStageInfo> i = getAllConstructionStageInfoList().iterator();
			while (i.hasNext()) {
				ConstructionStageInfo info = i.next();
				if (info.getName().equals(prerequisiteStageName)) {
					result = info;
					break;
				}
			}
		}

		return result;
	}
	
	/**
	 * Gets the highest construction skill of all people associated with a settlement.
	 * 
	 * @param settlement the settlement.
	 * @return highest effective construction skill.
	 */
	public static int getBestConstructionSkillAtSettlement(Settlement settlement) {
	    int result = 0;
	    
	    Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
	    while (i.hasNext()) {
	        Person person = i.next();
	        if (!person.getPhysicalCondition().isDead()) {
	            int constructionSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
	            if (constructionSkill > result) {
	                result = constructionSkill;
	            }
	        }
	    }
	    
	    return result;
	}
}
