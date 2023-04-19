package com.dynatrace.easytravel.thirdpartycontent;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.catalina.LifecycleException;
import org.apache.commons.cli.*;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.logging.RootLogger;
import com.dynatrace.easytravel.tomcat.MemoryManagingTomcatStarter;
import com.dynatrace.easytravel.tomcat.Tomcat7Config;
import com.dynatrace.easytravel.tomcat.Tomcat7Starter;

public class RunThirdPartyContentTomcat {

	private static final Logger log = LoggerFactory.make();


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
		try {
			EasyTravelConfig config = EasyTravelConfig.read();
			log.debug("Starting Third Party Content server with configuration settings read from file");

			long start = System.currentTimeMillis();

			Tomcat7Starter tomcatStarter = new MemoryManagingTomcatStarter();
			Tomcat7Config tomcatConfig = new Tomcat7Config.Tomcat7ConfigBuilder()
					.withHostName(config.thirdpartyHost)
					.withPort(config.thirdpartyPort)
					.withShutdownPort(config.thirdpartyShutdownPort)
					.withContextRoot(config.thirdpartyContextRoot)
					.withWebappBase(config.thirdpartyWebappBase)
					.withParentClass(this.getClass())
					.build();
			tomcatStarter.run(tomcatConfig);
			log.info("Customer Third Party Content server started at hostname: " + config.thirdpartyHost + " port: " + config.thirdpartyPort + " after " + (System.currentTimeMillis() - start) + "ms.");
		} catch (Throwable e) { // NOSONAR - on purpose here to report all things that happen in the application
			log.error(e.getMessage(), e);
			throw new Exception(e);
		}
	}


    public static void main(String[] args) throws Exception {

        Parser parser = new BasicParser();
        CommandLine commandLine = parser.parse(createOptions(), args);

        String propertiesFilePath = commandLine.getOptionValue(BaseConstants.CmdArguments.PROPERTY_FILE);
        if (propertiesFilePath != null && new File(propertiesFilePath).exists()) {
            EasyTravelConfig.createSingleton(propertiesFilePath);

            // tell Spring about the changed file
            System.setProperty("com.dynatrace.easytravel.propertiesfile", "file:" + propertiesFilePath);
        } else {
            log.warn("Run with default configuration because custom configuration file not available");
            System.setProperty("com.dynatrace.easytravel.propertiesfile", Thread.currentThread().getContextClassLoader().getResource(EasyTravelConfig.PROPERTIES_FILE + ".properties").toString());
        }

        RootLogger.setup(BaseConstants.LoggerNames.THIRDPARTY_CONTENT);
        RunThirdPartyContentTomcat inst = new RunThirdPartyContentTomcat();
        inst.run();
    }


    public static Options createOptions() {
        Options options = new Options();

        Option propertiesFilePath = new Option(BaseConstants.CmdArguments.PROPERTY_FILE, true, "the path to the configuration file");
        options.addOption(propertiesFilePath);

        Option ajpPort = new Option(BaseConstants.CustomerFrontendArguments.AJP_PORT, true, "the port Tomcat is listening for Apache connector");
        ajpPort.setType(Integer.class);
        options.addOption(ajpPort);

        return options;
    }
}
