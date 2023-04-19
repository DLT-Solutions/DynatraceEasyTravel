package com.dynatrace.easytravel.database.functions;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;

public class DerbyFunctions {
	private static Logger log = LoggerFactory.make();
	
    public static String normalizeLocation(String name, int size) {
    	
		if (log.isDebugEnabled()) {
    		log.debug("normalizeLocation: location: <" + name + ">" + "delay requested: <" + size + "> ms");
		}
        trimLocation(size);
        return name;
        
    }

    // The name of this method does not reflect what it does - on purpose.
    // It is simply a delay.
    private static void trimLocation(int size) {
    	try {
			Thread.sleep(size);
		} catch (InterruptedException e) {
			log.info("trimLocation: sleep failed, time is <" + System.currentTimeMillis() + ">");
			e.printStackTrace();
		}
    }
}
