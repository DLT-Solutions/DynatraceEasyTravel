package com.dynatrace.easytravel.spring;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DtVersionDetector;

/**
 * An independend thread which is doing the actual retrieving of the
 * enabled plugin information via the PluginStateProxy, i.e. the remote
 * call in the
 *
 * @author cwat-dstadler
 */
public class PluginRefreshThread extends Thread {
	private static final Logger log = LoggerFactory.make();

	// refresh interval in ms (10 seconds)
	private static int REFRESH_INTERVAL = 10000;

	// sleep REFRESH_INTERVAL/REFRESH_SLEEP_FACTOR, i.e. 500ms before waking up for shutdown
	private static final int REFRESH_SLEEP_FACTOR = 20;

	private volatile boolean shouldStop = false;

	private volatile String[] enabledPlugins = null;

	/**
	 * Used for testing only!
	 */
	public static final void setRefreshInterval(int intervall) {
		REFRESH_INTERVAL = intervall;
	}

	public PluginRefreshThread() {
		super("Plugin Refresh Thread");

		// ensure that the list is populated correctly upon initial startup
		retrieveEnabledPlugins();

		// make this thread a daemon to not keep the process in memory because of it
		setDaemon(true);
	}

	/**
	 * Indicate that the thread should stop. The method will block for 1s and wait
	 * for the thread to stop. If shutdown of the thread takes longer, it will continue
	 * and leave the thread in an undecided state.
	 */
	public void shouldStop() {
		shouldStop = true;

		// wait some time for the thread to stop, as we sleep 500ms below, we are joining twice as long to ensure in most cases that the thread stopped now
		try {
			join(REFRESH_INTERVAL/REFRESH_SLEEP_FACTOR*2);
		} catch (InterruptedException e) {
			log.warn("Joining refresh thread was interrupted", e);
		}
	}

	/**
	 * Internal thread method which loops unless shouldStop() is called and retrieves the
	 * list of enabled plugins every ten seconds.
	 */
	@Override
	public void run() {
		int i = 0;
		while(!shouldStop) {
			try {
				// sleep shorter than the 10 seconds refresh interval so we can stop the thread quicker than that at the end
				Thread.sleep(REFRESH_INTERVAL/REFRESH_SLEEP_FACTOR);

				// stop early without doing another request
				if(shouldStop) {
					break;
				}

				// don't lock as we replace the list with a new one, thus any other thread using the old list will continue to use it
				// without threading issues
				if(i >= REFRESH_SLEEP_FACTOR) {
					retrieveEnabledPlugins();

					// ensure that plugins are registered repeatedly, but ignore if we run without Spring being available, e.g. WebLauncher
					if(SpringUtils.hasAppContext()) {
						// get the list of Plugins that are known locally
						List<Plugin> plugins = SpringUtils.getPluginHolder().getPlugins();
						Map<String, Plugin> pluginMap = new HashMap<String, Plugin>();
						for(Plugin plugin : plugins) {
							pluginMap.put(plugin.getName(), plugin);
						}

						// get a list of all plugins that are known by the current PluginService
						String[] registeredPluginName = SpringUtils.getPluginStateProxy().getAllPluginNames();
						if(registeredPluginName != null) {
							// remove all known plugins from the list of plugins that this procedure knows about
							for(String plugin : registeredPluginName) {
								pluginMap.remove(plugin);
							}
						}

						// remove any plugins that are not matching current APM/Classic-mode to not
						// register them periodically as we do not receive them via getAllPluginNames()
						Iterator<Plugin> it = pluginMap.values().iterator();
						while(it.hasNext()) {
							Plugin plugin = it.next();
							if(!DtVersionDetector.getInstallationType().matches(
									InstallationType.fromString(plugin.getCompatibility()))) {
								it.remove();
							}
						}

						// if any are left, register them
						if(!pluginMap.isEmpty()) {
							log.warn("Some plugins are not registered any more, trying to register " + pluginMap.keySet());
							SpringUtils.getPluginStateProxy().registerPlugins(new PluginInfoList(pluginMap.values()).getData());
						}
					}

					i = 0;
				}

				i++;
			} catch (Exception e) {
				log.warn("Had Exception in plugin refresh thread", e);
			}
		}
	}

	/**
	 * Retrieve the list of enabled plugins. The method returns immediately
	 * without waiting for the list to be refreshed from an external process.
	 *
	 * The list of enabled plugins will be made current by the separate thread aprox. every 10s.
	 *
	 * @return
	 */
	public String[] getEnabledPlugins() {
		// called from multiple threads, we expect the array to be always replaced, newer modified
		return enabledPlugins;
	}

	private void retrieveEnabledPlugins() {
		EasyTravelConfig config = EasyTravelConfig.read();
    	String pluginServiceHost = config.pluginServiceHost;

    	final PluginStateProxy pluginStateProxy;
    	if (StringUtils.isNotBlank(pluginServiceHost)) {
    		pluginStateProxy = new RemotePluginService(pluginServiceHost, config.pluginServicePort);
    	} else {
    		pluginStateProxy = SpringUtils.getPluginStateProxy();
    	}

		if(log.isDebugEnabled()) {
			log.debug("Retrieving currently enabled plugins via proxy: " + pluginStateProxy.getClass().getSimpleName() + ": " + Arrays.toString(enabledPlugins));
		}

		// Note: this property is usually set on slave launchers via RESTProcedureClient
		String officialHost = config.officialHost;
		if (officialHost == null) {
			enabledPlugins = pluginStateProxy.getEnabledPluginNames();
		} else {
			enabledPlugins = pluginStateProxy.getEnabledPluginNamesForHost(officialHost);
		}
	}
}
