/*
 * Mars Simulation Project
 * FileFinder.java
 * @date 2021-09-05
 * @author Manny Kung
 */

package com.mars_sim.core.tool;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileFinder {

    public static void main(String[] args) throws Exception {

        String file = "buildings.xml";

        ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
            listFiles(file, url);
        }
    }

    private static void listFiles(String file, URL url) throws Exception {
    	try (ZipInputStream zip = new ZipInputStream(url.openStream())) {
    		while(true) {
    			ZipEntry e = zip.getNextEntry();
    			if (e == null)
    				break;
    			String name = e.getName();
    			if (name.endsWith(file)) {
    				System.out.println(url.toString() + " -> " + name);
    			}
    		}
    	}
	}
}
