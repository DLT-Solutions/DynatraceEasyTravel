/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ScenarioConfigurationTest.java
 * @date: 16.05.2012
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.launcher.config;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.launcher.scenarios.ScenarioGroup;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;


/**
 *
 * @author dominik.stadler
 */
public class ScenarioConfigurationTest {
	private final String scenario = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		    "<configuration defaulthash=\"0\">\n" +
		    "<group title=\"Production\">\n" +
		    "<scenario enabled=\"true\" title=\"Standard\">\n" +
		    "<description>desc</description>\n" +
		    "<procedure id=\"inprocess DBMS\"/>\n" +
		    "<procedure id=\"database content creator\"/>\n" +
		    "<procedure id=\"credit card authorization\">\n" +
		    "<setting name=\"IpcMode\" value=\"Socket\"/>\n" +
		    "</procedure>\n" +
		    "<procedure id=\"business backend\">\n" +
		    "<setting name=\"NamedPipeNativeApplication\" type=\"plugin\" value=\"off\"/>\n" +
		    "<setting name=\"SocketNativeApplication\" type=\"plugin\" value=\"on\"/>\n" +
		    "</procedure>\n" +
		    "<procedure id=\"customer frontend\"/>\n" +
		    "</scenario>\n" +
		    "</group>\n" +
		    "</configuration>\n";

	private final String userScenario = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		    "<configuration>\n" +
		    "<group title=\"Production12\">\n" +
		    "<scenario enabled=\"true\" title=\"Standard123\">\n" +
		    "<description>desc</description>\n" +
		    "<procedure id=\"inprocess DBMS\"/>\n" +
		    "<procedure id=\"database content creator\"/>\n" +
		    "<procedure id=\"credit card authorization\">\n" +
		    "<setting name=\"IpcMode\" value=\"Socket\"/>\n" +
		    "</procedure>\n" +
		    "<procedure id=\"business backend\">\n" +
		    "<setting name=\"NamedPipeNativeApplication\" type=\"plugin\" value=\"off\"/>\n" +
		    "<setting name=\"SocketNativeApplication\" type=\"plugin\" value=\"on\"/>\n" +
		    "</procedure>\n" +
		    "<procedure id=\"customer frontend\"/>\n" +
		    "</scenario>\n" +
		    "</group>\n" +
		    "</configuration>\n";

	private final String userScenario2 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		    "<configuration>\n" +
		    "<group title=\"Production\">\n" +
		    "<scenario enabled=\"true\" title=\"Standard2\">\n" +
		    "<description>desc</description>\n" +
		    "<procedure id=\"inprocess DBMS\"/>\n" +
		    "<procedure id=\"database content creator\"/>\n" +
		    "<procedure id=\"credit card authorization\">\n" +
		    "<setting name=\"IpcMode\" value=\"Socket\"/>\n" +
		    "</procedure>\n" +
		    "<procedure id=\"business backend\">\n" +
		    "<setting name=\"NamedPipeNativeApplication\" type=\"plugin\" value=\"off\"/>\n" +
		    "<setting name=\"SocketNativeApplication\" type=\"plugin\" value=\"on\"/>\n" +
		    "</procedure>\n" +
		    "<procedure id=\"customer frontend\"/>\n" +
		    "</scenario>\n" +
		    "</group>\n" +
		    "</configuration>\n";

	private final String userScenarioFail = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		    "<configuration>\n" +
		    "<group title=\"Production\">\n" +
		    "<scenario enabled=\"true\" title=\"Standard\">\n" +
		    "<description>desc</description>\n" +
		    "<procedure id=\"inprocess DBMS\"/>\n" +
		    "<procedure id=\"database content creator\"/>\n" +
		    "<procedure id=\"credit card authorization\">\n" +
		    "<setting name=\"IpcMode\" value=\"Socket\"/>\n" +
		    "</procedure>\n" +
		    "<procedure id=\"business backend\">\n" +
		    "<setting name=\"NamedPipeNativeApplication\" type=\"plugin\" value=\"off\"/>\n" +
		    "<setting name=\"SocketNativeApplication\" type=\"plugin\" value=\"on\"/>\n" +
		    "</procedure>\n" +
		    "<procedure id=\"customer frontend\"/>\n" +
		    "</scenario>\n" +
		    "</group>\n" +
		    "</configuration>\n";

	private final String userScenarioTenant = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		    "<configuration>\n" +
		    "<group title=\"Production\">\n" +
		    "<scenario enabled=\"true\" title=\"Standard2\">\n" +
		    "<description>desc</description>\n" +
		    "<procedure id=\"inprocess DBMS\" apmTenant=\"tenant1\"/>\n" +
		    "<procedure id=\"database content creator\" apmTenant=\"tenant2\"/>\n" +
		    "<procedure id=\"credit card authorization\">\n" +
		    "<setting name=\"IpcMode\" value=\"Socket\"/>\n" +
		    "</procedure>\n" +
		    "<procedure id=\"business backend\">\n" +
		    "<setting name=\"NamedPipeNativeApplication\" type=\"plugin\" value=\"off\"/>\n" +
		    "<setting name=\"SocketNativeApplication\" type=\"plugin\" value=\"on\"/>\n" +
		    "</procedure>\n" +
		    "<procedure id=\"customer frontend\"/>\n" +
		    "</scenario>\n" +
		    "</group>\n" +
		    "</configuration>\n";

	private final String userScenarioInvalidXML = "<?xml \n";

	// do this before accessing "Directories", this avoids having an existing userScenario cleared while running
	// the test
	static {
		System.setProperty(BaseConstants.SystemProperties.HOME_DIR_CORRECTION, TestEnvironment.RUNTIME_DATA_PATH + "/easyTravel");
	}

	private File file;
	private File userFile = new File(Directories.getConfigDir(), "userScenarios.xml");

	private final ConfigurationReader reader = new ConfigurationReader();

	@Before
	public void setUp() throws Exception {
		file = File.createTempFile("scenarios", ".xml");

		FileUtils.writeStringToFile(file, scenario);

		// initially, the user file should not exists
		assertTrue(!userFile.exists() || userFile.delete());
	}

	@After
	public void tearDown() throws Exception {
		assertTrue(file.delete());
	}

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.config.ScenarioConfiguration#ScenarioConfiguration()}.
	 * @throws Exception
	 */
	@Test
	public void testScenarioConfigurationNoUserFile() throws Exception {
		ScenarioConfiguration scenarioConfig = new ScenarioConfiguration(file);
        XMLConfiguration config = new XMLConfiguration(file);

        ConfigurationNode root = config.getRoot();
        scenarioConfig.read(root, reader);
        List<ScenarioGroup> scenarioGroups = scenarioConfig.getScenarioGroups();
		assertEquals(1, scenarioGroups.size());
        assertEquals("Production", scenarioGroups.get(0).getTitle());
	}

	@Test
	public void testScenarioConfigurationWithUserFile() throws Exception {
		FileUtils.writeStringToFile(userFile, userScenario);

		ScenarioConfiguration scenarioConfig = new ScenarioConfiguration(file);
        XMLConfiguration config = new XMLConfiguration(file);

        ConfigurationNode root = config.getRoot();
        scenarioConfig.read(root, reader);
        List<ScenarioGroup> scenarioGroups = scenarioConfig.getScenarioGroups();
		assertEquals(2, scenarioGroups.size());
		assertEquals("User Scenario Group is loaded first",
				"Production12", scenarioGroups.get(0).getTitle());
        assertEquals("Production", scenarioGroups.get(1).getTitle());

		assertTrue(userFile.delete());
	}

	@Test
	public void testReadTwice() throws Exception {
		ScenarioConfiguration scenarioConfig = new ScenarioConfiguration(file);
        XMLConfiguration config = new XMLConfiguration(file);

        ConfigurationNode root = config.getRoot();
        scenarioConfig.read(root, reader);

        config = new XMLConfiguration(file);
        root = config.getRoot();
        scenarioConfig.read(root, reader);

        List<ScenarioGroup> scenarioGroups = scenarioConfig.getScenarioGroups();
		assertEquals(1, scenarioGroups.size());
        assertEquals("Production", scenarioGroups.get(0).getTitle());
	}

	@Test
	public void testScenarioConfigurationWithUserFileSameGroup() throws Exception {
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		FileUtils.writeStringToFile(userFile, userScenario2);

		ScenarioConfiguration scenarioConfig = new ScenarioConfiguration(file);
        XMLConfiguration config = new XMLConfiguration(file);

        ConfigurationNode root = config.getRoot();
        scenarioConfig.read(root, reader);
        List<ScenarioGroup> scenarioGroups = scenarioConfig.getScenarioGroups();
		assertEquals(1, scenarioGroups.size());
        ScenarioGroup scenarioGroup = scenarioGroups.get(0);
		assertEquals("Production", scenarioGroup.getTitle());

        StringBuilder builder = new StringBuilder();
        List<Scenario> scenarios = scenarioGroup.getScenarios();
		for(Scenario scenario : scenarios) {
        	builder.append(scenario.getGroup()).append("-").append(scenario.getTitle()).append(",");
        }
        assertEquals("Scenarios: " + builder.toString() + ", had: " + scenarios,
        		2, scenarios.size());
        assertEquals("Standard2", scenarios.get(0).getTitle());

		assertTrue(userFile.delete());
	}

	@Test
	public void testScenarioConfigurationWithTenant() throws Exception {
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		FileUtils.writeStringToFile(userFile, userScenarioTenant);

		ScenarioConfiguration scenarioConfig = new ScenarioConfiguration(file);
        XMLConfiguration config = new XMLConfiguration(file);

        ConfigurationNode root = config.getRoot();
        scenarioConfig.read(root, reader);
        List<ScenarioGroup> scenarioGroups = scenarioConfig.getScenarioGroups();
		assertEquals(1, scenarioGroups.size());
        ScenarioGroup scenarioGroup = scenarioGroups.get(0);
		assertEquals("Production", scenarioGroup.getTitle());

        List<Scenario> scenarios = scenarioGroup.getScenarios();
        assertEquals(2, scenarios.size());

        Scenario scenario = scenarios.get(0);
		assertEquals("Standard2", scenario.getTitle());

        for(ProcedureMapping mapping : scenario.getProcedureMappings(InstallationType.Classic)) {
        	String apmTenantUUID = mapping.getAPMTenantUUID();
        	if(mapping.getId().equals(Constants.Procedures.INPROCESS_DBMS_ID)) {
        		assertEquals("tenant1", apmTenantUUID);
        	} else if(mapping.getId().equals(Constants.Procedures.DATABASE_CONTENT_CREATOR_ID)) {
        		assertEquals("tenant2", apmTenantUUID);
        	} else {
        		assertNull("TenantID should be null here, but was: " + apmTenantUUID, apmTenantUUID);
        	}
        }

		assertTrue(userFile.delete());
	}

	@Test
	public void testScenarioConfigurationWithUserFileSameGroupSameScenarioFails() throws Exception {
		FileUtils.writeStringToFile(userFile, userScenarioFail);

		ScenarioConfiguration scenarioConfig = new ScenarioConfiguration(file);
        XMLConfiguration config = new XMLConfiguration(file);

        ConfigurationNode root = config.getRoot();

        try {
        	scenarioConfig.read(root, reader);
        	fail("Expect to fail here");
        } catch (IllegalStateException e) {
        	TestHelpers.assertContains(e, "Production", "Standard", "need to be unique");
        }

		assertTrue(userFile.delete());
	}

	@Test
	public void testScenarioConfigurationInvalidUserFileFails() throws Exception {
		FileUtils.writeStringToFile(userFile, userScenarioInvalidXML);

		ScenarioConfiguration scenarioConfig = new ScenarioConfiguration(file);
        XMLConfiguration config = new XMLConfiguration(file);

        ConfigurationNode root = config.getRoot();

        try {
        	scenarioConfig.read(root, reader);
        	fail("Expect to fail here");
        } catch (ConfigurationException e) {
        	TestHelpers.assertContains(e, "Unable to read user scenario configuration", userFile.toString());
        }

		assertTrue(userFile.delete());
	}

	@Test
	public void testDefaultScenario() {
		ScenarioConfiguration scenarioConfig = new ScenarioConfiguration();
		scenarioConfig.createDefaultScenarios();

		boolean dcrum = false;
		boolean php = false;
		for(ScenarioGroup group : scenarioConfig.getScenarioGroups()) {
			if(group.getTitle().equals(MessageConstants.SCENARIO_GROUP_UEM_TITLE)) {
				for(Scenario scenario : group.getScenarios()) {
					if(scenario.getTitle().equals(MessageConstants.PHP_SCENARIO_DEFAULT_TITLE)) {
						php = true;
					}
				}
			}
		}

		assertTrue("We should find a PHP scenario", php);
	}
	
	private static boolean isWindows = SystemUtils.IS_OS_WINDOWS;
	
	@Test
	public void testCouchDBScenario() {
		ScenarioConfiguration scenarioConfig = new ScenarioConfiguration();
		scenarioConfig.createDefaultScenarios();

		boolean CouchDB = false;
		boolean CouchDBCC = false;
		
		if (isWindows) {
		for(ScenarioGroup group : scenarioConfig.getScenarioGroups()) {
			if(group.getTitle().equals(MessageConstants.SCENARIO_GROUP_UEM_TITLE)) {
				for(Scenario scenario : group.getScenarios()) {
					if(scenario.getTitle().equals(MessageConstants.UEM_SCENARIO_COUCHDB_TITLE)) {
						for(ProcedureMapping mapping : scenario.getProcedureMappings(InstallationType.Classic)) {
							if(mapping.getId().equals(Constants.Procedures.COUCHDB_ID)) {
								CouchDB = true;
							}
							if(mapping.getId().equals(Constants.Procedures.COUCHDB_CONTENT_CREATOR_ID)) {
								CouchDBCC = true;
							}
							if(mapping.getId().equals(Constants.Procedures.BUSINESS_BACKEND_ID)) {
								assertEquals(
									Constants.Misc.SETTING_VALUE_ON,
									mapping.getSettingValue(Constants.Misc.SETTING_TYPE_PLUGIN, BaseConstants.Plugins.COUCHDB));
							}
						}
					}
				}
			}
		}
		} else {
			// not Windows - not applicable so fake success
			CouchDB = true;
			CouchDBCC = true;
		}

		assertTrue("We should find a 'UEM' group with a 'CouchDB' scenario which contains a CouchDB proc, but we did not find this.", CouchDB);
		assertTrue("We should find a 'UEM' group with a 'CouchDB' scenario which contains a CouchDB Content creator proc, but we did not find this.", CouchDBCC);
	}

	@Test
	public void testScenarioConfigurationManualStart() throws Exception {
		ScenarioConfiguration.setManualStart(true);

		ScenarioConfiguration scenarioConfig = new ScenarioConfiguration(file);
		scenarioConfig.notifyTechnologyStateChanged(Technology.DOTNET_20, false, null, null);
		scenarioConfig.loadOrCreate();

		assertNotNull(scenarioConfig.findScenario(MessageConstants.UEM_SCENARIO_DEFAULT_TITLE));
		assertNotNull(scenarioConfig.findScenario(MessageConstants.UEM_SCENARIO_DEFAULT_TITLE, MessageConstants.SCENARIO_GROUP_UEM_TITLE));
		assertNotNull(scenarioConfig.findScenario(MessageConstants.UEM_SCENARIO_DEFAULT_TITLE, null));
		assertNotNull("null returns the first matching one", scenarioConfig.findScenario(null));
		assertNotNull("null returns the first matching one", scenarioConfig.findScenario(null, null));


		assertNull(scenarioConfig.findScenario("notexisting"));

		scenarioConfig.notifyTechnologyStateChanged(Technology.DOTNET_20, false, null, null);
		scenarioConfig.notifyTechnologyStateChanged(Technology.DOTNET_20, true, null, null);

		for(ScenarioGroup group : scenarioConfig.getScenarioGroups()) {
			for(Scenario scenario : group.getScenarios()) {
				List<ProcedureMapping> procedureMappings = scenario.getProcedureMappings(InstallationType.Both);
				assertNotNull(procedureMappings);
				assertTrue(procedureMappings.size() > 0);
				for(ProcedureMapping mapping : procedureMappings) {
					assertEquals(Constants.Misc.SETTING_VALUE_OFF, mapping.getSettingValue(Constants.Misc.SETTING_ENABLED));
				}
			}
		}
	}

	@Test
	public void testLoadOrCreate() throws Exception {
		ScenarioConfiguration scenarioConfig = new ScenarioConfiguration(file);
		scenarioConfig.loadOrCreate();

		assertNotNull(scenarioConfig.findScenario(MessageConstants.UEM_SCENARIO_DEFAULT_TITLE));
		assertNotNull(scenarioConfig.findScenario(MessageConstants.UEM_SCENARIO_DEFAULT_TITLE, MessageConstants.SCENARIO_GROUP_UEM_TITLE));
	}

	@Test
	public void testDefaultScenarioPHP() {
		ScenarioConfiguration scenarioConfig = new ScenarioConfiguration();

		System.setProperty("com.dynatrace.easytravel.host.apache_httpd_php", "somehost");
		try {
			scenarioConfig.createDefaultScenarios();
		} finally {
			System.clearProperty("com.dynatrace.easytravel.host.apache_httpd_php");
		}

		boolean php = false;
		for(ScenarioGroup group : scenarioConfig.getScenarioGroups()) {
			if(group.getTitle().equals(MessageConstants.SCENARIO_GROUP_UEM_TITLE)) {
				for(Scenario scenario : group.getScenarios()) {
					if(scenario.getTitle().equals(MessageConstants.PHP_SCENARIO_DEFAULT_TITLE)) {
						php = true;
					}
				}
			}
		}

		assertTrue("We should find a PHP scenario if set via system property", php);
	}

	@Test
	public void testPersistentDefault() throws ConfigurationException, IOException {
		TestEnvironment.createOrClearRuntimeData();

		File file = File.createTempFile("scenario", ".test", new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
		ScenarioConfigurationPersistence persistence = new ScenarioConfigurationPersistence(file);
		ScenarioConfiguration defaultConfig = new ScenarioConfiguration(persistence);
		defaultConfig.createDefaultScenarios();
		String defaultMd5 = persistence.calcMd5(defaultConfig);

		ScenarioConfiguration newConfig = new ScenarioConfiguration(persistence);
		newConfig.createDefaultScenarios();
		String newMd5 = persistence.calcMd5(newConfig);

		assertEquals(defaultMd5, newMd5);

		assertTrue(file.delete());
	}
}
