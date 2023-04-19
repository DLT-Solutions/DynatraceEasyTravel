package com.dynatrace.easytravel.launcher.config;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.util.TextUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ScenarioConfigurationPersistence {

	private static final Logger LOGGER = Logger.getLogger(ScenarioConfigurationPersistence.class.getName());

	private static final String ATTRIBUTE_DEFAULT_HASH = "defaulthash";

	private final ConfigurationReader reader = new ConfigurationReader();
	private final NodeFactory factory = new NodeFactory();

	private final File configFile;

	public ScenarioConfigurationPersistence(File configFile) {
		// disable special handling of ',' in XML which is very confusing when manually editing the descriptions!
		// see http://commons.apache.org/configuration/apidocs/org/apache/commons/configuration/XMLConfiguration.html
		AbstractConfiguration.setDefaultListDelimiter((char) 0);

		if (configFile == null) {
			this.configFile = new File(Directories.getConfigDir(), Constants.Misc.SCENARIOS_FILE);
		} else {
			this.configFile = configFile;
		}
	}

	public void load(ScenarioConfiguration config) throws ConfigurationException {
		try {
			// try to load stored configuration first
			loadValidConfig(config);
		} catch (ConfigurationException e) {
			LOGGER.log(Level.WARNING, "Failed to load a valid scenario configuration. A new scenario configuration is created.", e);
			createAndLoadDefaultConfig(config);
		}
	}

	public void createAndLoadDefaultConfig(ScenarioConfiguration config) throws ConfigurationException {
		// if the stored config cannot be parsed then replace existing with default config
		createDefaultConfig(getNewExistingConfigFile());

		// if previously a problem occurred then try to load the newly written config
		loadValidConfig(config);
	}

	private void loadValidConfig(ScenarioConfiguration scenarioConfig) throws ConfigurationException {
		XMLConfiguration config = loadConfigFile(getExistingConfigFile());
		ConfigurationNode root = config.getRoot();
		scenarioConfig.read(root, reader);
		scenarioConfig.setHash(readHash(root));
	}

	private XMLConfiguration loadConfigFile(File cfgLocation) throws ConfigurationException {
		try {
			return new XMLConfiguration(cfgLocation);
		} catch (org.apache.commons.configuration.ConfigurationException e) {
			throw new ConfigurationException("Unable to read scenario configuration.", e);
		}
	}

	public void save(ScenarioConfiguration scenarioConfig) throws ConfigurationException {
		save(scenarioConfig, calcMd5(scenarioConfig));
	}

	public void save(ScenarioConfiguration scenarioConfig, String hash) throws ConfigurationException {
		XMLConfiguration config = new XMLConfiguration();
		ConfigurationNode root = config.getRoot();

		root.addAttribute(factory.createNode(ATTRIBUTE_DEFAULT_HASH, hash));
		scenarioConfig.write(root, factory);

		try {
			config.save(configFile);
		} catch (org.apache.commons.configuration.ConfigurationException e) {
			throw new ConfigurationException("Unable to save scenario configuration.", e);
		}
	}


	/**
	 * @param technologyName defines the technology that should be enabled/disabled
	 * @param enabled        if <code>true</code> the technology is enabled,
	 * @throws ConfigurationException
	 * @author stefan.moschinski
	 */
	void saveTechnologyState(String technologyName, boolean enabled) throws ConfigurationException {
		// we do not want to save the current scenario configuration out of memory
		// because it also contains the userScenarios, so we load the configuration file
		// from memory again
		XMLConfiguration config = loadConfigFile(configFile);

		ConfigurationNode root = config.getRoot();
		List<ConfigurationNode> technology = new ConfigurationReader().getChildren(root,
				Constants.ConfigurationXml.NODE_TECHNOLOGY_PROPERTIES);

		ConfigurationNode techNode = technology.iterator().next();
		for (Object objNode : techNode.getChildren()) {
			ConfigurationNode tempNode = (ConfigurationNode) objNode;
			if (technologyName.equals(tempNode.getName())) {
				tempNode.setValue(enabled);
				break;
			}
		}

		try {
			config.save(configFile);
		} catch (org.apache.commons.configuration.ConfigurationException e) {
			throw new ConfigurationException("Unable to save scenario configuration.", e);
		}
	}

	/**
	 * @param uiElementName defines the ui element that should be enabled/disabled
	 * @param enabled       if <code>true</code> the ui element is enabled,
	 * @throws ConfigurationException
	 * @author zbigniew.sokolowski
	 */
	void saveUserInterfacePropsState(String uiElementName, boolean enabled) throws ConfigurationException {
		// we do not want to save the current scenario configuration out of memory
		// because it also contains the userScenarios, so we load the configuration file
		// from memory again
		XMLConfiguration config = loadConfigFile(configFile);

		ConfigurationNode root = config.getRoot();
		List<ConfigurationNode> ui = new ConfigurationReader().getChildren(root,
				Constants.ConfigurationXml.NODE_USER_INTERFACE_AVAILABILITY_PROPERTIES);
		if (ui.size() == 0) {
			root.addChild(new HierarchicalConfiguration.Node(Constants.ConfigurationXml.NODE_USER_INTERFACE_AVAILABILITY_PROPERTIES));
			ui = new ConfigurationReader().getChildren(root,
					Constants.ConfigurationXml.NODE_USER_INTERFACE_AVAILABILITY_PROPERTIES);

		}

		ConfigurationNode uiNode = ui.iterator().next();
		if (uiNode.getChildren(uiElementName).equals(Collections.emptyList())) {
			uiNode.addChild(new HierarchicalConfiguration.Node(uiElementName));
		}
		for (Object objNode : uiNode.getChildren()) {
			ConfigurationNode tempNode = (ConfigurationNode) objNode;
			if (uiElementName.equals(tempNode.getName())) {
				tempNode.setValue(enabled);
				break;
			}
		}
		try {
			config.save(configFile);
		} catch (org.apache.commons.configuration.ConfigurationException e) {
			throw new ConfigurationException("Unable to save scenario configuration, while saving .", e);
		}
	}


	public String calcMd5(ScenarioConfiguration scenarioConfig) throws ConfigurationException {
		XMLConfiguration config = new XMLConfiguration();

		// create scenario configuration without hash code of the default configuration
		scenarioConfig.write(config.getRootNode(), factory);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		try {
			config.save(outStream);
		} catch (org.apache.commons.configuration.ConfigurationException e) {
			throw new ConfigurationException("Unable to calculate MD5 of scenario configuration.", e);
		}

		byte[] buffer = outStream.toByteArray();

		return DigestUtils.md5Hex(buffer);
	}

	private File getNewExistingConfigFile() {
		if (configFile.exists()) {
			File dir = configFile.getParentFile();
			String name = "backup" + BaseConstants.UNDERSCORE + Long.toString(System.currentTimeMillis()) + BaseConstants.UNDERSCORE + configFile.getName();

			configFile.renameTo(new File(dir, name));
		}

		return getExistingConfigFile();
	}

	public File getExistingConfigFile() {
		if (!configFile.exists()) {
			createDefaultConfig(configFile);
		}

		return configFile;
	}

	private void createDefaultConfig(File configFile) {
		if (configFile.exists()) {
			configFile.delete();
		}

		try {
			ScenarioConfiguration config = new ScenarioConfiguration(this);
			config.createDefaultScenarios();
			config.save();
		} catch (ConfigurationException e) {
			LOGGER.log(Level.WARNING, "Unable to save default scenario configuration.", e);
		}
	}

	protected String readHash(ConfigurationNode root) throws ConfigurationException {
		String hash = reader.readStringAttribute(root, ATTRIBUTE_DEFAULT_HASH);
		if (hash == null || hash.isEmpty()) {
			throw new ConfigurationException(TextUtils.merge("Invalid value of ''{0}'' attribute.", ATTRIBUTE_DEFAULT_HASH));
		}

		return hash;
	}


}
