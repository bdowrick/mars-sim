/*
 * Mars Simulation Project
 * ConstructionStageInfo.java
 * @date 2022-08-10
 * @author Scott Davis
 */

package com.mars_sim.core.structure.construction;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Information about a construction stage.
 */
@SuppressWarnings("serial")
public class ConstructionStageInfo implements Serializable {

    // Stage types
    public static final String FOUNDATION = "foundation";
    public static final String FRAME = "frame";
    public static final String BUILDING = "building";

    // Data members
    private boolean unsetDimensions;
    private boolean constructable;
    private boolean salvagable;
    
    private int baseLevel;
    private int architectConstructionSkill;

    private double width;
    private double length;
    private double workTime;
    
    private String name;
    private String type;
    private String alignment;
    private String prerequisiteStage;
    
    private Map<Integer, Integer> parts;
    private Map<Integer, Double> resources;
    private List<ConstructionVehicleType> vehicles;

    /**
     * Constructor.
     * 
     * @param name the name of the stage.
     * @param type the stage type.
     * @param width the construction stage width (meters).
     * @param length the construction stage length (meters).
     * @param unsetDimensions true if stage dimensions are not initially set.
     * @param baseLevel -1 for in-ground, 0 for above-ground.
     * @param constructable true if stage can be constructed.
     * @param salvagable true if stage can be salvaged.
     * @param workTime the work time (millisols) required for construction.
     * @param architectConstructionSkill the construction skill required.
     * @param prerequisiteStage the name of the prerequisite stage.
     * @param parts map of parts required and their number.
     * @param resources map of resources required and their amount.
     * @param vehicles list of construction vehicles required.
     */
    ConstructionStageInfo(String name, String type, double width, double length,
    		String alignment,
    		boolean unsetDimensions, int baseLevel, boolean constructable,
    		boolean salvagable, double workTime, int architectConstructionSkill,
    		String prerequisiteStage, Map<Integer, Integer> parts, Map<Integer,
    		Double> resources, List<ConstructionVehicleType> vehicles) {

        this.name = name;
        this.type = type;
        this.width = width;
        this.length = length;
        this.alignment = alignment;
        this.unsetDimensions = unsetDimensions;
        this.constructable = constructable;
        this.salvagable = salvagable;
        this.workTime = workTime;
        this.architectConstructionSkill = architectConstructionSkill;
        this.prerequisiteStage = prerequisiteStage;
        this.parts = parts;
        this.resources = resources;
        this.vehicles = vehicles;
    }

    /**
     * Gets the architect construction skill level.
     * 
     * @return skill level.
     */
    public int getArchitectConstructionSkill() {
        return architectConstructionSkill;
    }

    /**
     * Gets the name of the stage.
     * 
     * @return name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the width of the stage.
     * 
     * @return the stage width (meters).
     */
    public double getWidth() {
        return width;
    }

    /**
     * Gets the length of the stage.
     * 
     * @return the stage length (meters).
     */
    public double getLength() {
        return length;
    }
	
    /**
     * Gets the north-south alignment info.
     * 
     * @return
     */
	public String getAlignment() {
		return alignment;
	}
	
    /**
     * Checks if the stage dimensions are initially unset.
     * 
     * @return true if dimensions unset.
     */
    public boolean isUnsetDimensions() {
        return unsetDimensions;
    }

    /**
     * Gets the base level of the building.
     * 
     * @return -1 for in-ground, 0 for above-ground.
     */
    public int getBaseLevel() {
        return baseLevel;
    }

    /**
     * Checks if the stage can be constructed.
     * 
     * @return true if stage can be constructed.
     */
    public boolean isConstructable() {
        return constructable;
    }

    /**
     * Checks if the stage can be salvaged.
     * 
     * @return true if stage can be salvaged.
     */
    public boolean isSalvagable() {
        return salvagable;
    }

    /**
     * Gets the parts needed for the stage.
     * 
     * @return map of parts and their number.
     */
    public Map<Integer, Integer> getParts() {
        return parts;
    }

    /**
     * Gets the prerequisite stage name.
     * 
     * @return name.
     */
    public String getPrerequisiteStage() {
        return prerequisiteStage;
    }

    /**
     * Gets the resources needed for the stage.
     * 
     * @return map of resources and their amounts (kg).
     */
    public Map<Integer, Double> getResources() {
        return resources;
    }

    /**
     * Gets the stage type.
     * 
     * @return type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the vehicles needed for the stage.
     * 
     * @return vehicle type.
     */
    public List<ConstructionVehicleType> getVehicles() {
        return vehicles;
    }

    /**
     * Gets the construction work time.
     * 
     * @return time (millisols).
     */
    public double getWorkTime() {
        return workTime;
    }

    /**
     * Prepares for garbage collection.
     */
    public void destroy() {
       name = null;
       type = null;
       prerequisiteStage = null;
       parts.clear();
       parts = null;
       resources.clear();
       resources = null;
       vehicles.clear();
       vehicles = null;
    }

    @Override
    public String toString() {
        return name;
    }
}
