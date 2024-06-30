/**
 * Mars Simulation Project
 * PlanetType.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.core.environment;

import com.mars_sim.tools.Msg;

public enum PlanetType {

	EARTH			(Msg.getString("PlanetType.earth.mass"), Msg.getString("PlanetType.earth.radius")), //$NON-NLS-1$
	MARS			(Msg.getString("PlanetType.mars.mass"), Msg.getString("PlanetType.mars.radius")) //$NON-NLS-1$
	;

	private final double mass;
	private final double radius;
	private final double surfaceGravity; // m/s^2
	private static final double G = 6.67300E-11; 
	
	/** hidden constructor. */
	private PlanetType(String mass, String radius) {
		this.mass = Double.parseDouble(mass);
		this.radius = Double.parseDouble(radius);
		surfaceGravity = G * this.mass / (this.radius * this.radius);
		
	}

	public final String getData() {
		return toString(); // will return the uppercase string of EARTH or MARS
	}
	
	public double getMass() {
		return mass;
	}

	public double getRadius() {
		return radius;
	}
	
	public double getSurfaceGravity() {
		return surfaceGravity;
	}
	
	//@Override
	//public final String toString() {
	//	return getName();
	//}
}
