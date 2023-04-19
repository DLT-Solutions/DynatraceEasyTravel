package com.dynatrace.easytravel.frontend.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * A listener which shuts down various things that are left over by the things that we use.
 *
 * * HttpClient threads
 * * Axis2 scheduler
 *

 * @author dominik.stadler
 */
public class ShutdownListener implements ServletContextListener {
    private static final Logger log = LoggerFactory.make();

	@Override
	public void contextInitialized(ServletContextEvent servletcontextevent) {
		// nothing to do here
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletcontextevent) {
		log.info("Shutting down Apache HTTP Client threads");
        // ensure that http-client threads are stopped, this avoids logs about "... appears to have started a thread named [MultiThreadedHttpConnectionManager cleanup] but has failed to stop it. This is very likely to create a memory leak."
		// Probably fixed in Axis2 later in 1.5.5 and 1.6, see https://issues.apache.org/jira/browse/AXIS2-4898
		MultiThreadedHttpConnectionManager.shutdownAll();
	}
}
