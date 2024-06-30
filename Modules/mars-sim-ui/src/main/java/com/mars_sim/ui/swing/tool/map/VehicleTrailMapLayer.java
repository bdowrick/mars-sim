/*
 * Mars Simulation Project
 * VehicleTrailMapLayer.java
 * @date 2022-07-31
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;

import com.mars_sim.core.tool.SimulationConstants;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.mapdata.location.IntPoint;
import com.mars_sim.mapdata.map.Map;
import com.mars_sim.mapdata.map.MapLayer;

/**
 * The VehicleTrailMapLayer is a graphics layer to display vehicle trails.
 */
public class VehicleTrailMapLayer implements MapLayer, SimulationConstants {

	// Data members
	private Vehicle singleVehicle;

	/**
	 * Sets the single vehicle trail to display. Set to null if display all vehicle
	 * trails.
	 * 
	 * @param singleVehicle the vehicle to display trail.
	 */
	public void setSingleVehicle(Vehicle singleVehicle) {
		this.singleVehicle = singleVehicle;
	}

	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g         graphics context of the map display.
	 */
	@Override
	public void displayLayer(Coordinates mapCenter, Map baseMap, Graphics g) {

		// Set trail color
		Color c = (baseMap.getMapMetaData().isColourful() ? Color.BLACK : new Color(0, 96, 0));
		g.setColor(c);

		// Draw trail
		if (singleVehicle != null)
			displayTrail(singleVehicle, mapCenter, baseMap, g);
		else {
			Iterator<Vehicle> i = unitManager.getVehicles().iterator();
			while (i.hasNext())
				displayTrail(i.next(), mapCenter, baseMap, g);
		}
	}

	/**
	 * Displays the trail behind a vehicle.
	 * 
	 * @param vehicle   the vehicle to display.
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g         the graphics context.
	 */
	private void displayTrail(Vehicle vehicle, Coordinates mapCenter, Map baseMap, Graphics g) {

		// Get map angle.
		double angle = baseMap.getHalfAngle();

		// Draw trail.
		IntPoint oldSpot = null;
		Iterator<Coordinates> j = (new ArrayList<>(vehicle.getTrail())).iterator();
		while (j.hasNext()) {
			Coordinates trailSpot = j.next();
			if (trailSpot != null) {
				if (mapCenter.getAngle(trailSpot) < angle) {
					IntPoint spotLocation = MapUtils.getRectPosition(trailSpot, mapCenter, baseMap);
					if ((oldSpot == null))
						g.drawRect(spotLocation.getiX(), spotLocation.getiY(), 1, 1);
					else if (!spotLocation.equals(oldSpot))
						g.drawLine(oldSpot.getiX(), oldSpot.getiY(), spotLocation.getiX(), spotLocation.getiY());
					oldSpot = spotLocation;
				}
			}
		}
	}
}
