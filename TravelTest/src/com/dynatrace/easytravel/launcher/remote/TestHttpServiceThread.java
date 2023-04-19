package com.dynatrace.easytravel.launcher.remote;

import java.io.IOException;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.logging.BasicLoggerConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.logging.RootLogger;


public class TestHttpServiceThread {
    //private static final Logger LOGGER = LoggerFactory.make();
    private static final EasyTravelConfig CONFIG = EasyTravelConfig.read();

    private static volatile boolean stop = false;

	/**
	 *
	 * @param args
	 * @author dominik.stadler
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		setUpLogger();

        HttpServiceThread remoteController = startHttpService();

        while(!stop) {
        	Thread.sleep(1000);
        }

        if (remoteController != null) {
            remoteController.stopService();
        }
	}

    /**
     * Initialize logger for Commandline Launcher
     *
     * @author martin.wurzinger
     */
    private static void setUpLogger() {
        // try to load the config from the classpath here...
        try {
            LoggerFactory.initLogging();
        } catch (IOException e) {
            System.err.println("Could not initialize logging from classpath: "); //NOPMD
            e.printStackTrace();
        }

        RootLogger.setup(new BasicLoggerConfig(MessageConstants.COMMANDLINE_LAUNCHER));
    }

	private static void exit() {
		stop = true;
	}

    private static HttpServiceThread startHttpService() throws IOException {
        Runnable exitInDisplayThreadRunnable = new Runnable() {
            @Override
            public void run() {
                exit();
            }
        };

        HttpServiceThread remoteController;

        // plus one to not collide with running launcher
        remoteController = new HttpServiceThread(CONFIG.launcherHttpPort+1, exitInDisplayThreadRunnable);
        remoteController.start();
        return remoteController;
    }
}
