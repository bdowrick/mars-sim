/**
 * Mars Simulation Project
 * ResourceException.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis 
 */

package com.mars_sim.core.resource;

/**
 * An exception related to resources.
 */
public class ResourceException extends Exception {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param message exception message.
	 */
	public ResourceException(String message) {
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param message exception message
	 * @param arg     cause
	 */
	public ResourceException(String message, Throwable arg) {
		super(message, arg);
	}

	/**
	 * Constructor
	 * 
	 * @param arg cause
	 */
	public ResourceException(Throwable arg) {
		super(arg);
	}
}
