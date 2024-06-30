/*
 * Mars Simulation Project
 * Lab.java
 * @date 2022-07-16
 * @author Scott Davis
 */
package com.mars_sim.core.structure;

import java.io.Serializable;

import com.mars_sim.core.science.ScienceType;

/**
 * The Lab interface represents a unit that can perform the function
 * of a research laboratory.
 */
public interface Lab extends Serializable {

	/** 
	 * Gets the laboratory size.
	 * This is the number of researchers supportable at any given time. 
	 * 
	 * @return the size of the laboratory (in researchers). 
	 */
	public int getLaboratorySize(); 

	/** 
	 * Gets the technology level of laboratory.
	 * 
	 * @return the technology level of the laboratory 
	 */
	public int getTechnologyLevel();

    /** 
     * Gets the lab's science specialties.
     * 
     * @return the lab's science specialties
     */
    public ScienceType[] getTechSpecialties();

    /**
     * Checks to see if the laboratory has a given tech specialty.
     * 
     * @return true if lab has tech specialty
     */
    public boolean hasSpecialty(ScienceType specialty);
    
    /**
     * Gets the number of people currently researching in the laboratory.
     * 
     * @return number of researchers
     */
    public int getResearcherNum(); 

	/**
	 * Adds a researcher to the laboratory.
	 * 
	 * @return 
	 * @throws Exception if person cannot be added.
	 */
	public boolean addResearcher();

	/**
	 * Removes a researcher from the laboratory.
	 * 
	 * @throws Exception if person cannot be removed.
	 */
	public void removeResearcher();

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy();
}
