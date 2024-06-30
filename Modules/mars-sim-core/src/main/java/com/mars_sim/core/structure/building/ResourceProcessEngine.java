/*
 * Mars Simulation Project
 * ResourceProcessEngine.java
 * @date 2022-10-23
 * @author Barry Evans
 */
package com.mars_sim.core.structure.building;

import java.io.Serializable;
import java.util.Set;

/**
 * This class represents an instance of a ResourceProcessing engine that hosts a Resource process and has
 * a number of modules. The input, output and power of the processSpec is multiplied by the number of modules.
 * It is a shared configuration entity.
 */
public class ResourceProcessEngine implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

    private int modules;
    private ResourceProcessSpec processSpec;

    ResourceProcessEngine(ResourceProcessSpec processSpec, int modules) {
        this.modules = modules;
        this.processSpec = processSpec;
    }

    public boolean getDefaultOn() {
        return processSpec.getDefaultOn();
    }

    public String getName() {
        return processSpec.getName();
    }

    public Set<Integer> getInputResources() {
        return processSpec.getInputResources();
    }

    public double getBaseSingleInputRate(Integer resource) {
        return processSpec.getBaseInputRate(resource);
    }
    public double getBaseFullInputRate(Integer resource) {
        return processSpec.getBaseInputRate(resource) * modules;
    }

    public boolean isAmbientInputResource(Integer resource) {
        return processSpec.isAmbientInputResource(resource);
    }

    public Set<Integer> getOutputResources() {
        return processSpec.getOutputResources();
    }

    public double getBaseSingleOutputRate(Integer resource) {
        return processSpec.getBaseOutputRate(resource);
    }

    public double getBaseFullOutputRate(Integer resource) {
        return processSpec.getBaseOutputRate(resource) * modules;
    }
    
    public boolean isWasteOutputResource(Integer resource) {
        return processSpec.isWasteOutputResource(resource);
    }

    public double getPowerRequired() {
        return processSpec.getPowerRequired() * modules;
    }

    public int getProcessTime() {
        return processSpec.getProcessTime();
    }

    public int getWorkTime() {
        return processSpec.getWorkTime();
    }

    /**
     * How many modules does this resource process engine have ?
     */
    public int getModules() {
        return modules;
    }
}
