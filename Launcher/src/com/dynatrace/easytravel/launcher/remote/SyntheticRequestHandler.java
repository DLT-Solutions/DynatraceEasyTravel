package com.dynatrace.easytravel.launcher.remote;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author Rafal.Psciuk
 *
 */

@Path("/" + Constants.REST.SYNTHETIC_REQUESTS)
public class SyntheticRequestHandler {
	private static final Logger LOGGER = LoggerFactory.make();

	@GET
	@Path("/{enabled}")
	@Produces("text/plain")
    public String setSyntheticRequests(@PathParam("enabled")boolean enabled) {
		LOGGER.trace(Constants.REST.SYNTHETIC_REQUESTS + " service called with parameter " + enabled);
		Launcher.setTaggedWebRequests(enabled);
    	return "synthetic requests set to " + enabled;
    }
}
