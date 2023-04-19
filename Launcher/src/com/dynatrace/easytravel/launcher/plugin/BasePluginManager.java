package com.dynatrace.easytravel.launcher.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.procedures.utils.CentralTechnologyActivator;
import com.dynatrace.easytravel.plugin.RemotePluginController;
import com.dynatrace.easytravel.spring.PluginInfoList;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * Class allows sending a request to enable or disable (to the Business Backend) of plugins within the launcher.
 * @author stefan.moschinski
 */
public class BasePluginManager extends Thread implements PluginManager {
	private static final Logger LOGGER = Logger.getLogger(BasePluginManager.class.getName());

	protected static final long DEFAULT_TIMEOUT = TimeUnit.MINUTES.toMillis(10);

	private static final String THREAD_NAME = "Base-Plugin-Manager-Thread";

	private final long timeOut;
	private static final int INTERVAL_MS = (int) TimeUnit.SECONDS.toMillis(1);

	private final RemotePluginController pluginController;
	private final Collection<String> pluginsToEnable = new ArrayList<String>();
	private final Collection<String> pluginsToDisable = new ArrayList<String>();
	private final AtomicBoolean running = new AtomicBoolean(false);

	// needed for testing
	private Throwable exception = null;
	private boolean finished = false;

	BasePluginManager(String threadName, RemotePluginController pluginController, long timeOut) {
		super(threadName);

		setDaemon(true);

		this.pluginController = pluginController;
		this.timeOut = timeOut;
	}

	public BasePluginManager() {
		this(THREAD_NAME, new RemotePluginController(), DEFAULT_TIMEOUT);
	}

	@Override
	public void addPluginsToEnable(Collection<String> pluginNames) {
		if (running.get()) {
			throw new IllegalStateException("Unable to enable plugins. Thread has been started already.");
		}
		pluginsToEnable.addAll(filter(pluginNames));
	}

	@Override
	public void addPluginsToDisable(Collection<String> pluginNames) {
		if (running.get()) {
			throw new IllegalStateException("Unable to disable plugins. Thread has been started already.");
		}
		pluginsToDisable.addAll(pluginNames);
	}

	protected Collection<? extends String> filter(Collection<String> pluginNames) {
		Collection<String> filtered = new ArrayList<String>(pluginNames.size());
		for (String plugin : pluginNames) {
			if (CentralTechnologyActivator.getIntance().isPluginAllowed(plugin)) {
				filtered.add(plugin);
			} else {
				LOGGER.warning("Cannot enable plugin " + plugin + ", because associated technology is not enabled");
			}
		}
		return filtered;
	}

	/*
	 * Be aware that we don't register any plugins in this method, that is, if the plugin is not registered on the business backend
	 * enabling or disabling the plugin will cause an IllegalStateException
	 */
	@Override
	public void run() {
		running.set(true);

		try {
			if (this.pluginsToEnable.isEmpty() && this.pluginsToDisable.isEmpty()) {
				return;
			}

			Collection<String> pluginsToEnable = new HashSet<String>(this.pluginsToEnable);
			Collection<String> pluginsToDisable = new HashSet<String>(this.pluginsToDisable);

			LOGGER.info("Trying to enable plugins: " + pluginsToEnable + " and disabled: " + pluginsToDisable + ", timeout: " + timeOut);

			boolean isDone = false;
			long deadline = System.currentTimeMillis() + timeOut;

			IOException ioe = null;

			while (!isDone && deadline > System.currentTimeMillis()) {
				String[] allPluginNames = null;

				try {
					allPluginNames = pluginController.requestAllPluginNames();
				} catch (IOException e) {
					// if no plugins have been registered yet then try again after interval
					LOGGER.log(Level.INFO, "Could not connect to web service at " + LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE) + ": " + e.getClass().getName() + ": " + e.getMessage());
					LOGGER.log(Level.FINE, "Exception", e);
					ioe = e;
					Thread.sleep(INTERVAL_MS);
					continue;
				}

				// if this point of code is reached, the IO exception cannot be the failure for the enabling/disabling of the plugins
				// so we reset it
				ioe = null;

				PluginInfoList registeredPlugins = new PluginInfoList(allPluginNames);

				// FIRST enable default plugins, SECOND disable some plugins that are not desired
				pluginsToEnable = enablePlugins(registeredPlugins, pluginsToEnable);
				pluginsToDisable = disablePlugins(registeredPlugins, pluginsToDisable);

				if (LOGGER.isLoggable(Level.FINE)) LOGGER.log(Level.FINE, "modified pluginsToEnable: " + pluginsToEnable);
				if (LOGGER.isLoggable(Level.FINE)) LOGGER.log(Level.FINE, "modified pluginsToDisable: " + pluginsToDisable);

				isDone = pluginsToDisable.isEmpty() && pluginsToEnable.isEmpty();

				if (!isDone) {
					Thread.sleep(INTERVAL_MS);
				}
			}

			if (!isDone) {
				logExceptionPluginStateChangeFailed(pluginsToEnable, pluginsToDisable, ioe);
			}
		} catch (InterruptedException e) {
			exception = e;
			LOGGER.log(Level.SEVERE, "Plugins could not be enabled/disabled.", e);
		} finally {
			finished = true;
		}
	}

	private void logExceptionPluginStateChangeFailed(Collection<String> pluginsToEnable, Collection<String> pluginsToDisable, IOException ioe) {
		String combErrMsg = "";
		if (!pluginsToEnable.isEmpty()) {
			String errMsg = "Could not enable: " + pluginsToEnable + " on " + LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE) + ", Exception: " + ioe;
			LOGGER.log(Level.SEVERE, errMsg);
			combErrMsg += errMsg + "\n";
		}
		if (!pluginsToDisable.isEmpty()) {
			String errMsg = "Could not disable: " + pluginsToDisable + " on " + LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE) + ", Exception: " + ioe;
			LOGGER.log(Level.SEVERE, errMsg);
			combErrMsg += errMsg;
		}

		IllegalStateException ise = new IllegalStateException(combErrMsg, ioe);

		// save exception for verification
		exception = ise;
	}

	/**
	 * Tries to enable the passed plugins by their names
	 * @param registeredPlugins
	 * @param pluginsToEnable plugins that should be disabled
	 * @return empty collection if all passed plugins where successfully enabled or a collection containing plugins that could not be enabled
	 * @author stefan.moschinski
	 */
	protected Collection<String> enablePlugins(PluginInfoList registeredPlugins, Collection<String> pluginsToEnable) {
		return setEnablementAndRemove(registeredPlugins, pluginsToEnable, true);
	}

	/**
	 * Tries to disable the passed plugins by their names
	 * @param registeredPlugins
	 * @param pluginsToDisable plugins that should be disabled
	 * @return empty collection if all passed plugins where successfully disabled or a collection containing plugins that could not be disabled
	 * @author stefan.moschinski
	 */
	protected Collection<String> disablePlugins(PluginInfoList registeredPlugins, Collection<String> pluginsToDisable) {
		return setEnablementAndRemove(registeredPlugins, pluginsToDisable, false);
	}

	private Collection<String> setEnablementAndRemove(PluginInfoList registeredPlugins, Collection<String> pluginsToManage, boolean enable) {
		// use iterator to be able to remove items below
		Iterator<String> iterator = pluginsToManage.iterator();
		while (iterator.hasNext()) {
			String pluginToManage = iterator.next();

			if (LOGGER.isLoggable(Level.FINE)) LOGGER.log(Level.FINE, "pluginToManage: " + pluginToManage + ", enable: " + enable + ", registeredPlugins.contains(pluginToManage): " + registeredPlugins.contains(pluginToManage));

			// check if plugin already registered and remove it
			if (registeredPlugins.contains(pluginToManage)) {
				iterator.remove();
				pluginController.sendPluginStateChanged(pluginToManage, enable);
			} else if (enable) {
				// for enablement, try to register the plugin if not available yet
				try {
					pluginController.registerPlugin(pluginToManage);

					iterator.remove();
					pluginController.sendPluginStateChanged(pluginToManage, enable);
		        } catch (IOException e) {
		            LOGGER.log(Level.WARNING, TextUtils.merge("Could not register plugin {0}", pluginToManage), e);
				}
			}
		}
		return pluginsToManage;
	}

	protected RemotePluginController getPluginController() {
		return pluginController;
	}

	@TestOnly
	Throwable getException() {
		return exception;
	}

	@TestOnly
	boolean isFinished() {
		return finished;
	}


}
