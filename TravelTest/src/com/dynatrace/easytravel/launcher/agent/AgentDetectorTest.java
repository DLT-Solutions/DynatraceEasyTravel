package com.dynatrace.easytravel.launcher.agent;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;



public class AgentDetectorTest {

    private static final String TEMP_DIR_PROPERTY = "java.io.tmpdir";

    private static String tempDir = System.getProperty(TEMP_DIR_PROPERTY);
    private static final File TEST_DIR = new File(tempDir, AgentDetectorTest.class.getCanonicalName());

    // Program Files
    private static final File WIN_PF32_DIR = new File(TEST_DIR, "Program Files (x86)");
    private static final File WIN_PF64_DIR = new File(TEST_DIR, "Program Files");

    // various sub-dirs under Program Files
    private static final File DT_PF32_DIR = new File(WIN_PF32_DIR, "dynaTrace");
    private static final File CPWR_PF32_DIR = new File(WIN_PF32_DIR, "Compuware");
    private static final File DT_PF64_DIR = new File(WIN_PF64_DIR, "dynaTrace");
    private static final File CPWR_PF64_DIR = new File(WIN_PF64_DIR, "Compuware");
    private static final File DT_LINUX_DIR = new File(TEST_DIR, "canBeAnyName");
    private static final File DT_PF32_DIR_LOWERCASE = new File(WIN_PF32_DIR, "dynatrace");
    private static final File CPWR_PF32_DIR_LOWERCASE = new File(WIN_PF32_DIR, "compuware");

    // easyTravel location
    private static final File FILE_EASY_TRAVEL_32 = new File(DT_PF32_DIR, "easyTravel 1.0");
    private static final File FILE_EASY_TRAVEL_64 = new File(DT_PF64_DIR, "easyTravel 1.0");
    private static final File FILE_LINUX_EASY_TRAVEL = new File(DT_LINUX_DIR, File.separator + "easyTravel-1.0");

    // 3.2
    private static final File FILE_PF32_V32_DT64_LIB = new File(DT_PF32_DIR, "dynaTrace 3.2 x64" + File.separator + "agent" + File.separator + "lib");
    private static final File FILE_PF32_V32_DT64_LIB64 = new File(DT_PF32_DIR, "dynaTrace 3.2 x64" + File.separator + "agent" + File.separator + "lib64");
    private static File FILE_PF32_V32_DT64_LIB_JAVA, FILE_PF32_V32_DT64_LIB_DOTNET20, FILE_PF32_V32_DT64_LIB64_JAVA, FILE_PF32_V32_DT64_LIB64_DOTNET20;

    // 3.5.0
    private static File FILE_PF32_V350_DT_LIB, FILE_PF32_V350_DT_LIB64;
    private static File FILE_PF32_V350_DT_LIB_JAVA, FILE_PF32_V350_DT_LIB_DOTNET20, FILE_PF32_V350_DT_LIB64_JAVA, FILE_PF32_V350_DT_LIB64_DOTNET20;
    private static File FILE_LINUX_V350_DT_LIB, FILE_LINUX_V350_DT_LIB64, FILE_LINUX_V350_DT_LIB_JAVA_LINUX, FILE_LINUX_V350_DT_LIB64_JAVA_LINUX;

    // 3.5.1
    private static File FILE_PF32_V351_DT_LIB, FILE_PF32_V351_DT_LIB64;
    private static File FILE_PF32_V351_DT_LIB_JAVA, FILE_PF32_V351_DT_LIB_DOTNET20, FILE_PF32_V351_DT_LIB64_JAVA, FILE_PF32_V351_DT_LIB64_DOTNET20;

    // 4.0.0
    private static File /*FILE_PF32_V350_DTA_LIB,*/ FILE_PF32_V400_DTA_LIB, /*FILE_PF64_V350_DTA_LIB64,*/ FILE_PF64_V400_DTA_LIB64;
    private static File /*FILE_PF32_V350_DTA_LIB_JAVA,*/ FILE_PF32_V400_DTA_LIB_JAVA, /*FILE_PF64_V350_DTA_LIB64_JAVA,*/ FILE_PF64_V400_DTA_LIB64_JAVA;
    private static File /*FILE_EASY_TRAVEL_32_LOWERCASE,*/ FILE_PF32_V400_DTA_LIB_LOWERCASE, FILE_PF32_V400_DTA_LIB_LOWERCASE_JAVA;

    // 4.1.0
    private static File FILE_PF32_V410_DTA_LIB, FILE_PF64_V410_DTA_LIB64;
    private static File FILE_PF32_V410_DTA_LIB_JAVA, FILE_PF64_V410_DTA_LIB64_JAVA;
	private static File FILE_PF32_V410_DTA_LIB_LOWERCASE, FILE_PF32_V410_DTA_LIB_LOWERCASE_JAVA;

    // 5.5.0
    private static File FILE_PF32_V550_DTA_LIB, FILE_PF32_V550_DTA_LIB64, FILE_PF64_V550_DTA_LIB64;
    private static File FILE_PF32_V550_DTA_LIB_JAVA, FILE_PF64_V550_DTA_LIB64_JAVA;
    private static File FILE_PF32_V550_DTA_LIB_DOTNET20, FILE_PF64_V550_DTA_LIB64_DOTNET20;
	private static File FILE_PF32_V550_DTA_LIB_LOWERCASE, FILE_PF32_V550_DTA_LIB_LOWERCASE_JAVA;
	
	// 6.2
    private static File FILE_PF32_V62_DTA_LIB, FILE_PF64_V62_DTA_LIB64;
    private static File FILE_PF32_V62_DTA_LIB_JAVA, FILE_PF64_V62_DTA_LIB64_JAVA;
    
	// 6.3
    private static File FILE_PF32_V63_DTA_LIB, FILE_PF64_V63_DTA_LIB64;
    private static File FILE_PF32_V63_DTA_LIB_JAVA, FILE_PF64_V63_DTA_LIB64_JAVA;
    
    // 6.5
    private static File FILE_PF32_V65_DTA_LIB, FILE_PF64_V65_DTA_LIB64;
    private static File FILE_PF32_V65_DTA_LIB_JAVA, FILE_PF64_V65_DTA_LIB64_JAVA;
	
    @Before
    public void setUp() {
        deleteTempDirectories();

        FILE_PF32_V32_DT64_LIB_JAVA = new File(FILE_PF32_V32_DT64_LIB, "dtagent.dll");
        FILE_PF32_V32_DT64_LIB_DOTNET20 = new File(FILE_PF32_V32_DT64_LIB, "dtnetagent20.dll");
        FILE_PF32_V32_DT64_LIB64_JAVA = new File(FILE_PF32_V32_DT64_LIB64, "dtagent.dll");
        FILE_PF32_V32_DT64_LIB64_DOTNET20 = new File(FILE_PF32_V32_DT64_LIB64, "dtnetagent20.dll");


        FILE_PF32_V350_DT_LIB = new File(DT_PF32_DIR, "dynaTrace 3.5.0" + File.separator + "agent" + File.separator + "lib");
        FILE_PF32_V350_DT_LIB64 = new File(DT_PF32_DIR, "dynaTrace 3.5.0" + File.separator + "agent" + File.separator + "lib64");

        FILE_PF32_V350_DT_LIB_JAVA = new File(FILE_PF32_V350_DT_LIB, "dtagent.dll");
        FILE_PF32_V350_DT_LIB_DOTNET20 = new File(FILE_PF32_V350_DT_LIB, "dtnetagent20.dll");
        FILE_PF32_V350_DT_LIB64_JAVA = new File(FILE_PF32_V350_DT_LIB64, "dtagent.dll");
        FILE_PF32_V350_DT_LIB64_DOTNET20 = new File(FILE_PF32_V350_DT_LIB64, "dtnetagent20.dll");


        FILE_PF32_V351_DT_LIB = new File(DT_PF32_DIR, "dynaTrace 3.5.1" + File.separator + "agent" + File.separator + "lib");
        FILE_PF32_V351_DT_LIB64 = new File(DT_PF32_DIR, "dynaTrace 3.5.1" + File.separator + "agent" + File.separator + "lib64");

        FILE_PF32_V351_DT_LIB_JAVA = new File(FILE_PF32_V351_DT_LIB, "dtagent.dll");
        FILE_PF32_V351_DT_LIB_DOTNET20 = new File(FILE_PF32_V351_DT_LIB, "dtnetagent20.dll");
        FILE_PF32_V351_DT_LIB64_JAVA = new File(FILE_PF32_V351_DT_LIB64, "dtagent.dll");
        FILE_PF32_V351_DT_LIB64_DOTNET20 = new File(FILE_PF32_V351_DT_LIB64, "dtnetagent20.dll");


        //FILE_PF32_V350_DTA_LIB = new File(DT_PF32_DIR, "dynaTrace Agent 3.5.0" + File.separator + "agent" + File.separator + "lib");
        FILE_PF32_V400_DTA_LIB = new File(DT_PF32_DIR, "dynaTrace Agent 4.0.0" + File.separator + "agent" + File.separator + "lib");
        //FILE_PF64_V350_DTA_LIB64 = new File(DT_PF64_DIR, "dynaTrace Agent 3.5.0" + File.separator + "agent" + File.separator + "lib64");
        FILE_PF64_V400_DTA_LIB64 = new File(DT_PF64_DIR, "dynaTrace Agent 4.0.0" + File.separator + "agent" + File.separator + "lib64");

        //FILE_PF32_V350_DTA_LIB_JAVA = new File(FILE_PF32_V350_DTA_LIB, "dtagent.dll");
        FILE_PF32_V400_DTA_LIB_JAVA = new File(FILE_PF32_V400_DTA_LIB, "dtagent.dll");
        //FILE_PF64_V350_DTA_LIB64_JAVA = new File(FILE_PF64_V350_DTA_LIB64, "dtagent.dll");
        FILE_PF64_V400_DTA_LIB64_JAVA = new File(FILE_PF64_V400_DTA_LIB64, "dtagent.dll");


        //FILE_EASY_TRAVEL_32_LOWERCASE = new File(DT_PF32_DIR_LOWERCASE, "easyTravel 1.0");
        FILE_PF32_V400_DTA_LIB_LOWERCASE = new File(DT_PF32_DIR_LOWERCASE, "dynatrace agent 4.0.0" + File.separator + "agent" + File.separator + "lib");
        FILE_PF32_V400_DTA_LIB_LOWERCASE_JAVA = new File(FILE_PF32_V400_DTA_LIB_LOWERCASE, "dtagent.dll");


        FILE_LINUX_V350_DT_LIB = new File(DT_LINUX_DIR, "dynatrace-3.5.0" + File.separator + "agent" + File.separator + "lib");
        FILE_LINUX_V350_DT_LIB64 = new File(DT_LINUX_DIR, "dynatrace-3.5.0" + File.separator + "agent" + File.separator + "lib64");
        FILE_LINUX_V350_DT_LIB_JAVA_LINUX = new File(FILE_LINUX_V350_DT_LIB, "libdtagent.so");
        FILE_LINUX_V350_DT_LIB64_JAVA_LINUX = new File(FILE_LINUX_V350_DT_LIB64, "libdtagent.so");

        // 4.1.0
        FILE_PF32_V410_DTA_LIB = new File(DT_PF32_DIR, "dynaTrace Agent 4.1.0" + File.separator + "agent" + File.separator + "lib");
        FILE_PF64_V410_DTA_LIB64 = new File(DT_PF64_DIR, "dynaTrace Agent 4.1.0" + File.separator + "agent" + File.separator + "lib64");

        FILE_PF32_V410_DTA_LIB_JAVA = new File(FILE_PF32_V410_DTA_LIB, "dtagent.dll");
        FILE_PF64_V410_DTA_LIB64_JAVA = new File(FILE_PF64_V410_DTA_LIB64, "dtagent.dll");

        FILE_PF32_V410_DTA_LIB_LOWERCASE = new File(DT_PF32_DIR_LOWERCASE, "dynatrace agent 4.1.0" + File.separator + "agent" + File.separator + "lib");
        FILE_PF32_V410_DTA_LIB_LOWERCASE_JAVA = new File(FILE_PF32_V410_DTA_LIB_LOWERCASE, "dtagent.dll");

        // 5.5.0
        FILE_PF32_V550_DTA_LIB = new File(DT_PF32_DIR, "dynaTrace Agent 5.5.0" + File.separator + "agent" + File.separator + "lib");
        FILE_PF32_V550_DTA_LIB64 = new File(DT_PF32_DIR, "dynaTrace Agent 5.5.0" + File.separator + "agent" + File.separator + "lib64");
        FILE_PF64_V550_DTA_LIB64 = new File(DT_PF64_DIR, "dynaTrace Agent 5.5.0" + File.separator + "agent" + File.separator + "lib64");

        FILE_PF32_V550_DTA_LIB_JAVA = new File(FILE_PF32_V550_DTA_LIB, "dtagent.dll");
        FILE_PF64_V550_DTA_LIB64_JAVA = new File(FILE_PF64_V550_DTA_LIB64, "dtagent.dll");
        
        // 6.2
        FILE_PF32_V62_DTA_LIB = new File(DT_PF32_DIR, "dynaTrace Agent 6.2" + File.separator + "agent" + File.separator + "lib");
        FILE_PF64_V62_DTA_LIB64 = new File(DT_PF64_DIR, "dynaTrace Agent 6.2" + File.separator + "agent" + File.separator + "lib64");

        FILE_PF32_V62_DTA_LIB_JAVA = new File(FILE_PF32_V62_DTA_LIB, "dtagent.dll");
        FILE_PF64_V62_DTA_LIB64_JAVA = new File(FILE_PF64_V62_DTA_LIB64, "dtagent.dll");
        
        // 6.3
        FILE_PF32_V63_DTA_LIB = new File(DT_PF32_DIR, "dynaTrace Agent 6.3" + File.separator + "agent" + File.separator + "lib");
        FILE_PF64_V63_DTA_LIB64 = new File(DT_PF64_DIR, "dynaTrace Agent 6.3" + File.separator + "agent" + File.separator + "lib64");

        FILE_PF32_V63_DTA_LIB_JAVA = new File(FILE_PF32_V63_DTA_LIB, "dtagent.dll");
        FILE_PF64_V63_DTA_LIB64_JAVA = new File(FILE_PF64_V63_DTA_LIB64, "dtagent.dll");
        
        // 6.5
        FILE_PF32_V65_DTA_LIB = new File(DT_PF32_DIR, "dynaTrace Agent 6.5" + File.separator + "agent" + File.separator + "lib");
        FILE_PF64_V65_DTA_LIB64 = new File(DT_PF64_DIR, "dynaTrace Agent 6.5" + File.separator + "agent" + File.separator + "lib64");

        FILE_PF32_V65_DTA_LIB_JAVA = new File(FILE_PF32_V65_DTA_LIB, "dtagent.dll");
        FILE_PF64_V65_DTA_LIB64_JAVA = new File(FILE_PF64_V65_DTA_LIB64, "dtagent.dll");

        //FILE_EASY_TRAVEL_32_LOWERCASE = new File(DT_PF32_DIR_LOWERCASE, "easyTravel 1.0");
        FILE_PF32_V550_DTA_LIB_LOWERCASE = new File(DT_PF32_DIR_LOWERCASE, "dynatrace agent 5.5.0" + File.separator + "agent" + File.separator + "lib");
        FILE_PF32_V550_DTA_LIB_LOWERCASE_JAVA = new File(FILE_PF32_V550_DTA_LIB_LOWERCASE, "dtagent.dll");

        FILE_PF32_V550_DTA_LIB_DOTNET20 = new File(FILE_PF32_V550_DTA_LIB, "dtnetagent20.dll");
        FILE_PF64_V550_DTA_LIB64_DOTNET20 = new File(FILE_PF64_V550_DTA_LIB64, "dtnetagent20.dll");
    }

    @After
    public void cleanUp() {
        deleteTempDirectories();
    }

    private static void deleteTempDirectories() {
        if (TEST_DIR != null && TEST_DIR.exists()) {
            TestEnvironment.deleteDirectory(TEST_DIR);
        }
    }

    @Test
    public void testDetection_Win_singleDtInstallations() throws IOException {
        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        AgentDetector detectorWin64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.WINDOWS);

        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);
        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_32);

        List<File> detectDtInstallDirs = detectorWin32bit.detectDtInstallDirs();
        assertTrue(detectDtInstallDirs.isEmpty());

        FILE_PF32_V32_DT64_LIB.mkdirs();
        FILE_PF32_V32_DT64_LIB64.mkdirs();

        detectDtInstallDirs = detectorWin32bit.detectDtInstallDirs();
        assertEquals("Exactly one directory expected", 1, detectDtInstallDirs.size());

        // agent does not exist yet
        assertNull(detectorWin32bit.getAgent(Technology.JAVA));
        assertNull(detectorWin64bit.getAgent(Technology.DOTNET_20));

        createFile(FILE_PF32_V32_DT64_LIB_JAVA);
        createFile(FILE_PF32_V32_DT64_LIB_DOTNET20);
        createFile(FILE_PF32_V32_DT64_LIB64_JAVA);
        createFile(FILE_PF32_V32_DT64_LIB64_DOTNET20);

        assertEquals(FILE_PF32_V32_DT64_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        assertEquals(FILE_PF32_V32_DT64_LIB_DOTNET20, detectorWin32bit.getAgent(Technology.DOTNET_20));
        assertEquals(FILE_PF32_V32_DT64_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        assertEquals(FILE_PF32_V32_DT64_LIB64_DOTNET20, detectorWin64bit.getAgent(Technology.DOTNET_20));
    }

    @Test
    public void testDetection_Win_singleDtInstallations_550() throws IOException {
        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        AgentDetector detectorWin64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.WINDOWS);

        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);
        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_32);

        List<File> detectDtInstallDirs = detectorWin32bit.detectDtInstallDirs();
        assertTrue(detectDtInstallDirs.isEmpty());

        FILE_PF32_V550_DTA_LIB.mkdirs();
        FILE_PF32_V550_DTA_LIB64.mkdirs();
        CPWR_PF32_DIR.mkdirs();
        CPWR_PF32_DIR_LOWERCASE.mkdirs();
        CPWR_PF64_DIR.mkdirs();


        detectDtInstallDirs = detectorWin32bit.detectDtInstallDirs();
        assertEquals("Exactly one directory expected, but had: " + detectDtInstallDirs, 1, detectDtInstallDirs.size());

        // agent does not exist yet
        assertNull(detectorWin32bit.getAgent(Technology.JAVA));
        assertNull(detectorWin64bit.getAgent(Technology.DOTNET_20));

        createFile(FILE_PF32_V550_DTA_LIB_JAVA);
        createFile(FILE_PF32_V550_DTA_LIB_DOTNET20);

        assertEquals(FILE_PF32_V550_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        assertEquals(FILE_PF32_V550_DTA_LIB_DOTNET20, detectorWin32bit.getAgent(Technology.DOTNET_20));
        /*assertEquals(FILE_PF64_V550_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        assertEquals(FILE_PF64_V550_DTA_LIB64_DOTNET20, detectorWin64bit.getAgent(Technology.DOTNET_20));*/
    }

    @Test
    public void testDetection_Win_singleDtInstallations_550_64bit() throws IOException {
        AgentDetector detectorWin64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.WINDOWS);

        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_64);

        List<File> detectDtInstallDirs = detectorWin64bit.detectDtInstallDirs();
        assertTrue(detectDtInstallDirs.isEmpty());

        FILE_PF64_V550_DTA_LIB64.mkdirs();
        CPWR_PF32_DIR.mkdirs();
        CPWR_PF32_DIR_LOWERCASE.mkdirs();
        CPWR_PF64_DIR.mkdirs();

        detectDtInstallDirs = detectorWin64bit.detectDtInstallDirs();
        assertEquals("Exactly one directory expected, but had: " + detectDtInstallDirs, 1, detectDtInstallDirs.size());

        // agent does not exist yet
        assertNull(detectorWin64bit.getAgent(Technology.JAVA));
        assertNull(detectorWin64bit.getAgent(Technology.DOTNET_20));

        createFile(FILE_PF64_V550_DTA_LIB64_JAVA);
        createFile(FILE_PF64_V550_DTA_LIB64_DOTNET20);

        assertEquals(FILE_PF64_V550_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        assertEquals(FILE_PF64_V550_DTA_LIB64_DOTNET20, detectorWin64bit.getAgent(Technology.DOTNET_20));
    }

    @Test
    public void testDetection_Win_multipleDtInstallations() throws IOException {
        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        AgentDetector detectorWin64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.WINDOWS);
        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);
        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_32);

        // test with 2 installations (dynaTrace 3.2, 3.5.0)
        createFile(FILE_PF32_V32_DT64_LIB_JAVA);
        createFile(FILE_PF32_V32_DT64_LIB_DOTNET20);
        createFile(FILE_PF32_V32_DT64_LIB64_JAVA);
        createFile(FILE_PF32_V32_DT64_LIB64_DOTNET20);

        createFile(FILE_PF32_V350_DT_LIB_JAVA);
        createFile(FILE_PF32_V350_DT_LIB_DOTNET20);
        createFile(FILE_PF32_V350_DT_LIB64_JAVA);
        createFile(FILE_PF32_V350_DT_LIB64_DOTNET20);

        // the agent of the latest installation is expected
        assertEquals(FILE_PF32_V350_DT_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        assertEquals(FILE_PF32_V350_DT_LIB_DOTNET20, detectorWin32bit.getAgent(Technology.DOTNET_20));
        assertEquals(FILE_PF32_V350_DT_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        assertEquals(FILE_PF32_V350_DT_LIB64_DOTNET20, detectorWin64bit.getAgent(Technology.DOTNET_20));

        // test with 3 installations (dynaTrace 3.2, 3.5.0, 3.5.1)
        createFile(FILE_PF32_V351_DT_LIB_JAVA);
        createFile(FILE_PF32_V351_DT_LIB_DOTNET20);
        createFile(FILE_PF32_V351_DT_LIB64_JAVA);
        createFile(FILE_PF32_V351_DT_LIB64_DOTNET20);

        assertEquals(FILE_PF32_V351_DT_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        assertEquals(FILE_PF32_V351_DT_LIB_DOTNET20, detectorWin32bit.getAgent(Technology.DOTNET_20));
        assertEquals(FILE_PF32_V351_DT_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        assertEquals(FILE_PF32_V351_DT_LIB64_DOTNET20, detectorWin64bit.getAgent(Technology.DOTNET_20));
    }

    @Test
    public void testDetection_CaseInsensitivity() throws IOException {
        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);

        createFile(FILE_PF32_V400_DTA_LIB_LOWERCASE_JAVA);

        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
            List<File> dirs = detectorWin32bit.detectDtInstallDirs();
            assertNotNull(dirs);
            assertEquals("[" + FILE_PF32_V400_DTA_LIB_LOWERCASE.getParentFile().getParent().toLowerCase() + "]", dirs.toString().toLowerCase());

        	assertEquals(FILE_PF32_V400_DTA_LIB_LOWERCASE_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        } else {
        	assertNull("Expected null but had: " + detectorWin32bit.getAgent(Technology.JAVA),
        			detectorWin32bit.getAgent(Technology.JAVA));
        }
    }

    @Test
    public void testDetection_CaseInsensitivity_410() throws IOException {
        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);

        createFile(FILE_PF32_V410_DTA_LIB_LOWERCASE_JAVA);

        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
            List<File> dirs = detectorWin32bit.detectDtInstallDirs();
            assertNotNull(dirs);
            assertEquals("[" + FILE_PF32_V410_DTA_LIB_LOWERCASE.getParentFile().getParent().toLowerCase() + "]", dirs.toString().toLowerCase());

        	assertEquals(FILE_PF32_V410_DTA_LIB_LOWERCASE_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        } else {
        	assertNull("Expected null but had: " + detectorWin32bit.getAgent(Technology.JAVA),
        			detectorWin32bit.getAgent(Technology.JAVA));
        }
    }

    @Test
    public void testDetection_CaseInsensitivity_550() throws IOException {
        CPWR_PF32_DIR.mkdirs();
        CPWR_PF32_DIR_LOWERCASE.mkdirs();
        CPWR_PF64_DIR.mkdirs();

        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);

        createFile(FILE_PF32_V550_DTA_LIB_LOWERCASE_JAVA);

        // ensure that this directory actually exists
        createFile(new File(FILE_EASY_TRAVEL_32, "test"));

        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
            List<File> dirs = detectorWin32bit.detectDtInstallDirs();
            assertNotNull(dirs);
            assertEquals("[" + FILE_PF32_V550_DTA_LIB_LOWERCASE.getParentFile().getParent().toLowerCase() + "]", dirs.toString().toLowerCase());

        	assertEquals(FILE_PF32_V550_DTA_LIB_LOWERCASE_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        } else {
        	assertNull("Expected null but had: " + detectorWin32bit.getAgent(Technology.JAVA),
        			detectorWin32bit.getAgent(Technology.JAVA));
        }
    }

    @Test
    public void testDetection_Win_multipleAgentInstallations() throws IOException {
        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        AgentDetector detectorWin64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.WINDOWS);

        // test dynaTrace 3.5.0 32bit, dynaTrace Agent 4.0.0 32bit and dynaTrace Agent 4.0.0 64bit
        createFile(FILE_PF32_V350_DT_LIB_JAVA);
        createFile(FILE_PF32_V350_DT_LIB64_JAVA);
        createFile(FILE_PF32_V400_DTA_LIB_JAVA);
        createFile(FILE_PF64_V400_DTA_LIB64_JAVA);

        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);
        assertEquals(FILE_PF32_V400_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_64);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF32_V400_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        } else {
        	assertNull("Expected null, but had: " + detectorWin32bit.getAgent(Technology.JAVA),
        			detectorWin32bit.getAgent(Technology.JAVA));
        }

        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_32);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF64_V400_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        } else {
        	assertEquals(FILE_PF32_V350_DT_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        }
        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_64);
        assertEquals(FILE_PF64_V400_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
    }

    @Test
    public void testDetection_Win_multipleAgentInstallations_410() throws IOException {
        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        AgentDetector detectorWin64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.WINDOWS);

        // test dynaTrace 3.5.0 32bit, dynaTrace Agent 4.0.0 32bit and dynaTrace Agent 4.0.0 64bit
        createFile(FILE_PF32_V350_DT_LIB_JAVA);
        createFile(FILE_PF32_V350_DT_LIB64_JAVA);
        createFile(FILE_PF32_V400_DTA_LIB_JAVA);
        createFile(FILE_PF64_V400_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V410_DTA_LIB_JAVA);
        createFile(FILE_PF64_V410_DTA_LIB64_JAVA);

        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);
        assertEquals(FILE_PF32_V410_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_64);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF32_V410_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        } else {
        	assertNull("Expected null, but had: " + detectorWin32bit.getAgent(Technology.JAVA),
        			detectorWin32bit.getAgent(Technology.JAVA));
        }

        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_32);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF64_V410_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        } else {
        	assertEquals(FILE_PF32_V350_DT_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        }
        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_64);
        assertEquals(FILE_PF64_V410_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
    }

    @Test
    public void testDetection_Win_multipleAgentInstallations_550() throws IOException {
        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        AgentDetector detectorWin64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.WINDOWS);

        // test some older agents and 5.5 under compuware
        createFile(FILE_PF32_V350_DT_LIB_JAVA);
        createFile(FILE_PF32_V350_DT_LIB64_JAVA);
        createFile(FILE_PF32_V400_DTA_LIB_JAVA);
        createFile(FILE_PF64_V400_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V410_DTA_LIB_JAVA);
        createFile(FILE_PF64_V410_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V550_DTA_LIB_JAVA);
        createFile(FILE_PF64_V550_DTA_LIB64_JAVA);
        
        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);
        assertEquals(FILE_PF32_V550_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));

        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_64);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF32_V550_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        } else {
        	assertNull("Expected null, but had: " + detectorWin32bit.getAgent(Technology.JAVA),
        			detectorWin32bit.getAgent(Technology.JAVA));
        }

        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_32);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF64_V550_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        } else {
        	assertEquals(FILE_PF32_V350_DT_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        }
        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_64);
        assertEquals(FILE_PF64_V550_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
    }
    
    @Test
    public void testDetection_Win_multipleAgentInstallations_62() throws IOException {
        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        AgentDetector detectorWin64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.WINDOWS);

        // test some older agents and 5.5 under compuware
        createFile(FILE_PF32_V350_DT_LIB_JAVA);
        createFile(FILE_PF32_V350_DT_LIB64_JAVA);
        createFile(FILE_PF32_V400_DTA_LIB_JAVA);
        createFile(FILE_PF64_V400_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V410_DTA_LIB_JAVA);
        createFile(FILE_PF64_V410_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V550_DTA_LIB_JAVA);
        createFile(FILE_PF64_V550_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V62_DTA_LIB_JAVA);
        createFile(FILE_PF64_V62_DTA_LIB64_JAVA);

        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);
        assertEquals(FILE_PF32_V62_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));

        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_64);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF32_V62_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        } else {
        	assertNull("Expected null, but had: " + detectorWin32bit.getAgent(Technology.JAVA),
        			detectorWin32bit.getAgent(Technology.JAVA));
        }

        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_32);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF64_V62_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        } else {
        	assertEquals(FILE_PF32_V350_DT_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        }
        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_64);
        assertEquals(FILE_PF64_V62_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
    }
    
    @Test
    public void testDetection_Win_multipleAgentInstallations_63() throws IOException {
        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        AgentDetector detectorWin64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.WINDOWS);

        // test some older agents and 5.5 under compuware
        createFile(FILE_PF32_V350_DT_LIB_JAVA);
        createFile(FILE_PF32_V350_DT_LIB64_JAVA);
        createFile(FILE_PF32_V400_DTA_LIB_JAVA);
        createFile(FILE_PF64_V400_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V410_DTA_LIB_JAVA);
        createFile(FILE_PF64_V410_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V550_DTA_LIB_JAVA);
        createFile(FILE_PF64_V550_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V62_DTA_LIB_JAVA);
        createFile(FILE_PF64_V62_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V63_DTA_LIB_JAVA);
        createFile(FILE_PF64_V63_DTA_LIB64_JAVA);

        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);
        assertEquals(FILE_PF32_V63_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));

        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_64);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF32_V63_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        } else {
        	assertNull("Expected null, but had: " + detectorWin32bit.getAgent(Technology.JAVA),
        			detectorWin32bit.getAgent(Technology.JAVA));
        }

        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_32);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF64_V63_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        } else {
        	assertEquals(FILE_PF32_V350_DT_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        }
        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_64);
        assertEquals(FILE_PF64_V63_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
    }
    
    @Test
    public void testDetection_Win_multipleAgentInstallations_65() throws IOException {
        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        AgentDetector detectorWin64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.WINDOWS);

        // test some older agents and 5.5 under compuware
        createFile(FILE_PF32_V350_DT_LIB_JAVA);
        createFile(FILE_PF32_V350_DT_LIB64_JAVA);
        createFile(FILE_PF32_V400_DTA_LIB_JAVA);
        createFile(FILE_PF64_V400_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V410_DTA_LIB_JAVA);
        createFile(FILE_PF64_V410_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V550_DTA_LIB_JAVA);
        createFile(FILE_PF64_V550_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V62_DTA_LIB_JAVA);
        createFile(FILE_PF64_V62_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V63_DTA_LIB_JAVA);
        createFile(FILE_PF64_V63_DTA_LIB64_JAVA);
        createFile(FILE_PF32_V65_DTA_LIB_JAVA);
        createFile(FILE_PF64_V65_DTA_LIB64_JAVA);

        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);
        assertEquals(FILE_PF32_V65_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));

        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_64);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF32_V65_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        } else {
        	assertNull("Expected null, but had: " + detectorWin32bit.getAgent(Technology.JAVA),
        			detectorWin32bit.getAgent(Technology.JAVA));
        }

        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_32);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF64_V65_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        } else {
        	assertEquals(FILE_PF32_V350_DT_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        }
        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_64);
        assertEquals(FILE_PF64_V65_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
    }

    @Ignore("Not done yet") // TODO: finish this test!
    @Test
    public void testDetection_Win_multipleAgentInstallations_550_mixed_64_32() throws IOException {
        CPWR_PF32_DIR.mkdirs();
        CPWR_PF32_DIR_LOWERCASE.mkdirs();
        CPWR_PF64_DIR.mkdirs();

        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        AgentDetector detectorWin64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.WINDOWS);

        // having 32-bit easyTravel, but 64-bit dynaTrace 5.5.0
        createFile(FILE_PF32_V410_DTA_LIB_JAVA);
        createFile(FILE_PF64_V410_DTA_LIB64_JAVA);
        createFile(FILE_PF64_V550_DTA_LIB64_JAVA);

        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);
        assertEquals(FILE_PF32_V550_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));

        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_64);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF32_V550_DTA_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));
        } else {
        	assertNull("Expected null, but had: " + detectorWin32bit.getAgent(Technology.JAVA),
        			detectorWin32bit.getAgent(Technology.JAVA));
        }

        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_32);
        // only Windows uses "Program Files" and "Program Files (x86)"
        if(OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
        	assertEquals(FILE_PF64_V550_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        } else {
        	assertEquals(FILE_PF32_V350_DT_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
        }
        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_64);
        assertEquals(FILE_PF64_V550_DTA_LIB64_JAVA, detectorWin64bit.getAgent(Technology.JAVA));
    }

    @Test
    public void testDetection_Linux() throws IOException {
        AgentDetector detectorLinux32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.LINUX);
        AgentDetector detectorLinux64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.LINUX);
        detectorLinux32bit.setInstallDir(FILE_LINUX_EASY_TRAVEL);
        detectorLinux64bit.setInstallDir(FILE_LINUX_EASY_TRAVEL);

        createFile(FILE_LINUX_V350_DT_LIB_JAVA_LINUX);
        createFile(FILE_LINUX_V350_DT_LIB64_JAVA_LINUX);

        assertEquals(FILE_LINUX_V350_DT_LIB_JAVA_LINUX, detectorLinux32bit.getAgent(Technology.JAVA));
        assertEquals(FILE_LINUX_V350_DT_LIB64_JAVA_LINUX, detectorLinux64bit.getAgent(Technology.JAVA));
    }

    @Test
    public void testDetection_MacOS() throws IOException {
        AgentDetector detectorLinux32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.MAC_OS);
        AgentDetector detectorLinux64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.MAC_OS);
        detectorLinux32bit.setInstallDir(FILE_LINUX_EASY_TRAVEL);
        detectorLinux64bit.setInstallDir(FILE_LINUX_EASY_TRAVEL);

        createFile(FILE_LINUX_V350_DT_LIB_JAVA_LINUX);
        createFile(FILE_LINUX_V350_DT_LIB64_JAVA_LINUX);

        assertEquals(FILE_LINUX_V350_DT_LIB_JAVA_LINUX, detectorLinux32bit.getAgent(Technology.JAVA));
        assertEquals(FILE_LINUX_V350_DT_LIB64_JAVA_LINUX, detectorLinux64bit.getAgent(Technology.JAVA));
    }

    @Test
    public void testDetection_DtProgramsDirAsInstallDir() throws IOException {
        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
        detectorWin32bit.setInstallDir(DT_PF32_DIR);

        createFile(FILE_PF32_V32_DT64_LIB_JAVA);
        assertEquals(FILE_PF32_V32_DT64_LIB_JAVA, detectorWin32bit.getAgent(Technology.JAVA));

    }

    @Test
    public void testAgentDetectorUnknownArchitecture() throws IOException {
    	String prev = System.getProperty(BaseConstants.SystemProperties.OS_ARCH);
    	System.setProperty(BaseConstants.SystemProperties.OS_ARCH, "");
    	try {
    		assertEquals(Architecture.UNKNOWN, Architecture.pickUp());

            new AgentDetector();
    	} finally {
    		System.setProperty(BaseConstants.SystemProperties.OS_ARCH, prev);
    	}
    }

    @Test
    public void testAgentDetectorUnsupportedOperatingSystem() throws IOException {
    	String prev = System.getProperty(BaseConstants.SystemProperties.OS_NAME);
    	System.setProperty(BaseConstants.SystemProperties.OS_NAME, "aix");
    	try {
    		assertFalse(OperatingSystem.pickUp().isSupported());
    		assertFalse(OperatingSystem.isCurrent(OperatingSystem.FREE_BSD));
    		assertEquals("AIX", OperatingSystem.pickUp().getName());

            new AgentDetector();
            fail("Should catch exception here because operating system is not supported....");
    	} catch (UnsupportedOperationException e) {
    		TestHelpers.assertContains(e, "is not supported", "AIX");
    	} finally {
    		System.setProperty(BaseConstants.SystemProperties.OS_NAME, prev);
    	}
    }

    @Test
    public void testAgentDetectorUnknownOperatingSystem() throws IOException {
    	String prev = System.getProperty(BaseConstants.SystemProperties.OS_NAME);
    	System.setProperty(BaseConstants.SystemProperties.OS_NAME, "someother");
    	try {
    		assertFalse(OperatingSystem.pickUp().isSupported());
    		assertFalse(OperatingSystem.isCurrent(OperatingSystem.FREE_BSD));
    		assertEquals("unknown", OperatingSystem.pickUp().getName());

            new AgentDetector();
            fail("Should catch exception here because operating system is unknown....");
    	} catch (UnsupportedOperationException e) {
    		TestHelpers.assertContains(e, "is not supported", "unknown");
    	} finally {
    		System.setProperty(BaseConstants.SystemProperties.OS_NAME, prev);
    	}
    }

    private static final Comparator<VersionedDir> DESC_COMPARATOR = new Comparator<VersionedDir>() {
        @Override
        public int compare(VersionedDir versionDirA, VersionedDir versionDirB) {
            return -versionDirA.compareTo(versionDirB);
        }
    };

    @Ignore("This was just for local testing, it fails on the isDirector() check in VersionedDirs()")
    @Test
    public void testLinuxDirs() {
        List<VersionedDir> sortingDirs = new ArrayList<VersionedDir>();
        for (File dtInstallDir : new File[] { new File("/opt/dynatrace/dynatrace-3.5.1"), new File("/opt/dynatrace/dynatrace-4.0.0") }) {
            sortingDirs.add(new VersionedDir(dtInstallDir)); // NOPMD
        }

        // sorts the install directories in descending order
        Collections.sort(sortingDirs, DESC_COMPARATOR);

        // now 4.0.0 must be first
        assertEquals(4, sortingDirs.get(0).getVersion().getMajor());
    }

    @Ignore("This was just for local testing, it fails on the isDirector() check in VersionedDirs()")
    @Test
    public void testLinuxDirs2() {
        List<VersionedDir> sortingDirs = new ArrayList<VersionedDir>();
        for (File dtInstallDir : new File[] { new File("/opt/dynatrace/dynatrace-4.0.0"), new File("/opt/dynatrace/dynatrace-3.5.1") }) {
            sortingDirs.add(new VersionedDir(dtInstallDir)); // NOPMD
        }

        // sorts the install directories in descending order
        Collections.sort(sortingDirs, DESC_COMPARATOR);

        // now 4.0.0 must be first
        assertEquals(4, sortingDirs.get(0).getVersion().getMajor());
    }

    private boolean createFile(File file) throws IOException {
        return file.getParentFile().mkdirs() & file.createNewFile();
    }

    @Test
    public void testGetAgentLookupDirSetInvalid() {
    	System.setProperty(BaseConstants.SystemProperties.AGENT_LOOKUP_DIR, "somefile");

    	try {
	        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
	        AgentDetector detectorWin64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.WINDOWS);

	        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);
	        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_32);

	        List<File> detectDtInstallDirs = detectorWin32bit.detectDtInstallDirs();
	        assertTrue(detectDtInstallDirs.isEmpty());

	        FILE_PF32_V32_DT64_LIB.mkdirs();
	        FILE_PF32_V32_DT64_LIB64.mkdirs();

	        detectDtInstallDirs = detectorWin32bit.detectDtInstallDirs();
	        assertEquals("Expect one entry because we set property to an invalid dir, but had: " + detectDtInstallDirs,
	        		1, detectDtInstallDirs.size());
    	} finally {
    		System.clearProperty(BaseConstants.SystemProperties.AGENT_LOOKUP_DIR);
    	}
    }

    @Test
    public void testGetAgentLookupDirSet() {
    	File newLocation = new File(DT_PF32_DIR, "dynaTrace 9.9.9");
    	newLocation.mkdirs();
    	System.setProperty(BaseConstants.SystemProperties.AGENT_LOOKUP_DIR, newLocation.getAbsolutePath());
    	try {
	        AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32, OperatingSystem.WINDOWS);
	        AgentDetector detectorWin64bit = new AgentDetector(Architecture.BIT64, OperatingSystem.WINDOWS);

	        detectorWin32bit.setInstallDir(FILE_EASY_TRAVEL_32);
	        detectorWin64bit.setInstallDir(FILE_EASY_TRAVEL_32);

	        List<File> detectDtInstallDirs = detectorWin32bit.detectDtInstallDirs();
	        assertEquals("One director expected, but had: " + detectDtInstallDirs,
	        		1, detectDtInstallDirs.size());
	        assertTrue("Directory 'lib123' expected, but had: " + detectDtInstallDirs,
	        		detectDtInstallDirs.get(0).getAbsolutePath().contains("9.9.9"));
    	} finally {
    		System.clearProperty(BaseConstants.SystemProperties.AGENT_LOOKUP_DIR);
    	}
    }

    @Test
    public void testCoverage() {
    	AgentDetector detectorWin32bit = new AgentDetector(Architecture.BIT32);
    	assertNotNull(detectorWin32bit);
    }
}
