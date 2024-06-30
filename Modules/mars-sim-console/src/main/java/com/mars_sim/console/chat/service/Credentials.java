/*
 * Mars Simulation Project
 * Credentials.java
 * @date 2021-09-05
 * @author Barry Evans
 */

package com.mars_sim.console.chat.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic serializable credentials store
 */
public class Credentials implements Serializable{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Credentials.class.getName());

	// The default admin user name
	public static final String ADMIN = "admin";
	
	private Map<String, String> users;

	private transient File backingFile;
	
	public Credentials(File credFile) {
		users = new HashMap<>();
		backingFile = credFile;
	}
	
	
	public boolean authenticate(String username, String password) {
		String found = users.get(username);
		
		return (found != null) && found.equals(password);
	}

	public void addUser(String username, String password) {
		users.put(username, password);
		save();
	}

	public boolean setPassword(String username, String password) {
		// Overwrite previous entry
		users.put(username, password);
		return save();
	}

	/**
	 * Save the credentials to the output stream
	 * @param out
	 */
	private boolean save() {
		try (FileOutputStream output = new FileOutputStream(backingFile)) {
			ObjectOutputStream outStream = new ObjectOutputStream(output);		
			outStream.writeObject(this);	
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error setting up output stream for writing object: ", e);
			return false;
		}
		return true;
	}


	/**
	 * Load a new credentials from a stream
	 * @param source
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static Credentials load(File source) throws IOException {
		try (FileInputStream input = new FileInputStream(source)) {
			ObjectInputStream inStream = new ObjectInputStream(input);
			Object loaded = inStream.readObject();
			if (!(loaded instanceof Credentials)) {
				throw new IOException("Loaded object is not a Credentials instance");
			}

			Credentials cred = (Credentials) loaded;
			cred.backingFile = source;
			return cred;
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Error loading credentials", e);
		}
		return null;
	}

	/**
	 * Get the password for a user. This is not ideal but needed in the bootstrap situation.
	 * @param username
	 * @return
	 */
	public String getPassword(String username) {
		return users.get(username);
	}
}
