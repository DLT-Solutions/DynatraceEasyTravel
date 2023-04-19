package com.dynatrace.easytravel.launcher.plugin.restore;

import com.dynatrace.easytravel.launcher.plugin.PluginEnabledListener;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.PluginInfoList;
import com.dynatrace.easytravel.util.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import ch.qos.logback.classic.Logger;

/**
 * Main class for tracking disabled boot plugins
 *
 * Boot plugins are all plugins defined in config.bootPlugins property
 * and plugins enabled in scenarios.xml file or userScenarios.xml file
 *
 * cwpl-rorzecho
 */
public class BootPluginStateRestore extends AbstractPluginStateRestore implements PluginEnabledListener {
    private static final Logger LOGGER = LoggerFactory.make();

    private final long INFO_TIMEOUT = TimeUnit.SECONDS.toNanos(30);
    private static AtomicLong lastInfo = new AtomicLong(0);

    private PluginInfoList enabledPlugins;

    public BootPluginStateRestore() {
        super();
    }

    @Override
    public Collection<String> find() {
        Collection<String> tmp = new ArrayList<String>(Arrays.asList(getPluginRestorePoint()));
        tmp.removeAll(Arrays.asList(enabledPlugins.getNames()));
        return tmp;
    }

    /**
     * Revert boot plugins changes
     */
    public static void revertToRestorePoint() {
        revert(PluginType.DISABLED_BOOT_PLUGINS);
    }

    public Collection<String> getDisabledBootPlugins() {
        return getPlugins(PluginType.DISABLED_BOOT_PLUGINS);
    }

    @Override
    public void notifyEnabledPlugins(PluginInfoList enabledPlugins) {
        this.enabledPlugins = enabledPlugins;
        if (DefaultRestorePoint.isLoaded()) {
            findAndStore(PluginType.DISABLED_BOOT_PLUGINS);
            printDisabledBootPlugins();
        }
    }

    private void printDisabledBootPlugins() {
        if (System.nanoTime() > lastInfo.get() + INFO_TIMEOUT) {
            Map<Long, String> disabledBootPlugins = getPluginsTimestamp(PluginType.DISABLED_BOOT_PLUGINS);
            if (!disabledBootPlugins.isEmpty()) {
                lastInfo.set(System.nanoTime());
                LOGGER.info(TextUtils.merge("Disabled boot plugins: {0}", disabledBootPlugins.entrySet().toString()));
            }
        }
    }

    /**
     * For testing only
     * @param enabledPlugins
     */
    public void setPluginInfoList(PluginInfoList enabledPlugins) {
        this.enabledPlugins = enabledPlugins;
    }

}
