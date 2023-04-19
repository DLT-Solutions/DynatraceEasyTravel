package com.dynatrace.easytravel.launcher.remote;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.logging.LoggerFactory;


/**
 * Handler for ping requests.
 *
 * @author martin.wurzinger
 */
@Path("/" + Constants.REST.PING)
public class PingRequestHandler {
	private static final Logger LOGGER = LoggerFactory.make();

    private static final String PONG = "pong";

	@GET
	@Produces("text/plain")
    public String ping() {
		LOGGER.info("REST Server was pinged");

    	return PONG;
    }
}
