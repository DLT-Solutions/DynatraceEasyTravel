package com.dynatrace.easytravel.launcher.config;

import java.util.List;

import org.apache.commons.configuration.tree.ConfigurationNode;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: cwpl-zsokolow
 * Date: 24.01.13
 * Time: 12:20
 */
public enum ScenarioConfigurationAdditionalSettings {
	INSTANCE;

	private static final Logger LOGGER = LoggerFactory.make();
	private ScenarioConfigurationPersistence persistence;

	public void setScenarioConfigurationPersistence(ScenarioConfigurationPersistence persistence) {
		this.persistence = persistence;
	}

	public void readUIProperties(ConfigurationNode node, ConfigurationReader reader, String propertyName) {
		List<ConfigurationNode> nodes = reader.getChildren(node, Constants.ConfigurationXml.NODE_USER_INTERFACE_AVAILABILITY_PROPERTIES);
		if (nodes.size() == 0) {
			LOGGER.warn(TextUtils.merge("User interface properties are set within ''{0}'', using default values.",
					persistence.getExistingConfigFile().getAbsolutePath()));
			return;
		}

		ConfigurationNode uiNode = nodes.iterator().next();
		for (Object objNode : uiNode.getChildren()) {
			ConfigurationNode tempNode = (ConfigurationNode) objNode;
			String name = ((ConfigurationNode) objNode).getName();
			UIProperties.findByPropName(name).setEnabled(
					Boolean.parseBoolean(tempNode.getValue().toString()));
		}


	}


	public void writeUIProperties(ConfigurationNode node, NodeFactory factory) {
		ConfigurationNode uiNode = factory.createNode(Constants.ConfigurationXml.NODE_USER_INTERFACE_AVAILABILITY_PROPERTIES);
		node.addChild(uiNode);
		for (UIProperties UIProperty : UIProperties.values()) {
			uiNode.addChild(factory.createNode(UIProperty.getPropertyName(), UIProperty.getEnabled()));
		}

	}

	public void persistConfigurationState(String propertyName, boolean propertyState) {

		try {
			persistence.saveUserInterfacePropsState(propertyName, propertyState);
		} catch (ConfigurationException e) {
			LOGGER.warn("Saving of changed configuration was not possible", e);
		}
	}


}
