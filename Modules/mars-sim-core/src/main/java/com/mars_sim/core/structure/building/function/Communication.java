/**
 * Mars Simulation Project
 * Communication.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function;

import java.util.Iterator;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.FunctionSpec;

/**
 * The Communication class is a building function for communication.
 */
public class Communication extends Function {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
  
    /**
     * Constructor.
     * 
     * @param building the building this function is for.
     */
    public Communication(Building building, FunctionSpec spec) {
        // Use Function constructor.
        super(FunctionType.COMMUNICATION, spec, building);
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {

        // Settlements need one communication building.
        // Note: Might want to update this when we do more with simulating communication.
        double demand = 1D;

        // Supply based on wear condition of buildings.
        double supply = 0D;
        Iterator<Building> i = settlement.getBuildingManager().getBuildingSet(FunctionType.COMMUNICATION).iterator();
        while (i.hasNext()) {
            supply += (i.next().getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
        }

        if (!newBuilding) {
            supply -= 1D;
            if (supply < 0D) supply = 0D;
        }

        return demand / (supply + 1D);
    }

    @Override
    public double getMaintenanceTime() {
        return 10;
    }
}
