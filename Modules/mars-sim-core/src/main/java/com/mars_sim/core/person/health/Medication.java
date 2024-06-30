/**
 * Mars Simulation Project
 * Medication.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.health;

import java.io.Serializable;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

/**
 * An abstract class representing a medication a person has taken.
 */
public abstract class Medication implements Serializable, Temporal, Comparable<Medication> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private double duration;
	private double timeElapsed;

	private String name;
	private Person person;

	/**
	 * Constructor.
	 * 
	 * @param name     the name of the medication.
	 * @param duration the time duration (millisols).
	 * @param person   the person to be medicated.
	 */
	protected Medication(String name, double duration, Person person) {
		this.name = name;
		this.duration = duration;
		this.person = person;
		timeElapsed = 0D;
	}

	/**
	 * Gets the name of the medication.
	 * 
	 * @return name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the time duration of the medication.
	 * 
	 * @return duration (millisols).
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * Gets the person taking the medication.
	 * 
	 * @return person.
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * Gets the time elapsed since medication was taken.
	 * 
	 * @return time (millisols).
	 */
	public double getTimeElapsed() {
		return timeElapsed;
	}

	/**
	 * Update the medication based on passing time. Child classes should override
	 * for other medical effects.
	 * 
	 * @param time amount of time (millisols).
	 */
	public boolean timePassing(ClockPulse pulse) {

		// Add to time elapsed.
		timeElapsed += pulse.getElapsed();
		
		return true;
	}

	/**
	 * Is the person under the influence of this medication?
	 * 
	 * @return true if medicated.
	 */
	public boolean isMedicated() {
		return (timeElapsed < duration);
	}

	public abstract ComplaintType getComplaintType();
	
	@Override
	public boolean equals(Object object) {
		boolean result = true;
		if (object instanceof Medication med) {
			if (!name.equals(med.name))
				result = false;
			if (duration != med.duration)
				result = false;
			if (timeElapsed != med.timeElapsed)
				result = false;
			if (!person.equals(med.person))
				result = false;
		} else
			result = false;

		return result;
	}

	@Override
	public int hashCode() {
		int hashCode = name.hashCode();
		hashCode *= Double.valueOf(duration).hashCode();
		hashCode *= Double.valueOf(timeElapsed).hashCode();
		hashCode *= person.hashCode();
		return hashCode;
	}

	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	public int compareTo(Medication o) {
		return name.compareTo(o.name);
	}
}
