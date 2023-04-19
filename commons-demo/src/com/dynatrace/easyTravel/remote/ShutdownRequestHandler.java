package com.dynatrace.easytravel.remote;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.RESTConstants;


/**
 * Handler for shutdown requests.
 *
 * @author martin.wurzinger
 */
@Path("/" + RESTConstants.SHUTDOWN)
public class ShutdownRequestHandler /*implements HttpRequestHandler*/ {
    private static final Logger LOGGER = LoggerFactory.make();
    
    private static Runnable shutdownExecutor;
	private static HttpServiceThread httpServiceThread;

    public static void setShutdownExecutor(Runnable shutdownExecutor) throws IllegalArgumentException {
        if (shutdownExecutor == null) {
            throw new IllegalArgumentException("Null argument is not allowed");
        }
        ShutdownRequestHandler.shutdownExecutor = shutdownExecutor;
    }

	public static void setServiceThread(HttpServiceThread httpServiceThread) {
		ShutdownRequestHandler.httpServiceThread = httpServiceThread;
	}

	@GET
	@Produces("text/plain")
    public String shutdown() {
		// asynchronously shut down in order to send a valid message back and correctly close
		// the REST/HTTP communication
    	new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					LOGGER.error("Interrupted while sleeping", e);
				}

		    	// first make sure the REST service itself stops
				httpServiceThread.stopService();

				// then also inform the application about the shutdown request
		    	shutdownExecutor.run();
			}
		}).start();

    	return "OK";
    }
}
