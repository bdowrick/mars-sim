/*
 * Mars Simulation Project
 * UnitWindowListener.java
 * @date 2021-12-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window;

import com.mars_sim.ui.swing.MainDesktopPane;

import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/** The UnitWindowListener class is a custom window listener for unit
 *  detail windows that handles their behavior.
 */
public class UnitWindowListener extends InternalFrameAdapter {

    // Data members
	// Main desktop pane that holds unit windows.
    private MainDesktopPane desktop; 

    /** 
     * Constructs a UnitWindowListener object
     * @param desktop the desktop pane
     */
    public UnitWindowListener(MainDesktopPane desktop) {
        this.desktop = desktop;
    }

    /**
     * Removes unit button from toolbar when unit window is closed.
     *
     * @param e internal frame event.
     */
    public void internalFrameClosing(InternalFrameEvent e) {
        desktop.disposeUnitWindow((UnitWindow) e.getSource());
    }
}

