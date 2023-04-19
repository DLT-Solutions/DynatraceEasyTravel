package com.dynatrace.easytravel.frontend;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.procedures.TomcatShutdownCommand;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.LocalUriProvider;

public class ShutdownAllCustomerFrontends {
	private static final Logger log = LoggerFactory.make();

	/**
	 *
	 * @param args
	 * @author dominik.stadler
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int portStart = 19180;
		int portEnd = 19199;

		if(args.length < 2) {
			System.err.println("Usage: <port-range-start> <port-range-end>\nUsing default values: " + portStart + " - " + portEnd);
		} else {
			portStart = Integer.parseInt(args[0]);
			portEnd = Integer.parseInt(args[1]);
		}

		for(int i = portStart;i <= portEnd;i++) {
			log.info("Shutting down on port: " + i);
	        TomcatShutdownCommand shutdown = new TomcatShutdownCommand(LocalUriProvider.getLoopbackAdapter(), i);
	        Feedback feedback = shutdown.execute();
	        if (!feedback.isOk()) {
	            log.warn("Could not shut down tomcat on port " + i);
	        }
		}
	}
}
