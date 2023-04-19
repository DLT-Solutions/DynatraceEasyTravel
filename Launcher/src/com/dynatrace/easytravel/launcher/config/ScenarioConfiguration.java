package com.dynatrace.easytravel.launcher.config;

import static com.dynatrace.easytravel.launcher.engine.ProcedureFactory.isRemote;
import static com.dynatrace.easytravel.launcher.misc.Constants.Procedures.B2B_FRONTEND_ID;
import static com.dynatrace.easytravel.launcher.misc.Constants.Procedures.PAYMENT_BACKEND_ID;
import static com.dynatrace.easytravel.launcher.misc.MessageConstants.*;
import static com.dynatrace.easytravel.launcher.scenarios.builder.ProcedureBuilder.*;
import static com.dynatrace.easytravel.launcher.scenarios.builder.ScenarioBuilder.scenario;
import static com.dynatrace.easytravel.launcher.scenarios.builder.ScenarioGroupBuilder.group;
import static com.dynatrace.easytravel.launcher.scenarios.builder.SettingBuilder.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.procedures.AntProcedure;
import com.dynatrace.easytravel.launcher.procedures.utils.CentralTechnologyActivator;
import com.dynatrace.easytravel.launcher.procedures.utils.TechnologyActivator;
import com.dynatrace.easytravel.launcher.procedures.utils.TechnologyActivatorListener;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.DefaultScenarioGroup;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.launcher.scenarios.ScenarioGroup;
import com.dynatrace.easytravel.launcher.scenarios.builder.ScenarioGroupBuilder;
import com.dynatrace.easytravel.util.TextUtils;


public class ScenarioConfiguration implements Persistable, TechnologyActivatorListener {

	private static final Logger LOGGER = Logger.getLogger(ScenarioConfiguration.class.getName());
	private static final String NODE_GROUP = "group";

	private static boolean manualStart = false;

	private final List<ScenarioGroup> scenarioGroups = new ArrayList<ScenarioGroup>();
	private final ScenarioConfigurationPersistence persistence;

	private String defaultHash = null;
	private String initialHash;

	private static boolean isWindows = SystemUtils.IS_OS_WINDOWS;
	private static boolean isLinux = SystemUtils.IS_OS_LINUX;
	private static boolean isMacOS = SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX;
	private static boolean isPluginServiceHost = StringUtils.isNotBlank(EasyTravelConfig.read().pluginServiceHost);

	public static void setManualStart(boolean manualStart) {
		ScenarioConfiguration.manualStart = manualStart;
	}

	public ScenarioConfiguration() {
		this((File) null);
	}

	public ScenarioConfiguration(File scenarioPersistenceFile) {
		this(new ScenarioConfigurationPersistence(scenarioPersistenceFile));
	}

	public ScenarioConfiguration(ScenarioConfigurationPersistence persistence) {
		this.persistence = persistence;
		CentralTechnologyActivator.getIntance().registerBackendListener(this);
		ScenarioConfigurationAdditionalSettings.INSTANCE.setScenarioConfigurationPersistence(persistence);
	}

	/**
	 * An ordered list of scenario groups.
	 *
	 * @return order list of scenario groups that must not be <code>null<code>
	 * @author martin.wurzinger
	 */
	public List<ScenarioGroup> getScenarioGroups() {
		return Collections.unmodifiableList(scenarioGroups);
	}

	private static boolean isWindowsOrRemote(String procedureId) {
		return isWindows || isRemote(procedureId);
	}

	public void createDefaultScenarios() {
		List<ScenarioGroup> scenarioGroups = new ArrayList<ScenarioGroup>();

		// +++++++ UEM GROUP - BEGIN ++++++++
		ScenarioGroupBuilder uemGroup = group(SCENARIO_GROUP_UEM_TITLE);

		// ------- STANDARD UEM SCENARIO ---------
		uemGroup.add(
				scenario(UEM_SCENARIO_DEFAULT_TITLE, UEM_SCENARIO_DEFAULT_DESC, InstallationType.Both).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.ThirdPartyAdvertisements).enable())).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend(), angularFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						add(thirdpartyContentServer()).
						addIf(isWindows, webserverAgentControl()).
						add(apacheHttpd()).
						addIf(isWindows, hostAgentControl()).
						create());

		// ------- STANDARD UEM WITH NGINX ---------
        // only for Linux
        if (isLinux) {
            uemGroup.add(
                    scenario(UEM_WITH_NGINX_SCENARIO_DEFAULT_TITLE, UEM_SCENARIO_DEFAULT_DESC, InstallationType.Both).
                            addIf(isPluginServiceHost, pluginService()).
                            add(dbms()).
                            add(contentCreator()).
                            addIf(!isMacOS, creditCard()).
                            add(businessBackend().
                                    set(plugin(Constants.Plugin.ThirdPartyAdvertisements).enable())).
                            addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
                            add(customerFrontend(), angularFrontend()).
                            addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
                            add(thirdpartyContentServer()).
                            addIf(isWindows, webserverAgentControl()).
                            add(nginxWebserver()).
                            addIf(isWindows, hostAgentControl()).
                            create());
        }

		// ------- UEM-ONLY SCENARIO WITH NO AGENTS in Java/.NET Procedures ---------
		uemGroup.add(
				scenario(UEM_SCENARIO_UEM_ONLY_TITLE, UEM_SCENARIO_UEM_ONLY_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						add(creditCard().
								set(config("config.creditCardAuthorizationAgent").value(BaseConstants.NONE))).
						add(businessBackend().
								set(plugin(Constants.Plugin.ThirdPartyAdvertisements).enable()).
								set(plugin(Constants.Plugin.JavascriptTagging).enable()).
								set(config("config.backendAgent").value(BaseConstants.NONE))).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend().
								set(config("config.paymentBackendEnvArgs").value("COR_ENABLE_PROFILING=0x0"))).
						add(customerFrontend().
								set(config("config.frontendAgent").value(BaseConstants.NONE))).
								set(plugin(Constants.Plugin.JavascriptTagging).enable()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend().
								set(config("config.b2bFrontendEnvArgs").value("COR_ENABLE_PROFILING=0x0"))).
						add(thirdpartyContentServer()).
						addIf(isWindows, webserverAgentControl()).
						add(apacheHttpd()).
						addIf(isWindows, hostAgentControl()).
						create());

		// ------- MINIMAL UEM SCENARIO ---------
		uemGroup.add(
				scenario(UEM_SCENARIO_MINIMAL_TITLE, UEM_SCENARIO_MINIMAL_DESC, InstallationType.Both).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend()).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend(), angularFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						// add procedures for Apache HTTPD
						addIf(isWindows, webserverAgentControl()).
						add(apacheHttpd()).
						addIf(isWindows, hostAgentControl()).
						create());

		// ------- PHP UEM SCENARIO ---------
		 	uemGroup.add(
					scenario(PHP_SCENARIO_DEFAULT_TITLE, PHP_SCENARIO_DEFAULT_DESC, InstallationType.Both).
							addIf(isPluginServiceHost, pluginService()).
							add(dbms()).
							add(contentCreator()).
							add(mdbms()).
							add(mdcontentCreator()).
							addIf(!isMacOS, creditCard()).
							add(businessBackend().
									// special plugin for enablement check in the Customer Frontend
									set(plugin(BaseConstants.Plugins.PHP_ENABLEMENT_PLUGIN).enable()).
									set(plugin(Constants.Plugin.ThirdPartyAdvertisements).enable())).
							addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
							add(customerFrontend(), angularFrontend()).
							addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
							add(thirdpartyContentServer()).
							addIf(isWindows, webserverAgentControl()).
							add(apacheHttpdPhp()).
							addIf(isWindows, hostAgentControl()).
							create());

			// ------- STANDARD UEM SCENARIO WITH REST AND ANGULAR ---------
			//TODO: Write description
			uemGroup.add(
					scenario(UEM_WITH_REST_SCENARIO_DEFAULT_TITLE, UEM_WITH_REST_SCENARIO_DEFAULT_DESC, InstallationType.Both).
							addIf(isPluginServiceHost, pluginService()).
							add(dbms()).
							add(contentCreator()).
							addIf(!isMacOS, creditCard()).
							add(businessBackend().
									set(plugin(Constants.Plugin.ThirdPartyAdvertisements).enable())).
							addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
							add(customerFrontend(), angularFrontend()).
							addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
							add(thirdpartyContentServer()).
							addIf(isWindows, webserverAgentControl()).
							add(apacheHttpd()).
							addIf(isWindows, hostAgentControl()).
							create());

			// ------- STANDARD UEM SCENARIO WITH REST, ANGULAR AND PHP ---------
			//TODO: Write description
			uemGroup.add(
					scenario(UEM_WITH_REST_AND_PHP_SCENARIO_DEFAULT_TITLE, UEM_WITH_REST_AND_PHP_SCENARIO_DEFAULT_DESC, InstallationType.Both).
							addIf(isPluginServiceHost, pluginService()).
							add(dbms()).
							add(contentCreator()).
							add(mdbms()).
							add(mdcontentCreator()).
							addIf(!isMacOS, creditCard()).
							add(businessBackend().
									set(plugin(BaseConstants.Plugins.PHP_ENABLEMENT_PLUGIN).enable()).
									set(plugin(Constants.Plugin.ThirdPartyAdvertisements).enable())).
							addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
							add(customerFrontend(), angularFrontend()).
							addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
							add(thirdpartyContentServer()).
							addIf(isWindows, webserverAgentControl()).
							add(apacheHttpdPhp()).
							addIf(isWindows, hostAgentControl()).
							create());

		// ------- Special scenario where Apache acts as Proxy and thus allows to inject Javascript Agent into any page ---------
		if (isWindows) {
			uemGroup.add(
					scenario(UEM_SCANARIO_PROXY_INJECTION, UEM_SCANARIO_PROXY_INJECTION_DESC, InstallationType.Classic).
							addIf(isWindows, webserverAgentControl()).
							add(apacheHttpd()).
							add(browser()).
							create());
		}

		// ------- BASELINING DEMO SCENARIO ---------
		uemGroup.add(
				scenario(UEM_SCANARIO_BASELINING_DEMO, UEM_SCANARIO_BASELINING_DEMO_DESC, InstallationType.Classic).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.JourneyUpdateSlow).setStayOffDuration(900).setStayOnDuration(300).disable()).
								set(plugin(Constants.Plugin.DisableHibernateCache).setStayOffDuration(900).setStayOnDuration(300).disable()).
								set(plugin(Constants.Plugin.LoginProblems).setStayOffDuration(720).setStayOnDuration(180).disable()).
								set(plugin(Constants.Plugin.ThirdPartyAdvertisements).enable()).
								set(config("config.backendJavaopts").value("-Xmx256m"))).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend().set(config("config.frontendJavaopts").value("-Xmx416m"))).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend().disable()).
						add(thirdpartyContentServer()).
						addIf(isWindows, webserverAgentControl()).
						add(apacheHttpd()).
						set(scenarioConfig("config.baseLoadDefault").value("30")).
						set(scenarioConfig("config.baseLoadB2BRatio").value("0.2")).
						set(scenarioConfig("config.baseLoadCustomerRatio").value("1.0")).
						set(scenarioConfig("config.baseLoadMobileNativeRatio").value("0.3")).
						set(scenarioConfig("config.baseLoadMobileBrowserRatio").value("0.3")).
						addIf(isWindows, hostAgentControl()).
						create());

		// ------- COUCHDB SCENARIO ---------
		// As Standard + CouchDB
		if (isWindows) { // CouchDB is currently only supported for Windows.
			uemGroup.add(
				scenario(UEM_SCENARIO_COUCHDB_TITLE, UEM_SCENARIO_COUCHDB_DESC, InstallationType.Both).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(isWindows, couchDB()).
						addIf(isWindows, couchDBContentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.ThirdPartyAdvertisements).enable()).
								set(plugin(Constants.Plugin.CouchDB).enable())).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend(), angularFrontend()).

						// iddIf(OS,...  checks are now redundant, as we have conditioned the entire scenario.
						// However, retaining them here is perhaps of informational value.

						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						add(thirdpartyContentServer()).
						addIf(isWindows, webserverAgentControl()).
						add(apacheHttpd()).
						create());
		}

		scenarioGroups.add(uemGroup.create());
		// +++++++ UEM GROUP - END ++++++++

		// +++++++ PRODUCTION GROUP - BEGIN ++++++++
		ScenarioGroupBuilder productionGroup = group(SCENARIO_GROUP_PRODCUTION_TITLE);

		// ------- STANDARD SCENARIO --------
		productionGroup.add(
				scenario(PRODUCTION_SCENARIO_STANDARD_TITLE, PRODUCTION_SCENARIO_STANDARD_DESC).
						// NOTE: the order of these procedures is the order in which we start the processes
						// stopping is done in reverse order, therefore it makes sense to order these depending
						// on the inter-dependencies between the procedures, although we do not have a full
						// dependency-management between procedures in the Launcher on purpose, the processes
						// should be robust enough to cope with missing backend et. al. themselves and need to
						// recover as soon as the services are restored!
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend()).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend(), angularFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						addIf(isWindows, hostAgentControl()).
						create());

		// ------- BASELINING DEMO SCENARIO ---------
		productionGroup.add(
				scenario(UEM_SCANARIO_BASELINING_DEMO, UEM_SCANARIO_BASELINING_DEMO_DESC).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.JourneyUpdateSlow).setStayOffDuration(900).setStayOnDuration(300).disable()).
								set(plugin(Constants.Plugin.DisableHibernateCache).setStayOffDuration(900).setStayOnDuration(300).disable()).
								set(plugin(Constants.Plugin.LoginProblems).setStayOffDuration(720).setStayOnDuration(180).disable()).
								set(plugin(Constants.Plugin.ThirdPartyAdvertisements).enable()).
								set(config("config.backendJavaopts").value("-Xmx256m"))).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend().set(config("config.frontendJavaopts").value("-Xmx416m"))).
						add(angularFrontend().set(config("config.frontendJavaopts").value("-Xmx416m"))).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend().disable()).
						add(thirdpartyContentServer()).
						addIf(isWindows, webserverAgentControl()).
						add(apacheHttpd()).
						set(scenarioConfig("config.baseLoadDefault").value("30")).
						set(scenarioConfig("config.baseLoadB2BRatio").value("0.2")).
						set(scenarioConfig("config.baseLoadCustomerRatio").value("1.0")).
						set(scenarioConfig("config.baseLoadMobileNativeRatio").value("0.3")).
						set(scenarioConfig("config.baseLoadMobileBrowserRatio").value("0.3")).
						addIf(isWindows, hostAgentControl()).
						create());

		// ------- UPDATE REGRESSION SCENARIO -------
		productionGroup.add(
				scenario(PRODUCTION_SCENARIO_UPDATE_REGRESSION_TITLE, PRODUCTION_SCENARIO_UPDATE_REGRESSION_DESC).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.DatabaseAccessFromFrontend).enable()).
								set(plugin(Constants.Plugin.MissingServletError404).enable()).
								set(plugin(Constants.Plugin.DBSpamming).enable()).
								set(plugin(Constants.Plugin.SmallMemoryLeak).enable())).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend(), angularFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						addIf(isWindows, hostAgentControl()).
						create());


		// ------- BLACK FRIDAY SCENARIO -------
		productionGroup.add(
				scenario(PRODUCTION_SCENARIO_BLACK_FRIDAY_TITLE, PRODUCTION_SCENARIO_BLACK_FRIDAY_DESC).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend()).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend(), angularFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						add(createCanooLoadTestProcedureMapping()).
						addIf(isWindows, hostAgentControl()).
						create());


		// ------- BEST PRACTICES SCENARIO -------
		productionGroup.add(
				scenario(DEMO_STORY_BOARD_SCENARIO_TITLE, DEMO_STORY_BOARD_SCENARIO_DESCRIPTION).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.BookingErrorAsHttp500).enable()).
								set(plugin(Constants.Plugin.GarbageCollectionEvery10Seconds).enable()).
								set(plugin(Constants.Plugin.UseLocationCache).enable()).
								set(plugin(Constants.Plugin.TravellersOptionBox).enable())).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend(), angularFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						addIf(isWindows, webserverAgentControl()).
						add(apacheHttpd()).
						addIf(isWindows, hostAgentControl()).
						create());

		// ------- MONGODB SCENARIO -------
		productionGroup.add(
				scenario(PRODUCTION_SCENARIO_MONGODB_TITLE, PRODUCTION_SCENARIO_MONGODB_DESC).
						addIf(isPluginServiceHost, pluginService()).
						add(mongodb()).
						add(contentCreator().
								set(property(Constants.Misc.SETTING_PERSISTENCE_MODE).value(BusinessBackend.Persistence.MONGODB))).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(property(Constants.Misc.SETTING_PERSISTENCE_MODE).value(BusinessBackend.Persistence.MONGODB))).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend().
								set(property(Constants.Misc.SETTING_PERSISTENCE_MODE).value(BusinessBackend.Persistence.MONGODB))).
						add(angularFrontend().
								set(property(Constants.Misc.SETTING_PERSISTENCE_MODE).value(BusinessBackend.Persistence.MONGODB))).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						addIf(isWindows, hostAgentControl()).
						create());

		// ------- JMS SCENARIO -------
		productionGroup.add(
				scenario(PRODUCTION_SCENARIO_JMS_TITLE, PRODUCTION_SCENARIO_JMS_DESC).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.HotDealServerJMS).enable()).
								set(plugin(Constants.Plugin.HotDealClientJMS).enable())).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend(), angularFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						create());


		// ------- RMI SCENARIO -------
		productionGroup.add(
				scenario(PRODUCTION_SCENARIO_RMI_TITLE, PRODUCTION_SCENARIO_RMI_DESC).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.HotDealServerRMI).enable()).
								set(plugin(Constants.Plugin.HotDealClientRMI).enable())).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend(), angularFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						create());

		scenarioGroups.add(productionGroup.create());
		// +++++++ PRODUCTION GROUP - END ++++++++


		// +++++++ TEST CENTER GROUP - BEGIN ++++++++
		ScenarioGroupBuilder testCenterGroup = group(SCENARIO_GROUP_TESTCENTER_TITLE);

		// ------- PERFORMANCE PROBLEM SCENARIO ---------
		testCenterGroup.add(
				scenario(TESTCENTER_SCENARIO_PERFORMANCE_TITLE, TESTCENTER_SCENARIO_PERFORMANCE_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.DBSpamming).enable())).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						create());


		// ------- MEMORY PROBLEM SCENARIO ---------
		testCenterGroup.add(
				scenario(TESTCENTER_SCENARIO_MEMORY_TITLE, TESTCENTER_SCENARIO_MEMORY_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.SmallMemoryLeak).enable())).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						create());


		// ------- INCREASING LOAD SCENARIO ---------
		testCenterGroup.add(
				scenario(TESTCENTER_SCENARIO_INCREASING_LOAD_TITLE, TESTCENTER_SCENARIO_INCREASING_LOAD_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.EnableCaching).enable())).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						create());


		// ------- SCALABILITY PROBLEM SCENARIO ---------
		testCenterGroup.add(
				scenario(TESTCENTER_SCENARIO_SCALABILITY_TITLE, TESTCENTER_SCENARIO_SCALABILITY_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend()).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).

						// disable this whole scenario as it is not fully done yet
						disable().

						create());

		scenarioGroups.add(testCenterGroup.create());


		// +++++++ TEST CENTER GROUP - END ++++++++


		// +++++++ DEV TEAM GROUP - BEGIN ++++++++
		ScenarioGroupBuilder devTeamGroup = group(MessageConstants.SCENARIO_GROUP_DEVTEAM_TITLE);

		// ------- UNIT TESTING SCENARIO ---------
		devTeamGroup.add(
				scenario(DEVTEAM_SCENARIO_UNITTESTING_TITLE, DEVTEAM_SCENARIO_UNITTESTING_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend()).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						add(createJUnitProcedureMapping()).
						create());

		// ------- webAPI TESTING SCENARIO ---------
				devTeamGroup.add(
						scenario(DEVTEAM_SCENARIO_WEBAPITESTING_TITLE, DEVTEAM_SCENARIO_WEBAPITESTING_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend()).
						add(customerFrontend()).
						add(createWebapiProcedureMapping()).
						create());

		// ------- FUNCTIONAL WEBSITE TESTING SCENARIO ---------
		devTeamGroup.add(
				scenario(DEVTEAM_SCENARIO_FUNCTIONALTESTING_TITLE, DEVTEAM_SCENARIO_FUNCTIONALTESTING_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								// disable DatabaseCleanup when running Webdriver/Selenium Tests
								set(plugin(Constants.Plugin.DatabaseCleanup).disable())). // Note: will only work if enable before
	// disable in BasePluginManager
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						add(createSeleniumProcedureMapping(MODULE_WEBDRIVER_TESTS_ONCE, "runAllTests", "ie ff", isWindows)).
						add(createSeleniumProcedureMapping(MODULE_WEBDRIVER_TESTS_ONCE, "runAllTests", "ff", !isWindows)).
						create());

		// ------- DRIVE AND RECORD LOAD TESTS SCENARIO ---------
		devTeamGroup.add(
				scenario(DEVTEAM_SCENARIO_LOADTESTING_TITLE, DEVTEAM_SCENARIO_LOADTESTING_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.EnableCaching).enable())).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						add(createLoadGeneratorJUnitProcedureMapping()).
						set(property(AntProcedure.FILE).value(BaseConstants.SubDirectories.TEST + "/runtest.xml")).
						create());


		// ------- BUILD BREAK SCENARIO ---------
		devTeamGroup.add(
				scenario(DEVTEAM_SCENARIO_BUILDBREAK_TITLE, DEVTEAM_SCENARIO_BUILDBREAK_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.DBSpamming).enable()).
								set(plugin(Constants.Plugin.WPOPagePlugin).enable()).
								set(plugin(Constants.Plugin.DatabaseCleanup).disable())). // Note: will only work if enable before
// disable in BasePluginManager
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						add(createSeleniumProcedureMapping(MODULE_WEBDRIVER_TESTS_ONCE, "runAllTestsOnce", "ie", isWindows)).
						add(createSeleniumProcedureMapping(MODULE_WEBDRIVER_TESTS_ONCE, "runAllTestsOnce", "ff", !isWindows)).
						create());


		// ------- PERFORMANCE PROBLEM SCENARIO ---------
		devTeamGroup.add(
				scenario(DEVTEAM_SCENARIO_PERFREG_TITLE, DEVTEAM_SCENARIO_PERFREG_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						addIf(!isMacOS, creditCard()).
						add(businessBackend().
								set(plugin(Constants.Plugin.DBSpamming).enable())).
						addIf(isWindowsOrRemote(PAYMENT_BACKEND_ID), paymentBackend()).
						add(customerFrontend()).
						addIf(isWindowsOrRemote(B2B_FRONTEND_ID), b2bFrontend()).
						create());


		scenarioGroups.add(devTeamGroup.create());
		// +++++++ DEV TEAM GROUP - END ++++++++

		// +++++++ Mainframe GROUP - BEGIN ++++++++
		ScenarioGroupBuilder mainframeGroup = group(SCENARIO_GROUP_MAINFRAME_TITLE);
		// ------- WSMQ+WSMB SCENARIO ---------
		mainframeGroup.add(
				scenario(MAINFRAME_SCENARIO_WSMQ_TITLE, MAINFRAME_SCENARIO_WSMQ_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						add(businessBackend().set(
								plugin(Constants.Plugin.WSMQNativeApplication).enable(),
								plugin(Constants.Plugin.DummyPaymentService).enable(),
								// Also deactive boot plugins:
								plugin(Constants.Plugin.NamedPipeNativeApplication).disable(),
								plugin(Constants.Plugin.SocketNativeApplication).disable(),
								plugin(Constants.Plugin.DotNetPaymentService).disable(),
								plugin(Constants.Plugin.DatabaseCleanup).disable())).
						add(customerFrontend()).
						create());

		mainframeGroup.add(
				scenario(MAINFRAME_SCENARIO_CICS_TITLE, MAINFRAME_SCENARIO_CICS_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						add(businessBackend().set(
								plugin(Constants.Plugin.CTGNativeApplication).enable(),
								plugin(Constants.Plugin.DummyPaymentService).enable(),
								// Also deactive boot plugins:
								plugin(Constants.Plugin.NamedPipeNativeApplication).disable(),
								plugin(Constants.Plugin.SocketNativeApplication).disable(),
								plugin(Constants.Plugin.DotNetPaymentService).disable(),
								plugin(Constants.Plugin.DatabaseCleanup).disable())).
						add(customerFrontend()).
						create());

		mainframeGroup.add(
				scenario(MAINFRAME_SCENARIO_IMS_TITLE, MAINFRAME_SCENARIO_IMS_DESC, InstallationType.Classic).
						addIf(isPluginServiceHost, pluginService()).
						add(dbms()).
						add(contentCreator()).
						add(businessBackend().set(
								plugin(Constants.Plugin.IMSNativeApplication).enable(),
								plugin(Constants.Plugin.DummyPaymentService).enable(),
								// Also deactive boot plugins:
								plugin(Constants.Plugin.NamedPipeNativeApplication).disable(),
								plugin(Constants.Plugin.SocketNativeApplication).disable(),
								plugin(Constants.Plugin.DotNetPaymentService).disable(),
								plugin(Constants.Plugin.DatabaseCleanup).disable())).
						add(customerFrontend()).
						create());


		scenarioGroups.add(mainframeGroup.create());
		// +++++++ Mainframe GROUP - END ++++++++


		reset();
		addGroups(scenarioGroups);
	}

	private void addGroups(Collection<? extends ScenarioGroup> scenarioGroups) {
		this.scenarioGroups.addAll(scenarioGroups);

		// verify that we do not have duplicates
		Set<String> groupSet = new HashSet<String>();
		// Set<String> scenarioSet = new HashSet<String>();
		for (ScenarioGroup group : this.scenarioGroups) {
			if (!groupSet.add(group.getTitle())) {
				throw new IllegalStateException("The scenario group '" + group.getTitle() +
						"' was used twice, but group titles need to be unique.");
			}

			/*
			 * same scenario in different groups is allowed...
			 * for(Scenario scenario : group.getScenarios()) {
			 * if(!scenarioSet.add(scenario.getTitle())) {
			 * throw new IllegalStateException("The scenario '" + scenario.getTitle() +
			 * "' was used twice, but scenario titles need to unique.");
			 * }
			 * }
			 */
		}
	}

	private static DefaultProcedureMapping createCanooLoadTestProcedureMapping() {
		return createCanooLoadTestProcedureMapping(MessageConstants.MODULE_CANOO_LOAD_TESTS);
	}

	private static DefaultProcedureMapping createCanooLoadTestProcedureMapping(String title) {
		try {
			return ant().
					set(property(AntProcedure.TITLE).value(title)).
					set(property(AntProcedure.FILE).value(BaseConstants.SubDirectories.CANOO + "/runtest.xml")).
					set(property(AntProcedure.TARGET).value("run")).
					set(property(AntProcedure.RECURRENCE).value("0")).
					set(property(AntProcedure.RECURRENCE_INTERVAL_SEC).value("15")).
					set(property(AntProcedure.INSTANCES).value("3")).
					set(property(AntProcedure.START_INTERVAL_SEC).value("10")).
					set(property(AntProcedure.FORK).value(Boolean.TRUE.toString())).
					set(antProperty("wt.config.resultpath").value(getTestReportPath(BaseConstants.SubDirectories.CANOO))).
					set(antProperty("wt.headless").value(Boolean.TRUE.toString())).
					set(antProperty("wt.groovyTests.skip").value(Boolean.TRUE.toString())).
					set(antProperty("wt.junitLikeReports.skip").value(Boolean.TRUE.toString())).
					set(antProperty("reallyFly").value(Boolean.TRUE.toString())).
					set(antProperty("~wt.htmlReports.skip").value(Boolean.TRUE.toString())).
					set(antProperty("wt.config.saveresponse").value(Boolean.FALSE.toString())).

					// use canonical path, otherwise Launcher/WebLauncher may create different paths and thus cause Scenario
// re-creations
					set(antProperty("distdir").value(Directories.getInstallDir().getCanonicalPath())).

					create();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to get canonical path of " + Directories.getInstallDir(), e);
		}
	}

	private static DefaultProcedureMapping createJUnitProcedureMapping() {
		return ant().
				set(property(AntProcedure.TITLE).value(MessageConstants.MODULE_JUNIT_TESTS)).
				set(property(AntProcedure.FILE).value(BaseConstants.SubDirectories.TEST + "/runtest.xml")).
				set(property(AntProcedure.TARGET).value(
						EasyTravelConfig.read().skipSessionRecording ? "runAllWithoutSession" : "runAllWithSession")).
				set(property(AntProcedure.RECURRENCE).value("1")).
				set(property(AntProcedure.INSTRUMENTATION).value(AntProcedure.SETTING_VALUE_ON)).
				set(property(AntProcedure.FORK).value(Boolean.TRUE.toString())).
				set(antProperty(AntProcedure.TEST_REPORT_DIR).value(getTestReportPath(BaseConstants.SubDirectories.JUNIT))).
				set(antProperty(AntProcedure.PROPERTY_BUILD_NUMBER_FILE).value(getTestReportPath(BaseConstants.SubDirectories.JUNIT) + "/build.number")).
				create();
	}

	private static DefaultProcedureMapping createWebapiProcedureMapping() {
		return ant().
				set(property(AntProcedure.TITLE).value(MessageConstants.MODULE_WEBAPI_TESTS)).
				set(property(AntProcedure.FILE).value(BaseConstants.SubDirectories.WEBAPI + "/runtest.xml")).
				set(property(AntProcedure.TARGET).value(
						EasyTravelConfig.read().skipSessionRecording ? "runWebApiTestsWithoutSession" : "runWebApiTestsWithSession")).
				set(property(AntProcedure.START_INTERVAL_SEC).value("180")).
				set(property(AntProcedure.RECURRENCE).value("1")).
				set(property(AntProcedure.RECURRENCE_INTERVAL_SEC).value("15")).
				set(property(AntProcedure.INSTRUMENTATION).value(AntProcedure.SETTING_VALUE_ON)).
				set(property(AntProcedure.FORK).value(Boolean.TRUE.toString())).
				set(antProperty(AntProcedure.TEST_REPORT_DIR).value(getTestReportPath(BaseConstants.SubDirectories.WEBAPI))).
				set(antProperty(AntProcedure.PROPERTY_BUILD_NUMBER_FILE).value(getTestReportPath(BaseConstants.SubDirectories.WEBAPI) + "/build.number")).
				create();
	}

	private static DefaultProcedureMapping createLoadGeneratorJUnitProcedureMapping() {
		String dateString = DateFormatUtils.ISO_DATE_FORMAT.format(new Date() /* today */);
		return ant().
				set(property(AntProcedure.TITLE).value("Load Testing Helper")).
				set(property(AntProcedure.TARGET).value("LoadTest")).
				set(property(AntProcedure.FILE).value(BaseConstants.SubDirectories.TEST + "/runtest.xml")).
				set(property(AntProcedure.RECURRENCE).value("1")).
				set(property(AntProcedure.DTTESTRUNNAME).value("Testrun on " + dateString)).
				set(property(AntProcedure.INSTRUMENTATION).value(AntProcedure.SETTING_VALUE_ON)).
				set(property(AntProcedure.FORK).value(Boolean.TRUE.toString())).
				set(antProperty(AntProcedure.TEST_REPORT_DIR).value(getTestReportPath(BaseConstants.SubDirectories.JUNIT))).
				create();
	}

	private static DefaultProcedureMapping createSeleniumProcedureMapping(final String title, final String target,
			final String browser, final boolean enabled) {
		return ant().
				set(property(AntProcedure.TITLE).value(title + "-" + browser)).
				set(property(AntProcedure.FILE).value(BaseConstants.SubDirectories.SELENIUM +
						"/runtest.xml")).
				set(property(AntProcedure.TARGET).value(target)).
				set(property(AntProcedure.RECURRENCE).value("1")).
				// set(property(AntProcedure.INSTRUMENTATION).value(AntProcedure.SETTING_VALUE_ON)).
				set(property(AntProcedure.FORK).value(Boolean.TRUE.toString())).
				set(antProperty(AntProcedure.TEST_REPORT_DIR).value(getTestReportPath(BaseConstants.SubDirectories.SELENIUM))).
				set(antProperty(AntProcedure.WEBDRIVER_BROWSER).value(browser)).
				set(property(Constants.Misc.SETTING_ENABLED).value(enabled
						? Constants.Misc.SETTING_VALUE_ON : Constants.Misc.SETTING_VALUE_OFF)).
			    set(EasyTravelConfig.read().skipSessionRecording ?
			    		antProperty(AntProcedure.PROPERTY_SESSION_RECORDING_SKIP).value("true") : // if config option is picked set the property
						antProperty("DUMMY").value("true")). // otherwise set dummy property, the PROPERTY_SESSION_RECORDING_SKIP property must not be set at all
				create();
	}


	private static String getTestReportPath(String subDir) {
		return new File(Directories.getExistingTestsDir(), subDir).getAbsolutePath();
	}

	/**
	 * Load scenario configuration from file. If the file does not exist at this moment it will be
	 * created. If a problem occur the default settings are returned.
	 *
	 * @return the loaded scenario configuration or the default settings if an error occur.
	 * @author martin.wurzinger
	 */
	public ScenarioConfiguration loadOrCreate() {
		try {
			persistence.load(this);
		} catch (ConfigurationException e) {
			LOGGER.log(Level.WARNING, "Failed to load scenario configuration. Proceeding with default settings.");
			createDefaultScenarios();
			return null;
		}

		String loadedMd5 = getHash();
		if (loadedMd5 == null) {
			LOGGER.log(Level.WARNING,
					"Failed to read MD5 hash of loaded scenario configuration. Proceeding with default settings.");
			createPersistentDefault();
			return null;
		}

		String defaultMd5 = null;
		ScenarioConfiguration defaultConfig = null;
		try {
			defaultConfig = new ScenarioConfiguration(persistence);
			defaultConfig.createDefaultScenarios();
			defaultMd5 = defaultConfig.calcHash();
			initialHash = defaultMd5;
		} catch (ConfigurationException e) {
			LOGGER.log(Level.WARNING,
					"Failed to calculate MD5 of default scenario configuration. Proceeding with new default scenario.");
			createDefaultScenarios();
			return null;
		}

		if (!loadedMd5.equalsIgnoreCase(defaultMd5)) {
			LOGGER.log(Level.WARNING, "The default scenario configuration has changed. A new configuration file was created.");
			createPersistentDefault();
		}

		if (manualStart) {
			for (ScenarioGroup group : scenarioGroups) {
				for (Scenario scenario : group.getScenarios()) {
					for (ProcedureMapping mapping : scenario.getProcedureMappings(InstallationType.Both)) {
						((DefaultProcedureMapping) mapping).removeSetting(new DefaultProcedureSetting(
								Constants.Misc.SETTING_ENABLED, Constants.Misc.SETTING_VALUE_ON));
						((DefaultProcedureMapping) mapping).addSetting(new DefaultProcedureSetting(
								Constants.Misc.SETTING_ENABLED, Constants.Misc.SETTING_VALUE_OFF));
					}
				}
			}
		}

		LOGGER.log(Level.FINE, "Scenario configuration loaded.");

		return defaultConfig;
	}

	private void reset() {
		scenarioGroups.clear();
	}

	private void createPersistentDefault() {
		reset();
		try {
			persistence.createAndLoadDefaultConfig(this);
		} catch (ConfigurationException e) {
			LOGGER.log(Level.WARNING,
					"Failed to create new scenario configuration file. Proceed with default settings without storing them.", e);
			createDefaultScenarios();
		}
	}

	public void save() throws ConfigurationException {
		persistence.save(this);
	}

	private String getHash() {
		return defaultHash;
	}

	public void setHash(String hash) {
		this.defaultHash = hash;
	}

	private String calcHash() throws ConfigurationException {
		return persistence.calcMd5(this);
	}

	public static boolean userScenarioFileExists() {
		File userScenarioFile = new File(Directories.getConfigDir(), Constants.Misc.USER_SCENARIOS_FILE);
		return userScenarioFile.exists();
	}

	@Override
	public void read(ConfigurationNode node, ConfigurationReader reader) throws ConfigurationException {
		// ensure that groups are cleaned up to avoid double
		this.scenarioGroups.clear();

		readTechnologyProperties(node, reader);
		ScenarioConfigurationAdditionalSettings.INSTANCE.readUIProperties(node, reader, UIProperties.PROBLEM_PATTERNS.getPropertyName());
		// read groups from default scenario
		addGroups(readGroups(node, reader));

		// add groups from an userScenario.xml file if available
		File userScenarioFile = new File(Directories.getConfigDir(), Constants.Misc.USER_SCENARIOS_FILE);
		if (userScenarioFile.exists()) {
			LOGGER.info("Reading additional scenarios from file " + userScenarioFile);

			XMLConfiguration config = null;
			try {
				config = new XMLConfiguration(userScenarioFile);
			} catch (org.apache.commons.configuration.ConfigurationException e) {
				throw new ConfigurationException("Unable to read user scenario configuration from " + userScenarioFile, e);
			}

			ConfigurationNode userConfigs = config.getRoot();
			List<ScenarioGroup> groups = readGroups(userConfigs, reader);
			int index = 0;
			for (ScenarioGroup group : groups) {
				ScenarioGroup existingGroup = getGroup(group.getTitle());
				if (existingGroup != null) {
					// merge Scenarios in both groups
					int scenIndex = 0;
					for (Scenario scenario : group.getScenarios()) {
						if (findScenario(scenario.getTitle(), existingGroup.getTitle()) != null) {
							throw new IllegalStateException("The scenario '" + existingGroup.getTitle() +
									"-" + scenario.getTitle() +
									"' was used twice, but scenario titles need to be unique in one group.");
						}

						existingGroup.addScenario(scenIndex, scenario);
						scenIndex++;
					}
				} else {
					this.scenarioGroups.add(index, group);
					index++;
				}
			}
		}

	}

	/**
	 *
	 * @param title
	 * @return
	 * @author dominik.stadler
	 */
	private ScenarioGroup getGroup(String title) {
		for (ScenarioGroup group : scenarioGroups) {
			if (group.getTitle().equals(title)) {
				return group;
			}
		}

		return null;
	}

	private List<ScenarioGroup> readGroups(ConfigurationNode node, ConfigurationReader reader) throws ConfigurationException {
		List<ScenarioGroup> result = new ArrayList<ScenarioGroup>();
		List<ConfigurationNode> groupNodes = reader.getChildren(node, ScenarioConfiguration.NODE_GROUP);

		for (ConfigurationNode groupNode : groupNodes) {
			DefaultScenarioGroup scenarioGroup = new DefaultScenarioGroup();
			scenarioGroup.read(groupNode, reader);

			result.add(scenarioGroup);
		}

		return result;
	}

	@Override
	public void write(ConfigurationNode node, NodeFactory factory) {

		writeTechnologyProperties(node, factory);
		ScenarioConfigurationAdditionalSettings.INSTANCE.writeUIProperties(node, factory);
		for (ScenarioGroup scenarioGroup : scenarioGroups) {
			ConfigurationNode groupNode = factory.createNode(ScenarioConfiguration.NODE_GROUP);

			scenarioGroup.write(groupNode, factory);

			node.addChild(groupNode);
		}
	}

	private void readTechnologyProperties(ConfigurationNode node, ConfigurationReader reader) {
		List<ConfigurationNode> nodes = reader.getChildren(node, Constants.ConfigurationXml.NODE_TECHNOLOGY_PROPERTIES);
		if (nodes.size() == 0) {
			LOGGER.warning(TextUtils.merge("No technology properties are set within ''{0}'', setting default values.",
					persistence.getExistingConfigFile().getAbsolutePath()));
			for (TechnologyActivator activator : CentralTechnologyActivator.getIntance().getActivators()) {
				activator.setInitiallyEnabled(true); // enable all technologies
			}
			return;
		}

		ConfigurationNode techNode = nodes.iterator().next();
		for (Object objNode : techNode.getChildren()) {
			ConfigurationNode tempNode = (ConfigurationNode) objNode;
			CentralTechnologyActivator.getIntance().setActivator(tempNode.getName(),
					Boolean.parseBoolean(tempNode.getValue().toString()));
		}
	}


	private void writeTechnologyProperties(ConfigurationNode node, NodeFactory factory) {
		Collection<TechnologyActivator> activators = CentralTechnologyActivator.getIntance().getActivators();
		if (activators.isEmpty()) {
			return;
		}
		ConfigurationNode techNode = factory.createNode(Constants.ConfigurationXml.NODE_TECHNOLOGY_PROPERTIES);
		node.addChild(techNode);
		for (TechnologyActivator activator : activators) {
			techNode.addChild(factory.createNode(activator.getName(), Boolean.valueOf(activator.isEnabled()).toString()));
		}
	}

	/**
	 * <p>
	 * Search a configured scenario with the matching title. If the configuration contains multiple scenarios with the specified
	 * title, the first one (ordering) will be returned.
	 * </p>
	 * <p>
	 * Titles are compared case-insensitive
	 * </p>
	 *
	 * @param scenarioTitle the title of the scenario to look for
	 * @return the matching scenario or <code>null</code> if no one could be found
	 * @author martin.wurzinger
	 */

	public Scenario findScenario(String scenarioTitle) {
		return findScenario(scenarioTitle, null);
	}

	/**
	 * <p>
	 * Search a configured scenario with the matching title and that is assigned to a group with the matching group title. If the
	 * group contains multiple scenarios with the specified title, the first one (ordering) will be returned.
	 * </p>
	 * <p>
	 * Titles are compared case-insensitive.
	 * </p>
	 *
	 * @param scenarioTitle the title of the scenario to look for
	 * @param scenarioGroupTitle the title of the scenario group the scenario has to be related to
	 * @return the matching scenario or <code>null</code> if no one could be found
	 * @author martin.wurzinger
	 */
	public Scenario findScenario(String scenarioTitle, String scenarioGroupTitle) {
		for (ScenarioGroup group : scenarioGroups) {
			if (scenarioGroupTitle == null || scenarioGroupTitle.equalsIgnoreCase(group.getTitle())) {

				for (Scenario scenario : group.getScenarios()) {
					if (scenarioTitle == null || scenarioTitle.equalsIgnoreCase(scenario.getTitle())) {
						return scenario;
					}
				}

			}
		}

		return null;
	}

	@Override
	public void notifyTechnologyStateChanged(Technology technology, boolean enabled, Collection<String> plugins,
			Collection<String> substitutes) {
		try {
			if (initialHash == null) {
				return;
			}
			// save configuration each time something changed
			persistence.saveTechnologyState(technology.getName(), enabled);
		} catch (ConfigurationException e) {
			LOGGER.log(Level.WARNING, "Saving of changed configuration was not possible", e);
		}
	}
}
