/*
 * Mars Simulation Project
 * RobotGood.java
 * @date 2024-06-29
 * @author Barry Evans
 */
package com.mars_sim.core.goods;

import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;

/**
 * This class represents how a Robot can be traded.
 */
class RobotGood extends Good {
	
	private static final long serialVersionUID = 1L;
	
    private static final double INITIAL_ROBOT_DEMAND = 10;
    private static final double INITIAL_ROBOT_SUPPLY = 10;

	private static final int ROBOT_VALUE = 200;
	private static final double ROBOT_FLATTENING_FACTOR = 2;
	
	/** The fixed flatten demand for this resource. */
	private double flattenDemand;
	/** The projected demand of each refresh cycle. */
	private double projectedDemand;
	
    private RobotType robotType;

    public RobotGood(RobotType type) {
        super(type.getName(), RobotType.getResourceID(type));
        this.robotType = type;
        
        // Calculate fixed values
     	flattenDemand = calculateFlattenDemand(type);
    }

    /**
	 * Calculates the flatten demand based on the equipment type.
	 * 
	 * @param robotType
	 * @return
	 */
	private double calculateFlattenDemand(RobotType robotType) {
//		if (robotType == RobotType.) {
//			return _FLATTENING_FACTOR;
//        }
		
		return ROBOT_FLATTENING_FACTOR; 
	}
	
    /**
     * Gets the flattened demand.
     * 
     * @return
     */
	@Override
    public double getFlattenDemand() {
    	return flattenDemand;
    }
    
    /**
     * Gets the projected demand of this resource.
     * 
     * @return
     */
	@Override
    public double getProjectedDemand() {
    	return projectedDemand;
    }
	
    @Override
    public GoodCategory getCategory() {
        return GoodCategory.ROBOT;
    }

    @Override
    public double getMassPerItem() {
        return Robot.EMPTY_MASS;
    }

    @Override
    public GoodType getGoodType() {
        // TODO Must be a better way 
        switch(robotType) {
            case CHEFBOT: return GoodType.CHEFBOT;
            case CONSTRUCTIONBOT: return GoodType.CONSTRUCTIONBOT;
            case DELIVERYBOT: return GoodType.DELIVERYBOT;
            case GARDENBOT: return GoodType.GARDENBOT;
            case MAKERBOT: return GoodType.MAKERBOT;
            case MEDICBOT: return GoodType.MEDICBOT;
            case REPAIRBOT: return GoodType.REPAIRBOT;
        }

        throw new IllegalStateException("Cannot mapt robot type " + robotType + " to GoodType");
    }

    @Override
    protected double computeCostModifier() {
        return ROBOT_VALUE;
    }

    @Override
    public double getNumberForSettlement(Settlement settlement) {
		// Get number of robots.
		return (int) settlement.getAllAssociatedRobots().stream()
                        .filter( r -> r.getRobotType() == robotType)
                        .count();	
    }

    @Override
    double getPrice(Settlement settlement, double value) {
        double mass = Robot.EMPTY_MASS;
        double quantity = settlement.getInitialNumOfRobots() ;
        double factor = Math.log(mass/50.0 + 1) / (5 + Math.log(quantity + 1));
        // Need to increase the value for robots
        return getCostOutput() * (1 + 2 * factor * Math.log(value + 1));
    }

    @Override
    double getDefaultDemandValue() {
        return INITIAL_ROBOT_DEMAND;
    }

    @Override
    double getDefaultSupplyValue() {
        return INITIAL_ROBOT_SUPPLY;
    }

    @Override
    void refreshSupplyDemandValue(GoodsManager owner) {
		Settlement settlement = owner.getSettlement();
		double previousDemand = owner.getDemandValue(this);

		double totalDemand = 0;
		
		// Determine projected demand for this cycle
		double projectedDemand = determineRobotDemand(owner, settlement);

		projectedDemand = Math.min(HIGHEST_PROJECTED_VALUE, projectedDemand);
		
		this.projectedDemand = projectedDemand;
		
		double projected = projectedDemand * flattenDemand;
				
		double totalSupply = getNumberForSettlement(settlement);
				
		owner.setSupplyValue(this, totalSupply);
		
		// This method is not using cache
		double trade = owner.determineTradeDemand(this);
		if (previousDemand == 0) {
			totalDemand = .5 * projected 
						+ .5 * trade;
		}
		else {
			// Intentionally lose 2% of its value
			totalDemand = .97 * previousDemand 
						+ .005 * projected 
						+ .005 * trade;
		}
				
		owner.setDemandValue(this, totalDemand);
    }
    
	/**
	 * Determines the demand for a robot type.
	 *
	 * @param settlement the location of this demand
	 * @return demand
	 */
	private double determineRobotDemand(GoodsManager owner, Settlement settlement) {
		double baseDemand = 1.5;

		int pop = settlement.getNumCitizens();
		
		if (robotType == RobotType.MAKERBOT) {
			
			double tech = JobUtil.numJobs(JobType.TECHNICIAN, settlement);
			
			double engineer = JobUtil.numJobs(JobType.ENGINEER, settlement);
			
			double comp = JobUtil.numJobs(JobType.COMPUTER_SCIENTIST, settlement);
			
			double makerFactor = 1 + .65 * engineer + .25 * tech + .1 * comp;
			
			baseDemand += baseDemand / makerFactor * pop / 6;
		}
		
		else if (robotType == RobotType.REPAIRBOT) {

			double tech = JobUtil.numJobs(JobType.TECHNICIAN, settlement);
			
			double engineer = JobUtil.numJobs(JobType.ENGINEER, settlement);
			
			double repairFactor = 1 + .75 * tech + .25 * engineer;
			
			baseDemand += baseDemand / repairFactor * pop / 6;
		}
		
		else if (robotType == RobotType.CONSTRUCTIONBOT) {
	
			double engineer = JobUtil.numJobs(JobType.ENGINEER, settlement);
			
			double comp = JobUtil.numJobs(JobType.COMPUTER_SCIENTIST, settlement);
			
			double architect = JobUtil.numJobs(JobType.ARCHITECT, settlement);
		
			double constructFactor = 1 + .60 * architect + .25 * engineer + .15 * comp;
			baseDemand += baseDemand / constructFactor * pop / 6;
		}
		
		else if (robotType == RobotType.GARDENBOT) {
			double botanistFactor = 1 + JobUtil.numJobs(JobType.BOTANIST, settlement);
			
			baseDemand += baseDemand / botanistFactor * pop / 6;
		}
		
		else if (robotType == RobotType.CHEFBOT) {
			double chiefFactor = 1 + JobUtil.numJobs(JobType.CHEF, settlement);
			
			baseDemand += baseDemand / chiefFactor * pop / 6;
		}
		
		else if (robotType == RobotType.DELIVERYBOT) {
			double trader = JobUtil.numJobs(JobType.TRADER, settlement);
			
			double pilot = JobUtil.numJobs(JobType.PILOT, settlement);
			
			double traderFactor = 1 + .75 * trader + .25 * pilot;
			
			baseDemand += baseDemand / traderFactor * pop / 6;
		}
		
		else if (robotType == RobotType.MEDICBOT) {
			double doc = JobUtil.numJobs(JobType.DOCTOR, settlement);
			
			double psy = JobUtil.numJobs(JobType.PSYCHOLOGIST, settlement);
			
			double medicFactor = 1 + .75 * doc + .25 * psy;
			
			baseDemand += baseDemand / medicFactor * pop / 6;
		}
		
		return baseDemand;
	}

		
}
