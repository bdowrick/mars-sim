/**
 * Mars Simulation Project
 * AddMembersDialog.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.mission.edit;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.ModalInternalFrame;

/**
 * A dialog window for adding members to the mission for the mission tool.
 */
class AddMembersDialog extends ModalInternalFrame {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Data members.
	private Mission mission;
	protected MainDesktopPane desktop;
	
	private DefaultListModel<Worker> memberListModel;
	private DefaultListModel<Worker> availableListModel;
	private JList<Worker> availableList;
	private JButton addButton;
	
	/**
	 * Constructor.
	 * @param owner {@link Dialog} the owner dialog.
	 * @param mission {@link Mission} the mission to add to.
	 * @param memberListModel {@link DefaultListModel}<{@link Worker}> the member list model in the edit mission dialog.
	 * @param availableMembers {@link Collection}<{@link Worker}> the available members to add.
	 */
	public AddMembersDialog(JInternalFrame owner, MainDesktopPane desktop, Mission mission, 
	        DefaultListModel<Worker> memberListModel, Collection<Worker> availableMembers) {
		// Use JDialog constructor
		//super(owner, "Add Members", true);
		// Use JInternalFrame constructor
        super("Add Members");
       		
		// Initialize data members.
		this.mission = mission;
		this.memberListModel = memberListModel;
		this.desktop = desktop;
		
		// Set the layout.
		setLayout(new BorderLayout(5, 5));
		
		// Set the border.
		((JComponent) getContentPane()).setBorder(new MarsPanelBorder());
		
		// Create the header label.
		JLabel headerLabel = new JLabel("Select available people to add to the mission.");
		add(headerLabel, BorderLayout.NORTH);
		
		// Create the available people panel.
		JPanel availablePeoplePane = new JPanel(new BorderLayout(0, 0));
		add(availablePeoplePane, BorderLayout.CENTER);
		
        // Create scroll panel for available list.
		JScrollPane availableScrollPane = new JScrollPane();
        availableScrollPane.setPreferredSize(new Dimension(100, 100));
        availablePeoplePane.add(availableScrollPane, BorderLayout.CENTER);
        
        // Create available list model
        availableListModel = new DefaultListModel<Worker>();
        Iterator<Worker> i = availableMembers.iterator();
        while (i.hasNext()) availableListModel.addElement(i.next());
        
        // Create member list
        availableList = new JList<Worker>(availableListModel);
        availableList.addListSelectionListener(
        		new ListSelectionListener() {
        			public void valueChanged(ListSelectionEvent e) {
        				// Enable the add button if there are available members.
        				addButton.setEnabled(availableList.getSelectedValuesList().size() > 0);
        			}
        		}
        	);
        availableScrollPane.setViewportView(availableList);
		
        // Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(buttonPane, BorderLayout.SOUTH);
		
		// Create add button.
		addButton = new JButton("Add");
		addButton.setEnabled(availableList.getSelectedValuesList().size() > 0);
		addButton.addActionListener(
				new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// Add members to the edit mission dialog and dispose this dialog.
        				addMembers();
        				dispose();
        			}
				});
		buttonPane.add(addButton);
		
		// Create cancel button.
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(
				new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// Dispose the dialog.
        				dispose();
        			}
				});
		buttonPane.add(cancelButton);
		 
		// Finish and display dialog.
		//pack();
		//setLocationRelativeTo(owner);
		//setResizable(false);
		//setVisible(true);
		
	    desktop.add(this);	    
	    
        setSize(new Dimension(700, 550));
		Dimension desktopSize = desktop.getParent().getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);
	    setVisible(true);
	}
	
	/**
	 * Add members to edit mission dialog.
	 */
	private void addMembers() {
		int[] selectedIndexes = availableList.getSelectedIndices();
        for (int selectedIndexe : selectedIndexes) {
            if (memberListModel.getSize() < mission.getMissionCapacity()) {
                memberListModel.addElement(availableListModel.elementAt(selectedIndexe));
            }
        }
	}
}
