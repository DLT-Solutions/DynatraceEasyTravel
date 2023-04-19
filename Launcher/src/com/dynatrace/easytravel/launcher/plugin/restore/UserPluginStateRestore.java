package com.dynatrace.easytravel.launcher.plugin.restore;

import com.dynatrace.easytravel.launcher.plugin.PluginEnabledListener;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.plugin.RemotePluginController;
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
 * Main class for tracking user enabled plugins
 *
 * cwpl-rorzecho
 */
public class UserPluginStateRestore extends AbstractPluginStateRestore implements PluginEnabledListener {
    private static final Logger LOGGER = LoggerFactory.make();

    private final long INFO_TIMEOUT = TimeUnit.SECONDS.toNanos(30);
    private static AtomicLong lastInfo = new AtomicLong(0);

    private PluginInfoList enabledPlugins;

    public UserPluginStateRestore() {
        super();
    }

    public UserPluginStateRestore(RemotePluginController remotePluginController) {
        super(remotePluginController);
    }

    @Override
    protected Collection<String> find() {
        Collection<String> tmp = new ArrayList<String>(Arrays.asList(enabledPlugins.getNames()));
        tmp.removeAll(Arrays.asList(getPluginRestorePoint()));
        return tmp;
    }

    /**
     * Revert user enabled plugins
     */
    public static void revertToRestorePoint() {
        revert(PluginType.ENABLED_USER_PLUGINS);
    }

    public Collection<String> getUserPlugins() {
        return getPlugins(PluginType.ENABLED_USER_PLUGINS);
    }

    @Override
    public void notifyEnabledPlugins(PluginInfoList enabledPlugins) {
        this.enabledPlugins = enabledPlugins;
        if (DefaultRestorePoint.isLoaded()) {
            findAndStore(PluginType.ENABLED_USER_PLUGINS);
            printUserEnabledPlugins();
        }
    }

    /**
     * For testing only
     * @param enabledPlugins
     */
    public void setPluginInfoList(PluginInfoList enabledPlugins) {
        this.enabledPlugins = enabledPlugins;
    }

    private void printUserEnabledPlugins() {
        if (System.nanoTime() > lastInfo.get() + INFO_TIMEOUT) {
            Map<Long, String> userEnabledPluginsTimestamps = getPluginsTimestamp(PluginType.ENABLED_USER_PLUGINS);
            if (!userEnabledPluginsTimestamps.isEmpty()) {
                lastInfo.set(System.nanoTime());
                LOGGER.info(TextUtils.merge("User enabled plugins: {0}", userEnabledPluginsTimestamps.entrySet().toString()));
            }
        }
    }

}
