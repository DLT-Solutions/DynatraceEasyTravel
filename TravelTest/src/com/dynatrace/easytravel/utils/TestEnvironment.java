package com.dynatrace.easytravel.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Test environment utilities, e.g. runtime data directory (also known as temporary directory).
 *
 * @author reinhold.fuereder
 */
public class TestEnvironment {

    /**
     * Directory for input/configuration data required by tests.
     *
     * Use ../TravelTest to still use the correct directory if we are
     * running tests from Distribution directory in CI/Coverage runs
     */
    public static final String TEST_DATA_PATH = "../TravelTest/testdata";
    public static final String ABS_TEST_DATA_PATH = new File(TEST_DATA_PATH).getAbsolutePath();

    /**
     * Directory for runtime data temporarily required by tests (aka temp directory).
     */
    public static final String RUNTIME_DATA_PATH = "runtimedata";
    public static final String ABS_RUNTIME_DATA_PATH =
            new File(RUNTIME_DATA_PATH).getAbsolutePath();

    public static final String ROOT_PATH = new File("..").getAbsolutePath();

    /**
     * Create (not yet existing) or clear (yet existing) runtime data directory.
     *
     * @return true in case of a successful directory creation
     * @throws IOException
     */
    public static void createOrClearRuntimeData() throws IOException {
        File dir = new File(ABS_RUNTIME_DATA_PATH);
        if (dir.exists()) {
            clearRuntimeData();
        }
        if(!new File(ABS_RUNTIME_DATA_PATH).mkdirs()) {
        	throw new IOException("Could not create directory " + ABS_RUNTIME_DATA_PATH);
        }
    }

    /**
     * Clear runtime data directory.
     *
     * Must be called in a test case's teardown() method if the test case makes use of the runtime
     * data directory.
     *
     * @return true in case of a successful clearing
     * @throws IOException
     */
    public static void clearRuntimeData() throws IOException {
        // Little sanity check:
        assert ABS_RUNTIME_DATA_PATH.toLowerCase().endsWith("test" + File.separator + RUNTIME_DATA_PATH);
        FileUtils.deleteDirectory(new File(ABS_RUNTIME_DATA_PATH));
    }

    /**
     * <em>Create</em> a new temporary directory for the given test class.
     *
     * @param testClass the test class to create the temporary directory for
     * @return the temporary directory for testing
     * @author martin.wurzinger
     */
    public static File createTestDirectory(Class<?> testClass) {
        File directory = getTestDirectory(testClass);
        directory.mkdirs();
        return directory;
    }

    /**
     * Get a new temporary directory for the given test class.
     *
     * @param testClass the test class to get the temporary directory for
     * @return the temporary directory for testing
     * @author martin.wurzinger
     */
    public static File getTestDirectory(Class<?> testClass) {
        return new File(System.getProperty("java.io.tmpdir"), testClass.getCanonicalName());
    }

    /**
     * Delete the directory and all its sub-directories and files.
     *
     * @param path the directory to delete
     * @return
     * @author martin.wurzinger
     */
    public static void deleteDirectory(File path) {
        if (path == null || !path.exists()) {
            return;
        }

        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                deleteDirectory(files[i]);
            } else {
                files[i].delete();
            }
        }

        path.delete();
    }
}
