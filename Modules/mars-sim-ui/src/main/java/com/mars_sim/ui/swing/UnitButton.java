/**
 * Mars Simulation Project
 * UnitButton.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing;

import javax.swing.JButton;
import javax.swing.SwingConstants;

import com.mars_sim.core.Unit;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The UnitButton class is a UI button for a given unit.
 * It is displayed in the unit tool bar.
 */
public class UnitButton extends JButton {

	private static final long serialVersionUID = 1L;
	// Data members
	private Unit unit;

    /**
     * Constructor
     *
     * @param unit the unit the button is for.
     */
	public UnitButton(Unit unit) {

		// Use JButton constructor
		super(unit.getName(),
            UnitDisplayInfoFactory.getUnitDisplayInfo(unit).getButtonIcon(unit));

		// Initialize unit
		this.unit = unit;

		// Prepare default unit button values
		setVerticalTextPosition(SwingConstants.BOTTOM);
		setHorizontalTextPosition(SwingConstants.CENTER);
		setAlignmentX(.5F);
		setAlignmentY(.5F);
	}

	/**
     * Gets the button's unit.
     *
     * @return the button's unit
     */
	public Unit getUnit() {
        return unit;
    }
}
