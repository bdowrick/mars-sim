/*
 * Mars Simulation Project
 * ConstructionConfig.java
 * @date 2022-08-09
 * @author Scott Davis
 */

package com.mars_sim.core.structure.construction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;


/**
 * Parses construction configuration file.
 */
public class ConstructionConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ConstructionConfig.class.getName());

    // Element names
    private static final String NAME = "name";
    private static final String WIDTH = "width";
    private static final String LENGTH = "length";
	private static final String N_S_ALIGNMENT = "north-south-alignment";    
    private static final String BASE_LEVEL = "base-level";
    private static final String CONSTRUCTABLE = "constructable";
    private static final String SALVAGABLE = "salvagable";
    private static final String WORK_TIME = "work-time";
    private static final String SKILL_REQUIRED = "skill-required";
    private static final String PART = "part";
    private static final String NUMBER = "number";
    private static final String RESOURCE = "resource";
    private static final String AMOUNT = "amount";
    private static final String VEHICLE = "vehicle";
    private static final String TYPE = "type";
    private static final String ATTACHMENT_PART = "attachment-part";

    // Data members
    private transient List<ConstructionStageInfo> foundationStageInfoList;
    private transient List<ConstructionStageInfo> frameStageInfoList;
    private transient List<ConstructionStageInfo> buildingStageInfoList;
    private transient List<ConstructionStageInfo> allConstructionStageInfoList;
	
    private transient List<Integer> constructionParts;
    private transient List<Integer> constructionResources;
    
    /**
     * Constructor.
     * 
     * @param constructionDoc DOM document with construction configuration
     */
    public ConstructionConfig(Document constructionDoc) {
    	foundationStageInfoList = createConstructionStageInfoList(constructionDoc,
    			ConstructionStageInfo.FOUNDATION);

    	frameStageInfoList = createConstructionStageInfoList(constructionDoc,
    			ConstructionStageInfo.FRAME);

    	buildingStageInfoList = createConstructionStageInfoList(constructionDoc,
    			ConstructionStageInfo.BUILDING);
    }

    /**
     * Gets a list of construction stage infos.
     *
     * @param stageType the type of stage.
     * @return list of construction stage infos.
     * @throws Exception if error parsing list.
     */
    public List<ConstructionStageInfo> getConstructionStageInfoList(String stageType) {

        List<ConstructionStageInfo> stageInfo = null;

        if (ConstructionStageInfo.FOUNDATION.equals(stageType)) {
            stageInfo = foundationStageInfoList;
        }
        else if (ConstructionStageInfo.FRAME.equals(stageType)) {
            stageInfo = frameStageInfoList;
        }
        else if (ConstructionStageInfo.BUILDING.equals(stageType)) {
            stageInfo = buildingStageInfoList;
        }
        else
        	stageInfo = new ArrayList<>(stageInfo);

        return stageInfo;
    }

    /**
     * Creates a stage info list.
     *
     * @param constructionDoc
     * @param stageType the stage type.
     * @return list of construction stage infos.
     * @throws Exception if error parsing XML file.
     */
	private List<ConstructionStageInfo> createConstructionStageInfoList(Document constructionDoc, String stageType) {

		List<ConstructionStageInfo> stageInfoList = null;
		if (ConstructionStageInfo.FOUNDATION.equals(stageType)) {
			foundationStageInfoList = new ArrayList<>();
			stageInfoList = foundationStageInfoList;
		}
		else if (ConstructionStageInfo.FRAME.equals(stageType)) {
			frameStageInfoList = new ArrayList<>();
			stageInfoList = frameStageInfoList;
		}
		else if (ConstructionStageInfo.BUILDING.equals(stageType)) {
			buildingStageInfoList = new ArrayList<>();
			stageInfoList = buildingStageInfoList;
		}
		else throw new IllegalStateException("stageType: " + stageType + " not valid.");

        Element stageInfoListElement = constructionDoc.getRootElement().getChild(stageType + "-list");
        List<Element> stageInfoNodes = stageInfoListElement.getChildren(stageType);

        for (Element stageInfoElement : stageInfoNodes) {
            String name = "";

            try {
                // Get name
                name = stageInfoElement.getAttributeValue(NAME);

                if (stageInfoList == buildingStageInfoList) {

	                Set<String> types = SimulationConfig.instance().getBuildingConfiguration().getBuildingTypes().stream()
                                            .map(b -> b.getName().toLowerCase()).collect(Collectors.toSet());
	                if (!types.contains(name.toLowerCase()))
	                	throw new IllegalStateException("ConstructionConfig : '" + name +
	                			"' in constructions.xml does not match to any building types in buildings.xml.");
                }

                String widthStr = stageInfoElement.getAttributeValue(WIDTH);
                double width = Double.parseDouble(widthStr);

                String lengthStr = stageInfoElement.getAttributeValue(LENGTH);
                double length = Double.parseDouble(lengthStr);

        		String alignment = stageInfoElement.getAttributeValue(N_S_ALIGNMENT);
        		
                boolean unsetDimensions = (width == -1D) || (length == -1D);

                String baseLevelStr = stageInfoElement.getAttributeValue(BASE_LEVEL);
                int baseLevel = Integer.parseInt(baseLevelStr);

                // Get constructable.
                // Note should be false if constructable attribute doesn't exist.
                boolean constructable = Boolean.parseBoolean(stageInfoElement.getAttributeValue(CONSTRUCTABLE));

                // Get salvagable.
                // Note should be false if salvagable attribute doesn't exist.
                boolean salvagable = Boolean.parseBoolean(stageInfoElement.getAttributeValue(SALVAGABLE));

                double workTime = Double.parseDouble(stageInfoElement.getAttributeValue(WORK_TIME));
                // convert work time from sols to millisols.
                workTime *= 1000D;

                int skillRequired = Integer.parseInt(stageInfoElement.getAttributeValue(SKILL_REQUIRED));

                String prerequisiteStage = null;
                String prerequisiteStageType = null;
                if (ConstructionStageInfo.FRAME.equals(stageType))
                    prerequisiteStageType = ConstructionStageInfo.FOUNDATION;
                else if (ConstructionStageInfo.BUILDING.equals(stageType))
                    prerequisiteStageType = ConstructionStageInfo.FRAME;
                if (prerequisiteStageType != null)
                    prerequisiteStage = stageInfoElement.getAttributeValue(prerequisiteStageType);

                List<Element> partList = stageInfoElement.getChildren(PART);

                Map<Integer, Integer> parts = new HashMap<>(partList.size());
                for (Element partElement : partList) {
                    String partName = partElement.getAttributeValue(NAME);
                    int partNum = Integer.parseInt(partElement.getAttributeValue(NUMBER));
                    Part part = (Part) ItemResourceUtil.findItemResource(partName);

    				if (part == null)
    					logger.severe(partName + " shows up in constructions.xml but doesn't exist in parts.xml.");
    				else
                        parts.put(ItemResourceUtil.findIDbyItemResourceName(partName), partNum);

                }

                List<Element> resourceList = stageInfoElement.getChildren(RESOURCE);
                Map<Integer, Double> resources =
                    new HashMap<>(resourceList.size());
                for (Element resourceElement : resourceList) {
                    String resourceName = resourceElement.getAttributeValue(NAME);
                    double resourceAmount = Double.parseDouble(resourceElement.getAttributeValue(AMOUNT));
                    AmountResource resource = ResourceUtil.findAmountResource(resourceName);
       				if (resource == null)
    					logger.severe(resourceName + " shows up in constructions.xml but doesn't exist in resources.xml.");
    				else
    					resources.put(ResourceUtil.findIDbyAmountResourceName(resourceName), resourceAmount);
                }

                List<Element> vehicleList = stageInfoElement.getChildren(VEHICLE);
                List<ConstructionVehicleType> vehicles =
                    new ArrayList<>(vehicleList.size());

                for (Element vehicleElement : vehicleList) {
                    String vehicleType = vehicleElement.getAttributeValue(TYPE);

                    Class<? extends Vehicle> vehicleClass = null;
                    if (vehicleType.toLowerCase().contains("rover")) vehicleClass = Rover.class;
                    else if (vehicleType.equalsIgnoreCase(LightUtilityVehicle.NAME))
                        vehicleClass = LightUtilityVehicle.class;
                    else throw new IllegalStateException("Unknown vehicle type: " + vehicleType);

                    List<Element> attachmentPartList = vehicleElement.getChildren(ATTACHMENT_PART);
                    List<Integer> attachmentParts = new ArrayList<>(attachmentPartList.size());
                    for (Element attachmentPartElement : attachmentPartList) {
                        String partName = attachmentPartElement.getAttributeValue(NAME);
                        attachmentParts.add(ItemResourceUtil.findIDbyItemResourceName(partName));
                    }

                    vehicles.add(new ConstructionVehicleType(vehicleType, vehicleClass, attachmentParts));
                }

                ConstructionStageInfo stageInfo = new ConstructionStageInfo(name, stageType, width, length,
                		alignment, unsetDimensions, baseLevel, constructable, salvagable, workTime, skillRequired,
                        prerequisiteStage, parts, resources, vehicles);
                stageInfoList.add(stageInfo);
            }
            catch (Exception e) {
                throw new IllegalStateException("Error reading construction stage '" + name + "': " + e.getMessage());
            }
        }

        return stageInfoList;
    }

	/**
	 * Gets a list of all construction stage info available.
	 * 
	 * @return list of construction stage info.
	 * @throws Exception if error getting list.
	 */
	public List<ConstructionStageInfo> getAllConstructionStageInfoList() {

		if (allConstructionStageInfoList == null) {
			
			List<ConstructionStageInfo> result = new ArrayList<>();
			
			List<ConstructionStageInfo> foundations = getConstructionStageInfoList(
					ConstructionStageInfo.FOUNDATION);
			List<ConstructionStageInfo> frames = getConstructionStageInfoList(
					ConstructionStageInfo.FRAME);
			List<ConstructionStageInfo> buildings = getConstructionStageInfoList(
					ConstructionStageInfo.BUILDING);

			result.addAll(foundations);
			result.addAll(frames);
			result.addAll(buildings);

			allConstructionStageInfoList = result;
		}
		
		return allConstructionStageInfoList;
	}
	
	/**
	 * Determines all resources needed for construction projects.
	 *
	 * @return
	 */
	public List<Integer> determineConstructionResources() {
		
		if (constructionResources == null) {
			List<Integer> resources = new ArrayList<>();
	
			Iterator<ConstructionStageInfo> i = ConstructionUtil.getAllConstructionStageInfoList().iterator();
			while (i.hasNext()) {
				ConstructionStageInfo info = i.next();
				if (info.isConstructable()) {
					Iterator<Integer> j = info.getResources().keySet().iterator();
					while (j.hasNext()) {
						Integer resource = j.next();
						if (!resources.contains(resource)) {
							resources.add(resource);
						}
					}
				}
			}
			
			constructionResources = resources;
		}
		
		return constructionResources;
	}

	/**
	 * Determines all parts needed for construction projects.
	 * 
	 * @return
	 */
	public List<Integer> determineConstructionParts() {
		
		if (constructionParts == null) {
			
			List<Integer> parts = new ArrayList<>();
	
			Iterator<ConstructionStageInfo> i = ConstructionUtil.getAllConstructionStageInfoList().iterator();
			while (i.hasNext()) {
				ConstructionStageInfo info = i.next();
				if (info.isConstructable()) {
					Iterator<Integer> j = info.getParts().keySet().iterator();
					while (j.hasNext()) {
						Integer part = j.next();
						if (!parts.contains(part)) {
							parts.add(part);
						}
					}
				}
			}
			
			constructionParts = parts;
		}
		
		return constructionParts;
	}

    /**
     * Prepares object for garbage collection.
     */
    public void destroy() {

    	foundationStageInfoList = null;
    	frameStageInfoList = null;
    	buildingStageInfoList = null;
    }
}
