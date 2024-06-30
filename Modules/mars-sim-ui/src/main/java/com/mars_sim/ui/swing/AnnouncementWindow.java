/*
 * Mars Simulation Project
 * AnnouncementWindow.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The AnnouncementWindow class is an internal frame for displaying popup
 * announcements in the main desktop pane.
 */
@SuppressWarnings("serial")
public class AnnouncementWindow extends JInternalFrame {

	private JLabel announcementLabel;

//	private MainDesktopPane desktop;

	/**
	 * Constructor .
	 * 
	 * @param desktop
	 *            the main desktop pane.
	 */
	public AnnouncementWindow(MainDesktopPane desktop) {

		// Use JDialog constructor
		super("", false, false, false, false); //$NON-NLS-1$

//		this.desktop = desktop;
		// Create the main panel
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
//		mainPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(mainPane);

		mainPane.setSize(new Dimension(200, 80));
		announcementLabel = new JLabel(" "); //$NON-NLS-1$
		announcementLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		announcementLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		mainPane.add(announcementLabel, BorderLayout.CENTER);
		mainPane.setCursor(new Cursor(java.awt.Cursor.WAIT_CURSOR));

	}

	/**
	 * Sets the announcement text for the window.
	 * 
	 * @param announcement
	 *            the announcement text.
	 */
	public void setAnnouncement(String announcement) {

		announcementLabel.setText(announcement);

	}
}
