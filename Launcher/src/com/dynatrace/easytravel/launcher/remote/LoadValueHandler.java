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
@Path("/" + Constants.REST.LOAD_VALUE)
public class LoadValueHandler {
	private static final Logger LOGGER = LoggerFactory.make();

	@GET
	@Path("/{enabled}")
	@Produces("text/plain")
	public String setLoad(@PathParam("enabled") int load) {
		LOGGER.trace(Constants.REST.LOAD_VALUE + " service called with parameter " + load);
		Launcher.setLoadValue(load);
		return "Load value set to " + load + " visits/min";
	}
}
