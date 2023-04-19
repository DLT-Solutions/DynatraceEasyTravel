package com.dynatrace.easytravel.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.easytravel.util.TextUtils;


public class RemotePluginController {
    private static final Logger LOGGER = Logger.getLogger(RemotePluginController.class.getName());

    // urls for accessing the business backend
    private static final String ALL_PLUGINS = "getAllPlugins";
    private static final String ALL_PLUGIN_NAMES = "getAllPluginNames";
    private static final String ENABLED_PLUGINS = "getEnabledPlugins";
    private static final String ENABLED_PLUGIN_NAMES = "getEnabledPluginNames";
    private static final String SET_PLUGIN_ENABLED = "setPluginEnabled?name={0}&enabled={1}";
    private static final String REGISTER_PLUGIN = "registerPlugins?pluginData={0}";
    private static final String SET_PLUGIN_HOSTS = "setPluginHosts?name={0}{1}";

    // pattern for parsing the list of plugins returned from the backend
    private static final Pattern NSRETURNPATTERN = Pattern.compile("<ns:return[^>]*>([^<]*)</ns:return>");

    /**
     * Enables/disables the given plugin on the Business Backend.
     *
     * @param pluginName
     * @param isEnabled
     * @param hasFinishedCallback A callback which is executed once the action is complete.
     * @return null if something happened, some result string otherwise.
     */
    public String sendEnabled(String pluginName, boolean isEnabled, Runnable hasFinishedCallback) {
        if (pluginName == null) {
            throw new IllegalStateException("Can not run a plugin action without a plugin name");
        }

        LOGGER.info(TextUtils.merge("Setting plugin-state of plugin {0} to state {1}", pluginName, (isEnabled ? "enabled" : "disabled")));

        String result = null;
        try {
        	// set timeout value so we do not block the whole process here
            result = UrlUtils.retrieveData(getBusinessBackendUriSetPluginEnabled(pluginName, isEnabled), null, 60000);

            if (hasFinishedCallback != null) {
                hasFinishedCallback.run();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, TextUtils.merge("Could not switch plugin state for plugin {0} to state {1}", pluginName, (isEnabled ? "enabled" : "disabled")), e);
        }

        return result;
    }

    /**
     * Informs the Business Backend that a plugin should be en- or disabled
     * @param pluginName name of the plugin that state should be changed
     * @param isEnabled new plugin state
     * @author stefan.moschinski
     */
    public void sendPluginStateChanged(String pluginName, boolean isEnabled) {
    	sendEnabled(pluginName, isEnabled, null);
    }

    public String[] requestAllPlugins() throws IOException {
        String xml = UrlUtils.retrieveData(getBusinessBackendUriAllPlugins(), null, 60000);
        return parsePluginList(xml);
    }

    public String[] requestAllPluginNames() throws IOException {
        String xml = UrlUtils.retrieveData(getBusinessBackendUriAllPluginNames(), null, 60000);
        return parsePluginList(xml);
    }

    public String[] requestEnabledPlugins() throws IOException {
        String xml = UrlUtils.retrieveData(getBusinessBackendUriEnabledPlugins(), null, 60000);
        return parsePluginList(xml);
    }

    public String[] requestEnabledPluginNames() throws IOException {
        String xml = UrlUtils.retrieveData(getBusinessBackendUriEnabledPluginNames(), null, 60000);
        return parsePluginList(xml);
    }

	public void registerPlugin(String name) throws IOException {
		UrlUtils.retrieveData(getBusinessBackendUriRegisterPlugin(name), 60000);
	}

	public void setPluginHosts(String name, String[] hosts) throws IOException {
		StringBuilder hostString = new StringBuilder();
		for(String host : hosts) {
			hostString.append("&hosts=").append(host);
		}
		String url = getBusinessBackendUriSetPluginHosts(name, hostString.toString());
		String data = UrlUtils.retrieveData(url, 60000);
		LOGGER.info("Got response '" + data + "' for request " + url);
	}

    /**
     * Read the plugin-names out of the response from the business backend based on the regular
     * expression pattern defined above.
     *
     * @param xml
     * @return
     * @author dominik.stadler
     */
    private static String[] parsePluginList(String xml) {
        List<String> plugins = new ArrayList<String>();
        Matcher match = NSRETURNPATTERN.matcher(xml);

        while (match.find()) {
            plugins.add(StringEscapeUtils.unescapeXml(match.group(1)));
        }

        return plugins.toArray(new String[plugins.size()]);
    }

    private static String getBusinessBackendUriAllPlugins() {
    	return LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE, ALL_PLUGINS);
    }

    private static String getBusinessBackendUriEnabledPlugins() {
    	return LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE, ENABLED_PLUGINS);
    }

    private static String getBusinessBackendUriAllPluginNames() {
    	return LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE, ALL_PLUGIN_NAMES);
    }

    private static String getBusinessBackendUriEnabledPluginNames() {
    	return LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE, ENABLED_PLUGIN_NAMES);
    }

    private static String getBusinessBackendUriSetPluginEnabled(String name, boolean enable) {
        return LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE, SET_PLUGIN_ENABLED, name, Boolean.toString(enable));
    }

    private static String getBusinessBackendUriRegisterPlugin(String name) {
        return LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE, REGISTER_PLUGIN, name);
    }

    private static String getBusinessBackendUriSetPluginHosts(String name, String hostsString) {
        return LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE, SET_PLUGIN_HOSTS, name, hostsString);
    }
}
