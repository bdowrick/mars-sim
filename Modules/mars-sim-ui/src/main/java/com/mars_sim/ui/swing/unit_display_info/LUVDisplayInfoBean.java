/**
 * Mars Simulation Project
 * LUVDisplayInfoFactory.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.unit_display_info;

import javax.swing.Icon;

import com.mars_sim.core.Unit;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.sound.SoundConstants;

/**
 * Provides display information about a light utility vehicle.
 */
public class LUVDisplayInfoBean extends VehicleDisplayInfoBean {

	// Data members
	private Icon buttonIcon = ImageLoader.getIconByName("unit/luv");


	/**
	 * Constructor.
	 */
	public LUVDisplayInfoBean() {
		// Use VehicleDisplayInfoBean
		super();
	}

    /**
     * Gets icon for unit button.
     * 
     * @return icon
     */
	@Override
	public Icon getButtonIcon(Unit unit) {
		return buttonIcon;
	}

    /**
     * Gets a sound appropriate for this unit.
     * @param unit the unit to display.
     * @return sound filepath for unit or empty string if none.
     */
	@Override
	public String getSound(Unit unit) {
		LightUtilityVehicle luv = (LightUtilityVehicle) unit;
    	if (luv.haveStatusType(StatusType.MAINTENANCE)) return SoundConstants.SND_ROVER_MAINTENANCE;
    	else if (luv.haveStatusType(StatusType.MALFUNCTION)) return SoundConstants.SND_ROVER_MALFUNCTION;
    	else if ((luv.getPrimaryStatus() == StatusType.GARAGED) || (luv.getPrimaryStatus() == StatusType.PARKED)) return SoundConstants.SND_ROVER_PARKED;
    	else if (luv.getCrewNum() > 0 || luv.getRobotCrewNum() > 0) return SoundConstants.SND_ROVER_MOVING;
    	else return "";
	}

	@Override
	public boolean isMapDisplayed(Unit unit) {
        return false;
    }

	@Override
    public boolean isGlobeDisplayed(Unit unit) {
        return false;
    }
}
