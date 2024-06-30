/**
 * Mars Simulation Project
 * MarsPanelBorder.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;

/**
 * The MarsPanelBorder is a common compound border used for panels.
 */
public class MarsPanelBorder extends CompoundBorder {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public MarsPanelBorder() {

		super(new EtchedBorder(), MainDesktopPane.newEmptyBorder());
	}
}
