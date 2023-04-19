package com.dynatrace.easytravel.launcher.remote;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.dynatrace.easytravel.launcher.ant.AntController;
import com.dynatrace.easytravel.launcher.misc.Constants;


/**
 * Handler for ping requests.
 *
 * @author peter.kaiser
 */
@Path("/" + Constants.REST.CONTINUOUSLY)
public class ContinuouslyRequestHandler {
    
    private static volatile AntController antController;
    
    
    public static void setAntController(AntController antController) {
        if (antController == null) {
            throw new NullPointerException("antController must not be null");
        }
        ContinuouslyRequestHandler.antController = antController;
    }

    
	@GET
	@Path("/{continuously}")
	@Produces("text/plain")
    public String setContinuously(@PathParam("continuously")boolean continuously) {
    	if (antController == null) {
    	    throw new  IllegalStateException("antController must not be null");
    	}
	    antController.setContinuously(continuously);
	    return Boolean.TRUE.toString();
    }
}
