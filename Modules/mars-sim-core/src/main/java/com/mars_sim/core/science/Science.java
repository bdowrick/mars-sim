/*
 * Mars Simulation Project
 * Science.java
 * @date 2023-10-19
 * @author Scott Davis
 */
package com.mars_sim.core.science;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mars_sim.core.person.ai.job.util.JobType;

/**
 * A class representing a field of science.
 */
public class Science
implements Serializable, Comparable<Object> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private ScienceType type;
	private List<JobType> jobs = new ArrayList<>();
	private List<ScienceType> collaborativeSciences = new ArrayList<>();

	/**
	 * Constructor 1.
	 * 
	 * @param type {@link ScienceType} the name of the field of science.
	 * @param job job associated with the field.
	 */
	public Science(ScienceType type) {
		this.type = type;
		this.jobs.add(type.getJobType());
	}

	/**
	 * Constructor 2.
	 * 
	 * @param type {@link ScienceType} the name of the field of science.
	 * @param jobs jobs associated with the field.
	 */
	public Science(ScienceType type, JobType[] jobs) {
		this.type = type;
		Collections.addAll(this.jobs, jobs);
	}

	/**
	 * Sets the sciences that can collaborate on research with this field of science.
	 * 
	 * @param collaborativeScience sciences that can collaborate.
	 */
	void setCollaborativeSciences(Science[] collaborativeSciences) {
		for (Science science : collaborativeSciences) {
			this.collaborativeSciences.add(science.getType());
		}
	}

	/**
	 * Gets the sciences that can collaborate on research with this field of science.
	 * 
	 * @return sciences.
	 */
	public List<ScienceType> getCollaborativeSciences() {
		return collaborativeSciences;
	}

	/**
	 * Gets the type of the field of science.
	 * 
	 * @return type.
	 */
	public ScienceType getType() {
		return type;
	}

	/**
	 * Gets the jobs associated with this field of science.
	 * 
	 * @return jobs.
	 */
	public final List<JobType> getJobs() {
		return jobs;
	}

	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than, 
	 * equal to, or greater than the specified object.
	 */
	public int compareTo(Object o) {
		if (o instanceof Science) return type.compareTo(((Science) o).type);
		else return 0;
	}

	/**
	 * Checks if an object is equal to this object.
	 * 
	 * @return true if equal
	 */
	public boolean equals(Object object) {
		if (object instanceof Science) {
			Science otherObject = (Science) object;
            return type.equals(otherObject.type);
		}
		return false;
	}

	/**
	 * Gets the hash code value.
	 */
	public int hashCode() {
		return (type.hashCode());
	}

	@Override
	public String toString() {
		return type.getName();
	}
}
