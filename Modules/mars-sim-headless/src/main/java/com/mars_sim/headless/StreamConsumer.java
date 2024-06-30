/*
 * Mars Simulation Project
 * StreamConsumer.java
 * @date 2021-08-28
 * @author Scott Davis
 */

package com.mars_sim.headless;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A thread for consuming output from another stream.
 */
public class StreamConsumer extends Thread {

	// Data members
	private InputStream in;
	private String type;

	/**
	 * Constructor.
	 * 
	 * @param in   the input stream to consume.
	 * @param type the stream type.
	 */
	public StreamConsumer(InputStream in, String type) {
		this.in = in;
		this.type = type;
	}

	@Override
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				System.out.println(type + line);
			
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}
}
