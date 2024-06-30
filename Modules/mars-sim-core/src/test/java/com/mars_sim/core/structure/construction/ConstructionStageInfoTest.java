/**
 * Mars Simulation Project
 * ConstructionStageInfoTest.java
 * @version 3.1.0 2017-09-11
 * @author Scott Davis
 */

package com.mars_sim.core.structure.construction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.LightUtilityVehicle;

/**
 * Unit test for the ConstructionStageInfo class.
 */
public class ConstructionStageInfoTest extends AbstractMarsSimUnitTest {

    // Data members
    private ConstructionStageInfo info;

    @Override
    public void setUp() {
        super.setUp();

        Map<Integer, Integer> parts = new HashMap<>(1);
        Part p = ItemResourceUtil.findItemResource(ItemResourceUtil.backhoeID);
        parts.put(p.getID(), 1);

        Map<Integer, Double> resources = new HashMap<>(1);

        AmountResource ar = ResourceUtil.sandAR;
        resources.put(ar.getID(), 1D);

        List<ConstructionVehicleType> vehicles =
            new ArrayList<>(1);
        List<Integer> attachments = new ArrayList<>(1);
        
        Part atth = ItemResourceUtil.findItemResource(ItemResourceUtil.pneumaticDrillID);
        attachments.add(atth.getID());

        vehicles.add(new ConstructionVehicleType("Light Utility Vehicle", LightUtilityVehicle.class,
                attachments));

        info = new ConstructionStageInfo("test stage", ConstructionStageInfo.FOUNDATION, 10D, 10D, 
        		"length", false, 0,
                false, false, 10000D, 1, null, parts, resources, vehicles);
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getArchitectConstructionSkill()'
     */
    public void testGetArchitectConstructionSkill() {
        assertEquals(1, info.getArchitectConstructionSkill());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getName()'
     */
    public void testGetName() {
        assertEquals("test stage", info.getName());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getParts()'
     */
    public void testGetParts() {
        Map<Integer, Integer> parts = info.getParts();
        assertNotNull(parts);
        assertEquals(1, parts.size());
        Iterator<Integer> i = parts.keySet().iterator();
        while (i.hasNext()) {
        	Integer id = i.next();
        	Part part = ItemResourceUtil.findItemResource(id);
            assertEquals(ItemResourceUtil.backhoeID, part.getID());
            assertEquals(1, (int) parts.get(id));
        }
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getPrerequisiteStage()'
     */
    public void testGetPrerequisiteStage() {
        assertNull(info.getPrerequisiteStage());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getResources()'
     */
    public void testGetResources() {
        Map<Integer, Double> resources = info.getResources();
        assertNotNull(resources);
        assertEquals(1, resources.size());
        Iterator<Integer> i = resources.keySet().iterator();
        while (i.hasNext()) {
            Integer id = i.next();
            AmountResource expected = ResourceUtil.sandAR;
            AmountResource resource = ResourceUtil.findAmountResource(id);
            assertEquals(expected.getName(), resource.getName());
            assertEquals(expected.getPhase(), resource.getPhase());
            assertEquals(1D, resources.get(id));
        }
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getType()'
     */
    public void testGetType() {
        assertEquals(ConstructionStageInfo.FOUNDATION, info.getType());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getVehicles()'
     */
    public void testGetVehicles() {
        List<ConstructionVehicleType> vehicles = info.getVehicles();
        assertEquals(1, vehicles.size());
        ConstructionVehicleType vehicle = vehicles.get(0);
        assertEquals("Light Utility Vehicle", vehicle.getVehicleType());
        assertEquals(LightUtilityVehicle.class, vehicle.getVehicleClass());
        List<Integer> parts = vehicle.getAttachmentParts();
        assertEquals(1, parts.size());
//        assertEquals("attachment part", ItemResourceUtil.findItemResource(parts.get(2)).getName());
//        assertEquals("attachment part", parts.get(2).getName());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.
     * construction.ConstructionStageInfo.getWorkTime()'
     */
    public void testGetWorkTime() {
        assertEquals(10000D, info.getWorkTime());
    }
}