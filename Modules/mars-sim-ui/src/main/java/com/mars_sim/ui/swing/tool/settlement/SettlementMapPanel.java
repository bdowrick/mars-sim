/**
 * Mars Simulation Project
 * SettlementMapPanel.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JPanel;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.construction.ConstructionSite;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.MoreMath;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.UIConfig;

/**
 * A panel for displaying the settlement map.
 */
@SuppressWarnings("serial")
public class SettlementMapPanel extends JPanel {

	// Property names for UI Config
	private static final String BUILDING_LBL_PROP = "BUILDING_LABELS";
	private static final String CONSTRUCTION_LBL_PROP = "CONSTRUCTION_LABELS";
	private static final String PERSON_LBL_PROP = "PERSON_LABELS";
	private static final String VEHICLE_LBL_PROP = "VEHICLE_LABELS";
	private static final String ROBOT_LBL_PROP = "ROBOT_LABELS";
	private static final String SPOT_LBL_PROP = "SPOT_LABELS_";
	private static final String SETTLEMENT_PROP = "SETTLEMENT";
	private static final String DAYLIGHT_PROP = "DAYLIGHT_LAYER";
	private static final String X_PROP = "XPOS";
	private static final String Y_PROP = "YPOS";
	private static final String SCALE_PROP = "SCALE";
	private static final String ROTATION_PROP = "ROTATION";

	// Static members.
	private static final double WIDTH = 6D;
	public static final double DEFAULT_SCALE = 10D;

	// Data members
	private boolean exit = true;

	private double xPos;
	private double yPos;
	private double rotation;
	private double scale;

	/** Last X mouse drag position. */
	private int xLast;
	/** Last Y mouse drag position. */
	private int yLast;

	private boolean showBuildingLabels;
	private boolean showConstructionLabels;
	private boolean showPersonLabels;
	private boolean showVehicleLabels;
	private boolean showRobotLabels;
	private Set<FunctionType> showSpotLabels = new HashSet<>();

	private boolean showDaylightLayer;

	private MainDesktopPane desktop;

	private SettlementWindow settlementWindow;
	private Settlement settlement;
	private PopUpUnitMenu menu;
	private SettlementTransparentPanel settlementTransparentPanel;

	private DayNightMapLayer dayNightMapLayer;

	private List<SettlementMapLayer> mapLayers;
	private Map<Settlement, Person> selectedPerson;
	private Map<Settlement, Robot> selectedRobot;
	private Map<Settlement, Building> selectedBuilding;

	private Font sansSerif = new Font("SansSerif", Font.BOLD, 11);

	/**
	 * Constructor 1: A panel for displaying a settlement map.
	 */
	public SettlementMapPanel(MainDesktopPane desktop, final SettlementWindow settlementWindow,
							Properties userSettings) {
		super();
		this.settlementWindow = settlementWindow;
		this.desktop = desktop;

		UnitManager unitManager = desktop.getSimulation().getUnitManager();
		
		List<Settlement> settlements = new ArrayList<>(unitManager.getSettlements());
		
		if (!settlements.isEmpty()) {
			Collections.sort(settlements);

			// Search for matching settlement
			String userChoice = ((userSettings != null) && userSettings.containsKey(SETTLEMENT_PROP) ?
											userSettings.getProperty(SETTLEMENT_PROP) : null);
			if (userChoice != null) {
				for (Settlement s : settlements) {
					if (s.getName().equals(userChoice)) {
						settlement = s;
					}
				}
			}
										
			if (settlement == null) {
				settlement = settlements.get(0);
			}
		}
		
		setLayout(new BorderLayout());

		setDoubleBuffered(true);

		// Initialize data members.
		xPos = UIConfig.extractDouble(userSettings, X_PROP, 0D);
		yPos = UIConfig.extractDouble(userSettings, Y_PROP, 0D);
		rotation = UIConfig.extractDouble(userSettings, ROTATION_PROP, 0D);
		scale = UIConfig.extractDouble(userSettings, SCALE_PROP, DEFAULT_SCALE);
		showBuildingLabels = UIConfig.extractBoolean(userSettings, BUILDING_LBL_PROP, false);
		showConstructionLabels = UIConfig.extractBoolean(userSettings, CONSTRUCTION_LBL_PROP, false);
		showPersonLabels = UIConfig.extractBoolean(userSettings, PERSON_LBL_PROP, false);
		showVehicleLabels = UIConfig.extractBoolean(userSettings, VEHICLE_LBL_PROP, false);
		showRobotLabels = UIConfig.extractBoolean(userSettings, ROBOT_LBL_PROP, false);
		showDaylightLayer = UIConfig.extractBoolean(userSettings, DAYLIGHT_PROP, false); 

		for(FunctionType ft : FunctionType.values()) {
			if (UIConfig.extractBoolean(userSettings, SPOT_LBL_PROP + ft.name(), false)) {
				showSpotLabels.add(ft);
			}
		}
		selectedBuilding = new HashMap<>();
		selectedPerson = new HashMap<>();
		selectedRobot = new HashMap<>();
	}

	void createUI() {

		initLayers(desktop);

		// Set foreground and background colors.
		setOpaque(false);
		setBackground(new Color(0,0,0,128));

		setForeground(Color.ORANGE);

		detectMouseMovement();
		setFocusable(true);
		requestFocusInWindow();

		setVisible(true);

		repaint();
	}

	/**
	 * Initializes map layers.
	 * 
	 * @param desktop
	 */
	public void initLayers(MainDesktopPane desktop) {

		// Set up the dayNightMapLayer layers
		dayNightMapLayer = new DayNightMapLayer(this);

		// Check the DayNightLayer at the start of the sim
		setShowDayNightLayer(false);

		// Create map layers.
		mapLayers = new ArrayList<>();
		mapLayers.add(new BackgroundTileMapLayer(this));
		mapLayers.add(dayNightMapLayer);
		mapLayers.add(new BuildingMapLayer(this));
		mapLayers.add(new ConstructionMapLayer(this));
		mapLayers.add(new VehicleMapLayer(this));
		mapLayers.add(new PersonMapLayer(this));
		mapLayers.add(new RobotMapLayer(this));

		settlementTransparentPanel = new SettlementTransparentPanel(desktop, this);
		settlementTransparentPanel.createAndShowGUI();
		settlementTransparentPanel.getSettlementListBox().setSelectedItem(settlement);

		// Loads the value of scale possibly modified from UIConfig's Properties
		settlementTransparentPanel.setZoomValue((int)scale);
		
		repaint();
	}

	public void detectMouseMovement() {

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent evt) {
				// Move map center based on mouse drag difference.
				int xDiff = evt.getX() - xLast;
				int yDiff = evt.getY() - yLast;
				moveCenter(1.0 * xDiff, 1.0 * yDiff);
				xLast = evt.getX();
				yLast = evt.getY();
			}

			@Override
			public void mouseMoved(MouseEvent evt) {
				int x = evt.getX();
				int y = evt.getY();
				
				settlementWindow.setPop(getSettlement().getNumCitizens());
				// Call to determine if it should display or remove the building coordinate within a building
				showBuildingCoord(x, y);
				// Display the pixel coordinate of the window panel
				// Note: the top left-most corner of window panel is (0,0)
				settlementWindow.setPixelXYCoord(x, y, false);
				// Display the settlement map coordinate of the hovering mouse pointer
				settlementWindow.setMapXYCoord(convertToSettlementLocation(x,y), false);

				if (exit) {
					exit = false;
				}
			}
		});

		addMouseListener(new MouseAdapter() {

			@Override
		    public void mouseEntered(MouseEvent evt) {
				exit = false;
			}

			@Override
			public void mouseExited(MouseEvent evt) {
				if (!exit)  {
					int x = evt.getX();
					int y = evt.getY();
					
					settlementWindow.setPop(getSettlement().getNumCitizens());
					// Remove the pixel coordinate of the window panel
					// Note: the top left most corner is (0,0)
					settlementWindow.setPixelXYCoord(x, y, true);
					// Remove the settlement map coordinate of the hovering mouse pointer
					settlementWindow.setMapXYCoord(convertToSettlementLocation(x,y), true);
					// Remove the building coordinate
					settlementWindow.setBuildingXYCoord(0, 0, true);
					exit = true;
				}
			}

			@Override
			public void mousePressed(MouseEvent evt) {
				// Set initial mouse drag position.
				xLast = evt.getX();
				yLast = evt.getY();
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					setCursor(new Cursor(Cursor.HAND_CURSOR));
					doPop(evt);
				}
				else
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

				// Reset them to zero to prevent over-dragging of the settlement map
				xLast = 0;
				yLast = 0;
			}
		});
	}

	/**
	 * Checks if the player selected an unit.
	 *
	 * @param evt
	 */
	private void doPop(final MouseEvent evt) {
		int x = evt.getX();
		int y = evt.getY();

		final ConstructionSite site = selectConstructionSiteAt(x, y);
		final Building building = selectBuildingAt(x, y);
		final Vehicle vehicle = selectVehicleAt(x, y);
		final Person person = selectPersonAt(x, y);
		final Robot robot = selectRobotAt(x, y);

		// Deconflict cases by the virtue of the if-else order below
		// when one or more are detected
		if (person != null) {
			setPopUp(evt, x, y, person);
		}
		else if (robot != null) {
			setPopUp(evt, x, y, robot);
		}
		else if (vehicle != null) {
			setPopUp(evt, x, y, vehicle);
		}
		else if (building != null) {
			setPopUp(evt, x, y, building);
		}
		else if (site != null) {
			setPopUp(evt, x, y, site);
		}
		
		repaint();
	}

	private void setPopUp(final MouseEvent evt, int x, int y, Unit unit) {
		menu = new PopUpUnitMenu(settlementWindow, unit);
		menu.show(evt.getComponent(), x, y);
	}
	
	/**
	 * Displays the specific x y coordinates within a building
	 * (based upon where the mouse is pointing at).
	 *
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 */
	public void showBuildingCoord(int xPixel, int yPixel) {
		
		boolean showBlank = true;

		Point.Double mousePos = convertToSettlementLocation(xPixel, yPixel);

		Iterator<Building> j = settlement.getBuildingManager().getBuildingSet().iterator();
		while (j.hasNext()) {
			Building building = j.next();

			if (!building.getInTransport()) {

				double width = building.getWidth();
				double length = building.getLength();
				int facing = (int) building.getFacing();
				double x = building.getPosition().getX();
				double y = building.getPosition().getY();
				double xx = 0;
				double yy = 0;

				if (facing == 0) {
					// Server Farm
					xx = width / 2D;
					yy = length / 2D;
				} 
				else if (facing == 90) {
					yy = width / 2D;
					xx = length / 2D;
				}
				else if (facing == 180) {
					// Loading Dock Garage
					xx = width / 2D;
					yy = length / 2D;
					
				} 
				else if (facing == 270) {
					// Most of the 7mx9m modules such as Inflatable Greenhouse, 
					// Fish Farm, Algae Pond, Command and Control, etc.
					yy = width / 2D;
					xx = length / 2D;
				}

				// Note: Both ERV Base and Starting ERV Base have 45 / 135 deg facing
				// Fortunately, they both have the same width and length
				// Future: Need to fix the xx and yy for the following two facings
				else if (facing == 45) {
					yy = width / 2D;
					xx = length / 2D;
				} else if (facing == 135) {
					yy = width / 2D;
					xx = length / 2D;
				}

				double mX = mousePos.getX();
				double mY = mousePos.getY();

				double rangeX = Math.round((mX - x) * 100.0) / 100.0;
				double rangeY = Math.round((mY - y) * 100.0) / 100.0;

				if (Math.abs(rangeX) <= Math.abs(xx) && Math.abs(rangeY) <= Math.abs(yy)) {
					if (facing == 0) {
						// Display the coordinate within a building of the hovering mouse pointer
						settlementWindow.setBuildingXYCoord(rangeX, rangeY, false);
					} else if (facing == 180) {
						// Display the coordinate within a building of the hovering mouse pointer
						settlementWindow.setBuildingXYCoord(-rangeX, -rangeY, false);
					} else if (facing == 90) {
						// Display the coordinate within a building of the hovering mouse pointer
						settlementWindow.setBuildingXYCoord(rangeY, rangeX, false);
					} else if (facing == 270) {
						// Display the coordinate within a building of the hovering mouse pointer
						settlementWindow.setBuildingXYCoord(-rangeY, rangeX, false);
					}
					// Note: not considering facing = 45 and 135 yet
					showBlank = false;
					break;
				}
			}
		}

		if (showBlank)
			// Remove the building coordinate
			settlementWindow.setBuildingXYCoord(0, 0, true);
	}

	/**
	 * Gets the settlement currently displayed.
	 *
	 * @return settlement or null if none.
	 */
	public synchronized Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Gets the SettlementWindow class.
	 *
	 * @return settlementWindow or null if none.
	 */
	public SettlementWindow getSettlementWindow() {
		return settlementWindow;
	}

	/**
	 * Sets the settlement to display.
	 *
	 * @param settlement the settlement.
	 */
	public synchronized void setSettlement(Settlement newSettlement) {
		if (newSettlement != settlement) {
			this.settlement = newSettlement;
			getSettlementTransparentPanel().getSettlementListBox()
				.setSelectedItem(settlement);
			repaint();
		}
	}

	/**
	 * Gets the map scale.
	 *
	 * @return scale (pixels per meter).
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * Sets the map scale.
	 *
	 * @param scale (pixels per meter).
	 */
	public void setScale(double scale) {
		this.scale = scale;
		repaint();
	}

	/**
	 * Gets the map rotation.
	 *
	 * @return rotation (radians).
	 */
	public double getRotation() {
		return rotation;
	}

	/**
	 * Sets the map rotation.
	 *
	 * @param rotation (radians).
	 */
	public void setRotation(double rotation) {
		this.rotation = rotation;
		repaint();
	}

	/**
	 * Resets the position, scale and rotation of the map. Separate function that
	 * only uses one repaint.
	 */
	public void reCenter() {
		xPos = 0D;
		yPos = 0D;
		setRotation(0D);
		scale = DEFAULT_SCALE;
		settlementTransparentPanel.setZoomValue((int)scale);
		repaint();
	}

	/**
	 * Moves the center of the map by a given number of pixels.
	 *
	 * @param xDiff the X axis pixels.
	 * @param yDiff the Y axis pixels.
	 */
	public void moveCenter(double xd, double yd) {
		setCursor(new Cursor(Cursor.MOVE_CURSOR));
		double xDiff = xd /= scale;
		double yDiff = yd /= scale;

		// Correct due to rotation of map.
		double c = MoreMath.cos(rotation);
		double s = MoreMath.sin(rotation);

		double realXDiff = c * xDiff + s * yDiff;
		double realYDiff = c * yDiff - s * xDiff;

		xPos += realXDiff;
		yPos += realYDiff;

		repaint();
	}

	/**
	 * Selects a person if any person is at the given x and y pixel position.
	 *
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selectedPerson;
	 */
	public Person selectPersonAt(int xPixel, int yPixel) {
		double range = WIDTH / scale;
		Point.Double settlementPosition = convertToSettlementLocation(xPixel, yPixel);
		Person foundPerson = null;

		Iterator<Person> i = settlement.getIndoorPeople().iterator(); // CollectionUtils.getPeopleInSettlementVicinity(settlement).iterator();
		while (i.hasNext()) {
			Person person = i.next();
			double distanceX = person.getPosition().getX() - settlementPosition.getX();
			double distanceY = person.getPosition().getY() - settlementPosition.getY();
			double distance = Math.hypot(distanceX, distanceY);
			if (distance <= range) {
				foundPerson = person;
			}
		}

		if (foundPerson != null) {
			selectPerson(foundPerson);

		}
		return foundPerson;
	}

	/**
	 * Selects the robot if any robot is at the given x and y pixel position.
	 *
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selectedRobot;
	 */
	public Robot selectRobotAt(int xPixel, int yPixel) {
		double range = WIDTH / scale;
		Point.Double settlementPosition = convertToSettlementLocation(xPixel, yPixel);
		Robot foundRobot = null;

		Iterator<Robot> i = CollectionUtils.getAssociatedRobotsInSettlementVicinity(settlement).iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
			double distanceX = robot.getPosition().getX() - settlementPosition.getX();
			double distanceY = robot.getPosition().getY() - settlementPosition.getY();
			double distance = Math.hypot(distanceX, distanceY);
			if (distance <= range) {
				foundRobot = robot;
			}
		}

		if (foundRobot != null) {
			selectRobot(foundRobot);

		}
		return foundRobot;
	}

	/**
	 * Selects a building.
	 *
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selectedBuilding
	 */
	public Building selectBuildingAt(int xPixel, int yPixel) {

		Building foundBuilding = null;
		
		Point.Double mousePos = convertToSettlementLocation(xPixel, yPixel);
		
		Iterator<Building> j = settlement.getBuildingManager().getBuildingSet().iterator();
		while (j.hasNext()) {
			Building building = j.next();

			if (!building.getInTransport()) {

				double width = building.getWidth();
				double length = building.getLength();
				int facing = (int) building.getFacing();
				double x = building.getPosition().getX();
				double y = building.getPosition().getY();
				double xx = 0;
				double yy = 0;

				if (facing == 0) {
					xx = width / 2D;
					yy = length / 2D;
				} else if (facing == 90) {
					yy = width / 2D;
					xx = length / 2D;
				}
				// Loading Dock Garage
				if (facing == 180) {
					xx = width / 2D;
					yy = length / 2D;
				} else if (facing == 270) {
					yy = width / 2D;
					xx = length / 2D;
				}

				// Note: Both ERV Base and Starting ERV Base have 45 / 135 deg facing
				// Fortunately, they both have the same width and length
				else if (facing == 45) {
					yy = width / 2D;
					xx = length / 2D;
				} else if (facing == 135) {
					yy = width / 2D;
					xx = length / 2D;
				}

				double cX = mousePos.getX();
				double cY = mousePos.getY();

				double rangeX = Math.round((cX - x) * 100.0) / 100.0; 
				double rangeY = Math.round((cY - y) * 100.0) / 100.0;

				if (Math.abs(rangeX) <= Math.abs(xx) && Math.abs(rangeY) <= Math.abs(yy)) {
					foundBuilding = building;
					selectBuilding(foundBuilding);
					return foundBuilding;
				}
			}
		}
		return foundBuilding;
	}

	/**
	 * Selects a construction site.
	 *
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selected construction site
	 */
	public ConstructionSite selectConstructionSiteAt(int xPixel, int yPixel) {
		Point.Double clickPosition = convertToSettlementLocation(xPixel, yPixel);
		ConstructionSite site = null;

		Iterator<ConstructionSite> j = settlement.getConstructionManager().getConstructionSites().iterator();
		while (j.hasNext()) {
			ConstructionSite s = j.next();

			if (!ConstructionMapLayer.getConstructionLabel(s).equals(Msg.getString("LabelMapLayer.noConstruction"))) {
				double width = s.getWidth();
				double length = s.getLength();
				int facing = (int) s.getFacing();
				double x = s.getPosition().getX();
				double y = s.getPosition().getY();
				double xx = 0;
				double yy = 0;

				if (facing == 0) {
					xx = width / 2D;
					yy = length / 2D;
				} else if (facing == 90) {
					yy = width / 2D;
					xx = length / 2D;
				}
				// Loading Dock Garage
				if (facing == 180) {
					xx = width / 2D;
					yy = length / 2D;
				} else if (facing == 270) {
					yy = width / 2D;
					xx = length / 2D;
				}

				// Note: Both ERV Base and Starting ERV Base have 45 / 135 deg facing
				// Fortunately, they both have the same width and length
				else if (facing == 45) {
					yy = width / 2D;
					xx = length / 2D;
				} else if (facing == 135) {
					yy = width / 2D;
					xx = length / 2D;
				}

				double distanceX = Math.abs(x - clickPosition.getX());
				double distanceY = Math.abs(y - clickPosition.getY());

				if (distanceX <= xx && distanceY <= yy) {
					site = s;
					break;
				}
			}
		}

		return site;
	}

	/**
	 * Selects a vehicle.
	 *
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selectedVehicle
	 */
	public Vehicle selectVehicleAt(int xPixel, int yPixel) {
		Point.Double pos = convertToSettlementLocation(xPixel, yPixel);

		Vehicle selectedVehicle = null;

		Iterator<Vehicle> j = settlement.getParkedGaragedVehicles().iterator(); // CollectionUtils.getVehiclesInSettlementVicinity(settlement).iterator();
		while (j.hasNext()) {
			Vehicle vehicle = j.next();
			double width = vehicle.getWidth(); // width is on y-axis ?
			double length = vehicle.getLength(); // length is on x-axis ?
			double newRange;

			// Select whichever longer
			if (width > length)
				newRange = width / 2.0;
			else
				newRange = length / 2.0;

			double x = vehicle.getPosition().getX();
			double y = vehicle.getPosition().getY();

			double distanceX = x - pos.getX();
			double distanceY = y - pos.getY();
			
			double distance = Math.hypot(distanceX, distanceY);
			if (distance <= newRange) {
				selectedVehicle = vehicle;
				break;
			}
		}
		return selectedVehicle;
	}

	/**
	 * Selects a vehicle, as used by TransportWizard.
	 *
	 * @param xLoc the position of the template building on the displayed map.
	 * @param yLoc the position of the template building on the displayed map.
	 * @return selectedVehicle
	 */
	public Vehicle selectVehicleAsObstacle(double xLoc, double yLoc) {

		Vehicle selectedVehicle = null;

		Iterator<Vehicle> j = settlement.getParkedGaragedVehicles().iterator(); // CollectionUtils.getVehiclesInSettlementVicinity(settlement).iterator();
		while (j.hasNext()) {
			Vehicle vehicle = j.next();
			double width = vehicle.getWidth(); // width is on y-axis ?
			double length = vehicle.getLength(); // length is on x-axis ?
			double buildingWidth = 10;
			double buildingLength = 10;
			double newRange;

			// Select whichever longer
			if (width > length)
				newRange = (width + buildingWidth) / 2.0;
			else
				newRange = (length + buildingLength) / 2.0;

			double x = vehicle.getPosition().getX();
			double y = vehicle.getPosition().getY();

			// distances between the center of the vehicle and the center of the building
			double distanceX = x - xLoc;
			double distanceY = y - yLoc;
			double distance = Math.hypot(distanceX, distanceY);
			if (distance <= newRange) {
				selectedVehicle = vehicle;

				repaint();
			}
		}
		return selectedVehicle;
	}


	/**
	 * Selects a person on the map.
	 *
	 * @param person the selected person.
	 */
	public void selectPerson(Person person) {
		if ((settlement != null) && (person != null)) {
			Person currentlySelected = selectedPerson.get(settlement);
			if (person.equals(currentlySelected)) {
				selectedPerson.put(settlement, null);
			} else {
				selectedPerson.put(settlement, person);
			}
		}
	}

	/**
	 * Displays the person on the map
	 *
	 * @param person
	 */
	public void displayPerson(Person person) {
		if (settlement != null && person != null)
			selectedPerson.put(settlement, person);
	}

	/**
	 * Gets the selected person for the current settlement.
	 *
	 * @return the selected person.
	 */
	public Person getSelectedPerson() {
		Person result = null;
		if (settlement != null) {
			result = selectedPerson.get(settlement);
		}
		return result;
	}

	/**
	 * Selects a building on the map.
	 *
	 * @param person the selected building.
	 */
	public void selectBuilding(Building building) {
		if ((settlement != null) && (building != null)) {
			Building currentlySelected = selectedBuilding.get(settlement);
			if (building.equals(currentlySelected)) {
				selectedBuilding.put(settlement, null);
			} else {
				selectedBuilding.put(settlement, building);
			}
		}
	}

	/**
	 * Gets the selected building for the current settlement.
	 *
	 * @return the selected building.
	 */
	public Building getSelectedBuilding() {
		Building result = null;
		if (settlement != null) {
			result = selectedBuilding.get(settlement);
		}
		return result;
	}

	/**
	 * Selects a robot on the map.
	 *
	 * @param robot the selected robot.
	 */
	public void selectRobot(Robot robot) {
		if ((settlement != null) && (robot != null)) {
			Robot currentlySelected = selectedRobot.get(settlement);
			if (robot.equals(currentlySelected)) {
				selectedRobot.put(settlement, null);
			} else {
				selectedRobot.put(settlement, robot);
			}
		}
	}

	/**
	 * Displays the robot on the map.
	 *
	 * @param robot
	 */
	public void displayRobot(Robot robot) {
		if (settlement != null && robot != null)
			selectedRobot.put(settlement, robot);
	}

	/**
	 * Gets the selected Robot for the current settlement.
	 *
	 * @return the selected Robot.
	 */
	public Robot getSelectedRobot() {
		Robot result = null;
		if (settlement != null) {
			result = selectedRobot.get(settlement);
		}
		return result;
	}

	/**
	 * Converts a pixel X,Y position to a X,Y (meter) position local to the
	 * settlement in view.
	 *
	 * @param xPixel the pixel X position.
	 * @param yPixel the pixel Y position.
	 * @return the X,Y settlement position.
	 */
	public Point.Double convertToSettlementLocation(int xPixel, int yPixel) {

		Point.Double result = new Point.Double(0D, 0D);

		double xDiff1 = (getWidth() / 2.0) - xPixel;
		double yDiff1 = (getHeight() / 2.0) - yPixel;

		double xDiff2 = xDiff1 / scale;
		double yDiff2 = yDiff1 / scale;

		// Correct due to rotation of map.
		double xDiff3 = (Math.cos(rotation) * xDiff2) + (Math.sin(rotation) * yDiff2);
		double yDiff3 = (Math.cos(rotation) * yDiff2) - (Math.sin(rotation) * xDiff2);

		double newXPos = xPos + xDiff3;
		double newYPos = yPos + yDiff3;

		result.setLocation(newXPos, newYPos);

		return result;
	}

	/**
	 * Checks if building labels should be displayed.
	 *
	 * @return true if building labels should be displayed.
	 */
	public boolean isShowBuildingLabels() {
		return showBuildingLabels;
	}

	/**
	 * Sets if building labels should be displayed.
	 *
	 * @param showLabels true if building labels should be displayed.
	 */
	void setShowBuildingLabels(boolean showLabels) {
		this.showBuildingLabels = showLabels;
		repaint();
	}

	/**
	 * Reverses the settings of the Spot label.
	 * 
	 * @param possible The range of possible values
	 */
	void reverseSpotLabels(Collection<FunctionType> possible) {
		if (!showSpotLabels.isEmpty()) {
			showSpotLabels.clear();
		}
		else {
			showSpotLabels.addAll(possible);
		}
	}

	/**
	 * Checks if building spots should be displayed.
	 * 
	 * @param ft
	 * @return true if building activity spots should be displayed.
	 */
	boolean isShowSpotLabels(FunctionType ft) {
		return showSpotLabels.contains(ft);
	}

	/**
	 * Gets all active Function Activity Spots enabled.
	 */
	Set<FunctionType> getShowSpotLabels() {
		return showSpotLabels;
	}

	/**
	 * Sets if spot labels should be displayed.
	 * 
	 * @param ft
	 * @param showLabels true if spot labels should be displayed.
	 */
	void setShowSpotLabels(FunctionType ft, boolean showLabels) {
		if (showLabels) {
			this.showSpotLabels.add(ft);
		}
		else {
			this.showSpotLabels.remove(ft);
		}
		repaint();
	}

	/**
	 * Checks if construction site labels should be displayed.
	 *
	 * @return true if construction site labels should be displayed.
	 */
	public boolean isShowConstructionLabels() {
		return showConstructionLabels;
	}

	/**
	 * Sets if construction site labels should be displayed.
	 *
	 * @param showLabels true if construction site labels should be displayed.
	 */
	public void setShowConstructionLabels(boolean showLabels) {
		this.showConstructionLabels = showLabels;
		repaint();
	}

	/**
	 * Checks if person labels should be displayed.
	 *
	 * @return true if person labels should be displayed.
	 */
	public boolean isShowPersonLabels() {
		return showPersonLabels;
	}

	/**
	 * Sets if person labels should be displayed.
	 *
	 * @param showLabels true if person labels should be displayed.
	 */
	public void setShowPersonLabels(boolean showLabels) {
		this.showPersonLabels = showLabels;
		repaint();
	}

	/**
	 * Checks if Robot labels should be displayed.
	 *
	 * @return true if Robot labels should be displayed.
	 */
	public boolean isShowRobotLabels() {
		return showRobotLabels;
	}

	/**
	 * Sets if Robot labels should be displayed.
	 *
	 * @param showLabels true if Robot labels should be displayed.
	 */
	public void setShowRobotLabels(boolean showLabels) {
		this.showRobotLabels = showLabels;
		repaint();
	}

	/**
	 * Checks if vehicle labels should be displayed.
	 *
	 * @return true if vehicle labels should be displayed.
	 */
	public boolean isShowVehicleLabels() {
		return showVehicleLabels;
	}

	/**
	 * Sets if vehicle labels should be displayed.
	 *
	 * @param showLabels true if vehicle labels should be displayed.
	 */
	public void setShowVehicleLabels(boolean showLabels) {
		this.showVehicleLabels = showLabels;
		repaint();
	}

	/**
	 * Checks if DaylightLayer should be displayed.
	 *
	 * @return true if DaylightLayer should be displayed.
	 */
	public boolean isDaylightTrackingOn() {
		return showDaylightLayer;
	}

	/**
	 * Sets if DayNightLayer should be displayed.
	 *
	 * @param showDayNightLayer true if DayNightLayer should be displayed.
	 */
	public void setShowDayNightLayer(boolean showDayNightLayer) {
		this.showDaylightLayer = showDayNightLayer;
		repaint();
	}

	public DayNightMapLayer getDayNightMapLayer() {
		return dayNightMapLayer;
	}


	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (desktop != null && settlementWindow.isShowing() && desktop.isToolWindowOpen(SettlementWindow.NAME)) {
			Graphics2D g2d = (Graphics2D) g;

			g2d.setFont(sansSerif);

			// Set graphics rendering hints.
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

			// Display all map layers.
			var viewpoint = new MapViewPoint(g2d, xPos, yPos, getWidth(), getHeight(), rotation, scale);
			for(SettlementMapLayer layer : mapLayers) {
				layer.displayLayer(settlement, viewpoint);
			}
		}
	}

	public SettlementTransparentPanel getSettlementTransparentPanel() {
		return settlementTransparentPanel;
	}
	
    void update(ClockPulse pulse) {
		settlementTransparentPanel.update(pulse);
		repaint();
	}

	/**
	 * Gets the user display settings.
	 */
	Properties getUIProps() {
		Properties props = new Properties();
		props.setProperty(SETTLEMENT_PROP, settlement.getName());

		props.setProperty(BUILDING_LBL_PROP, Boolean.toString(showBuildingLabels));
		props.setProperty(CONSTRUCTION_LBL_PROP, Boolean.toString(showConstructionLabels));
		props.setProperty(PERSON_LBL_PROP, Boolean.toString(showPersonLabels));
		props.setProperty(VEHICLE_LBL_PROP, Boolean.toString(showVehicleLabels));
		props.setProperty(ROBOT_LBL_PROP, Boolean.toString(showRobotLabels));
		props.setProperty(DAYLIGHT_PROP, Boolean.toString(showDaylightLayer));
		props.setProperty(X_PROP, Double.toString(xPos));
		props.setProperty(Y_PROP, Double.toString(yPos));
		props.setProperty(ROTATION_PROP, Double.toString(rotation));
		props.setProperty(SCALE_PROP, Double.toString(scale));

		for(FunctionType ft : showSpotLabels) {
			props.setProperty(SPOT_LBL_PROP + ft.name(), "true");
		}
		return props;
	}

	/**
	 * Cleans up the map panel for disposal.
	 */
	public void destroy() {
		settlementTransparentPanel.destroy();
		
		menu = null;
		settlement = null;
		selectedPerson = null;
		settlementWindow = null;

		// Destroy all map layers.
		Iterator<SettlementMapLayer> i = mapLayers.iterator();
		while (i.hasNext()) {
			i.next().destroy();
		}

		mapLayers = null;
		selectedRobot = null;
		settlementTransparentPanel = null;
	}
}
