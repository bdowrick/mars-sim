/*
 * Mars Simulation Project
 * JComboBoxMW.java
 * @date 2022-07-28
 * @author stpa
 */
package com.mars_sim.ui.swing;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

/**
 * A Combobox that is mouse wheel-enabled.
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class JComboBoxMW<T> extends JComboBox implements MouseWheelListener {

	private boolean wide = true;

	/**
	 * constructor.
	 */
	public JComboBoxMW() {
		super();
		this.addMouseWheelListener(this);
//		((JTextField) this.getEditor().getEditorComponent()).setBorder(
//				BorderFactory.createCompoundBorder(this.getBorder(), BorderFactory.createEmptyBorder(0, 1, 0, 1)));
	}

	/**
	 * constructor.
	 *
	 * @param items {@link Vector}<T> the initial items.
	 */
	@SuppressWarnings("unchecked")
	public JComboBoxMW(Vector<T> items) {
		super(items);
		this.addMouseWheelListener(this);
	}

	/**
	 * Constructor.
	 *
	 * @param model {@link ComboBoxModel}<T>
	 */
	@SuppressWarnings("unchecked")
	public JComboBoxMW(ComboBoxModel<T> model) {
		super(model);
		this.addMouseWheelListener(this);
	}

	/**
	 * Constructor.
	 *
	 * @param items T[]
	 */
	@SuppressWarnings("unchecked")
	public JComboBoxMW(T[] items) {
		super(items);
		this.addMouseWheelListener(this);
	}

	
	@SuppressWarnings("unchecked")
	public void replaceModel(ComboBoxModel<T> model) {
		setModel(model);
	}
	
	
	/** Use mouse wheel to cycle through items if any. */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (getItemCount() > 0) {
			boolean up = e.getWheelRotation() < 0;
			this.setSelectedIndex(
					(this.getSelectedIndex() + (up ? -1 : 1) + this.getItemCount()) % this.getItemCount());
		}
	}

	public void doLayout() {
		try {
//			layingOut = true;
			super.doLayout();
		} finally {
//			layingOut = false;
		}
	}

	public boolean isWide() {
		return wide;
	}

	public void setWide(boolean wide) {
		this.wide = wide;
	}

//	public Dimension getSize() {
//		Dimension dim = super.getSize();
//		if (!layingOut && isWide())
//			dim.width = Math.min(dim.width, Toolkit.getDefaultToolkit().getScreenSize().width);
//		return dim;
//	}

}
