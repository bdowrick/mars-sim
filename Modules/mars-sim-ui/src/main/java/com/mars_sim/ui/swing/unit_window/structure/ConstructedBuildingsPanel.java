/**
 * Mars Simulation Project
 * ConstructedBuildingsPanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.data.History.HistoryItem;
import com.mars_sim.core.structure.construction.ConstructionManager;
import com.mars_sim.ui.swing.StyleManager;

@SuppressWarnings("serial")
public class ConstructedBuildingsPanel
extends JPanel {

	// Data members
	private JTable constructedTable;
	private ConstructedBuildingTableModel constructedTableModel;

	/**
	 * Constructor.
	 * @param manager the settlement construction manager.
	 */
	public ConstructedBuildingsPanel(ConstructionManager manager) {
		// Use JPanel constructor.
		super();

		setLayout(new BorderLayout(0, 0));

		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(titlePanel, BorderLayout.NORTH);

		JLabel titleLabel = new JLabel("Constructed Buildings");
		StyleManager.applySubHeading(titleLabel);
		titlePanel.add(titleLabel);

		// Create scroll panel for the outer table panel.
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPanel, BorderLayout.CENTER);

		// Prepare constructed table model.
		constructedTableModel = new ConstructedBuildingTableModel(manager);

		// Prepare constructed table.
		constructedTable = new JTable(constructedTableModel);
		scrollPanel.setViewportView(constructedTable);
		constructedTable.setRowSelectionAllowed(true);
		constructedTable.getColumnModel().getColumn(0).setPreferredWidth(105);
		constructedTable.getColumnModel().getColumn(1).setPreferredWidth(105);

		constructedTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		constructedTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		constructedTable.setAutoCreateRowSorter(true);
	}

	/**
	 * Update the information on this panel.
	 */
	public void update() {
		constructedTableModel.update();
	}

	/**
	 * Internal class used as model for the constructed table.
	 */
	private static class ConstructedBuildingTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		ConstructionManager manager;

		private ConstructedBuildingTableModel(ConstructionManager manager) {
			this.manager = manager;
		}

		public int getRowCount() {
			return manager.getConstructedBuildingLog().size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Building";
			else if (columnIndex == 1) return "Time Stamp";
			else return null;
		}

		public Object getValueAt(int row, int column) {
			if (row < getRowCount()) {
				HistoryItem<String> logEntry = manager.getConstructedBuildingLog().get(row);
				if (column == 0) return logEntry.getWhat();
				else if (column == 1) return logEntry.getWhen().getTruncatedDateTimeStamp();
				else return null;
			}
			else return null;
		}

		public void update() {
			fireTableDataChanged();
		}
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		constructedTable = null;
		constructedTableModel = null;
	}
}
