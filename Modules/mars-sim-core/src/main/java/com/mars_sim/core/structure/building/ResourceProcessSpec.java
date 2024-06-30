/*
 * Mars Simulation Project
 * ResourceProcessSpec.java
 * @date 2021-08-20
 * @author Barry Evans
 */
package com.mars_sim.core.structure.building;

import com.mars_sim.core.process.ProcessSpec;

/**
 * The ResourceProcessSpec class represents the specification of a process of converting one set of
 * resources to another. This object is shared amongst ResourceProcess of the same type.
 */
public class ResourceProcessSpec extends ProcessSpec {

	private static final long serialVersionUID = 1L;

	private boolean defaultOn;

	/**
	 * Constructor.
	 * 
	 * @param name
	 * @param powerRequired
	 * @param processTime
	 * @param workTime
	 * @param defaultOn
	 */
	public ResourceProcessSpec(String name, double powerRequired, int processTime, int workTime, boolean defaultOn) {
		super(name, powerRequired, processTime, workTime);

		this.defaultOn = defaultOn;
	}

	public boolean getDefaultOn() {
		return defaultOn;
	}
}
