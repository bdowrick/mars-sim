/**
 * Mars Simulation Project
 * QuotationPopup.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.core.quotation;

import java.io.Serializable;

public class Quotation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private String name, text;

	public Quotation(String name, String text) {
		this.name = name;
		this.text = text;
	}
	
	public String getName() {
		return name;
	}

	public String getText() {
		return text;
	}
}
