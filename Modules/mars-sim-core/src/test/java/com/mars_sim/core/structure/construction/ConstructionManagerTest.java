/**
 * Mars Simulation Project
 * ConstructionManagerTest.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */

package com.mars_sim.core.structure.construction;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;

import junit.framework.TestCase;

/**
 * Unit test for the ConstructionManager class.
 */
public class ConstructionManagerTest extends TestCase {

    // Data members
    ConstructionManager manager;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        SimulationConfig.instance().loadConfig();
        Simulation.instance().testRun();
        
//      UnitManager unitManager = Simulation.instance().getUnitManager();
//		Iterator<Settlement> i = unitManager.getSettlements().iterator();
        Settlement settlement = new MockSettlement();
        manager = new ConstructionManager(settlement);
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.getConstructionSites()'
     */
    public void testGetConstructionSites() {
        ConstructionSite site = manager.createNewConstructionSite();
        manager.createNewConstructionSite();
        assertEquals(2, manager.getConstructionSites().size());
        assertEquals(site, manager.getConstructionSites().get(0));
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.getConstructionSitesNeedingMission()'
     */
    public void testGetConstructionSitesNeedingMission() {
        manager.createNewConstructionSite();
        ConstructionSite site2 = manager.createNewConstructionSite();
        manager.createNewConstructionSite();
        assertEquals(3, manager.getConstructionSitesNeedingConstructionMission().size());
        site2.setUndergoingConstruction(true);
        assertEquals(2, manager.getConstructionSitesNeedingConstructionMission().size());
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.createNewConstructionSite()'
     */
    public void testCreateNewConstructionSite() {
        ConstructionSite site = manager.createNewConstructionSite();
        assertNotNull(site);
    }

    /*
     * Test method for 'com.mars_sim.simulation.structure.construction.
     * ConstructionManager.getConstructionValues()'
     */
    public void testGetConstructionValues() {
        ConstructionValues values = manager.getConstructionValues();
        assertNotNull(values);
    }
}