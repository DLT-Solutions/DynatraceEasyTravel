package com.dynatrace.easytravel.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.tree.ConfigurationNode;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.ThirdPartyContentProxySelector;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.config.ConfigFileInitializer;
import com.dynatrace.easytravel.launcher.config.ScenarioConfiguration;
import com.dynatrace.easytravel.launcher.config.ScenarioConfigurationPersistence;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.launcher.scenarios.ScenarioGroup;
import com.dynatrace.easytravel.logging.BasicLoggerConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.logging.RootLogger;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

public class CommandlineLauncher extends AbstractLauncher {
    private static final Logger LOGGER = LoggerFactory.make();

    public static void main(String[] args) {
    	LOGGER.info("Had arguments: " + Arrays.toString(args));

        CommandlineArguments arguments;
        try {
            arguments = new CommandlineArguments(args);

            if(arguments.isHelp()) {
            	return;
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid comand line arguments. The application is going to be terminated", e);
            return;
        } catch (ParseException e) {
            LOGGER.error("Unable to parse command line arguments. The application is going to be terminated", e);
            return;
        } catch (FileNotFoundException fnfe) {
            LOGGER.error("Unable to find file specified by command line argument", fnfe);
            return;
        }

        /*
         * Set location were the easyTravel JARs and executables can be found. The location can be
         * absolute or relative to the working directory.
         */
        System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

        File propertyFile = arguments.getPropertyFile();
        if (propertyFile != null) {
            EasyTravelConfig.createSingleton(propertyFile.getAbsolutePath());
        }

        ConfigFileInitializer.initializeForCommandlineLauncher();

		// apply proxy settings if there are any
        ThirdPartyContentProxySelector.applyProxy();

        new CommandlineLauncher().run(arguments);
    }

    /**
     * Try to find the scenario for the given scenario and group title. If no matching scenario
     * could be found the first configured scenario will be returned. If no scenarios are configured
     * at all, <code>null</code> will be returned.
     *
     * @param config the scenario configuration to search throw
     * @param scenarioTitle the title of the scenario
     * @param scenarioGroupTitle the title of the scenario group
     * @return the scenario with the matching title and the matching group or <code>null</code> if
     *         no scenario is configured
     * @author martin.wurzinger
     */
    private Scenario identifyScenario(ScenarioConfiguration config, String scenarioTitle, String scenarioGroupTitle) {
    	LOGGER.info("Looking for Scenario: " + scenarioGroupTitle + "/" + scenarioTitle);

        Scenario findScenario = config.findScenario(scenarioTitle, scenarioGroupTitle);
        if (findScenario != null) {
            return findScenario;
        }

        List<ScenarioGroup> groups = config.getScenarioGroups();
        if (groups.isEmpty()) {
            return null;
        }

        ScenarioGroup firstGroup = groups.get(0);
        List<Scenario> scenarios = firstGroup.getScenarios();
        if (scenarios.isEmpty()) {
            return null;
        }

        LOGGER.warn("Did not find Scenario: " + scenarioGroupTitle + "/" + scenarioTitle + ", using default scenario");
        return scenarios.get(0);
    }

    private void run(CommandlineArguments arguments) {
        setRunning(true);

        setUpLogger();

        // create and load scenario configuration
        ScenarioConfigurationPersistence persistence = new ScenarioConfigurationPersistence(arguments.getScenarioFile()) {
        	@Override
        	protected String readHash(ConfigurationNode root)
        			throws ConfigurationException {
        		return BaseConstants.EMPTY_STRING;
        	}

        	@Override
        	public String calcMd5(ScenarioConfiguration scenarioConfig)
        			throws ConfigurationException {
        		return BaseConstants.EMPTY_STRING;
        	}
        };
        ScenarioConfiguration configuration = new ScenarioConfiguration(persistence);
        configuration.loadOrCreate();

        Launcher.initPluginScheduler(MessageConstants.CMD_LAUNCHER);
        LaunchEngine engine = LaunchEngine.getNewInstance();
        engine.addProcedureStateListener(new ProcedureStateListener() {
			@Override
			public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
				if (oldState == newState) {
					return;
                }
				LOGGER.info(TextUtils.merge("Procedure ''{0}'' state changed to ''{1}''", subject.getName(), newState));
			}
		});

        if(arguments.isNoAutostart()) {
        	LOGGER.info("Not starting a scenario at startup, waiting for start instructions via REST.");
        } else {
	        Scenario scenario = identifyScenario(configuration, arguments.getStartScenario(), arguments.getStartGroup());
	        if (scenario == null) {
	            LOGGER.info("No configured scenario could be found for the given arguments: " + arguments.getStartGroup() + "/" + arguments.getStartScenario());
	        } else {
		        // blocking start of scenario
	        	LOGGER.info("Found Scenario " + scenario.getGroup() + "/" + scenario.getTitle());
		        engine.run(scenario);
	        }
        }

        try {
            startHttpService(EasyTravelConfig.read().launcherHttpPort);
        } catch (IOException e) {
            LaunchEngine.stop();
            LOGGER.error("Unable to run Command Line Launcher because the required HTTP Service could not be started", e);
            return;
        }

        try {
			EasyTravelConfig config = EasyTravelConfig.read();
            while (isRunning()) {
				Thread.sleep(config.isRunningCheckIntervalMs);
            }
        } catch (InterruptedException e) {
            LOGGER.error("Command Line Launcher is forced to stop.", e);
        }

		// do a println here as logging might already be stopped
        LOGGER.info("Stopping HTTP Service Thread and Engine because scenario is not executing any more or a shutdown request was received.");
        shutdown();
    }

	/**
     * Initialize logger for Commandline Launcher
     *
     * @author martin.wurzinger
     */
    private void setUpLogger() {
        // try to load the config from the classpath here...
        try {
            LoggerFactory.initLogging();
        } catch (IOException e) {
            System.err.println("Could not initialize logging from classpath: "); //NOPMD
            e.printStackTrace();
        }

        RootLogger.setup(new BasicLoggerConfig(MessageConstants.COMMANDLINE_LAUNCHER));
    }
}
