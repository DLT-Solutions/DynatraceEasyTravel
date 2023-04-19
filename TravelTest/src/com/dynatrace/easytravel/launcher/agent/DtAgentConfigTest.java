package com.dynatrace.easytravel.launcher.agent;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.google.common.base.Joiner;

import ch.qos.logback.classic.Level;

public class DtAgentConfigTest {

    private static final String TEMP_DIR_PROPERTY = "java.io.tmpdir";

    private static File TEST_DIR = null;
    private static File EXISTING_FILE = null;
    private static File NOT_EXISTING_FILE = null;

    private static DtAgentConfig CONFIG_AUTO_DETECTED_NULL_AGENT = null;
    private static DtAgentConfig CONFIG_AUTO_DETECTED_NOT_EXISTING_AGENT = null;
    private static DtAgentConfig CONFIG_AUTO_DETECTED_EXISTING_AGENT = null;

    private static DtAgentConfig CONFIG_NE_DETECTED_NULL_AGENT = null;
    private static DtAgentConfig CONFIG_NE_DETECTED_NE_AGENT = null;
    private static DtAgentConfig CONFIG_NE_DETECTED_E_AGENT = null;

    private static DtAgentConfig CONFIG_NULL_AGENT = null;


    @BeforeClass
    public static void setUp() {
        String tempDir = System.getProperty(TEMP_DIR_PROPERTY);
        TEST_DIR = new File(tempDir, VersionedDirTest.class.getCanonicalName());
        EXISTING_FILE = new File(TEST_DIR, "existingFile");
        EXISTING_FILE.mkdirs();
        NOT_EXISTING_FILE = new File(TEST_DIR, "nonExistingFile");

        EasyTravelConfig config = EasyTravelConfig.read();

        config.dtServer = "server";
        config.dtServerPort = "1234";
        CONFIG_AUTO_DETECTED_NULL_AGENT = new DtAgentConfig("agentName", BaseConstants.AUTO, null, null) {

            @Override
            protected File detectAgent(Technology technology) {
                return null;
            }
        };
        config.dtServer = "server";
        config.dtServerPort = "1234";
        CONFIG_AUTO_DETECTED_NOT_EXISTING_AGENT = new DtAgentConfig("agentName", BaseConstants.AUTO, null, null) {

            @Override
            protected File detectAgent(Technology technology) {
                return NOT_EXISTING_FILE;
            }
        };
        config.dtServer = "server";
        config.dtServerPort = "1234";
        CONFIG_AUTO_DETECTED_EXISTING_AGENT = new DtAgentConfig("agentName", BaseConstants.AUTO, null, null) {

            @Override
            protected File detectAgent(Technology technology) {
                return EXISTING_FILE;
            }
        };


        config.dtServer = "server";
        config.dtServerPort = "1234";
        CONFIG_NE_DETECTED_NULL_AGENT = new DtAgentConfig("agentName", NOT_EXISTING_FILE.getAbsolutePath(), null, null) {
            @Override
            protected File detectAgent(Technology technology) {
                return null;
            }
        };
        config.dtServer = "server";
        config.dtServerPort = "1234";
        CONFIG_NE_DETECTED_NE_AGENT = new DtAgentConfig("agentName", NOT_EXISTING_FILE.getAbsolutePath(), null, null) {
            @Override
            protected File detectAgent(Technology technology) {
                return NOT_EXISTING_FILE;
            }
        };
        config.dtServer = "server";
        config.dtServerPort = "1234";
        CONFIG_NE_DETECTED_E_AGENT = new DtAgentConfig("agentName", NOT_EXISTING_FILE.getAbsolutePath(), null, null) {
            @Override
            protected File detectAgent(Technology technology) {
                return EXISTING_FILE;
            }
        };

        config.dtServer = "server";
        config.dtServerPort = "1234";
        CONFIG_NULL_AGENT = new DtAgentConfig("agentName", BaseConstants.NONE, null, null);
    }

    @AfterClass
    public static void cleanUp() {
        if (TEST_DIR != null && TEST_DIR.exists()) {
            TestEnvironment.deleteDirectory(TEST_DIR);
        }

        // clear cached values
        DtVersionDetector.clearCache();
        DtVersionDetector.enforceInstallationType(null);
    }

    @Test
    public void testConstructor() throws ConfigurationException {
    	DtVersionDetector.enforceInstallationType(InstallationType.Classic);
    	assertTrue(DtVersionDetector.isClassic());

    	EasyTravelConfig config = EasyTravelConfig.read();
    	DtAgentConfig dtAgentConfig;

        config.dtServer = "server";
        config.dtServerPort = "1234";
        dtAgentConfig = new DtAgentConfig("agentName", null, null, null);
        assertEquals("server", dtAgentConfig.getServer());
        assertEquals("agentName", dtAgentConfig.getAgentName());
        assertTrue(dtAgentConfig.hasServerPort());
        assertEquals(1234, dtAgentConfig.getServerPort());

        config.dtServer = "server";
        config.dtServerPort = "1234";
        dtAgentConfig = new DtAgentConfig("agentName", null, null, null);
        assertTrue(dtAgentConfig.hasServerPort());
        assertEquals(1234, dtAgentConfig.getServerPort());

        config.dtServer = "server";
        config.dtServerPort = BaseConstants.AUTO;
        dtAgentConfig = new DtAgentConfig("agentName", null, null, null);
        assertFalse(dtAgentConfig.hasServerPort());

        config.dtServer = "server";
        config.dtServerPort = null;
        dtAgentConfig = new DtAgentConfig("agentName", null, null, null);
        assertFalse(dtAgentConfig.hasServerPort());

        config.dtServer = "server";
        config.dtServerPort = "port1234";
        dtAgentConfig = new DtAgentConfig("agentName", null, null, null);
        assertFalse("Use auto settings if unable to parse port string.", dtAgentConfig.hasServerPort());

        config.dtServer = "server";
        config.dtServerPort = "1234";
        dtAgentConfig = new DtAgentConfig("agentName", null, null, new String[] { "arg1", "arg2"});
        assertTrue(dtAgentConfig.hasServerPort());
        assertEquals(1234, dtAgentConfig.getServerPort());

        config.dtServer = "server";
        config.dtServerPort = "1234";
        dtAgentConfig = new DtAgentConfig("agentName", null, null, new String[] { "arg1", "arg2:colon"});
        assertTrue(dtAgentConfig.hasServerPort());
        assertEquals(1234, dtAgentConfig.getServerPort());

        try {
        	new DtAgentConfig("agentName", null, null, new String[] { "arg1", "DT_SERVER=arg2:23423:bla"});
        	fail("Should catch exception here");
        } catch (IllegalArgumentException e) {
        	TestHelpers.assertContains(e, "arg2:23423:bla", "dynaTrace server", "is not valid");
        }

        config.dtServer = "server";
        config.dtServerPort = "1234";
        dtAgentConfig = new DtAgentConfig("agentName", null, null, new String[] { "arg1", "DT_SERVER=arg2:23423"});
        assertTrue(dtAgentConfig.hasServerPort());
        assertEquals(23423, dtAgentConfig.getServerPort());

        config.dtServer = "server";
        config.dtServerPort = "1234";
        dtAgentConfig = new DtAgentConfig(BaseConstants.NONE, BaseConstants.NONE, null, null);
        assertNull(dtAgentConfig.getAgentFile(Technology.JAVA));

        config.dtServer = "server";
        config.dtServerPort = "1234";
        dtAgentConfig = new DtAgentConfig("some", BaseConstants.NONE, null, null);
        assertNull(dtAgentConfig.getAgentFile(Technology.JAVA));

        config.dtServer = "server";
        config.dtServerPort = "1234";
        dtAgentConfig = new DtAgentConfig(BaseConstants.NONE, "some", null, null);
        assertNull(dtAgentConfig.getAgentFile(Technology.JAVA));

        // to avoid finding agents in other tests
        System.setProperty(BaseConstants.SystemProperties.AGENT_LOOKUP_DIR, ".");
        try {
	        assertNull(dtAgentConfig.detectAgent(Technology.JAVA));
	        assertNull(dtAgentConfig.detectAgent(Technology.WEBSERVER));
	        assertNull(dtAgentConfig.detectAgent(Technology.WEBPHPSERVER));
        } finally {
        	System.clearProperty(BaseConstants.SystemProperties.AGENT_LOOKUP_DIR);
        }

        TestHelpers.ToStringTest(dtAgentConfig);
        // TODO: assertNull(dtAgentConfig.getLog(Technology.JAVA));
    }

    @Test
    public void testGetPath_AutoConfiguredAgent() throws ConfigurationException {
    	EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
        DtVersionDetector.enforceInstallationType(null);

        try {
        	// check a few things up-front to make sure we are in the correct "state"
        	assertFalse(DtVersionDetector.isAPM());
        	assertNull(CONFIG_AUTO_DETECTED_NULL_AGENT.detectAgent(null));
        	assertNull(CONFIG_NULL_AGENT.detectAgent(Technology.JAVA));

        	try {
        		CONFIG_AUTO_DETECTED_NULL_AGENT.getAgentPath(null);
        		fail("No configuration exception thrown if no agent found.");
        	} catch (ConfigurationException ce) {
        	}

        	try {
        		CONFIG_AUTO_DETECTED_NOT_EXISTING_AGENT.getAgentPath(null);
        		fail("No configuration exception thrown if no existing agent found.");
        	} catch (ConfigurationException ce) {
        	}

        	String agentPath = CONFIG_AUTO_DETECTED_EXISTING_AGENT.getAgentPath(null);
        	assertEquals(EXISTING_FILE, new File(agentPath));

        	// no logfile found as "log" directory does not exist three directories up (agent/lib/dtagent.dll)
        	assertNull(CONFIG_AUTO_DETECTED_EXISTING_AGENT.getLog(null));
        	assertNull(CONFIG_AUTO_DETECTED_EXISTING_AGENT.getLog(null));
        	assertNull(CONFIG_AUTO_DETECTED_EXISTING_AGENT.getBootstrapLog(null));

        	assertNull(CONFIG_NULL_AGENT.getLog(null));
        	assertNull(CONFIG_NULL_AGENT.getBootstrapLog(null));
        } finally {
        	EasyTravelConfig.resetSingleton();
        }
    }


    @Test
    public void testGetPath_AutoConfiguredAgentAPM() throws ConfigurationException, IOException {
        DtVersionDetector.enforceInstallationType(InstallationType.APM);

    	// check a few things up-front to make sure we are in the correct "state"
    	assertTrue(DtVersionDetector.isAPM());

    	// all return null with APM and BaseConstants.AUTO
    	assertNull(CONFIG_AUTO_DETECTED_NULL_AGENT.getAgentPath(null));
        assertNull(CONFIG_AUTO_DETECTED_NOT_EXISTING_AGENT.getAgentPath(null));
        assertNull(CONFIG_AUTO_DETECTED_EXISTING_AGENT.getAgentPath(null));
    	assertNull(CONFIG_NULL_AGENT.getAgentPath(null));

        File file = File.createTempFile("agent", "test", new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
        DtAgentConfig dtAgentConfig = new DtAgentConfig("agentname", file.getAbsolutePath(), null, null);
        assertNotNull(dtAgentConfig.getAgentPath(Technology.JAVA));
    }

    @Test
    public void testGetPath_ConfiguredAgentNotExisting() {
    	EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
        DtVersionDetector.enforceInstallationType(null);

        try {
        	// check a few things up-front to make sure we are in the correct "state"
        	assertFalse(DtVersionDetector.isAPM());
        	assertNull(CONFIG_AUTO_DETECTED_NULL_AGENT.detectAgent(null));
        	assertNull(CONFIG_NULL_AGENT.detectAgent(Technology.JAVA));

        	try {
        		CONFIG_NE_DETECTED_NULL_AGENT.getAgentPath(null);
        		fail("No configuration exception thrown if no agent found.");
        	} catch (ConfigurationException ce) {
        	}

        	try {
        		CONFIG_NE_DETECTED_NE_AGENT.getAgentPath(null);
        		fail("No configuration exception thrown if no existing agent found.");
        	} catch (ConfigurationException ce) {
        	}

        	try {
        		String agentPath = CONFIG_NE_DETECTED_E_AGENT.getAgentPath(null);
        		assertEquals(EXISTING_FILE, new File(agentPath));
        	} catch (ConfigurationException e) {
        		fail("Agent exists. Configuration Exception must not be thrown. " + e.getMessage());
        	}
        } finally {
        	EasyTravelConfig.resetSingleton();
        }
    }

    @Test
    public void testParseFurtherArgs() {
        DtAgentConfig config;
        List<String> furtherDtArgs;

        config = new DtAgentConfig(null, null, new String[] { "wait=1", "sotimeout=2", "ctimeout=3" }, null);
        furtherDtArgs = config.getFurtherDtArgs();
        assertEquals("Unexpected length of further DT argument list.", 3, furtherDtArgs.size());
        assertEquals("wait=1", furtherDtArgs.get(0));
        assertEquals("sotimeout=2", furtherDtArgs.get(1));
        assertEquals("ctimeout=3", furtherDtArgs.get(2));

        config = new DtAgentConfig(null, null, new String[] { "wait=1" }, null);
        furtherDtArgs = config.getFurtherDtArgs();
        assertEquals("Unexpected length of further DT argument list.", 1, furtherDtArgs.size());
        assertEquals("wait=1", furtherDtArgs.get(0));

        config = new DtAgentConfig(null, null, null, null);
        furtherDtArgs = config.getFurtherDtArgs();
        assertEquals("Unexpected length of further DT argument list.", 0, furtherDtArgs.size());

        config = new DtAgentConfig(null, null, new String[0], null);
        furtherDtArgs = config.getFurtherDtArgs();
        assertEquals("Unexpected length of further DT argument list.", 0, furtherDtArgs.size());
    }

    @Test
    public void testParseEnvironmentArgs() {
    	DtVersionDetector.enforceInstallationType(InstallationType.Classic);
    	assertTrue(DtVersionDetector.isClassic());

        DtAgentConfig config;
        Map<String, String> envArgs;

        config = new DtAgentConfig(null, null, null, new String[] { "DT_SERVER=server", "DT_AGENTNAME=agentName", "DT_AGENTLIBRARY=./lib/dtagent.dll" });
        envArgs = config.getEnvironmentArgs();
		assertEquals("Unexpected length of further DT argument list.", 2, envArgs.size()); // DT_SERVER is used as server host
//        assertEquals("server", envArgs.get("DT_SERVER"));
        assertEquals("agentName", envArgs.get("DT_AGENTNAME"));
        assertEquals("./lib/dtagent.dll", envArgs.get("DT_AGENTLIBRARY"));

        config = new DtAgentConfig(null, null, null, new String[] { "DT_SERVER =server", "DT_AGENTNAME= agentName", "DT_AGENTLIBRARY = ./lib/dtagent.dll" });
        envArgs = config.getEnvironmentArgs();
		assertEquals("Unexpected length of further DT argument list.", 2, envArgs.size());
//        assertEquals("server", envArgs.get("DT_SERVER"));
        assertEquals("agentName", envArgs.get("DT_AGENTNAME"));
        assertEquals("./lib/dtagent.dll", envArgs.get("DT_AGENTLIBRARY"));

		config = new DtAgentConfig(null, null, null, new String[] { "DT_AGENTNAME= agentName" });
        envArgs = config.getEnvironmentArgs();
        assertEquals("Unexpected length of further DT argument list.", 1, envArgs.size());
		assertEquals("agentName", envArgs.get("DT_AGENTNAME"));

        config = new DtAgentConfig(null, null, null, new String[0]);
        envArgs = config.getEnvironmentArgs();
        assertEquals("Unexpected length of further DT argument list.", 0, envArgs.size());

        config = new DtAgentConfig(null, null, null, null);
        envArgs = config.getEnvironmentArgs();
        assertEquals("Unexpected length of further DT argument list.", 0, envArgs.size());

        // invalid arguments:
		config = new DtAgentConfig(null, null, null, new String[] { "DT_SERVER=", "DT_AGENTNAME=agentName",
				"DT_AGENTLIBRARY= " });
        envArgs = config.getEnvironmentArgs();
        assertEquals("Unexpected length of further DT argument list.", 2, envArgs.size());
		assertEquals("agentName", envArgs.get("DT_AGENTNAME"));
		assertEquals("", envArgs.get("DT_AGENTLIBRARY"));

        config = new DtAgentConfig(null, null, null, new String[] { "=server", "abc", "DT_AGENTNAME=agentname" });
        envArgs = config.getEnvironmentArgs();
        assertEquals("Unexpected length of further DT argument list.", 1, envArgs.size());
        assertEquals("agentname", envArgs.get("DT_AGENTNAME"));

        config = new DtAgentConfig(null, null, null, new String[] { "", "abc", "DT_AGENTNAME=agentname" });
        envArgs = config.getEnvironmentArgs();
        assertEquals("Unexpected length of further DT argument list.", 1, envArgs.size());
        assertEquals("agentname", envArgs.get("DT_AGENTNAME"));

    }

	@Test
	public void testDefaultServerAddress() throws ConfigurationException {
    	DtVersionDetector.enforceInstallationType(InstallationType.Classic);
    	assertTrue(DtVersionDetector.isClassic());

    	EasyTravelConfig config = EasyTravelConfig.read();
        config.dtServer = "defaultHost";
        config.dtServerPort = "1111";
		DtAgentConfig dtAgentConfig = new DtAgentConfig("agentName", Joiner.on(File.separator).join(
				TestEnvironment.TEST_DATA_PATH, "agent", "dtagent.dll"), new String[] {}, null);

		assertEquals("defaultHost", dtAgentConfig.getServer());
		assertEquals(1111, dtAgentConfig.getServerPort());
		assertTrue("Should have a server port because it was provided", dtAgentConfig.hasServerPort());
	}

	@Test
	public void testDefaultServerAddressWithoutPort() throws ConfigurationException {    	
    	EasyTravelConfig config = EasyTravelConfig.read();
    	EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
    	try {
        config.dtServer = "defaultHost";
        config.dtServerPort = "0";
		DtAgentConfig dtAgentConfig = new DtAgentConfig("agentName", Joiner.on(File.separator).join(
				TestEnvironment.TEST_DATA_PATH, "agent", "dtagent.dll"), new String[] {}, null);

		assertEquals("defaultHost", dtAgentConfig.getServer());
		assertEquals(0, dtAgentConfig.getServerPort());
		assertFalse("Should not have a server port because it was not provided", dtAgentConfig.hasServerPort());
    	} finally {
			EasyTravelConfig.resetSingleton();
			DtVersionDetector.enforceInstallationType(null);
		}
	}

	@Test
	public void testCustomServerAddress() throws ConfigurationException {
		DtAgentConfig dtAgentConfig = new DtAgentConfig("agentName", Joiner.on(File.separator).join(
				TestEnvironment.TEST_DATA_PATH, "agent", "dtagent.dll"), new String[] { "server=customHost:7777" }, null);

		assertEquals("customHost", dtAgentConfig.getServer());
		assertEquals(7777, dtAgentConfig.getServerPort());
		assertTrue("Should have a server port because it was provided", dtAgentConfig.hasServerPort());

		assertTrue("custom argument is used as server address and should be used twice",
				dtAgentConfig.getFurtherDtArgs().isEmpty());
	}

	@Test
	public void testCustomServerAddressWithoutPort() throws ConfigurationException {
		DtAgentConfig dtAgentConfig = new DtAgentConfig("agentName", Joiner.on(File.separator).join(
				TestEnvironment.TEST_DATA_PATH, "agent", "dtagent.dll"), new String[] { "server=customHost" }, null);

		assertEquals("customHost", dtAgentConfig.getServer());
		assertEquals(0, dtAgentConfig.getServerPort());
		assertFalse("Should not have a server port because it was not provided", dtAgentConfig.hasServerPort());

		assertTrue("custom argument is used as server address and should be used twice",
				dtAgentConfig.getFurtherDtArgs().isEmpty());
	}

	@Test
	public void testCustomNativeServerAddress() throws ConfigurationException {
		DtAgentConfig dtAgentConfig = new DtAgentConfig("agentName", Joiner.on(File.separator).join(
				TestEnvironment.TEST_DATA_PATH, "agent", "dtagent.dll"), null, new String[] { "DT_SERVER=customHost:7777" });

		assertEquals("customHost", dtAgentConfig.getServer());
		assertEquals(7777, dtAgentConfig.getServerPort());
		assertTrue("Should have a server port because it was provided", dtAgentConfig.hasServerPort());

		assertNull(dtAgentConfig.getEnvironmentArgs().get("DT_SERVER"));
	}

	@Test
	public void testCustomNativeServerAddressWithoutPort() throws ConfigurationException {
		DtAgentConfig dtAgentConfig = new DtAgentConfig("agentName", Joiner.on(File.separator).join(
				TestEnvironment.TEST_DATA_PATH, "agent", "dtagent.dll"), null, new String[] { "DT_SERVER=customHost" });

		assertEquals("customHost", dtAgentConfig.getServer());
		assertEquals(0, dtAgentConfig.getServerPort());
		assertFalse("Should not have a server port because it was not provided", dtAgentConfig.hasServerPort());

		assertNull(dtAgentConfig.getEnvironmentArgs().get("DT_SERVER"));
	}

	@Test
	public void testWithDifferentLoglevel() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					testConstructor();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, DtAgentConfig.class.getName(), Level.DEBUG);
	}

	@Test
	public void testAPMMode() throws Exception {
		DtVersionDetector.clearCache();

		// by default set it to something where we do not find a server even if one is running locally
		EasyTravelConfig.read().apmServerHost = "localhost";
		try {
	 		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "[{\"deploymentMetaInfoDto\":{\"id\":1,\"loadFactor\":1.0,\"osInfo\":\"Platform: Windows Server 2008 R2, Version: 6.1, Architecture: amd64\",\"jvmInfo\":\"VM: Java HotSpot(TM) 64-Bit Server VM, Version: 1.7.0_25, Vendor: Oracle Corporation, Max-memory: 2014M\",\"buildVersion\":\"0.4.0.20130801-063958\",\"uri\":\"SECRET\"},\"runtimeMetaInfoDto\":{\"operationState\":\"RUNNING\",\"startupTimestamp\":1375333070791}}]");
	 		EasyTravelConfig.read().apmServerWebPort = String.valueOf(server.getPort());
			try {
				assertEquals("0.4.0.20130801-063958", DtVersionDetector.determineDTVersion(null));
				for (Technology technology : Technology.values()) {
					assertNull(CONFIG_AUTO_DETECTED_EXISTING_AGENT.getAgentPath(technology));
				}
			} finally {
				server.stop();
			}
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testAPMServerPort() {
		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		try {
			EasyTravelConfig config = EasyTravelConfig.read();
			try {
				config.apmServerPort = "auto";
				DtAgentConfig agent = new DtAgentConfig("name", "somepath", null, new String[] {"arg1=valu1", null, ""});
				assertEquals(8020, agent.getServerPort());
			} finally {
				EasyTravelConfig.resetSingleton();
			}
		} finally {
			DtVersionDetector.enforceInstallationType(null);
		}
	}
}
