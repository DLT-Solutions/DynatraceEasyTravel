package com.dynatrace.easytravel.launcher.config;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.utils.TestEnvironment;


public class ScenarioConfigurationPersistenceTest {
    private File configfile;

    @Before
    public void setUp() throws IOException {
        TestEnvironment.createOrClearRuntimeData();

        configfile = File.createTempFile("testscenario", ".xml", new File(TestEnvironment.RUNTIME_DATA_PATH));
        assertTrue(configfile.delete());
    }

    @After
    public void tearDown() throws IOException {
        TestEnvironment.clearRuntimeData();
    }

    @Test
    public void testScenarioConfigurationPersistence() throws ConfigurationException {
        // create and load scenario configuration
        ScenarioConfigurationPersistence persistence = new ScenarioConfigurationPersistence(configfile);
        ScenarioConfiguration configuration = new ScenarioConfiguration(persistence);

        assertFalse(configfile.exists());
        configuration.loadOrCreate();
        assertTrue(configfile.exists());

        assertTrue(configfile.delete());

        assertFalse(configfile.exists());
        configuration.save();
        assertTrue(configfile.exists());
    }

    @Test
    public void testLoad() throws ConfigurationException {
        ScenarioConfigurationPersistence persistence = new ScenarioConfigurationPersistence(configfile);
        ScenarioConfiguration configuration = new ScenarioConfiguration(persistence);
        assertFalse(configfile.exists());
        persistence.load(configuration);
        assertTrue(configfile.exists());
    }

    @Test
    public void testCreateAndLoadDefaultConfig() throws ConfigurationException {
        ScenarioConfigurationPersistence persistence = new ScenarioConfigurationPersistence(configfile);
        ScenarioConfiguration configuration = new ScenarioConfiguration(persistence);
        assertFalse(configfile.exists());
        persistence.createAndLoadDefaultConfig(configuration);
        assertTrue(configfile.exists());
    }

    @Test
    public void testCalcMd5() throws ConfigurationException {
        ScenarioConfigurationPersistence persistence = new ScenarioConfigurationPersistence(configfile);
        ScenarioConfiguration configuration = new ScenarioConfiguration(persistence);
        assertNotNull(persistence.calcMd5(configuration));
        assertFalse(persistence.calcMd5(configuration).isEmpty());
    }

    @Test
    public void testReadHash() throws ConfigurationException {
        ScenarioConfigurationPersistence persistence = new ScenarioConfigurationPersistence(configfile);
        XMLConfiguration config = new XMLConfiguration();
        config.getRootNode().addAttribute(new DefaultConfigurationNode("defaulthash", "123456"));
        assertNotNull(persistence.readHash(config.getRootNode()));
        assertEquals("123456", persistence.readHash(config.getRootNode()));
    }

    @Test
    public void testSaveUserInterfacePropsState() throws ConfigurationException, org.apache.commons.configuration.ConfigurationException {
        ScenarioConfigurationPersistence persistence = new ScenarioConfigurationPersistence(configfile);
        ScenarioConfiguration configuration = new ScenarioConfiguration(persistence);
        persistence.createAndLoadDefaultConfig(configuration);
        persistence.saveUserInterfacePropsState("test_element", true);

        XMLConfiguration configToCheck = new XMLConfiguration(configfile);
        assertNotNull(configToCheck);
        assertTrue(configToCheck.getBoolean("UIAvailabilityProperties.test_element"));
    }
}
