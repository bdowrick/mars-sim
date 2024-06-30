/*
 * Mars Simulation Project
 * PlanetOrbit.java
 * @date 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.core.astroarts;

/**
 * PlanetOrbit Class
 */
public class PlanetOrbit {
	private double	jd;
	private int		nDivision;
	private  Xyz	orbit[];
	
	/**
	 * Constructor.
	 * 
	 * @param planetNo
	 * @param atime
	 * @param nDivision
	 */
	public PlanetOrbit(int planetNo, ATime atime, int nDivision) {
		this.jd = atime.getJd();
		this.nDivision = nDivision;
		PlanetElm planetElm = new PlanetElm(planetNo, atime);
		orbit = new Xyz[nDivision + 1];
		doGetPlanetOrbit(planetElm);
		Matrix vec = Matrix.VectorConstant(planetElm.peri * Math.PI/180.0,
										   planetElm.node * Math.PI/180.0,
										   planetElm.incl * Math.PI/180.0,
										   atime);
		Matrix prec = Matrix.PrecMatrix(atime.getJd(), 2451512.5);
		for (int i = 0; i <= nDivision; i++) {
			orbit[i] = orbit[i].Rotate(vec).Rotate(prec);
		}
	}
	
	
	private void doGetPlanetOrbit(PlanetElm planetElm) {
		double ae2 = -2.0 * planetElm.axis * planetElm.e;
		double t = Math.sqrt(1.0 - planetElm.e * planetElm.e);
		int xp1 = 0;
		int xp2 = (nDivision/2);
		int xp3 = (nDivision/2);
		int xp4 = nDivision;
		double E = 0.0;
		for (int i = 0; i <= (nDivision/4); i++, E += (360.0 / nDivision)) {
			double rcosv = planetElm.axis * (UdMath.udcos(E) - planetElm.e);
			double rsinv = planetElm.axis * t * UdMath.udsin(E);
			orbit[xp1++] = new Xyz(rcosv,        rsinv, 0.0);
			orbit[xp2--] = new Xyz(ae2 - rcosv,  rsinv, 0.0);
			orbit[xp3++] = new Xyz(ae2 - rcosv, -rsinv, 0.0);
			orbit[xp4--] = new Xyz(rcosv,       -rsinv, 0.0);
		}
	}
	
	/**
	 * Gets Epoch.
	 */
	public double getEpoch() {
		return jd;
	}
	
	/**
	 * Gets Division Count.
	 */
	public int getDivision() {
		return nDivision;
	}
	
	/**
	 * Gets Orbit Point.
	 */
	public Xyz getAt(int nIndex) {
		return orbit[nIndex];
	}
}
