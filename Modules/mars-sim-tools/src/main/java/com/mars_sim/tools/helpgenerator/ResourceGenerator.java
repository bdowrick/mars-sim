/*
 * Mars Simulation Project
 * ResourceGenerator.java
 * @date 2024-02-23
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.tools.helpgenerator.HelpGenerator.ResourceUse;

/**
 * Generates help output for any Amount Resource entity
 */
public class ResourceGenerator extends TypeGenerator<AmountResource> {

    // Represents where a Resource is used in a Food process
    public static final String TYPE_NAME = "resource";

    private Map<String, ResourceUse> foodProductionUse;

    protected ResourceGenerator(HelpGenerator parent) {
        super(parent, TYPE_NAME, "Resources",
        "Resources that can be stored and used for manufacturing and cooking.");

        // Groups according to Resource Phase
        setGrouper("Phase", r-> r.getPhase().getName());
    }

    /**
	 * Get the usage of a Resource by it's name. This will return where is an inout or output
	 * to a process.
	 */
	private ResourceUse getFoodUsageByName(String name) {
		if (foodProductionUse == null) {
			foodProductionUse = new HashMap<>();
			for (var m: getParent().getConfig().getFoodProductionConfiguration().getFoodProductionProcessList()) {
				for (var r: m.getInputNames()) {
					foodProductionUse.computeIfAbsent(r.toLowerCase(),
                                k -> HelpGenerator.buildEmptyResourceUse()).asInput().add(m);
				}
				for (var r: m.getOutputNames()) {
					foodProductionUse.computeIfAbsent(r.toLowerCase(),
                                k-> HelpGenerator.buildEmptyResourceUse()).asOutput().add(m);
				}
			}
		}

		return foodProductionUse.get(name.toLowerCase());
	}

    /**
     * Generator an output for a specific resource. This will also use the process-flow partial template
     * as well as the main resource-detail template.
     * @param r Resource for generation
     * @param output Destination of the content
     */
    @Override
    public void generateEntity(AmountResource r, OutputStream output) throws IOException {
        var generator = getParent();
        var scope = generator.createScopeMap("Resource - " + r.getName());
        scope.put(TYPE_NAME, r);

        // Add Manu processes
        addProcessFlows(r.getName(), scope);

        // Add food production
        var foodUse = getFoodUsageByName(r.getName());
        if (foodUse == null) {
            foodUse = EMPTY_USE;
        }
        scope.put("foodInput", foodUse.asInput());
        scope.put("foodOutput", foodUse.asOutput());
        
        generator.generateContent("resource-detail", scope, output);
    }

    /**
     * Get a list of all resources.
     */
    @Override
    protected List<AmountResource> getEntities() {
        return ResourceUtil.getAmountResources().stream()
		 							.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
									 .toList();
    }

    @Override
    protected String getEntityName(AmountResource v) {
        return v.getName();
    }

}
