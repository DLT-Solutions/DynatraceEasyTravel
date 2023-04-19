package com.dynatrace.easytravel.couchdb;

import java.util.ArrayList;
import java.util.List;

import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class CouchDBCommons {

	private static final Logger log = LoggerFactory.make();
	
	public static ArrayList<String> myList;
	
 	/**
	 * Initialize the default list of files we expect to serve from the image DB.
	 */
	
	static {
		myList = new ArrayList <String>();
		
		myList.add("/img/result_pic_1.png");
		myList.add("/img/result_pic_2.png");
		myList.add("/img/easyTravel_banner.png");
		myList.add("/img/header7.jpg");
		myList.add("/img/header6.jpg");
		myList.add("/img/header5.jpg");
		myList.add("/img/header4.jpg");
		myList.add("/img/header3.jpg");
		myList.add("/img/header2.jpg");
		myList.add("/img/header1.png");
		myList.add("/img/booking/Booking_transaction_picture_page2.png");
		myList.add("/img/booking/easyTravel_bookingtransaction_Header.png");
	}

	public static ArrayList<String> getImageList() {
		return myList;
	}
		
	public static void setImageList(ArrayList<String> newList) {
		myList = newList;
	}
	
	/**
	 * Initialize the connection to the CouchDB. Note: if the specified db does
	 * not exist, we force failure (if the parameter indicates that we should
	 * not try to create the db).
	 */
	public static CouchDbClient initCouchDbClient(boolean create) {

		CouchDbProperties properties = null;
		CouchDbClient myClient = null;
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();

		properties = new CouchDbProperties()
				.setHost(EASYTRAVEL_CONFIG.couchDBHost)
				.setPort(EASYTRAVEL_CONFIG.couchDBPort)
				.setDbName(EASYTRAVEL_CONFIG.couchDBName)
				// Note param passed here to create or use existing database.
				.setCreateDbIfNotExist(create).setProtocol("http")
				.setUsername(EASYTRAVEL_CONFIG.couchDBAdminUser)
				.setPassword(EASYTRAVEL_CONFIG.couchDBAdminPassword)
				.setMaxConnections(100);

		if (create) {

			// ================================================
			// Create a new database.
			// ================================================

			log.info("Creating a new CouchDB database for configuration: <"
					+ EASYTRAVEL_CONFIG.couchDBHost + "> Port: <"
					+ EASYTRAVEL_CONFIG.couchDBPort + "> Name: <"
					+ EASYTRAVEL_CONFIG.couchDBName + ">");

			try {
				myClient = new CouchDbClient(properties);
			} catch (Exception e) {
				// Note that an exception will be caught if e.g. CouchDB is not
				// installed on the target
				// system or if the service is not running.
				log.info("Error creating CouchDB client\n" + e);
				return null;
			}

		} else {

			// ================================================
			// Use an existing base.
			// ================================================

			log.info("Connecting to an existing CouchDB database for configuration: <"
					+ EASYTRAVEL_CONFIG.couchDBHost
					+ "> Port: <"
					+ EASYTRAVEL_CONFIG.couchDBPort
					+ "> Name: <"
					+ EASYTRAVEL_CONFIG.couchDBName + ">");

			try {
				// It seems that the following call to create a client will NOT
				// fail even if the database
				// is not there! This is because the client gives you a
				// connection to the server, and if
				// the particular database we name here does not exist and we
				// therefore do not create it,
				// it is not considered a failure. Therefore we need to actually
				// look through the currently
				// existing databases to see if ours is already there or not.
				myClient = new CouchDbClient(properties);
				List<String> databaseList = myClient.context().getAllDbs();
				if (databaseList != null) {
					if (databaseList.contains(EASYTRAVEL_CONFIG.couchDBName)) {
						return myClient;
					}
				}
				myClient.shutdown();
				return null;

			} catch (Exception e) {
				// We have failed to connect to an existing database.
				log.info("Error: Failed to connect to an existing database.");
				return null;
			}
		}

		return myClient;
	}
}
