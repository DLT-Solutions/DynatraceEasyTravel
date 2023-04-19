package com.dynatrace.easytravel.launcher.plugin.restore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.dynatrace.easytravel.plugin.RemotePluginController;

/**
 * Base abstract class for restoring plugins state
 *  - all disabled boot plugins will be enabled
 *  - all enabled by user plugins which are not boot plugins will be disabled
 *
 * cwpl-rorzecho
 */
public abstract class AbstractPluginStateRestore {
    //private static final Logger LOGGER = LoggerFactory.make();

    private static RestoreDataContainer container;

    protected static RemotePluginController remotePluginController;

    public static enum PluginType {
        DISABLED_BOOT_PLUGINS,
        ENABLED_USER_PLUGINS,
    }

    public AbstractPluginStateRestore() {
        this(new RemotePluginController());
    }

    public AbstractPluginStateRestore(RemotePluginController remotePluginController) {
        AbstractPluginStateRestore.remotePluginController = remotePluginController;
        AbstractPluginStateRestore.container = new RestoreDataContainer();
    }

    abstract protected Collection<String> find();

    protected void findAndStore(Enum<?> type) {
        store(type, find());
    }

    /**
     * Store plugins into container
     *
     * @param pluginType
     * @param plugins
     */
    protected void store(Enum<?> pluginType, Collection<String> plugins) {
        if (container.size(pluginType) > plugins.size()) {
            removePlugins(pluginType, plugins);
        }

        if (plugins.size() != 0 ) {
            for (String plugin : plugins) {
                boolean contains = container.contains(pluginType, plugin);
                if (!contains) {
                    container.add(pluginType, plugin);
                }
            }
        } else if(!container.isEmpty(pluginType)) {
            container.clear(pluginType);
        }
    }

    private void removePlugins(Enum<?> pluginType, Collection<String> plugins) {
        Collection<String> tmp = new ArrayList<String>(container.getAll(pluginType));
        tmp.removeAll(plugins);
        container.removeAll(pluginType, tmp);
    }

    public static Collection<String> getPlugins(PluginType pluginType) {
        return container.getAll(pluginType);
    }

    protected Map<Long, String> getPluginsTimestamp(Enum<?> pluginType) {
        return container.getAsMap(pluginType);
    }

    private static void disablePlugins(PluginType pluginType) {
        Collection<String> plugins = new ArrayList<String>(getPlugins(pluginType));
        for (String plugin : plugins) {
            remotePluginController.sendPluginStateChanged(plugin, false);
            container.remove(pluginType, plugin);
        }
    }

    private static void enablePlugin(PluginType pluginType) {
        Collection<String> plugins = new ArrayList<String>(getPlugins(pluginType));
        for (String plugin : plugins) {
            remotePluginController.sendPluginStateChanged(plugin, true);
            container.remove(pluginType, plugin);
        }
    }

    protected static void revert(PluginType pluginType) {
        switch (pluginType) {
            case ENABLED_USER_PLUGINS:
                disablePlugins(pluginType);
                break;
            case DISABLED_BOOT_PLUGINS:
                enablePlugin(pluginType);
                break;
        }
    }

    public static void revertPlugins() {
        revert(PluginType.ENABLED_USER_PLUGINS);
        revert(PluginType.DISABLED_BOOT_PLUGINS);
    }

    public String[] getPluginRestorePoint() {
        return RestorePointHolder.getInstance().getPluginRestorePoint();
    }

    public String getScenarioRestorePoint() {
        return RestorePointHolder.getInstance().getScenarioRestorePoint();
    }

}
