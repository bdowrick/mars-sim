/*
 * Mars Simulation Project
 * PartPackageConfig.java
 * @date 2023-10-20
 * @author Scott Davis
 */
package com.mars_sim.core.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Provides configuration information about part packages. Uses a JDOM document
 * to get the information.
 */
public class PartPackageConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(PartPackageConfig.class.getName());

	// Element names
	private final String PART_PACKAGE = "part-package";
	private final String PART = "part";
	private final String NAME = "name";
	private final String TYPE = "type";
	private final String NUMBER = "number";

	// Data members
	private Collection<PartPackage> partPackages;

	/**
	 * Constructor.
	 * 
	 * @param partPackageDoc the part package XML document.
	 * @throws Exception if error reading XML document
	 */
	public PartPackageConfig(Document partPackageDoc) {
		loadPartPackages(partPackageDoc);
	}

	/**
	 * Loads the part packages for the simulation.
	 * 
	 * @param partPackageDoc the part package XML document.
	 * @throws Exception if error reading XML document.
	 */
	private synchronized void loadPartPackages(Document partPackageDoc) {
		if (partPackages != null) {
			// just in case if another thread is being created
			return;
		}
		
		List<PartPackage> newList = new ArrayList<>();
		
		Element root = partPackageDoc.getRootElement();
		List<Element> partPackageNodes = root.getChildren(PART_PACKAGE);
		for (Element partPackageElement : partPackageNodes) {
			PartPackage partPackage = new PartPackage();

			partPackage.name = partPackageElement.getAttributeValue(NAME);

			List<Element> partNodes = partPackageElement.getChildren(PART);
			for (Element partElement : partNodes) {
				String partType = partElement.getAttributeValue(TYPE);
				Part part = (Part) ItemResourceUtil.findItemResource(partType);
				if (part == null)
					logger.severe(partType + " shows up in part_packages.xml but doesn't exist in parts.xml.");
				else {
					int partNumber = Integer.parseInt(partElement.getAttributeValue(NUMBER));
					partPackage.parts.put(part, partNumber);
				}
			}
			// Add partPackage to newList.
			newList.add(partPackage);
		}
		
		// Assign the newList now built
		partPackages = Collections.unmodifiableList(newList);
	}

	/**
	 * Gets the parts stored in a given part package.
	 * 
	 * @param name the part package name.
	 * @return parts and their numbers.
	 * @throws Exception if part package name does not match any packages.
	 */
	public Map<Part, Integer> getPartsInPackage(String name) {
		Map<Part, Integer> result = null;

		PartPackage foundPartPackage = null;
		for (PartPackage partPackage : partPackages) {
			if (partPackage.name.equals(name)) {
				foundPartPackage = partPackage;
				break;
			}
		}

		if (foundPartPackage != null)
			result = new HashMap<>(foundPartPackage.parts);
		else
			throw new IllegalStateException("name: " + name + " does not match any part packages.");

		return result;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		Iterator<PartPackage> i = partPackages.iterator();
		while (i.hasNext()) {
			i.next().parts.clear();
		}
		partPackages = null;
	}

	/**
	 * Private inner class for storing part packages.
	 */
	private static class PartPackage implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private String name;
		private Map<Part, Integer> parts;

		private PartPackage() {
			parts = new HashMap<>();
		}
	}
}
