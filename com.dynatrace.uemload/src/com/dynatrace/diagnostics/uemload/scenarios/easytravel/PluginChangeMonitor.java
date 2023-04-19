package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.plugin.RemotePluginController;
import com.dynatrace.easytravel.spring.PluginInfoList;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;


public abstract class PluginChangeMonitor {

	public static class Plugins {

		public static final String DC_RUM_EMULATOR = BaseConstants.Plugins.DC_RUM_EMULATOR;
		public static final String TABLET_CRASHES = BaseConstants.Plugins.TABLET_CRASHES;
		public static final String MOBILE_CRASHES_PEAK = BaseConstants.Plugins.MOBILE_CRASHES_PEAK;
		public static final String MOBILE_ERRORS = BaseConstants.Plugins.MOBILE_ERRORS;
		public static final String SLOW_TRANSACTION_FOR_PHP_BLOG = BaseConstants.Plugins.SLOW_TRANSACTION_FOR_PHP_BLOG;
		public static final String PHP_ENABLEMENT_PLUGIN = BaseConstants.Plugins.PHP_ENABLEMENT_PLUGIN;
		public static final String ADS_ENABLEMENT_PLUGIN = BaseConstants.Plugins.ADS_ENABLEMENT_PLUGIN;
		public static final String SLOW_APACHE_WEBSERVER = BaseConstants.Plugins.SLOW_APACHE_WEBSERVER;
		public static final String COUCHDB = BaseConstants.Plugins.COUCHDB;

		public static final String STREAMING_MEDIA_TRAFFIC = "StreamingMediaTraffic";
		public static final String JAVASCRIPT_CHANGE_DETECTION_WITH_SLOW_BROWSER = "JavascriptChangeDetectionWithSlowBrowser";
		public static final String CRASH_COUCH_DB = "CrashCouchDB";
		public static final String JAVASCRIPT_FRAMEWORK_DETECTION = "JavascriptFrameworkDetection";
		public static final String JAVASCRIPT_FRAMEWORK_DETECTION_UPDATE = "JavascriptFrameworkDetectionUpdate";
		public static final String JAVASCRIPT_INCREASED_ERROR_COUNT = "JavascriptIncreasedErrorCount";
		public static final String JAVASCRIPT_CHANGE_DETECTION_WITH_ERROR = "JavascriptChangeDetectionWithError";
		public static final String FAILED_IMAGES_DETECTION = "FailedImagesDetection";
		public static final String FAILED_XHRs = "FailedXHRs";
		public static final String NETWORK_PACKET_DROP = "NetworkPacketDrop";
		public static final String SCALE_MICRO_JOURNEY_SERVICE = "ScaleMicroJourneyService";
		public static final String MAGENTO_SHOP = "MagentoShop";
		public static final String JAVASCRIPT_APP_VERSION_SPECIFICERROR = "JavascriptAppVersionSpecificError";
		public static final String JAVASCRIPT_USER_ACTIONERROR = "JavascriptUserActionError";
		public static final String JAVASCRIPT_ERROR_ONLABEL_CLICK = "JavascriptErrorOnLabelClick";
		public static final String NODEJS_WEATHER_APPLICATION = "NodeJSWeatherApplication";
		public static final String LOAD_CHANGE_PLUGIN = "LoadChange";
		public static final String WORLD_MAP_DNS_FAILS_ASIA = "WorldMapDNSFailsAsia";
		public static final String WORLD_MAP_DNS_FAILS_EUROPE = "WorldMapDNSFailsEurope";
		public static final String WORLD_MAP_DNS_FAILS_UNITEDSTATES = "WorldMapDNSFailsUnitedStates";
		public static final String THIRD_PARTY_CONTENT = "ThirdPartyContent";
		public static final String USABILITY_ISSUE = "UsabilityIssue";
		public static final String ANGULAR_BOOKING_ERROR_500 = "AngularBookingError500";
		public static final String ANGULAR_BIZ_EVENTS_PLUGIN = "BizEventsPlugin";
		
		private Plugins() {
		// dummy constructor to hide the implicit public one, as per SONNAR
		// recommendation
	}

	}

	private PluginChangeMonitor() {
		// dummy constructor to hide the implicit public one, as per SONNAR
		// recommendation
	}

	private static final Logger LOGGER = Logger.getLogger(PluginChangeMonitor.class.getName());

	public static final int PLUGIN_STATE_CHECKER_DELAY = 10;
	private static ScheduledExecutorService PluginStateChecker;

	//hold list of currently enabled plugin names
	static AtomicReference<String[]> enabledPluginNames = new AtomicReference<String[]>();

	private static Set<PluginChangeListener> scenarios = new HashSet<PluginChangeListener>();

	/*
	 * Note: these objects are used here is preference to the Plugin class object,
	 * as there is no need to maintain such heavy objects.
	 * The only thing we care is if the plugin is enable or disabled
	 */

private static Map<String, Boolean> pluginListToMonitor = new ConcurrentHashMap<String, Boolean>();

	static {
		// TBD: This list should be built dynamically by the registering listeners.
		pluginListToMonitor.put(Plugins.PHP_ENABLEMENT_PLUGIN, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.DC_RUM_EMULATOR, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.TABLET_CRASHES, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.MOBILE_CRASHES_PEAK, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.MOBILE_ERRORS, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.SLOW_TRANSACTION_FOR_PHP_BLOG, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.ADS_ENABLEMENT_PLUGIN, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.STREAMING_MEDIA_TRAFFIC, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.WORLD_MAP_DNS_FAILS_ASIA, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.WORLD_MAP_DNS_FAILS_EUROPE, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.WORLD_MAP_DNS_FAILS_UNITEDSTATES, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.SLOW_APACHE_WEBSERVER, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.JAVASCRIPT_APP_VERSION_SPECIFICERROR, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.JAVASCRIPT_INCREASED_ERROR_COUNT, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.JAVASCRIPT_USER_ACTIONERROR, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.NODEJS_WEATHER_APPLICATION, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.JAVASCRIPT_CHANGE_DETECTION_WITH_ERROR, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.JAVASCRIPT_CHANGE_DETECTION_WITH_SLOW_BROWSER, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.JAVASCRIPT_ERROR_ONLABEL_CLICK, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.FAILED_IMAGES_DETECTION, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.FAILED_XHRs, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.NETWORK_PACKET_DROP, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.CRASH_COUCH_DB, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.JAVASCRIPT_FRAMEWORK_DETECTION, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.JAVASCRIPT_FRAMEWORK_DETECTION_UPDATE, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.MAGENTO_SHOP, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.SCALE_MICRO_JOURNEY_SERVICE, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.LOAD_CHANGE_PLUGIN, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.THIRD_PARTY_CONTENT, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.USABILITY_ISSUE, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.ANGULAR_BOOKING_ERROR_500, Boolean.FALSE);
		pluginListToMonitor.put(Plugins.ANGULAR_BIZ_EVENTS_PLUGIN, Boolean.FALSE);

		PluginStateChecker = Executors.newScheduledThreadPool(1,
				new ThreadFactoryBuilder()
				.setDaemon(true)
						.setNameFormat(BaseConstants.UEM_LOAD_HOST_PLUGIN_ENABLEMENT_WATCHER_THREAD + "-%d")
				.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(Thread t, Throwable e) {
						LOGGER.log(Level.SEVERE, TextUtils.merge("An uncaught exception happened in ''{0}''", t.getName()), e);

					}
				})
				.build());
		PluginStateChecker.scheduleAtFixedRate(new PluginEnablementWatcher(), PLUGIN_STATE_CHECKER_DELAY, 5, TimeUnit.SECONDS);
	}

	@TestOnly
	public static void setupPlugin(String pluginName, Boolean state) {
		if (pluginName != null) {
			pluginListToMonitor.put(pluginName, state);
		}
	}

	public static boolean isPluginEnabled(String pluginName) {
		if (pluginName == null) {
			return false;
		}

		Boolean myPluginState = pluginListToMonitor.get(pluginName);
		if (myPluginState != null) {
			return myPluginState.booleanValue();
		}

		// The list search returned null:
		// This can happen, if the plugin is not in the list.
		return false;
	}

	private static class PluginEnablementWatcher implements Runnable {

		@Override
		public void run() {
			try {
				RemotePluginController controller = new RemotePluginController();
				String[] names = controller.requestEnabledPluginNames();
				PluginInfoList enabledPlugins = new PluginInfoList(names);
				enabledPluginNames.set(names);

				boolean monitoredPluginsChanged = false;

				// Note re. synchronization of the concurrent map:
				// It is synchronized by definition - for each individual operation.
				// It does not provide for locking an element during a two-stage
				// read/write update.
				// However, we do not need this latter kind of synchronization.
				// Updates are done only in one thread and the only thing that other
				// threads do is read the table.

				Set<String> keys = pluginListToMonitor.keySet();
				for (String key : keys) {
					Boolean myPluginState = pluginListToMonitor.get(key);
					if (myPluginState != null) {
						if (myPluginState) {
							if (!enabledPlugins.contains(key)) {
								// it has been turned off now
								pluginListToMonitor.put(key, Boolean.FALSE);
								monitoredPluginsChanged = true;
							}
						} else { // plugin was not active
							if (enabledPlugins.contains(key)) {
								// it has been turned on now
								pluginListToMonitor.put(key, Boolean.TRUE);
								monitoredPluginsChanged = true;
							}
						}
					} else {
						// We are scanning over the key set and each entry, should specify a plugin we want to monitor.
						// It should not happen that an entry contains null, as we never put nulls in there when we build it.
						// If there is a null, the entry should simply be ignored, as it is meaningless.
						// Perhaps we should report a warning.
						LOGGER.warning("Null entry found in plugin list to monitor.");
					}
				}

				if (monitoredPluginsChanged) {

					LOGGER.info("Updating scenarios after plugin settings have changed.");
					for(PluginChangeListener scenario : scenarios) {

						// TBD: Only notify those listeners, which had interest in particular
						// plugins.
						notifyListener(scenario);
					}
				}

			} catch (Exception e) {
				// ignore exceptions
				LOGGER.log(Level.FINE, "Exception while polling for enabled plugins, maybe Business Backend is not yet started.", e);
			}
		}

		private void notifyListener (PluginChangeListener scenario) {
			try {
				scenario.pluginsChanged();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, TextUtils.merge("Exception while notyfing listener {0} about plugins change", scenario.getClass().getName() ), e);
			}
		}

	}


	public static void registerForPluginChanges(PluginChangeListener scenario) {
		scenarios.add(scenario);
	}

	public static void unregisterFromPluginChanges(PluginChangeListener scenario) {
		scenarios.remove(scenario);
	}

	public static void shutdown() {
		PluginStateChecker.shutdown();
	}
}
