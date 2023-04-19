package com.dynatrace.easytravel.business;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;

import com.dynatrace.easytravel.config.BusinessBackendReservation;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend.Persistence;
import com.dynatrace.easytravel.constants.BaseConstants.SystemProperties;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.logging.RootLogger;
import com.dynatrace.easytravel.spring.SpringUtils;
import com.dynatrace.easytravel.spring.TomcatHolderBean;
import com.dynatrace.easytravel.tomcat.Tomcat7Config;
import com.dynatrace.easytravel.tomcat.Tomcat7Starter;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

public class RunTomcat {

	private static final Logger log = LoggerFactory.make();

	// Tomcat-configuration values that we set higher for the Business Backend
	// in order to scale better
	private static final int MAX_BACKLOG = 500;
	private static final int MAX_THREADS = 1000;
	private static final int MAX_CONNECTIONS = 1000;

	public void run() throws Exception {
        log.info("Running in mode: " + DtVersionDetector.getInstallationType());

		EasyTravelConfig config = EasyTravelConfig.read();

		long start = System.currentTimeMillis();

		Tomcat7Starter tomcatStarter = new Tomcat7Starter();
		Tomcat7Config tomcatConfig = new Tomcat7Config.Tomcat7ConfigBuilder()
				.withHostName(BaseConstants.LOCALHOST)
				.withPort(config.backendPort)
				.withShutdownPort(config.backendShutdownPort)
				.withContextRoot(config.backendContextRoot)
				.withCookies(true)
				.withParentClass(this.getClass())
				.build();
		Tomcat tomcat = tomcatStarter.run(tomcatConfig);		

		Tomcat7Starter.adjustThreads(tomcat, MAX_BACKLOG, MAX_THREADS, MAX_CONNECTIONS);

		try {
			//register easyTravelAmdin application
			tomcatStarter.addContext(tomcat, config.backendPort, config.backendEasyTravelMonitorContextRoot, config.webappBase + "/" + config.backendEasyTravelMonitorWar);

			SpringUtils.getPluginHolder().registerPlugins();

			/*
			 * We removed this as it took aprox. 1 second during startup, let's see if startup feels quicker without this
			 * // do a bit of "warmup" to fail here and stop the app if something is badly broken inside
			 * log.fine("Loading all journeys:");
			 * DatabaseAccess service = SpringUtils.getBean("databaseAccess", DatabaseAccess.class);
			 * Object entityManagerDelegate = service.getEntityManagerDelegate();
			 * if (log.isLoggable(Level.FINE)) log.fine("Having database access: " + entityManagerDelegate);
			 */

			log.info("Business Backend started at port " + config.backendPort + " after " + (System.currentTimeMillis() - start) +
					"ms.");
		} catch (Exception e) {
			// shutdown if we could not start correctly, i.e. if spring or database is not working correctly
			tomcatStarter.stop();
			throw e;
		}
	}

	/**
	 * Starts the embedded Tomcat server.
	 *
	 * @throws LifecycleException
	 * @throws MalformedURLException if the server could not be configured
	 * @throws LifecycleException if the server could not be started
	 * @throws MalformedURLException
	 * @throws Exception
	 */
	public void run(BusinessBackendReservation reservation) throws Exception {
        log.info("Running in mode: " + DtVersionDetector.getInstallationType());

        EasyTravelConfig config = EasyTravelConfig.read();

		long start = System.currentTimeMillis();
		Tomcat7Starter tomcatStarter = new Tomcat7Starter();

		int backendPort = config.backendPort;
		int ajpPort = 0;
		int backendShutdownPort = config.backendShutdownPort;
		String backendContextRoot = config.backendContextRoot;
		String webappBase = config.webappBase;
		String routePrefix = null;
		String easyTravelMonitorWar = config.backendEasyTravelMonitorWar;
		String easyTravelMonitorContext = config.backendEasyTravelMonitorContextRoot;
		if (reservation != null) {
			backendPort = reservation.getPort();
			ajpPort = reservation.getAjpPort();
			backendShutdownPort = reservation.getShutdownPort();
			backendContextRoot = reservation.getContextRoot();
			webappBase = reservation.getWebappBase();
			routePrefix = reservation.getRoutePrefix();
		}
		
		Tomcat7Config tomcatConfig = new Tomcat7Config.Tomcat7ConfigBuilder()
				.withPort(backendPort)
				.withShutdownPort(backendShutdownPort)
				.withRoutePrefix(routePrefix)
				.withAjpPort(ajpPort)
				.withContextRoot(backendContextRoot)
				.withWebappBase(webappBase)
				.withCookies(true)
				.withPersistentSessionManager(true)
				.withParentClass(this.getClass())
				.build();

		Tomcat tomcat = tomcatStarter.run(tomcatConfig);
		Tomcat7Starter.adjustThreads(tomcat, MAX_BACKLOG, MAX_THREADS, MAX_CONNECTIONS);

		try {
			tomcatStarter.addContext(tomcat, backendPort, easyTravelMonitorContext, webappBase + "/" + easyTravelMonitorWar);
			SpringUtils.getPluginHolder().registerPlugins();

			/*
			 * We removed this as it took aprox. 1 second during startup, let's see if startup feels quicker without this
			 * // do a bit of "warmup" to fail here and stop the app if something is badly broken inside
			 * log.fine("Loading all journeys:");
			 * DatabaseAccess service = SpringUtils.getBean("databaseAccess", DatabaseAccess.class);
			 * Object entityManagerDelegate = service.getEntityManagerDelegate();
			 * if (log.isLoggable(Level.FINE)) log.fine("Having database access: " + entityManagerDelegate);
			 */

			//store tomcat reservation in the spring context
			SpringUtils.getBean("tomcatHolderBean", TomcatHolderBean.class).setReservation(reservation);

			log.info("Business Backend started at port " + backendPort + " after " + (System.currentTimeMillis() - start) +
					"ms.");
		} catch (Exception e) {
			// shutdown if we could not start correctly, i.e. if spring or database is not working correctly
			tomcatStarter.stop();
			throw e;
		}
	}

	public static void main(String[] args) throws Exception {
		Parser parser = new BasicParser();
		CommandLine commandLine = parser.parse(createOptions(), args);

		String propertiesFilePath = commandLine.getOptionValue(BaseConstants.CmdArguments.PROPERTY_FILE);

		String persistenceMode = commandLine.getOptionValue(BaseConstants.CmdArguments.PERSISTENCE_MODE, Persistence.JPA);
		System.setProperty(SystemProperties.PERSISTENCE_MODE, persistenceMode);

		if (propertiesFilePath != null && new File(propertiesFilePath).exists()) {
			EasyTravelConfig.createSingleton(propertiesFilePath);

			// tell Spring about the changed file
			System.setProperty("com.dynatrace.easytravel.propertiesfile", "file:" + propertiesFilePath);
		} else {
			log.warn("Run with default configuration because custom configuration file not available");
			System.setProperty(
					"com.dynatrace.easytravel.propertiesfile",
					Thread.currentThread().getContextClassLoader().getResource(EasyTravelConfig.PROPERTIES_FILE + ".properties").toString());
		}

        String installationMode = commandLine.getOptionValue(BaseConstants.CmdArguments.INSTALLATION_MODE);
        if(installationMode != null) {
        	DtVersionDetector.enforceInstallationType(InstallationType.fromString(installationMode));
        } else {
        	// default to APM if not set
        	DtVersionDetector.enforceInstallationType(InstallationType.APM);
        }

		if (log.isDebugEnabled()) {
			log.debug(TextUtils.merge("Setting persistence mode to ''{0}'', because command line argument is ''{1}''",
					System.getProperty(SystemProperties.PERSISTENCE_MODE), persistenceMode == null ? "not set" : persistenceMode));
            log.debug(TextUtils.merge("Setting installation type to ''{0}''", DtVersionDetector.getInstallationType()));
        }

		RunTomcat inst = new RunTomcat();

		BusinessBackendReservation reservation = null;
		String portStr = null;
		portStr = commandLine.getOptionValue(BaseConstants.CustomerFrontendArguments.PORT);
		if (portStr != null) {
			RootLogger.setup(BaseConstants.LoggerNames.BUSINESS_BACKEND, portStr);
			int port = Integer.parseInt(portStr);
	        int shutdownPort = Integer.parseInt(commandLine.getOptionValue(BaseConstants.CustomerFrontendArguments.SHUTDOWN_PORT));
	        int ajpPort = Integer.parseInt(commandLine.getOptionValue(BaseConstants.CustomerFrontendArguments.AJP_PORT));
	        String contextRoot = commandLine.getOptionValue(BaseConstants.CustomerFrontendArguments.CONTEXT_ROOT);
	        String webappBase = commandLine.getOptionValue(BaseConstants.CustomerFrontendArguments.WEBAPP_BASE);
	        String routePrefix = commandLine.getOptionValue(BaseConstants.CustomerFrontendArguments.ROUTEPREFIX);
	        reservation = new BusinessBackendReservation(port, shutdownPort, ajpPort, contextRoot, webappBase);
	        reservation.setRoutePrefix(routePrefix);
	        inst.run(reservation);
		} else {
			RootLogger.setup(BaseConstants.LoggerNames.BUSINESS_BACKEND);
			inst.run();
		}
	}

	public static Options createOptions() {
		Options options = new Options();

		Option propertiesFilePath = new Option(BaseConstants.CmdArguments.PROPERTY_FILE, true,
				"the path to the configuration file");
		options.addOption(propertiesFilePath);

		Option persistenceMode = new Option(BaseConstants.CmdArguments.PERSISTENCE_MODE, true,
				"the chosen persistence mode");
		options.addOption(persistenceMode);

        Option installationMode = new Option(BaseConstants.CmdArguments.INSTALLATION_MODE, true, "the current installation mode: Apm/dynaTrace");
        options.addOption(installationMode);

        Option port = new Option(BaseConstants.CustomerFrontendArguments.PORT, true, "the port Tomcat is listening for requests");
        port.setType(Integer.class);
        options.addOption(port);

        Option shutdownPort = new Option(BaseConstants.CustomerFrontendArguments.SHUTDOWN_PORT, true, "the port Tomcat is listening for shutdown commands");
        shutdownPort.setType(Integer.class);
        options.addOption(shutdownPort);

        Option ajpPort = new Option(BaseConstants.CustomerFrontendArguments.AJP_PORT, true, "the port Tomcat is listening for Apache connector");
        ajpPort.setType(Integer.class);
        options.addOption(ajpPort);

        Option contextRoot = new Option(BaseConstants.CustomerFrontendArguments.CONTEXT_ROOT, true, "the web application context root");
        options.addOption(contextRoot);

        Option webappDir = new Option(BaseConstants.CustomerFrontendArguments.WEBAPP_BASE, true, "the web application directory");
        options.addOption(webappDir);

        Option routePrefix = new Option(BaseConstants.CustomerFrontendArguments.ROUTEPREFIX, true, "the prefix for the load balancer route");
        options.addOption(routePrefix);

		return options;
	}
}
