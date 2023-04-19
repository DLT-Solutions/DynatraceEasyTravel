package com.dynatrace.easytravel.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Logger;

import com.dynatrace.easytravel.config.Directories;

/**
 * 
 * @author Michal.Bakula
 *
 */

public class ResourceFileReader {
	
	public static final String USERS = "Users.txt";
	public static final String EXTENDEDUSERS = "ExtendedUsers.txt";
	public static final String RENTALCARS = "RentalCars.txt";
	public static final String GEO = "geo.txt";
	public static final String GEOCOORDINATES = "ipCoordinates.txt";
	public static final String PLUGINNOTIFICATIONCONFIG = "pluginNotificationConfig.json";
	public static final String SPECIALUSERSDATA = "specialusersdata.json";
	
	private static final Logger LOGGER = Logger.getLogger(ResourceFileReader.class.getName());
	
	public static InputStream getInputStream(String name) throws FileNotFoundException{		
		File externalFile = new File(Directories.getConfigDir(), name);
        InputStream inputStream = null;
        
        if (externalFile.exists()) {
        	LOGGER.info("Loading external users file from " + externalFile);
            inputStream = new FileInputStream(externalFile);
        }
        if (inputStream == null) {
            inputStream = ResourceFileReader.class.getResourceAsStream("/"+name); //if started in process
        }
        if (inputStream == null) {
	        inputStream = ResourceFileReader.class.getResourceAsStream("/resources/"+name); //if started as procedure
		}
		if (inputStream == null) {
            inputStream = new FileInputStream("resources/"+name);
        }
		
		return inputStream;
	}
	
	public static InputStream getInputStream(File directory, String name) throws FileNotFoundException{
		File externalFile = new File(directory, name);
        InputStream inputStream = null;
        if (externalFile.exists()) {
        	LOGGER.info("Loading external users file from " + externalFile);
            inputStream = new FileInputStream(externalFile);
        }
        
        return inputStream;
	}
}
