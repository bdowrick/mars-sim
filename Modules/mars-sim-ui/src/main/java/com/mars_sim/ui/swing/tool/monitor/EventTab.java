/**
 * Mars Simulation Project
 * EventTab.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.monitor;

import com.mars_sim.ui.swing.MainDesktopPane;

/**
 * This class represents a historical event table displayed within the Monitor
 * Window.
 */
@SuppressWarnings("serial")
public class EventTab extends TableTab {
	private static final String EVENT_ICON = "event";

	/**
	 * constructor.
	 * 
	 * @param window {@link MonitorWindow} the containing window
	 * @param notifyBox  {@link NotificationWindow}
	 * @param desktop
	 */
	public EventTab(final MonitorWindow window, MainDesktopPane desktop) {
		// Use TableTab constructor
		super(window, new EventTableModel(desktop), true, false,
				EVENT_ICON);
		
		setFilterable(true);
		setEntityDriven(true);
	}

	void filterCategories(MainDesktopPane desktop) {
		EventFilter filter = new EventFilter((EventTableModel) getModel(), desktop);
		filter.show();
	}
}
