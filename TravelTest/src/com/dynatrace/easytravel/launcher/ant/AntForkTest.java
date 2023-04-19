package com.dynatrace.easytravel.launcher.ant;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.apache.commons.cli.ParseException;
import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.process.JavaProcess;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.TestEnvironment;



public class AntForkTest {

    private static File TEST_DIR = null;

    @BeforeClass
    public static void setUp() {
        TEST_DIR = TestEnvironment.createTestDirectory(AntForkTest.class);
    }

    @AfterClass
    public static void cleanUp() {
        TestEnvironment.deleteDirectory(TEST_DIR);
    }

    @Test
    public void testCreateAndParseCmdArguments() throws IOException, NumberFormatException, ParseException {
    	DtVersionDetector.enforceInstallationType(InstallationType.Classic);
        File moduleJar = new File(TEST_DIR, "module.jar");
        moduleJar.createNewFile();
        File agentDll = new File(TEST_DIR, "agent.dll");
        agentDll.createNewFile();
        File buildFile = new File(TEST_DIR, "build.xml");
        buildFile.createNewFile();

        DtAgentConfig agentConfig = new DtAgentConfig("agent", agentDll.getAbsolutePath(), null, null);

        JavaProcess javaProcess = new JavaProcess(moduleJar, agentConfig);
        javaProcess.setMainClass(AntFork.class.getCanonicalName());
     
        AntController oopController = EasyMock.createNiceMock(AntController.class);
        EasyMock.expect(oopController.getBuildFile()).andReturn(buildFile).anyTimes();
        EasyMock.expect(oopController.getBuildTarget()).andReturn("target").anyTimes();
        EasyMock.expect(oopController.getInstances()).andReturn(1).anyTimes();
        EasyMock.expect(oopController.getRecurrenceIntervalMs()).andReturn(2L).anyTimes();
        EasyMock.expect(oopController.getRecurrence()).andReturn(3).anyTimes();

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("key1", "val1");
        properties.put("wt.headless", "true");
        properties.put("~wt.htmlReports.skip", "false");
        properties.put("path", "C:\\\\any path\\");
        EasyMock.expect(oopController.getProperties()).andReturn(properties).anyTimes();
        EasyMock.replay(oopController);
        
        // simulate writing arguments to command line
        AntFork.addArguments(javaProcess, oopController);

        // the add argument method sets the config singleton;
        // in production code AntFork.read(..) is called within a new VM and it will set a new config singleton
        EasyTravelConfig.resetSingleton();

        // simulate reading and parsing arguments from command line
        InProcessAntController resultController = AntFork.read(extractArguments(javaProcess));
        Assert.assertEquals(buildFile.getAbsolutePath(), resultController.getBuildFile().getAbsolutePath());
        Assert.assertEquals("target", resultController.getBuildTarget());
        Assert.assertEquals(1, resultController.getInstances());
        Assert.assertEquals(2L, resultController.getRecurrenceIntervalMs());
        Assert.assertEquals(3, resultController.getRecurrence());
        Assert.assertEquals("val1", resultController.getProperties().get("key1"));
        Assert.assertEquals("true", resultController.getProperties().get("wt.headless"));
        Assert.assertEquals("false", resultController.getProperties().get("~wt.htmlReports.skip"));
        Assert.assertEquals("C:\\\\any path\\", resultController.getProperties().get("path"));
    }

    private String[] extractArguments(JavaProcess javaProcess) {
        String[] commandAndArgs = javaProcess.createCommand().toStrings();

        int skipCounter = 0;
        for (String part : commandAndArgs) {
            skipCounter++;
            if (part.equals(AntFork.class.getCanonicalName())) {
                break;
            }
        }

        return Arrays.copyOfRange(commandAndArgs, skipCounter, commandAndArgs.length);
    }

}
