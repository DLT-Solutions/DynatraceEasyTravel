package com.dynatrace.easytravel.launcher.plugin;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureSetting;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.plugin.RemotePluginController;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * Works like {@link BasePluginManager}, but furthermore provides the capability to enable/ disable plugins in a defined interval
 *
 * @author richard.uttenthaler
 */
public class CyclicPluginManager extends BasePluginManager {
	private static final Logger log = LoggerFactory.make();

	private static final int INTERVAL_MILLIS = (int) TimeUnit.SECONDS.toMillis(1);
	private static final String THREAD_NAME = "Cyclic-Plugin-Manager-Thread";

	private final Map<String, Boolean> pluginStateMap = new HashMap<String, Boolean>();
	//keeps timestamp (in seconds) when plugin's state should be switched 
	private final Map<String, Long> switchTimePluginStateMap = new HashMap<String, Long>();
	//keeps timestamp of last log message for plugin
	private final Map<String, Long> logTimePluginStateMap = new HashMap<String, Long>();
	private final ProcedureMapping mappings;
	private final AtomicBoolean keepRunning = new AtomicBoolean(true);

	// mainly for testing
	public CyclicPluginManager(String threadname, ProcedureMapping mappings, RemotePluginController pluginController) {
		super(threadname, pluginController, DEFAULT_TIMEOUT);

		// fail fast with NPE
		checkNotNull(mappings, "Need a valid ProcedureMapping to enable/disable plugins.");
		this.mappings = mappings;
	}

	public CyclicPluginManager(ProcedureMapping mappings) {
		this(THREAD_NAME, mappings, new RemotePluginController());
	}

	@Override
	public void run() {
		try {
			super.run();
		} catch (Exception e) {
			log.warn("Had exception while trying to enable/disable plugins, Service-URL: " + LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE), e);
		}

		try {
			enablePluginsCyclic();
		} catch (Exception e) {
			log.warn("Had exception during cyclic enable/disable plugins, Service-URL: " + LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE), e);
		}
	}

	private void enablePluginsCyclic() {
		try {
			initPluginOnOffMaps(mappings);

			// in this scenario no plugin uses the temporally schedule mechanism
			// -> we can "stop" the thread
			if (pluginStateMap.isEmpty()) {
				setKeeprunning(false);
			}

			while (keepRunning.get()) {
				Collection<ProcedureSetting> settings = mappings.getScheduledOnOffSettings();

				assert !settings.isEmpty() : "Expecting to have scheduled on/off settings for procedure: " + mappings.getId();

				for (ProcedureSetting setting : settings) {
					String key = setting.getName();

					boolean pluginActive = pluginStateMap.get(key);
					long switchTimestamp = switchTimePluginStateMap.get(key);
					long curTime = System.currentTimeMillis()/1000;

					// only log every half minute or ten seconds before switch happens...
					if (isTimeToLog(key)) {
						String state = pluginActive ? "disable" : "enable";

						int timeToSwith = (int)(switchTimestamp - curTime);
						
						int minutes = timeToSwith / 60;
						int seconds = timeToSwith % 60;
						if (minutes > 0) {
							log.info(TextUtils.merge("{0} minutes and {1} seconds to {2} plugin \"{3}\"", minutes, seconds,
									state, key));
						} else if (minutes == 0 && seconds > 0) {
							log.info(TextUtils.merge("{0} seconds to {1} plugin \"{2}\"", seconds, state, key));
						}
						//save log time
						logTimePluginStateMap.put(key, curTime);
					}

					if (curTime >= switchTimestamp) {
						// perform switch
						if (pluginActive) {
							getPluginController().sendEnabled(key, false, null);
							switchTimePluginStateMap.put(key, curTime+setting.getStayOffDuration());
							log.debug(TextUtils.merge("plugin \"{0}\" is disabled and will be enabled after {1} seconds", key, setting.getStayOffDuration()));
							pluginStateMap.put(key, false);
						} else {
							getPluginController().sendEnabled(key, true, null);
							switchTimePluginStateMap.put(key, curTime+setting.getStayOnDuration());
							log.debug(TextUtils.merge("plugin \"{0}\" is enabled and will be disabled after {1} seconds", key, setting.getStayOnDuration()));
							pluginStateMap.put(key, true);
						}
						//save log time, we may not log anything here, but it will look better in the logs
						logTimePluginStateMap.put(key, curTime);
					} 
				}
				Thread.sleep(INTERVAL_MILLIS);
			}
		} catch (InterruptedException e) {
			log.error("Plugins could not be enabled/disabled.", e);
		}
	}

	public void setKeeprunning(boolean keeprunning) {
		this.keepRunning.set(keeprunning);

		// make sure the loop is ended immediately
		this.interrupt();
	}

	protected void initPluginOnOffMaps(ProcedureMapping mappings) {
		Collection<ProcedureSetting> settings = mappings.getScheduledOnOffSettings();
		long curTime = System.currentTimeMillis()/1000;

		for (ProcedureSetting setting : settings) {
			// init maps with state of plugin			
			if (Constants.Misc.SETTING_VALUE_ON.equals(setting.getValue())) {
				switchTimePluginStateMap.put(setting.getName(), curTime+setting.getStayOnDuration());
				pluginStateMap.put(setting.getName(), true);
			} else {
				switchTimePluginStateMap.put(setting.getName(), curTime+setting.getStayOffDuration());
				pluginStateMap.put(setting.getName(), false);
			}
		}
	}
	
	/**
	 * only log every half minute or ten seconds before switch happens...
	 * @param key
	 * @return
	 */
	protected boolean isTimeToLog(String key){
		long switchTimestamp = switchTimePluginStateMap.get(key);
		long lastLogTimestamp = (logTimePluginStateMap.get(key) == null ? 0 : logTimePluginStateMap.get(key).longValue());
		long curTime = System.currentTimeMillis()/1000;
		
		//it's time to switch, do not log a message
		if (curTime >= switchTimestamp) {
			return false;
		} 
		//last message was logged less then 10 seconds ago, skip logging
		if (curTime < lastLogTimestamp + 10) { 
			return false;
		} 
		 //it's 30 seconds from last log, log a message
		if (curTime >= lastLogTimestamp + 30) {
			return true;
		}
		 //10 seconds to switch, log a message
		if (curTime >= switchTimestamp - 10) { 
			return true;
		}
		
		return false;
	}
}
