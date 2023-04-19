package com.dynatrace.easytravel.launcher.agent;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;

public class VersionedDirTest {

    private static final String TEMP_DIR_PROPERTY = "java.io.tmpdir";

    private static File TEST_DIR = null;
    private static File DT_PROG_DIR = null;

    @BeforeClass
    public static void setUp() {
        String tempDir = System.getProperty(TEMP_DIR_PROPERTY);
        TEST_DIR = new File(tempDir, VersionedDirTest.class.getCanonicalName());
        DT_PROG_DIR = new File(TEST_DIR, "dynaTrace");
    }

    @AfterClass
    public static void cleanUp() {
        if (TEST_DIR != null && TEST_DIR.exists()) {
            TestEnvironment.deleteDirectory(TEST_DIR);
        }
    }

    @Test
    public void testConstructor() {
        File originalDir = new File(DT_PROG_DIR, "dynaTrace 3.5.1");
        originalDir.mkdirs();

        VersionedDir versionedDir = new VersionedDir(originalDir, ".");
        assertEquals("3.5.1", versionedDir.getVersion().toString());
        assertEquals(originalDir.getAbsolutePath(), versionedDir.getDirectory().getAbsolutePath());

        versionedDir = new VersionedDir(originalDir);
        assertEquals("3.5.1", versionedDir.getVersion().toString());

        try {
            new VersionedDir(null);
            fail("Null argument not allowed.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new VersionedDir(new File(DT_PROG_DIR, "not existing dir"));
            fail("Directory not existing.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testDetectVersion() {
        assertEquals("3.5.1", VersionedDir.detectVersion(new File(DT_PROG_DIR, "dynaTrace 3.5.1"), ".").toString());
        assertEquals("3.5", VersionedDir.detectVersion(new File(DT_PROG_DIR, "dynaTrace 3.5"), ".").toString());
        assertEquals("3", VersionedDir.detectVersion(new File(DT_PROG_DIR, "dynaTrace 3"), ".").toString());
        assertEquals("13,0,4", VersionedDir.detectVersion(new File(DT_PROG_DIR, "dynaTrace 13,0,4"), ",").toString());

        try {
            VersionedDir.detectVersion(new File(DT_PROG_DIR, "dynaTrace 13,0,4"), ".");
            fail("\"13,0,4\" must not be parsable with separator \".\"");
        } catch (IllegalArgumentException iae) {
        }

        assertEquals("3.2", VersionedDir.detectVersion(new File(DT_PROG_DIR, "dynaTrace 3.2 x64"), ".").toString());
        assertEquals("3.2.1", VersionedDir.detectVersion(new File(DT_PROG_DIR, "dynaTrace 3.2.1 "), ".").toString());
        assertEquals("3.2.1", VersionedDir.detectVersion(new File(DT_PROG_DIR, "dynaTrace 3.2.1 abc def ghi"), ".").toString());

        try {
            VersionedDir.detectVersion(new File(DT_PROG_DIR, "noDynaTrace 3.5.1"), ".");
            fail("The name of the version based directory must start with \"dynaTrace\".");
        } catch (IllegalArgumentException iae) {
        }

        try {
            VersionedDir.detectVersion(new File(DT_PROG_DIR, "dynaTrace"), ".");
            fail("The name of the version based directory must contain a version.");
        } catch (IllegalArgumentException iae) {
        }
    }

    @Test
    public void testCompareTo() {
        File originalDir351 = new File(DT_PROG_DIR, "dynaTrace 3.5.1");
        originalDir351.mkdirs();

        VersionedDir versionedDir351 = new VersionedDir(originalDir351);
        assertEquals(0, versionedDir351.compareTo(new VersionedDir(originalDir351)));

        File versionedDir350 = new File(DT_PROG_DIR, "dynaTrace 3.5.0");
        versionedDir350.mkdirs();
        assertTrue(versionedDir351.compareTo(new VersionedDir(versionedDir350)) > 0);

        File versionedDir352 = new File(DT_PROG_DIR, "dynaTrace 3.5.2");
        versionedDir352.mkdirs();
        assertTrue(versionedDir351.compareTo(new VersionedDir(versionedDir352)) < 0);

        File versionedDir400 = new File(DT_PROG_DIR, "dynaTrace 4.0.0");
        versionedDir400.mkdirs();
        assertTrue(versionedDir351.compareTo(new VersionedDir(versionedDir400)) < 0);

        File versionedDirAgent400 = new File(DT_PROG_DIR, "dynaTrace Agent 4.0.0");
        versionedDirAgent400.mkdirs();
        assertTrue(versionedDir351.compareTo(new VersionedDir(versionedDirAgent400)) < 0);

        File versionedDir410 = new File(DT_PROG_DIR, "dynaTrace 4.1.0");
        versionedDir410.mkdirs();
        assertTrue(versionedDir351.compareTo(new VersionedDir(versionedDir410)) < 0);

        File versionedDirAgent410 = new File(DT_PROG_DIR, "dynaTrace Agent 4.1.0");
        versionedDirAgent410.mkdirs();
        assertTrue(versionedDir351.compareTo(new VersionedDir(versionedDirAgent410)) < 0);
    }

    @Test
    public void testInvalidDir() {
        File originalDir = new File(DT_PROG_DIR, BaseConstants.DYNATRACE_AGENT);
        originalDir.mkdirs();

        try {
        	new VersionedDir(originalDir, ".");
        	fail("Should catch exception here");
        } catch (IllegalArgumentException e) {
        	TestHelpers.assertContains(e, "Directory argument must be the root of a dynaTrace Agent installation");
        }
    }

    @Test
    public void testDashInDirectory() {
    	assertEquals(3, VersionedDir.detectVersion(new File("/opt/dynatrace/dynatrace-3.5.1"), ".").getMajor());
    	assertEquals(4, VersionedDir.detectVersion(new File("/opt/dynatrace/dynatrace-4.0.0"), ".").getMajor());

    	try {
    		VersionedDir.detectVersion(new File("/opt/dynatrace/dynatrace-"), ".").getMajor();
    		fail("Should throw Exception here");
    	} catch (IllegalArgumentException e) {
    		TestHelpers.assertContains(e, "Empty version string specified");
    	}
    }

    @Test
    public void testCompare() {
    	new File(DT_PROG_DIR, "dynaTrace 2.5.1").mkdirs();
    	new File(DT_PROG_DIR, "dynaTrace 3.5.0").mkdirs();
    	new File(DT_PROG_DIR, "dynaTrace 3.5.1").mkdirs();
    	new File(DT_PROG_DIR, "dynaTrace 3.5.2").mkdirs();
    	new File(DT_PROG_DIR, "dynaTrace 3.5.1.1").mkdirs();
    	new File(DT_PROG_DIR, "dynaTrace 4.5.1").mkdirs();
    	//new File(DT_PROG_DIR, "dynaTraceA 3.5.1").mkdirs();

        VersionedDir obj = new VersionedDir(new File(DT_PROG_DIR, "dynaTrace 3.5.1"));
    	VersionedDir equal = new VersionedDir(new File(DT_PROG_DIR, "dynaTrace 3.5.1"));
    	VersionedDir notequal = new VersionedDir(new File(DT_PROG_DIR, "dynaTrace 3.5.2"));
    	TestHelpers.CompareToTest(obj, equal, notequal, false);

    	notequal = new VersionedDir(new File(DT_PROG_DIR, "dynaTrace 4.5.1"));
    	TestHelpers.CompareToTest(obj, equal, notequal, false);

    	notequal = new VersionedDir(new File(DT_PROG_DIR, "dynaTrace 3.5.1.1"));
    	TestHelpers.CompareToTest(obj, equal, notequal, false);

    	notequal = new VersionedDir(new File(DT_PROG_DIR, "dynaTrace 2.5.1"));
    	TestHelpers.CompareToTest(obj, equal, notequal, true);

    	notequal = new VersionedDir(new File(DT_PROG_DIR, "dynaTrace 3.5.0"));
    	TestHelpers.CompareToTest(obj, equal, notequal, true);

    	/*notequal = new VersionedDir(new File(DT_PROG_DIR, "dynaTraceA 3.5.1"));
    	TestHelpers.CompareToTest(obj, equal, notequal);*/
    }
}
