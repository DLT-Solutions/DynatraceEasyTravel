package com.dynatrace.easytravel.persistence;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class PersistenceMode {
	private static final Logger log = LoggerFactory.make();
	
	public enum AllowedPersistenceMode {
		mongodb, jpa, hbase, cassandra
	}
	
	public static boolean isAllowedPersistenceMode(String val) {
	    for (AllowedPersistenceMode mode : AllowedPersistenceMode.values()) {
	        if (mode.name().equals(val)) {
	            return true;
	        }
	    }
	    log.error(val +" is not allowed persistence mode.");
	    return false;
	}
}