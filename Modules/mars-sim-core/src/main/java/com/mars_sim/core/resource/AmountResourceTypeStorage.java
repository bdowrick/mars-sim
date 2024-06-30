/*
 * Mars Simulation Project
 * AmountResourceTypeStorage.java
 * @date 2021-08-28
 * @author Scott Davis 
 */

package com.mars_sim.core.resource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage for types of amount resource.
 */
class AmountResourceTypeStorage implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members

	/** Capacity for each type of amount resource. */
	private Map<Integer, ResourceAmount> typeCapacities = null;

	/** Stored resources by type. */
	private Map<Integer, ResourceAmount> typeStored = null;

	/** Cache value for the total amount of resources stored. */
	private transient double totalAmountCache = 0D;
	private transient boolean totalAmountCacheDirty = true;

	/**
	 * Adds capacity for a resource type.
	 * 
	 * @param resource the resource.
	 * @param capacity the extra capacity amount (kg).
	 */
	void addAmountResourceTypeCapacity(AmountResource resource, double capacity) {
		addAmountResourceTypeCapacity(resource.getID(), capacity);
	}

	/**
	 * Adds capacity for a resource type.
	 * 
	 * @param resource the resource.
	 * @param capacity the extra capacity amount (kg).
	 */
	void addAmountResourceTypeCapacity(int resource, double capacity) {

		if (capacity < 0D) {
			throw new IllegalStateException("Cannot add negative type capacity: " + capacity);
		}

		if (typeCapacities == null) {
			typeCapacities = new ConcurrentHashMap<>();
		}

		if (hasARTypeCapacity(resource)) {
			ResourceAmount existingCapacity = typeCapacities.get(resource);
			existingCapacity.setAmount(existingCapacity.getAmount() + capacity);
		} else {
			typeCapacities.put(resource, new ResourceAmount(capacity));
		}
	}

	/**
	 * Removes capacity for a resource type.
	 * 
	 * @param resource the resource.
	 * @param capacity the capacity amount (kg).
	 */
	void removeAmountResourceTypeCapacity(AmountResource resource, double capacity) {
		removeTypeCapacity(resource.getID(), capacity);
	}

	/**
	 * Removes capacity for a resource type.
	 * 
	 * @param resource the resource.
	 * @param capacity the capacity amount (kg).
	 */
	void removeTypeCapacity(int resource, double capacity) {

		if (capacity < 0D) {
			throw new IllegalStateException("Cannot remove negative type capacity: " + capacity);
		}

		if (typeCapacities == null) {
			typeCapacities = new ConcurrentHashMap<>();
		}

		double existingCapacity = getAmountResourceTypeCapacity(resource);
		double newCapacity = existingCapacity - capacity;
		if (newCapacity > 0D) {
			if (hasARTypeCapacity(resource)) {
				ResourceAmount existingCapacityAmount = typeCapacities.get(resource);
				existingCapacityAmount.setAmount(newCapacity);
			} else {
				typeCapacities.put(resource, new ResourceAmount(newCapacity));
			}
		} else if (newCapacity == 0D) {
			typeCapacities.remove(resource);
		} else {
			throw new IllegalStateException("Insufficient existing resource type capacity to remove - existing: "
					+ existingCapacity + ", removed: " + capacity);
		}
	}

	/**
	 * Checks if storage has capacity for a resource type.
	 * 
	 * @param resource the resource.
	 * @return true if storage capacity.
	 */
	boolean hasAmountResourceTypeCapacity(AmountResource resource) {
		return hasARTypeCapacity(resource.getID());
	}

	/**
	 * Checks if storage has capacity for a resource type.
	 * 
	 * @param resource the resource.
	 * @return true if storage capacity.
	 */
	boolean hasARTypeCapacity(int resource) {

		boolean result = false;

		if (typeCapacities != null) {
			result = typeCapacities.containsKey(resource);
		}

		return result;
	}

	/**
	 * Gets the storage capacity for a resource type.
	 * 
	 * @param resource the resource.
	 * @return capacity amount (kg).
	 */
	double getAmountResourceTypeCapacity(AmountResource resource) {
		return getAmountResourceTypeCapacity(resource.getID());
	}

	/**
	 * Gets the storage capacity for a resource type.
	 * 
	 * @param resource the resource.
	 * @return capacity amount (kg).
	 */
	double getAmountResourceTypeCapacity(int resource) {

		double result = 0D;

		if (hasARTypeCapacity(resource)) {
			result = (typeCapacities.get(resource)).getAmount();
		}

		return result;
	}

	/**
	 * Gets the amount of a resource type stored.
	 * 
	 * @param resource the resource.
	 * @return stored amount (kg).
	 */
	double getAmountResourceTypeStored(AmountResource resource) {

		double result = 0D;

		ResourceAmount storedAmount = getARTypeStoredObject(resource.getID());
		if (storedAmount != null) {
			result = storedAmount.getAmount();
		}

		return result;
	}

	/**
	 * Gets the amount of a resource type stored.
	 * 
	 * @param resource the resource.
	 * @return stored amount (kg).
	 */
	double getAmountResourceTypeStored(int resource) {

		double result = 0D;

		ResourceAmount storedAmount = getARTypeStoredObject(resource);
		if (storedAmount != null) {
			result = storedAmount.getAmount();
		}

		return result;
	}

	/**
	 * Gets the amount of a resource type stored.
	 * 
	 * @param resource the resource.
	 * @return stored amount as ResourceAmount object.
	 */
	private ResourceAmount getAmountResourceTypeStoredObject(AmountResource resource) {

		ResourceAmount result = null;

		if (typeStored != null) {
			result = typeStored.get(resource.getID());
		}

		return result;
	}

	/**
	 * Gets the amount of a resource type stored.
	 * 
	 * @param resource the resource.
	 * @return stored amount as ResourceAmount object.
	 */
	private ResourceAmount getARTypeStoredObject(int resource) {

		ResourceAmount result = null;

		if (typeStored != null) {
			result = typeStored.get(resource);
		}

		return result;
	}

	/**
	 * Gets the total amount of resources stored.
	 * 
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return stored amount (kg).
	 */
	double getTotalAmountResourceTypesStored(boolean allowDirty) {

		if (totalAmountCacheDirty && !allowDirty) {
			// Update total amount cache.
			updateTotalAmountResourceTypesStored();
		}

		return totalAmountCache;
	}

	/**
	 * Gets the total amount of resources stored.
	 * 
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return stored amount (kg).
	 */
	double getTotalTypesStored(boolean allowDirty) {

		if (totalAmountCacheDirty && !allowDirty) {
			// Update total amount cache.
			updateTotalARTypesStored();
		}

		return totalAmountCache;
	}

	/**
	 * Updates the total amount of resources stored.
	 */
	private void updateTotalAmountResourceTypesStored() {

		double totalAmount = 0D;

		if (typeStored != null) {
			Map<Integer, ResourceAmount> tempMap = Collections.unmodifiableMap(typeStored);
			Iterator<Integer> i = tempMap.keySet().iterator();
			while (i.hasNext()) {
				totalAmount += tempMap.get(i.next()).getAmount();
			}
		}

		totalAmountCache = totalAmount;
		totalAmountCacheDirty = false;
	}

	/**
	 * Updates the total amount of resources stored.
	 */
	private void updateTotalARTypesStored() {

		double totalAmount = 0D;

		if (typeStored != null) {
			Map<Integer, ResourceAmount> tempMap = Collections.unmodifiableMap(typeStored);
			Iterator<Integer> i = tempMap.keySet().iterator();
			while (i.hasNext()) {
				totalAmount += tempMap.get(i.next()).getAmount();
			}
		}

		totalAmountCache = totalAmount;
		totalAmountCacheDirty = false;
	}

	/**
	 * Gets a set of resources stored.
	 * 
	 * @return set of resources.
	 */
	Set<AmountResource> getAllAmountResourcesStored() {
		Set<AmountResource> set = ConcurrentHashMap.newKeySet();
		for (int ar : getAllARStored()) {
			set.add(ResourceUtil.findAmountResource(ar));
		}
		return set;
	}

	/**
	 * Gets a set of resources stored.
	 * 
	 * @return set of resources.
	 */
	Set<Integer> getAllARStored() {

		Set<Integer> result = ConcurrentHashMap.newKeySet();

		if (typeStored != null) {
			Iterator<Integer> i = typeStored.keySet().iterator();
			while (i.hasNext()) {
				Integer resource = i.next();
				if (getAmountResourceTypeStored(resource) > 0D) {
					result.add(resource);
				}
			}
		}

		return result;
	}

	/**
	 * Gets the remaining capacity available for a resource type.
	 * 
	 * @param resource the resource.
	 * @return remaining capacity amount (kg).
	 */
	double getAmountResourceTypeRemainingCapacity(AmountResource resource) {

		double result = 0D;

		if (hasAmountResourceTypeCapacity(resource)) {
			result = getAmountResourceTypeCapacity(resource) - getAmountResourceTypeStored(resource);
		}

		return result;
	}

	/**
	 * Gets the remaining capacity available for a resource type.
	 * 
	 * @param resource the resource.
	 * @return remaining capacity amount (kg).
	 */
	double getARTypeRemainingCapacity(int resource) {

		double result = 0D;

		if (hasARTypeCapacity(resource)) {
			result = getAmountResourceTypeCapacity(resource) - getAmountResourceTypeStored(resource);
		}

		return result;
	}

	/**
	 * Store an amount of a resource type.
	 * 
	 * @param resource the resource.
	 * @param amount   the amount (kg).
	 */
	void storeAmountResourceType(AmountResource resource, double amount) {
		storeARType(resource.getID(), amount);
	}

	/**
	 * Store an amount of a resource type.
	 * 
	 * @param resource the resource.
	 * @param amount   the amount (kg).
	 */
	void storeARType(int resource, double amount) {

		if (amount < 0D) {
			throw new IllegalStateException("Cannot store negative amount of type: " + amount);
		}

		if (amount > 0D) {
			if (getARTypeRemainingCapacity(resource) >= amount) {

				// Set total amount cache to dirty since value is changing.
				totalAmountCacheDirty = true;

				if (typeStored == null) {
					typeStored = new ConcurrentHashMap<Integer, ResourceAmount>();
				}

				ResourceAmount stored = getARTypeStoredObject(resource);
				if (stored != null) {
					stored.setAmount(stored.getAmount() + amount);
				} else {
					typeStored.put(resource, new ResourceAmount(amount));
				}
			} else
				throw new IllegalStateException("Amount resource could not be added in type storage.");
		}
	}

	/**
	 * Retrieves an amount of a resource type from storage.
	 * 
	 * @param resource the resource.
	 * @param amount   the amount (kg).
	 */
	void retrieveAmountResourceType(AmountResource resource, double amount) {

		if (amount < 0D) {
			throw new IllegalStateException("Cannot retrieve negative amount of type: " + amount);
		}

		if (amount > 0D) {
			if (getAmountResourceTypeStored(resource) >= amount) {

				// Set total amount cache to dirty since value is changing.
				totalAmountCacheDirty = true;

				ResourceAmount stored = getAmountResourceTypeStoredObject(resource);
				if (stored != null)
					stored.setAmount(stored.getAmount() - amount);
			} else {
				throw new IllegalStateException("Amount resource (" + resource.getName() + ":" + amount
						+ ") could not be retrieved from type storage");
			}
		}
	}

	/**
	 * Retrieves an amount of a resource type from storage.
	 * 
	 * @param resource the resource.
	 * @param amount   the amount (kg).
	 */
	void retrieveAmountResourceType(int resource, double amount) {

		if (amount < 0D) {
			throw new IllegalStateException("Cannot retrieve negative amount of type: " + amount);
		}

		if (amount > 0D) {
			if (getAmountResourceTypeStored(resource) >= amount) {

				// Set total amount cache to dirty since value is changing.
				totalAmountCacheDirty = true;

				ResourceAmount stored = getARTypeStoredObject(resource);
				if (stored != null)
					stored.setAmount(stored.getAmount() - amount);
			} else {
				throw new IllegalStateException(
						"Amount resource (" + resource + ":" + amount + ") could not be retrieved from type storage");
			}
		}
	}

	/**
	 * Internal class for storing type resource amounts.
	 */
	private static class ResourceAmount implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		
		private double amount;

		private ResourceAmount(double amount) {
			this.amount = amount;
		}

		private void setAmount(double amount) {
			this.amount = amount;
		}

		private double getAmount() {
			return amount;
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		typeCapacities = null;
		typeStored = null;
	}

	/**
	 * Implementing readObject method for serialization.
	 * 
	 * @param in the input stream.
	 * @throws IOException            if error reading from input stream.
	 * @throws ClassNotFoundException if error creating class.
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

		in.defaultReadObject();

		// Initialize transient variables that need it.
		totalAmountCacheDirty = true;
	}
}
