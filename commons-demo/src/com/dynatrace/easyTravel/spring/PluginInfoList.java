package com.dynatrace.easytravel.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

/**
 * This class serves as a wrapper for a list of plugin data that is transferred
 * via the web services.
 * It maintains a sorted list of plugins.
 *
 * @author philipp.grasboeck
 */
public class PluginInfoList implements Iterable<Plugin> {

    // name:groupName:installationMode:description
    private static final String SEPARATOR = ":";

    private final Map<String, Plugin> pluginMap = new HashMap<String, Plugin>();

    public PluginInfoList() {
    }

    public PluginInfoList(String[] pluginData) {
        if (pluginData != null) {
            addData(pluginData);
        }
    }

    public PluginInfoList(Collection<Plugin> plugins) {
        if (plugins != null) {
            addAll(plugins);
        }
    }

    public PluginInfoList(Plugin plugin) {
        if (plugin != null) {
            add(plugin);
        }
    }

    public void addAll(Collection<? extends Plugin> plugins) {
        for (Plugin plugin : plugins) {
            add(plugin);
        }
    }

    public void addData(String[] pluginData) {
        for (String data : pluginData) {
            AbstractPlugin pluginObject = getPluginObject(data);
            pluginObject.getPluginDependencies();

            add(pluginObject);
        }
    }

    public void updateData(String[] pluginData) {
        for (String data : pluginData) {
            update(getPluginObject(data));
        }
    }

    public void add(Plugin plugin) {
        // don't overwrite plugins with less information, e.g. group name
        if(StringUtils.isEmpty(plugin.getGroupName()) && pluginMap.containsKey(plugin.getName())) {
            return;
        }

        pluginMap.put(plugin.getName(), plugin);
    }

    public void update(Plugin plugin) {
        if (contains(plugin)) {
            add(plugin);
        }
    }

    public void remove(Plugin plugin) {
        pluginMap.remove(plugin.getName());
    }

    /**
     * Method for getting plugins according to installation mode Classic/APM
     *
     * When installation mode is Classic, all plugins with property <property name="compatibility" value="Classic" /> defined
     * in plugin *.ctx.xml file and all plugins with property <property name="compatibility" value="Both" /> are available.
     *
     * When installation mode is APM, all plugins with property <property name="compatibility" value="APM" /> defined
     * in plugin *.ctx.xml file and all plugins with property <property name="compatibility" value="Both" /> are available.
     *
     * @return Map<String, Plugin> filteredPlugins
     */
    private Map<String, Plugin> getFilteredPlugins() {
        Predicate<Plugin> matchingPlugins = new Predicate<Plugin>() {
            @Override
            public boolean apply(Plugin plugin) {
                return DtVersionDetector.getInstallationType().matches(InstallationType.fromString(plugin.getCompatibility()));
            }
            
            //@Override
            public boolean test(Plugin plugin) {
            	return apply(plugin);
            }
        };

        Map<String, Plugin> filteredPlugins = new TreeMap<String, Plugin> (
                Maps.filterValues(pluginMap, matchingPlugins)
        );

        return filteredPlugins;
    };

    public String[] getNames() {
        Map<String, Plugin> tmpMap = getFilteredPlugins();  // sorted by name
        String[] result = new String[tmpMap.size()];
        int i = 0;
        for (Plugin plugin : tmpMap.values()) {
            result[i++] = plugin.getName();
        }
        return result;
    }

    public String[] getNames(String host) {
        if (host == null) {
            return new String[0];
        }
        Map<String, Plugin> tmpMap = getFilteredPlugins();  // sorted by name
        Collection<String> resultList = new ArrayList<String>(tmpMap.size());
        for (Plugin plugin : tmpMap.values()) {
            if (plugin.isEnabledFor(host)) {
                resultList.add(plugin.getName());
            }
        }
        return resultList.toArray(new String[resultList.size()]);
    }

    public String[] getData() {
        Map<String, Plugin> tmpMap = getFilteredPlugins();  // sorted by name
        String[] result = new String[tmpMap.size()];
        int i = 0;
        for (Plugin plugin : tmpMap.values()) {
            result[i++] = getPluginData(plugin);
        }
        return result;
    }

    public String[] getData(String host) {
        if (host == null) {
            return new String[0];
        }
        Map<String, Plugin> tmpMap = getFilteredPlugins();  // sorted by name
        Collection<String> resultList = new ArrayList<String>(tmpMap.size());
        for (Plugin plugin : tmpMap.values()) {
            if (plugin.isEnabledFor(host)) {
                resultList.add(getPluginData(plugin));
            }
        }
        return resultList.toArray(new String[resultList.size()]);
    }

    @Override
    public String toString() {
        Map<String, Plugin> tmpMap = getFilteredPlugins();  // sorted by name
        return tmpMap.keySet().toString();
    }

    public boolean contains(Plugin plugin) {
        return pluginMap.containsKey(plugin.getName());
    }

    public boolean contains(String pluginName) {
        return pluginMap.containsKey(pluginName);
    }

    public Plugin get(String name) {
        return pluginMap.get(name);
    }

    public boolean isEmpty() {
        return pluginMap.isEmpty();
    }

    @Override
    public Iterator<Plugin> iterator() {
        Map<String, Plugin> tmpMap = getFilteredPlugins();  // sorted by name
        return tmpMap.values().iterator();
    }

	/* --- conversion methods --- */

    private static String getPluginData(Plugin plugin) {
        StringBuilder buf = new StringBuilder();
        if (plugin.getName() == null) {
            throw new IllegalArgumentException("name must not be null: " + plugin);
        }
        if (plugin.getName().contains(SEPARATOR)) {
            throw new IllegalArgumentException("A plugin name must not contain '" + SEPARATOR + "': " + plugin);
        }
        buf.append(plugin.getName());

        if (plugin.getGroupName() != null) {
            if (plugin.getGroupName().contains(SEPARATOR)) {
                throw new IllegalArgumentException("A plugin groupName must not contain '" + SEPARATOR + "': " + plugin);
            }
            buf.append(SEPARATOR);
            buf.append(plugin.getGroupName());
        } else {
        	buf.append(SEPARATOR);
        }

        // compatibility cannot be empty!
        buf.append(SEPARATOR);
        buf.append(plugin.getCompatibility());

        if (plugin.getDescription() != null) {
            buf.append(SEPARATOR);
            buf.append(plugin.getDescription());
        }
        return buf.toString();
    }

    private static AbstractPlugin getPluginObject(String data) {
    	String name = data;
        String groupName = null;
        String compatibility = null;
        String description = null;
        int i = data.indexOf(SEPARATOR);
        if (i != -1) {
            name = data.substring(0, i);
            groupName = data.substring(i + 1);
            int j = data.indexOf(SEPARATOR, i + 1);
            if (j != -1) {
                groupName = data.substring(i + 1, j);
                int k = data.indexOf(SEPARATOR, j+1);
                if( k != -1) {
                    compatibility = data.substring(j + 1, k);

                    description = data.substring(k + 1);
                } else {
                	compatibility = data.substring(j + 1);
                }
            }
        }
        return new AbstractPlugin(name, groupName, compatibility, description);
    }
}
