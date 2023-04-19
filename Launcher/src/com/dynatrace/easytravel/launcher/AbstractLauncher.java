package com.dynatrace.easytravel.launcher;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.qos.logback.classic.Logger;

import com.dynatrace.diagnostics.uemload.utils.ShutdownUtils;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.remote.HttpServiceThread;
import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * Base class for some Launcher-implementations, i.e. Cmd, Web and AntFork
 *
 * Note: GUI Launcher currently does not use this base class as it is fundamentally different than the others
 *
 * @author dominik.stadler
 */
public class AbstractLauncher {
    private static final Logger LOGGER = LoggerFactory.make();

    // Handle running-state, not used in GUI Launcher currently!
	private final AtomicBoolean isRunning = new AtomicBoolean(false);

	// REST server
    private HttpServiceThread httpService;


    ////// IS RUNNING ////////////

    protected boolean isRunning() {
    	return isRunning.get();
    }

    protected void setRunning(boolean state) {
    	isRunning.set(state);
    }

    protected void exit() {
        setRunning(false);
    }

    /////// REST HTTP SERVER ////////

    /**
     * Create and start a new HTTP Service.
     *
     * @param port The port where the service should listen
     *
     * @throws IOException if the HTTP Service could not be stopped
     * @author martin.wurzinger
     */
    protected void startHttpService(int port) throws IOException {
        final Runnable exitRunnable = new Runnable() {

            @Override
            public void run() {
            	LOGGER.info("Shutting down launcher upon request from HTTP Service Thread");
                exit();
            }
        };

        startHttpService(port, exitRunnable);
    }

    protected void startHttpService(int port, Runnable exitRunnable) throws IOException {
        httpService = new HttpServiceThread(port, exitRunnable);
        httpService.start();

        // do shutdown with a shutdown hook to run these steps also on CTRL-C and kill
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown hook") {
			@Override
			public void run() {
				// do a println here as logging might already be stopped
                System.out.println("Stopping HTTP Service Thread and Engine because scenario is not executing any more or a shutdown request was received."); //NOPMD
		        shutdown();
			}
        });
    }

	protected synchronized void shutdown() {
		httpService.stopService();
		ShutdownUtils.shutdown();
        LaunchEngine.stop();
	}
}
