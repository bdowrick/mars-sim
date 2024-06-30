/*
 * Mars Simulation Project
 * ClockPulse.java
 * @date 2022-06-26
 * @author Barry Evans
 */
package com.mars_sim.core.time;

public class ClockPulse {
	
	/** Initialized logger. */
	// may add back private static final SimLogger logger = SimLogger.getLogger(ClockPulse.class.getName())
	
	private boolean newSol;
	private boolean newHalfSol;
	private boolean newIntMillisol;
	
	/** The last millisol integer from the last pulse. */
	private int lastIntMillisol;
	/** The last sol from the last pulse. */
	private int lastSol;

	private long id;
	
	/** The sols passed since last pulse. */
	private double elapsed;
	/** The last millisol from the last pulse. */
	private double lastMillisol;
	
	private MarsTime marsTime;
	private MasterClock master;

	
	/**
	 * Creates a pulse defining a step forward in the simulation.
	 * 
	 * @param id 			Unique pulse ID. Sequential.
	 * @param elapsed 		This must be a final & positive number.
	 * @param marsTime
	 * @param master
	 * @param newSol 		Has the pulse just crossed into the new sol ? 
	 * @param isNewHalfSol 	Has the pulse just crossed the half sol ?
	 * @param newMSol 		Does this pulse start a new msol (an integer millisol) ?
	 */
	public ClockPulse(long id, double elapsed, 
					MarsTime marsTime, MasterClock master, 
					boolean newSol, boolean newHalfSol, boolean newMSol) {
		super();
		
		if ((elapsed <= 0) || !Double.isFinite(elapsed)) {
			throw new IllegalArgumentException("Elapsed time must be positive : " + elapsed);
		}
		
		this.id = id;
		this.elapsed = elapsed;
		this.marsTime = marsTime;
		this.master = master;
		this.newSol = newSol;
		this.newHalfSol = newHalfSol;
		this.newIntMillisol = newMSol;
	}

	public long getId() {
		return id;
	}
	
	/**
	 * Gets the elapsed real time since the last pulse.
	 * 
	 * @return
	 */
	public double getElapsed() {
		return elapsed;
	}
	
	/**
	 * Gets MarsClock instance.
	 * 
	 * @return
	 */
	public MarsTime getMarsTime() {
		return marsTime;
	}

	/**
	 * Gets MasterClock instance.
	 * 
	 * @return
	 */
	public MasterClock getMasterClock() {
		return master;
	}
	
	/**
	 * Is this a new sol ?
	 * 
	 * @return
	 */
	public boolean isNewSol() {
		return newSol;
	}
	
	/**
	 * Is this a new half sol ?
	 * 
	 * @return
	 */
	public boolean isNewHalfSol() {
		return newHalfSol;
	}
	
	/**
	 * Is this a new integer millisol ?
	 * 
	 * @return
	 */
	public boolean isNewMSol() {
		return newIntMillisol;
	}

	/**
	 * Creates a new pulse based on this one but add extra elapsed time.
	 * Note: This does not change any of the original flags; only the elapses time.
	 * 
	 * @param msolsSkipped 
	 * @return
	 */
	public ClockPulse addElapsed(double msolsSkipped) {
		double actualElapsed = msolsSkipped + elapsed;
		// Add the skipped millisols
		MarsTime newMars = marsTime.addTime(msolsSkipped);

		////////////////////////////////////////////////////////////////////////////////////		
		// NOTE: Any changes made below need to be brought to MasterClock's fireClockPulse()
		////////////////////////////////////////////////////////////////////////////////////
		double currentMillisol = marsTime.getMillisol();
		// Check if the simulation is just starting up		
//		boolean atStartup = actualElapsed > currentMillisol;
		// Get the current sol
		int currentSol = marsTime.getMissionSol();
		// Identify if this pulse crosses a sol
		boolean isNewSol = (lastSol != currentSol);
		// Updates lastSol
		if (isNewSol) {
//			logger.info("newSol: " + newSol 
//					+ "  isNewSol: " + isNewSol 
//					+ "  lastSol: " + lastSol 
//					+ "  currentSol: " + currentSol);
			lastSol = currentSol;
		}
		// Identify if it just passes half a sol
		boolean isNewHalfSol = isNewSol || (lastMillisol <= 500 && currentMillisol > 500);	

		if (isNewHalfSol) {
//			logger.info("newHalfSol: " + newHalfSol 
//					+ "  isNewHalfSol: " + isNewHalfSol 
//					+ "  lastMillisol: " + lastMillisol 
//					+ "  currentMillisol: " + currentMillisol);
		}
		
		// Update the lastMillisol
		lastMillisol = currentMillisol;	
		// Get the current millisol integer
		int currentIntMillisol = marsTime.getMillisolInt();
		// Checks if this pulse starts a new integer millisol
		boolean isNewIntMillisol = lastIntMillisol != currentIntMillisol; 
		// Update the lastIntMillisol
		if (isNewIntMillisol) {
			lastIntMillisol = currentIntMillisol;
		}
		////////////////////////////////////////////////////////////////////////////////////
		// Do NOT delete the following logger. For future debugging when changes are made //		
//		logger.info("newSol: " + newSol 
//				+ "  newHalfSol: " + newHalfSol 
//				+ "  isNewSol: " + isNewSol 
//				+ "  lastSol: " + lastSol 
//				+ "  currentSol: " + currentSol 
//				+ "  currentMillisol: " + currentMillisol
//				+ "  elapsed: " + elapsed 
//				+ "  actualElapsed: " + actualElapsed 
//				+ "  msolsSkipped: " + msolsSkipped 
//				);
		
		return new ClockPulse(id, actualElapsed, newMars, master, isNewSol, isNewHalfSol, isNewIntMillisol);
	}
}
