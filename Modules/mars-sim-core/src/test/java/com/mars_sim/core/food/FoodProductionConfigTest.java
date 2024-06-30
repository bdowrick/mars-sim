package com.mars_sim.core.food;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Maps;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemType;

class FoodProductionConfigTest {

    private static final String PACKAGE_FOOD = "Package Preserved Food";
    private static final int PACKAGE_INPUTS = 2;
    private static final int PACKAGE_ALTERNATIVES = 4;


    private FoodProductionConfig getFoodConfig() {
        var config = SimulationConfig.instance();
        config.reloadConfig();
        return config.getFoodProductionConfiguration();
    }

    @Test
    void testProcessesLoaded() {
        var manuProcesses = getFoodConfig().getFoodProductionProcessList();
        assertTrue("Food processes defined", !manuProcesses.isEmpty());
    }

    @Test
    void testPackageFood() {
        // Build mapped key on process name
        var processByName =
                    Maps.uniqueIndex(getFoodConfig().getFoodProductionProcessList(),
                        FoodProductionProcessInfo::getName);
        var process = processByName.get(PACKAGE_FOOD);
        assertNotNull("Food processes defined", process);
        assertEquals("primary inputs", PACKAGE_INPUTS, process.getInputList().size());

        // Check the alternative are present and they have different inputs
        Set<List<ProcessItem>> alternatives = new HashSet<>();
        alternatives.add(process.getInputList());

        for(int i = 1; i <= PACKAGE_ALTERNATIVES; i++) {
            var found = processByName.get(PACKAGE_FOOD + FoodProductionConfig.RECIPE_PREFIX + i);
            assertNotNull(PACKAGE_FOOD + " alternative " + i, found);
            assertEquals(PACKAGE_FOOD + " alternative " + i + "inputs", PACKAGE_INPUTS, found.getInputList().size());
            alternatives.add(found.getInputList());
        }

        assertEquals("All alternatives have different inputs", PACKAGE_ALTERNATIVES + 1, alternatives.size());
    }

    @Test
    void testMakeSoybean() {
        // Build mapped key on process name
        var processByName =
                    Maps.uniqueIndex(getFoodConfig().getFoodProductionProcessList(),
                        FoodProductionProcessInfo::getName);
        var process = processByName.get("Process Soybean into Soy Flour");
        assertNotNull("Food processes defined", process);

        List<ProcessItem> expectedInputs = new ArrayList<>();
        expectedInputs.add(new ProcessItem("Soybean", ItemType.AMOUNT_RESOURCE, 1D));
        expectedInputs.add(new ProcessItem("Water", ItemType.AMOUNT_RESOURCE, 1D));
        expectedInputs.add(new ProcessItem("oven", ItemType.PART, 1D));

        assertEquals("Antenna expected inputs", expectedInputs, process.getInputList());

        List<ProcessItem> expectedOutputs = new ArrayList<>();
        expectedOutputs.add(new ProcessItem("Soy Flour", ItemType.AMOUNT_RESOURCE, 1D));
        expectedOutputs.add(new ProcessItem("oven", ItemType.PART, 1D));

        assertEquals("Antenna expected outputs", expectedOutputs, process.getOutputList());

    }
}
