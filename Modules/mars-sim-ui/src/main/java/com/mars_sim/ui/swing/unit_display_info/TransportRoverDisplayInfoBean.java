/**
 * Mars Simulation Project
 * TransportRoverDisplayInfoBean.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.unit_display_info;

import com.mars_sim.core.Unit;
import com.mars_sim.ui.swing.ImageLoader;

import javax.swing.Icon;

/**
 * Provides display information about a transport rover.
 */
class TransportRoverDisplayInfoBean extends RoverDisplayInfoBean {
    
    // Data members
    private Icon buttonIcon = ImageLoader.getIconByName("unit/rover_transport");

    
    /**
     * Constructor
     */
    TransportRoverDisplayInfoBean() {
        super();
//        buttonIcon = ImageLoader.getIcon("TransportRoverIcon", ImageLoader.VEHICLE_ICON_DIR);
    }
    
    /** 
     * Gets icon for unit button.
     * @return icon
     */
    public Icon getButtonIcon(Unit unit) {
        return buttonIcon;
    }
}
