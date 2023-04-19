package com.dynatrace.easytravel.frontend.tomcat;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.CustomerFrontendReservation;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.SystemProperties;
import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend.Persistence;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.logging.RootLogger;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.spring.SpringUtils;
import com.dynatrace.easytravel.tomcat.MemoryManagingTomcatStarter;
import com.dynatrace.easytravel.tomcat.Tomcat7Config;
import com.dynatrace.easytravel.tomcat.Tomcat7Starter;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * @author tibor.varga
 */
public class FrontendTomcatStarter {
	
	private static final Logger log = LoggerFactory.make();
	private final String loggerName;
	
	private static final int MAX_BACKLOG = 500;
	private static final int MAX_THREADS = 1000;
	private static final int MAX_CONNECTIONS = 1000;
	
	public FrontendTomcatStarter(String loggerName) {
		this.loggerName = loggerName;
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

    public void run() throws Exception {
        log.debug("Starting " + loggerName + " with configuration settings read from file");

        run(CustomerFrontendReservation.reserveResources());
    }
    
    /**
     * Starts the embedded Tomcat server.
     *
     * @param port the port to start Tomcat at
     * @param frontendShutdownPort the shutdown port to start Tomcat at
     * @param webappBase the web application base directory
     * @throws LifecycleException
     * @throws MalformedURLException if the server could not be configured
     * @throws LifecycleException if the server could not be started
     * @throws MalformedURLException
     * @throws Exception
     */
    public void run(CustomerFrontendReservation config) throws Exception {
        log.info("Running in mode: " + DtVersionDetector.getInstallationType());

        long start = System.currentTimeMillis();
        log.info("Starting " + loggerName + " with configuration settings: " + config.toString());

        Tomcat7Starter tomcatStarter = new MemoryManagingTomcatStarter();
        Tomcat7Config tomcatConfig = new Tomcat7Config.Tomcat7ConfigBuilder()
        		.withPort(config.getPort())
        		.withShutdownPort(config.getShutdownPort())
        		.withRoutePrefix(config.getRoutePrefix())
        		.withAjpPort(config.getAjpPort())
        		.withContextRoot(config.getContextRoot())
        		.withWebappBase(config.getWebappBase())
        		.withCookies(true)
        		.withPersistentSessionManager(true)
        		.withParentClass(this.getClass())
        		.build();
        Tomcat tomcat = tomcatStarter.run(tomcatConfig);

		Tomcat7Starter.adjustThreads(tomcat, MAX_BACKLOG, MAX_THREADS, MAX_CONNECTIONS);

        try {
        	waitUntilBackendAvailable(); // not quite beautiful

        	SpringUtils.getPluginHolder().registerPlugins(); // this asserts that the backend is ready to process Axis calls!!

            log.info(loggerName + " started at port: " + config.getPort() + " after " + (System.currentTimeMillis() - start) + "ms.");
        } catch (Exception e) {
	    	// shutdown if we could not start correctly, i.e. if spring or database is not working correctly
	    	tomcatStarter.stop();
	        throw e;
	    }
    }
    
    private static final int WAIT_FOR_BACKEND_TIMEOUT = 30 * 1000; // 30 seconds

    private static boolean isBackendAvailable() {
        return UrlUtils.checkRead(LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE) + "ping").isOK();
    }

    private static void waitUntilBackendAvailable() {
    	long start = System.currentTimeMillis();
    	long now = System.currentTimeMillis();
    	while (!isBackendAvailable() && ((now - start) <= WAIT_FOR_BACKEND_TIMEOUT))
    	{
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//ignored by intention
			}
			now = System.currentTimeMillis();
    	}
    }
    
    public void start(String[] args) throws Exception {
    	if (args == null || args.length == 0) {
            RootLogger.setup(loggerName);
            this.run();
            return;
        }

        String portStr = null;
        boolean loggerSetUp = false;
        try {
            Parser parser = new BasicParser();
            CommandLine commandLine = parser.parse(createOptions(), args);
            portStr = commandLine.getOptionValue(BaseConstants.CustomerFrontendArguments.PORT);
            RootLogger.setup(loggerName, portStr);
            loggerSetUp = true;

            String propertiesFilePath = commandLine.getOptionValue(BaseConstants.CmdArguments.PROPERTY_FILE);
            if (propertiesFilePath != null && new File(propertiesFilePath).exists()) {
                EasyTravelConfig.createSingleton(propertiesFilePath);
            } else {
                log.warn("Run with default configuration because custom configuration file not available");
            }
            log.info("Connecting to backend at: " + EasyTravelConfig.read().backendHost + ":" + EasyTravelConfig.read().backendPort);

			String persistenceMode = commandLine.getOptionValue(BaseConstants.CmdArguments.PERSISTENCE_MODE, Persistence.JPA);
			System.setProperty(SystemProperties.PERSISTENCE_MODE, persistenceMode);

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

    		if(portStr == null) {
    			this.run();
    			return;
    		}

            int port = Integer.parseInt(portStr);
            int shutdownPort = Integer.parseInt(commandLine.getOptionValue(BaseConstants.CustomerFrontendArguments.SHUTDOWN_PORT));
            int ajpPort = Integer.parseInt(commandLine.getOptionValue(BaseConstants.CustomerFrontendArguments.AJP_PORT));
            String contextRoot = commandLine.getOptionValue(BaseConstants.CustomerFrontendArguments.CONTEXT_ROOT);
            String webappBase = commandLine.getOptionValue(BaseConstants.CustomerFrontendArguments.WEBAPP_BASE);
            String routePrefix = commandLine.getOptionValue(BaseConstants.CustomerFrontendArguments.ROUTEPREFIX);
            CustomerFrontendReservation reservation = new CustomerFrontendReservation(port, shutdownPort, ajpPort, contextRoot, webappBase);
            reservation.setRoutePrefix(routePrefix);
            this.run(reservation);
        } catch (Exception e) {
            if (!loggerSetUp) {
                RootLogger.setup(loggerName);
            }
            log.warn("Unable to parse arguments. Resume starting with default arguments.", e);
            this.run();
        }
    }
    
    public static Options createOptions() {
        Options options = new Options();

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

        Option routePrefix = new Option(BaseConstants.CustomerFrontendArguments.ROUTEPREFIX, true, "the prefix for jvm routes");
        options.addOption(routePrefix);

        Option propertiesFilePath = new Option(BaseConstants.CmdArguments.PROPERTY_FILE, true, "the path to the configuration file");
        options.addOption(propertiesFilePath);

		Option persistenceMode = new Option(BaseConstants.CmdArguments.PERSISTENCE_MODE, true,
				"the chosen persistence mode");
		options.addOption(persistenceMode);

        return options;
    }    
}
