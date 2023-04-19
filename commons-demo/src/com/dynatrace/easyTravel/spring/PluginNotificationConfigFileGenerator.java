package com.dynatrace.easytravel.spring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Class used to generate a template configuration file for plugin change notifications.
 *
 * @author tomasz.wieremjewicz
 * @date 20 lis 2017
 *
 */
public class PluginNotificationConfigFileGenerator {
	private static final Logger LOGGER = Logger.getLogger(PluginNotificationConfigFileGenerator.class.getName());

	public void generateConfigFile() {
		generateConfigFile(createDefaultTemplate(), ResourceFileReader.PLUGINNOTIFICATIONCONFIG);
	}

	public void generateConfigFile(PluginNotificationTemplate[] templates, String fileName) {
		LOGGER.log(Level.INFO, "Creating " + fileName + " file.");

		File file = new File(Directories.getConfigDir(), fileName);

		if (!file.exists()) {
			file.getParentFile().mkdirs();

			PrintWriter writer = null;
			try {
				writer = new PrintWriter(file);
				ObjectMapper mapper = new ObjectMapper();
				mapper.enable(SerializationFeature.INDENT_OUTPUT);
				writer.println(mapper.writeValueAsString(templates));
			} catch (FileNotFoundException e) {
				LOGGER.log(Level.WARNING, "Can't create config file for plugin notifications - file not found", e);
			} catch (JsonProcessingException e) {
				LOGGER.log(Level.WARNING, "Can't create config file for plugin notifications - json parsing problem", e);
			} finally {
				if(writer != null){
					writer.close();
				}
			}
		}
		else {
			LOGGER.log(Level.WARNING, "File was already present - new file was not generated.");
		}
	}

	private PluginNotificationTemplate[] createDefaultTemplate() {
		PluginNotificationTemplate[] templates = new PluginNotificationTemplate[2];
		templates[0] = new PluginNotificationTemplate();
		templates[0].pluginNames = new String[] {"REPLACEME"};
		templates[0].entityIds = new String[] {"REPLACEME"};
		templates[0].title = "Plugin state change";
		templates[0].source = "ServiceNow";
		templates[0].version = "_etVersion_._timestamp_";
		templates[0].customProperties = new HashMap<>();
		templates[0].customProperties.put("Plugin enabled", "_pluginState_");
		templates[0].customProperties.put("Plugin name", "_pluginName_");
		templates[0].deploymentProject = "easyTravel";
		templates[0].ciBackLink = "REPLACEME";
		templates[0].remediationAction = "REPLACEME";

		templates[1] = new PluginNotificationTemplate();
		templates[1].pluginNames = new String[] {PluginNotificationTemplate.DEFAULT_PLUGIN_NAME};
		templates[1].entityIds = new String[] {};
		templates[1].title = "Plugin state change";
		templates[1].source = "easyTravel";
		templates[1].version = "_etVersion_._timestamp_";
		templates[1].customProperties = new HashMap<>();
		templates[1].customProperties.put("Plugin enabled", "_pluginState_");
		templates[1].customProperties.put("Plugin name", "_pluginName_");
		templates[1].deploymentProject = "easyTravel";
		templates[1].ciBackLink = "REPLACEME";
		templates[1].remediationAction = "REPLACEME";
		return templates;
	}
}

