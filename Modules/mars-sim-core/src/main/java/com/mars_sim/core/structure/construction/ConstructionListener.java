/*
 * Mars Simulation Project
 * ConstructionListener.java
 * @date 2021-12-15
 * @author Scott Davis
 */
package com.mars_sim.core.structure.construction;

/**
 * Interface for a construction event listener.
 */
public interface ConstructionListener {

    /**
     * Catch construction update event.
     * @param event the mission event.
     */
    public void constructionUpdate(ConstructionEvent event);
}
