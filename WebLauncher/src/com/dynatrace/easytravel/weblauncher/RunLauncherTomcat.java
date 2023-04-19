package com.dynatrace.easytravel.weblauncher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;

import org.apache.catalina.LifecycleException;
import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.ThirdPartyContentProxySelector;
import com.dynatrace.easytravel.launcher.AbstractLauncher;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.config.ConfigFileInitializer;
import com.dynatrace.easytravel.launcher.misc.DocumentStarter;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.logging.RootLogger;
import com.dynatrace.easytravel.tomcat.Tomcat7Config;
import com.dynatrace.easytravel.tomcat.Tomcat7Starter;
import com.dynatrace.easytravel.tomcat.Tomcat7StarterWebLauncher;
import com.dynatrace.easytravel.util.LocalUriProvider;

import ch.qos.logback.classic.Logger;

public class RunLauncherTomcat extends AbstractLauncher {

	private static final Logger log = LoggerFactory.make();

	// for the Tomcat-procedures it is ok to have a static config
	private static EasyTravelConfig config = EasyTravelConfig.read();

	/**
	 * Starts the embedded Tomcat server.
	 *
	 * @throws LifecycleException
	 * @throws MalformedURLException if the server could not be configured
	 * @throws LifecycleException if the server could not be started
	 * @throws MalformedURLException
	 * @throws Exception
	 */
	public void run() throws Exception {
		final Tomcat7Starter tomcatStarter = new Tomcat7StarterWebLauncher();
		Tomcat7Config tomcatConfig = new Tomcat7Config.Tomcat7ConfigBuilder()
				.withPort(config.weblauncherPort)
				.withShutdownPort(config.weblauncherShutdownPort)
				.withContextRoot(config.weblauncherContextRoot)
				.withAuthentication(config.isWebLauncherAuthEnabled)
				.withParentClass(this.getClass())
				.build();
		tomcatStarter.run(tomcatConfig);

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				log.info("Shutting down web launcher upon request from HTTP Service Thread");
				try {
					tomcatStarter.stop();
				} catch (Exception e) {
					log.error("Failed to stop web launcher", e);
				}
			}
		};

		// start the REST Server
		try {
			startHttpService(EasyTravelConfig.read().launcherHttpPort, runnable);
		} catch (IOException e) {
			log.error("Unable to run Web Launcher because the required HTTP Service could not be started", e);
			return;
		}

		ThirdPartyContentProxySelector.applyProxy();

		log.info("Web Launcher fully started at port " + config.weblauncherPort);
	}


	public static void main(String[] args) throws Exception {
		ConfigFileInitializer.initializeForLauncher();

		// try to load the config from the classpath here...
		try {
			LoggerFactory.initLogging();
		} catch (IOException e) {
			System.err.println("Could not initialize logging from classpath: "); // NOPMD
			e.printStackTrace();
		}

		RootLogger.setup(MessageConstants.WEBLAUNCHER);

		// commandline can specify the scenario that should be started automatically
		if (args.length > 0 &&
				(args[0].equals("-h") || args[0].equals("--help"))) {
			System.out.println("Usage: [<Autostart-Group>] <Autostart-Scenario>");
			return;
		}

        // initialize plugin job scheduler for WebLauncher
        Launcher.initPluginScheduler(MessageConstants.WEBLAUNCHER);

		Launcher.setAutostartFromCommandline(Arrays.asList(args));

		RunLauncherTomcat inst = new RunLauncherTomcat();
		inst.run();

		// if we do not open the page in the Browser the scenario is never started because this depends
		// on the Launcher UI being initialized. Therefore we need to process the autostart manually here
		if (StringUtils.isNotEmpty(config.autostart)) {
			// use HtmlUnit to query the page once in order to trigger Autostart
			log.info("Trigger a page load in order to get RWT Launcher to handle autostart of scenario: " +
					config.autostartGroup + "/" + config.autostart);

			ScenarioServlet.startScenario(config.autostartGroup, config.autostart); 
			Launcher.initForPluginChanges();
		}

		if (config.isLocalEnvironment()) { // JLT-43300
			DocumentStarter starter = new DocumentStarter();
			try{
				starter.openURL(LocalUriProvider.getLocalUri(config.weblauncherPort, "/"));
			} catch (Exception e){
				log.debug("Exception occurred while opening URL.", e);
			}
		}
	}
}
