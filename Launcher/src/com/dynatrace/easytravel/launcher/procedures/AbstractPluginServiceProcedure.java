package com.dynatrace.easytravel.launcher.procedures;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.engine.AbstractJavaProcedure;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.plugin.CyclicPluginManager;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureSetting;
import com.dynatrace.easytravel.plugin.RemotePluginController;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginList;
import com.dynatrace.easytravel.spring.RemotePluginService;

/**
 * Provides helper methods that are used in both {@link BusinessBackendProcedure} and {@link PluginServiceProcedure}.
 *
 * @author cwat-dstadler
 */
public abstract class AbstractPluginServiceProcedure extends AbstractJavaProcedure {
	private static final Logger logger = Logger.getLogger(AbstractPluginServiceProcedure.class.getName());

	private CyclicPluginManager pluginSetupManager;

	public AbstractPluginServiceProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
		super(mapping);

		// add commandline argument for the type of installation (APM NG or Classic) we are currently running
        addInstalltionModeSetting(mapping);

        // tell application to use the current settings as input property file (e.g. in commandline launcher or in distributed setups)
        process.setPropertyFile();
	}

    @Override
    public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
        super.notifyProcedureStateChanged(subject, oldState, newState);

		if (logger.isLoggable(Level.FINE)) logger.log(Level.FINE, "handleStateForPluginService: " + oldState + " -> " + newState);

        switch (newState) {
			case OPERATING:
			case TIMEOUT:
				// tell plugin system about all host-settings for plugins, i.e. plugins which should only run on specific hosts
		        setPluginHosts();

				// start Plugin Manager which will handle any cyclic plugin, i.e. plugins which are
				// enabled/disabled at certain times
				// this also handles the normal plugin enable/disable settings found in the scenario for this procedure
				pluginSetupManager = new CyclicPluginManager(getMapping());
				pluginSetupManager.addPluginsToEnable(getTurnedOnPlugins(getMapping()));
				pluginSetupManager.addPluginsToDisable(getTurnedOffPlugins(getMapping()));
				pluginSetupManager.start();
				break;
			case STOPPING:
		    	if (pluginSetupManager != null) {
		    		pluginSetupManager.setKeeprunning(false);
		    	}

		    	// stop things that will try to access the PluginService that is now likely gone,
		    	// but are not needed in the context of the Launcher anyway (periodic execution of ProblemPatterns)
		    	GenericPluginList.cancelTimer();
		    	PluginList.stopRefreshThread();

				break;
			default:
				break;
		}
    }

	private void setPluginHosts() {
		Collection<ProcedureSetting> settings = getMapping().getSettings(Constants.Misc.SETTING_TYPE_PLUGIN_HOSTS);
		if (!settings.isEmpty()) {
			// collect all host-settings for plugins
			HashMap<String,String[]> hostsByPluginName = new HashMap<String,String[]>();
			for (ProcedureSetting setting : settings) {
				String name = setting.getName();
				String value = setting.getValue();
				if (name == null) {
					continue;
				}
				if (value == null) {
					continue;
				}
				logger.info("Setting host for plugin " + name + " to " + value);
				String[] hosts = StringUtils.split(value, ',');
				hostsByPluginName.put(name, hosts);
			}

			// store the host-settings for the plugin service
			EasyTravelConfig config = EasyTravelConfig.read();
			for (Entry<String, String[]> entry : hostsByPluginName.entrySet()) {
				String pluginName = entry.getKey();
				String[] hosts = entry.getValue();

				// need to send plugin actions to either pluginservice or businessbackend...
				if(StringUtils.isEmpty(config.pluginServiceHost)) {
					try {
						RemotePluginController controller = new RemotePluginController();
						controller.setPluginHosts(pluginName, hosts);
					} catch (IOException e) {
						logger.log(Level.WARNING, "Failed to set hosts " + Arrays.toString(hosts) + " for plugin " + pluginName, e);
					}
				} else {
					RemotePluginService remotePluginService = new RemotePluginService(config.pluginServiceHost, config.pluginServicePort);
					remotePluginService.setPluginHosts(pluginName, hosts);
				}
			}
		}
	}

    @Override
    public void transfer(ProcedureMapping mapping, State state) {
    	// stop old PluginSetupManager
    	if (pluginSetupManager != null) {
    		pluginSetupManager.setKeeprunning(false);
    	}

        pluginSetupManager = new CyclicPluginManager(mapping);

        // first disable all plugins that were enabled for this scenario
        pluginSetupManager.addPluginsToDisable(getTurnedOnPlugins(getMapping())); // use old mapping
        pluginSetupManager.addPluginsToDisable(getTurnedOffPlugins(getMapping())); // use old mapping

        final EasyTravelConfig CONFIG = EasyTravelConfig.read();

        // then handle plugins, that were specifically disabled for this procedure,
        // only enable them again if they are listed as "boot plugin"
        for (String plugin : getTurnedOffPlugins(getMapping())) { // use old mapping
        	if (ArrayUtils.contains(CONFIG.bootPlugins, plugin) && !getTurnedOffPlugins(mapping).contains(plugin)) {
        		// handle case where the same boot-plugin is disabled in the new mapping as well
        		pluginSetupManager.addPluginsToEnable(Collections.singleton(plugin));
        	}
        }

        // finally enable all plugins which are needed for the new procedure
        pluginSetupManager.addPluginsToEnable(getTurnedOnPlugins(mapping)); // use new mapping

        super.transfer(mapping, state);

        if (state == State.OPERATING || state == State.TIMEOUT) {
            pluginSetupManager.start();
        }
    }

	@Override
    protected boolean isTransferable(ProcedureSetting setting) {
    	// we can "transfer" all plugin-settings as we handle this ourselves here in transfer()
        if (Constants.Misc.SETTING_TYPE_PLUGIN.equalsIgnoreCase(setting.getType())) {
            return true;
        }

        return super.isTransferable(setting);
    }


    private Collection<String> getTurnedOnPlugins(ProcedureMapping mapping) {
        Collection<ProcedureSetting> settings = mapping.getSettings(Constants.Misc.SETTING_TYPE_PLUGIN);
        List<String> result = new ArrayList<String>(settings.size());

        for (ProcedureSetting setting : settings) {
            if (Constants.Misc.SETTING_VALUE_ON.equalsIgnoreCase(setting.getValue())) {
                result.add(setting.getName());
            }
        }

		if (logger.isLoggable(Level.FINE)) logger.log(Level.FINE, "getTurnedOnPlugins: " + result);

        return result;
    }

    private Collection<String> getTurnedOffPlugins(ProcedureMapping mapping) {
        Collection<ProcedureSetting> settings = mapping.getSettings(Constants.Misc.SETTING_TYPE_PLUGIN);
        List<String> result = new ArrayList<String>(settings.size());

        for (ProcedureSetting setting : settings) {
            if (Constants.Misc.SETTING_VALUE_OFF.equalsIgnoreCase(setting.getValue())) {
                result.add(setting.getName());
            }
        }

		if (logger.isLoggable(Level.FINE)) logger.log(Level.FINE, "getTurnedOffPlugins: " + result);

        return result;
    }
}
