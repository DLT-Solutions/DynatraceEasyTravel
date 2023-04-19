package com.dynatrace.easytravel.launcher.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.utils.TestEnvironment;

public class ScenarioConfigurationAdditionalSettingsTest {
    private File configFile;
    private final ConfigurationReader reader = new ConfigurationReader();
    private final NodeFactory factory = new NodeFactory();

    private final String uiTestScenario = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<configuration defaulthash=\"0\">\n" +
            "<UIAvailabilityProperties>\n" +
            "<UIProblemPatterns>true</UIProblemPatterns>\n" +
            "</UIAvailabilityProperties>\n" +
            "</configuration>\n";

    @Before
    public void setUp() throws Exception {
        TestEnvironment.createOrClearRuntimeData();
        configFile = File.createTempFile("testscenario", ".xml", new File(TestEnvironment.RUNTIME_DATA_PATH));
        FileUtils.writeStringToFile(configFile, uiTestScenario);
    }

    @After
    public void tearDown() throws Exception {
        assertTrue(configFile.delete());
    }

    /**
     * Tests if method actually can get info about value of node UIProblemPatterns defined in configuration
     * @throws ConfigurationException
     */
    @Test
    public void testReadUIProps() throws ConfigurationException {
        XMLConfiguration config = new XMLConfiguration(configFile);
        ConfigurationNode root = config.getRoot();

        ScenarioConfigurationAdditionalSettings.INSTANCE.readUIProperties(root, reader, UIProperties.PROBLEM_PATTERNS.getPropertyName());
        assertTrue(UIProperties.PROBLEM_PATTERNS.getEnabled());

    }
    
    /**
     * 
     * @throws ConfigurationException
     */
    @Test
    public void testWriteUIProps() throws ConfigurationException {
        XMLConfiguration config = new XMLConfiguration(configFile);
        ConfigurationNode root = config.getRoot();
        UIProperties.PROBLEM_PATTERNS.setEnabled(false);
        assertFalse(UIProperties.PROBLEM_PATTERNS.getEnabled());
        ScenarioConfigurationAdditionalSettings.INSTANCE.writeUIProperties(root, factory);
        
        List<ConfigurationNode> nodes = reader.getChildren(root, Constants.ConfigurationXml.NODE_USER_INTERFACE_AVAILABILITY_PROPERTIES);
        assertTrue(nodes.size() > 0);

    }
}
