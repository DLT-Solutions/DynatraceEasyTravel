package com.dynatrace.diagnostics.uemload;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.ws.Endpoint;
import javax.xml.ws.http.HTTPBinding;

import com.dynatrace.diagnostics.uemload.scenarios.*;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.HainerOpenKitMobileAppScenario;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.OpenKitMobileAppScenario;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.dynatrace.diagnostics.uemload.headless.DriverEntryPoolSingleton;
import com.dynatrace.diagnostics.uemload.headless.MobileDriverEntryPoolSingleton;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.EasyTravelMobileAppScenario;
import com.dynatrace.diagnostics.uemload.utils.UserFileGenerator;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.ScenarioNames;
import com.dynatrace.easytravel.logging.RootLogger;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.dynatrace.easytravel.util.SpecialUserMode;
import com.dynatrace.easytravel.util.process.HeadlessProcessKillerFactory;
import com.google.common.base.Strings;

/***
 * Example usages:
 * 
 * For special user visits use a command like: 
 * java -jar uemload.jar --autorun true --server "http://XXX.XXX.XXX.XXX:9080/easytravel/home" --installationMode APM --scenario "Headless Angular Special User Visit"
 * To specify which visits to generate use the extra option of "--specialUserMode X" where X is the mode you want:
 * M/m - monthly user visits only
 * W/w - weekly user visits only
 * B/b - both weekly and monthly user visits. This is the default mode.
 * 
 * @author tomasz.wieremjewicz
 *
 */
public class CLI {

	private static final Logger LOGGER = Logger.getLogger(CLI.class.getName());
	private static Endpoint endpoint;

	private static Options createOptions() {
		Options options = new Options();
		options.addOption("h", false, "print this message");

		options.addOption("server", true, "location of the server, default: http://localhost:8080");
		options.addOption("b2bServer", true, "location of the b2b server, default: http://localhost:9000");
		options.addOption("highLoadFromAsia", true, "simulates high load from asian countries, it is disabled by default");
		options.addOption("uemLoadShutdownPort", true, "port that is used to initiate the shutdown of UEMLoad, default: 8995");

		options.addOption("scenario", true, "simulated scenario, default: EasyTravel");

		options.addOption("const", true, "constant load (<arg> visits per minute)");
		options.addOption("linear", true, "start with <arg> visits per minute, increase by <arg> every minute");
		options.addOption("sinus", true, "sinus load (maximum <arg> visits per minute)");
		options.addOption("random", true, "random walk load (maximum <arg> visits per minute)");
		options.addOption("once", true, "simulate <arg> visits within 1 minute, then stop");
		options.addOption("loadtime", true, "Average page load time in milliseconds");

		options.addOption("autorun", true, "Should simulation run automatically");

		options.addOption("installationMode", true, "Overwrites the current mode");
		
		options.addOption("specialUserMode", true, "Sets the generation to weekly users, monthly users or both with values W, M, B. B by default.");

		return options;
	}

	private static CommandLine cmd(String[] args) throws ParseException {
		Options options = createOptions();
		CommandLineParser parser = new PosixParser();
		return parser.parse(options, args);
	}

	public static void main(String[] args) throws Exception {		
	    RootLogger.setup(BaseConstants.LoggerNames.UEM_LOAD);

		CommandLine cmd = cmd(args);

		if (cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("uemload", createOptions());
			System.exit(0);
		}

		String server = cmd.getOptionValue("server", "http://localhost:8080");
		String scenarioName = cmd.getOptionValue("scenario", "EasyTravel");
		String b2bServer = cmd.getOptionValue("b2bServer", "http://localhost:8900");
		boolean highLoadFromAsia = cmd.getOptionValue("highLoadFromAsia", "false").trim().equalsIgnoreCase("true");

		String uemLoadShutdownPort = cmd.getOptionValue("uemLoadShutdownPort", "8095");

		String autorun = cmd.getOptionValue("autorun", "false");

		if (!Strings.isNullOrEmpty(cmd.getOptionValue("installationMode"))) {
			HashMap<String,String> customOptions = new HashMap<String,String>();
			customOptions.put("config.apmServerDefault", cmd.getOptionValue("installationMode"));
			EasyTravelConfig.applyCustomSettings(customOptions);
		}

		endpoint = Endpoint.create(HTTPBinding.HTTP_BINDING,
									new ShutdownListener());

		endpoint.publish("http://localhost:" + uemLoadShutdownPort + "/uemload/shutdown");

		LOGGER.info("UEMLoad Scenario: " + scenarioName);

		Series numVisits;
		int once = -1;
		if (cmd.hasOption("const")) {
			int c = Integer.parseInt(cmd.getOptionValue("const"));
			numVisits = new LinearSeries(c, 0);
		} else if (cmd.hasOption("linear")) {
			int c = Integer.parseInt(cmd.getOptionValue("linear"));
			numVisits = new LinearSeries(c, c);
		} else if (cmd.hasOption("sinus")) {
			int c = Integer.parseInt(cmd.getOptionValue("sinus"));
			numVisits = new SinusSeries(0, c, 60);
		} else if (cmd.hasOption("random")) {
			int c = Integer.parseInt(cmd.getOptionValue("random"));
			numVisits = new RandomWalk(0, c);
		} else if (cmd.hasOption("once")) {
			once = Integer.parseInt(cmd.getOptionValue("once"));
			numVisits = null;
		} else {
			numVisits = new LinearSeries(30, 0);
		}

		int defaultPageLoadTime = 3000;
		if (cmd.hasOption("loadtime")) {
			defaultPageLoadTime = Integer.parseInt(cmd.getOptionValue("loadtime"));
		}
		
		SpecialUserMode specialUserMode = SpecialUserMode.BOTH;
		if (cmd.hasOption("specialUserMode")) {
			specialUserMode = SpecialUserMode.valueOfOptionValue(cmd.getOptionValue("specialUserMode", "N").toUpperCase());
		}

		// wait for plugin refresh thread to be started
		PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.ADS_ENABLEMENT_PLUGIN);
		Thread.sleep(PluginChangeMonitor.PLUGIN_STATE_CHECKER_DELAY*2*1000);
		
		UEMLoadScenario scenario = null;
		if ("AjaxWorld".equals(scenarioName)) {
			scenario = new AjaxWorldPageviews(server + "/ajax", 1, 0, 4000);
		} else if ("EasyTravel".equals(scenarioName)) {
			scenario = new EasyTravelCustomer(server, b2bServer, highLoadFromAsia);
		} else if ("EasyTravelB2B".equals(scenarioName)) {
			scenario = new EasyTravelB2B(b2bServer, highLoadFromAsia);
		} else if ("EasyTravelFixed".equals(scenarioName)) {
			scenario = new EasyTravelFixedCustomer(server, b2bServer, highLoadFromAsia);
		} else if ("EasyTravelPredictable".equals(scenarioName)) {
			//using 10 as base value for load. It will be updated later in VisitScheduler
			int load = (once > 0 ? once : 10);
			scenario = new EasyTravelPredictableCustomer(server, b2bServer, highLoadFromAsia, load);
		} else if ("EasyTravelApache".equals(scenarioName)) {
			scenario = new EasyTravelCustomer("http://localhost:8079", "http://localhost:8002", highLoadFromAsia);
		} else if ("Page".equals(scenarioName)) {
			scenario = new SinglePage(server, defaultPageLoadTime);
		} else if ("MobileApp".equals(scenarioName)) {
			scenario = new EasyTravelMobileAppScenario("http://localhost:8080");
		} else if ("MobileAppOpenKit".equals(scenarioName)) {
			scenario = new OpenKitMobileAppScenario();
		} else if ("HainerMobileAppOpenKit".equals(scenarioName)) {
			scenario = new HainerOpenKitMobileAppScenario();
		} else if ("RentalCarsIoTApp".equals(scenarioName)) {
			scenario = new IotDevicesScenario();
		} else if (ScenarioNames.HEADLESS_CUSTOMER.equals(scenarioName)) {
			scenario = new HeadlessCustomerScenario();
		} else if (ScenarioNames.HEADLESS_ANGULAR.equals(scenarioName)) {
			scenario = new HeadlessAngularScenario();
		} else if (ScenarioNames.HEADLESS_ANGULAR_MOBILE.equals(scenarioName)) {
			scenario = new HeadlessMobileAngularScenario();
		} else if (ScenarioNames.HEADLESS_ANGULAR_MOUSE_VISIT.equals(scenarioName)) {
			scenario = new HeadlessAngularOneUserMouseVisitScenario(ExtendedDemoUser.HAINER_USER);
		} else if (ScenarioNames.HEADLESS_ANGULAR_SPECIAL_USER_VISIT.equals(scenarioName)) {
			scenario = new HeadlessAngularOneSpecialUserVisitScenario(ExtendedDemoUser.WEEKLY_USER_1);     
		} else if (ScenarioNames.HEADLESS_ONLINE_BOUTIQUE.equals(scenarioName)) {
			scenario = new HeadlessOnlineBoutiqueScenario();	
		} else if (ScenarioNames.HEADLESS_B2B.equals(scenarioName)) {
			scenario = new HeadlessB2BScenario(server);	
		} else {
			System.err.println("Unknown scenario '" + scenarioName + "'");
			System.exit(1);
		}
		
		setServer(scenario, server);
		setupDriverEntryPool(scenario);

		if(!(new File(Directories.getConfigDir(), ResourceFileReader.EXTENDEDUSERS).exists())){
			UserFileGenerator generator = new UserFileGenerator();
			generator.generateUserFile();
		}
		
		if (autorun.equalsIgnoreCase("true")) {
			if (ScenarioNames.HEADLESS_ANGULAR_SPECIAL_USER_VISIT.equals(scenarioName)) {
				generateSpecialVisits(scenario, specialUserMode);
				System.exit(0);
			}
			else {
				Simulator simulator = scenario.createSimulator();
				if (once > 0) {
					simulator.loggingActivated = true;
					simulator.runOnce(once);
					
					shutDown(scenario);
					System.exit(0);
				} else {
					simulator.run(numVisits, true, true);
				}
			}
		}
	}
		
	private static void generateSpecialVisits(UEMLoadScenario scenario, SpecialUserMode specialUserMode) throws InterruptedException {
		List<ExtendedCommonUser> weekelyUsers = Stream.of(
				ExtendedDemoUser.WEEKLY_USER_1, ExtendedDemoUser.WEEKLY_USER_2,
				ExtendedDemoUser.WEEKLY_USER_3, ExtendedDemoUser.WEEKLY_USER_4,
				ExtendedDemoUser.WEEKLY_USER_5, ExtendedDemoUser.WEEKLY_USER_6,
				ExtendedDemoUser.WEEKLY_USER_7, ExtendedDemoUser.WEEKLY_USER_8,
				ExtendedDemoUser.WEEKLY_USER_9, ExtendedDemoUser.WEEKLY_USER_10,
				ExtendedDemoUser.WEEKLY_USER_11)
				.collect(Collectors.toList());
		
		List<ExtendedCommonUser> monthlyUsers = Stream.of(
				ExtendedDemoUser.MONTHLY_USER_1, ExtendedDemoUser.MONTHLY_USER_2,
				ExtendedDemoUser.MONTHLY_USER_3, ExtendedDemoUser.MONTHLY_USER_4,
				ExtendedDemoUser.MONTHLY_USER_5, ExtendedDemoUser.MONTHLY_USER_6,
				ExtendedDemoUser.MONTHLY_USER_7, ExtendedDemoUser.MONTHLY_USER_8,
				ExtendedDemoUser.MONTHLY_USER_9, ExtendedDemoUser.MONTHLY_USER_10,
				ExtendedDemoUser.MONTHLY_USER_11)
				.collect(Collectors.toList());
	
		Simulator simulator;
		simulator = scenario.createSimulator();
		simulator.loggingActivated = true;
		
		if (SpecialUserMode.BOTH.equals(specialUserMode) || SpecialUserMode.WEEKLY.equals(specialUserMode)) {
			generateSpecialVisitsForUsers(scenario, simulator, weekelyUsers, 30000);
			generateSpecialVisitsForUsers(scenario, simulator, weekelyUsers, 30000);
		}
		
		if (SpecialUserMode.BOTH.equals(specialUserMode) || SpecialUserMode.MONTHLY.equals(specialUserMode)) {
			generateSpecialVisitsForUsers(scenario, simulator, monthlyUsers, 30000);
		}
		
		Thread.sleep(60000);
		simulator.shutdownUemLoadScheduler();
		shutDown(scenario);
	}
	
	private static void generateSpecialVisitsForUsers(
			UEMLoadScenario scenario, Simulator simulator, List<ExtendedCommonUser> users, int delayMillis) throws InterruptedException {
		for (ExtendedCommonUser extendedCommonUser : users) {
			((HeadlessAngularOneSpecialUserVisitScenario) scenario).setUser(extendedCommonUser);
			simulator.runOnceSkipShutdown(1);
			Thread.sleep(delayMillis);
		}
	}
	
	private static void shutDown(UEMLoadScenario scenario) {
		if(scenario == null || isHeadlessScenario(scenario)) {
			DriverEntryPoolSingleton.getInstance().getPool().stopAll();
			MobileDriverEntryPoolSingleton.getInstance().getPool().stopAll();

			HeadlessProcessKillerFactory.stopChromeProcesses(true);
		}
		if(scenario == null ||  isOpenkitScenario(scenario)) {
			MobileDevice.shutdownOpenKit();
		}
	}
	
	private static void setServer(UEMLoadScenario scenario, String server) {
		if (isHeadlessAngularScenario(scenario)) {
			((HeadlessScenario) scenario).getHostsManager().addAngularFrontendHost(server);
		}		
		if (scenario instanceof OpenKitScenario) {
			((OpenKitScenario) scenario).getHostsManager().addAngularFrontendHost(server);
		}
		if (scenario instanceof HeadlessOnlineBoutiqueScenario) {
			((HeadlessOnlineBoutiqueScenario) scenario).getHostsManager().addOnlineBoutiqueHost(server);
		}
		if ( isHeadlessCustomerScenario(scenario)) {
			((HeadlessScenario) scenario).getHostsManager().addCustomerFrontendHost(server);
		}
	}
	
	private static boolean isHeadlessAngularScenario(UEMLoadScenario scenario) {
		return scenario instanceof HeadlessAngularScenarioBase || scenario instanceof HeadlessAngularOneVisitScenario
				|| scenario instanceof HeadlessAngularOneSpecialUserVisitScenario || scenario instanceof HeadlessOnlineBoutiqueScenario;
	}
	
	private static boolean isOpenkitScenario(UEMLoadScenario scenario) {
		return scenario instanceof OpenKitMobileAppScenario; 
	}
	
	private static boolean isHeadlessCustomerScenario(UEMLoadScenario scenario) {
		return scenario instanceof HeadlessCustomerScenario || scenario instanceof HeadlessCustomerOverloadScenario;
	}
	
	private static void setupDriverEntryPool(UEMLoadScenario scenario) {
		if ( isHeadlessScenario(scenario) ) {
			DriverEntryPoolSingleton.getInstance().getPool().start();
			MobileDriverEntryPoolSingleton.getInstance().getPool().start();
		}
	}
	
	private static boolean isHeadlessScenario(UEMLoadScenario scenario) {
		return isHeadlessAngularScenario(scenario) || (scenario instanceof HeadlessOnlineBoutiqueScenario) || isHeadlessCustomerScenario(scenario);
	}
	
	public static void stopUemLoad() {
		UemLoadScheduler.shutdownNow();
		int sleepTime = 100;
		for (int i = 0; !UemLoadScheduler.isShutdown() || i < 5; i++) {
			try {
				Thread.sleep(sleepTime);
				LOGGER.info(i + " time: " + (i + 1) * sleepTime);
			} catch (InterruptedException e) {
			}
		}
		shutDown(null);
		endpoint.stop();
		LOGGER.info("UEMLoad stopped successfully.");
		System.exit(0);
	}

}
