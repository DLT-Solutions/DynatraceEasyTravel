package com.dynatrace.easytravel.remote;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;

import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.net.httpserver.HttpServer;


/**
 * HTTP Service in order to control the easyTravel Launcher from another process.
 * This thread just accepts incoming requests and delegates the work to other classes
 * in this package.
 *
 * @author martin.wurzinger
 */
public class HttpServiceThread {
	private static final Logger LOGGER = Logger.getLogger(HttpServiceThread.class.getName());

	private HttpServer server;
	private final int port;
	private final String[] packages;

	public HttpServiceThread(int port, Runnable shutdownExecutor) {
		this(port, shutdownExecutor, "com.dynatrace.easytravel.launcher.remote");
	}

	public HttpServiceThread(int port, Runnable shutdownExecutor, String... packages) {
		ShutdownRequestHandler.setShutdownExecutor(shutdownExecutor);
		ShutdownRequestHandler.setServiceThread(this);
		this.port = port;

		// always add the "remote" package so we have the shutdown handling always
		this.packages = ArrayUtils.add(packages, "com.dynatrace.easytravel.remote");
	}

	/**
	 * Create a HTTP Service that is hosting REST interfaces.
	 *
	 * @param shutdownExecutor a handler for shoutdown requests
	 * @throws IOException if the HTTP Service is unable to create a new server socket on the
	 *         configured port
	 * @author martin.wurzinger
	 */
	@SuppressWarnings("unchecked")
	public final void start() throws IOException {
		final String baseUri = "http://localhost:" + port + "/";

		LOGGER.info("Starting REST server on URI: '" + baseUri + "'...");
		ResourceConfig config = new PackagesResourceConfig(packages);
		// compress using GZIP
		config.getContainerResponseFilters().add(new GZIPContentEncodingFilter());

        server = HttpServerFactory.create(baseUri, config);
        server.start();
	}

	protected void onStop() {
		// subclasses may override
	}

	public final void stopService() {
		if(server == null) {
			return;
		}

		LOGGER.info("Stopping REST server");

		// allow derived classes to perform some work before actually stopping
		onStop();

		server.stop(0);

		// Workaround for Jersey shortcoming: It adds a ThreadPoolExecutor with 60 seconds Worker-timeout
		// which causes the application to wait up to 60 seconds before actually shutting down
		// because there is still a non-daemon thread alive...
		Executor executor = server.getExecutor();
		if(executor instanceof ExecutorService) {
			((ExecutorService)executor).shutdown();
		}

		// indicate that we do not run the service any more by setting this to null
		server = null;
	}
}
