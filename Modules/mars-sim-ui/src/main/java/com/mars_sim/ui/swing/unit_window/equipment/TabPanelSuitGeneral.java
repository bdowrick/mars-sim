/*
 * Mars Simulation Project
 * TabPanelSuitGeneral.java
 * @date 2023-07-01
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.equipment;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.data.History;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.person.Person;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ColumnSpec;
import com.mars_sim.ui.swing.utils.JHistoryPanel;

/**
 * This tab displays general information about an EVA Suit.
 */
@SuppressWarnings("serial")
public class TabPanelSuitGeneral extends TabPanel {

	private static final String ID_ICON = "info"; //$NON-NLS-1$
	
	/** The suit instance. */
	private EVASuit suit;
	
	private JLabel registeredOwnerLabel;
	
	private String pOwnerCache;
	
	private HistoryPanel historyPanel;
		
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelSuitGeneral(EVASuit suit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelSuitGeneral.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(ID_ICON),		
			Msg.getString("TabPanelSuitGeneral.title"), //$NON-NLS-1$
			desktop
		);

		this.suit = suit;
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel(1);
		
		content.add(infoPanel, BorderLayout.NORTH);

		// Prepare registered owner label
		pOwnerCache = "";
		Person p = suit.getRegisteredOwner();	
		if (p != null) {
			pOwnerCache = suit.getRegisteredOwner().getName();
		}		
		registeredOwnerLabel = infoPanel.addTextField(Msg.getString("TabPanelSuitGeneral.regOwner"), //$NON-NLS-1$
				pOwnerCache, null);

		historyPanel = new HistoryPanel(suit.getHistory());
		historyPanel.setPreferredSize(new Dimension(225, 100));

		content.add(historyPanel, BorderLayout.CENTER);

	}
	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		
		String pOwner = "";
		Person p = suit.getRegisteredOwner();	
		if (p != null) {
			pOwner = suit.getRegisteredOwner().getName();
		}
		if (!pOwnerCache.equalsIgnoreCase(pOwner)) {
			pOwnerCache = pOwner;
			registeredOwnerLabel.setText(pOwner); 
		}

		historyPanel.refresh();
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private class HistoryPanel extends JHistoryPanel<Unit> {
		private static final ColumnSpec[] COLUMNS = {new ColumnSpec("Location", String.class)};


		HistoryPanel(History<Unit> source) {
			super(source, COLUMNS);
		}

		@Override
		protected Object getValueFrom(Unit value, int columnIndex) {
			return value.getName();
		}
	}
}
