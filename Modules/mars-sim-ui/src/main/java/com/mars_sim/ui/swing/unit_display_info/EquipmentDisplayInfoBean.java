/**
 * Mars Simulation Project
 * EquipmentDisplayInfo.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.unit_display_info;

import com.mars_sim.core.Unit;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.sound.SoundConstants;

import javax.swing.Icon;

/**
 * Provides display information about a piece of equipment.
 */
class EquipmentDisplayInfoBean extends AbstractUnitDisplayInfo {

    // Data members
    private Icon buttonIcon = ImageLoader.getIconByName("unit/equipment");

    /**
     * Constructor
     */
    EquipmentDisplayInfoBean() {
    	super();
    }

    /**
     * Gets icon for unit button.
     * @return icon
     */
    public Icon getButtonIcon(Unit unit) {
        return buttonIcon;
    }

    /**
     * Gets a sound appropriate for this unit.
     * @param unit the unit to display.
     * @returns sound filepath for unit or empty string if none.
     */
    public String getSound(Unit unit) {
    	return SoundConstants.SND_EQUIPMENT;
    }
}
