/**
 * Mars Simulation Project
 * ResearchScientificStudy.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.io.Serializable;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.science.ScienceType;

/**
 * Interface for tasks the add research credit to scientific studies.
 */
public interface ResearchScientificStudy extends Serializable {

	/**
	 * Gets the scientific field that is being researched for the study.
	 * 
	 * @return scientific field.
	 */
	public ScienceType getResearchScience();

	/**
	 * Gets the researcher who is being assisted.
	 * 
	 * @return researcher.
	 */
	public Person getResearcher();

	/**
	 * Checks if there is a research assistant.
	 * 
	 * @return research assistant.
	 */
	public boolean hasResearchAssistant();

	/**
	 * Gets the research assistant.
	 * 
	 * @return research assistant or null if none.
	 */
	public Person getResearchAssistant();

	/**
	 * Sets the research assistant.
	 */
	public void setResearchAssistant(Person researchAssistant);
}
