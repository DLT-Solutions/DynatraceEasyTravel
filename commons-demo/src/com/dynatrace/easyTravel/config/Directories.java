package com.dynatrace.easytravel.config;

import java.io.File;

import com.dynatrace.easytravel.constants.BaseConstants;


public final class Directories {

    private static final File USER_HOME = detectUserHome();	// i.e. C:\Users\cwat-abcdef
    private static final File WORKING_DIR = detectWorkingDir();
    private static final File DYNATRACE_HOME = detectDynaTraceHome(USER_HOME);	// i.e. C:\Users\cwat-abcdef\.dynaTrace
    private static final File DEMOAPPS_HOME = detectDemoAppsHome(DYNATRACE_HOME);	// i.e. C:\Users\cwat-abcdef\.dynaTrace\easyTravel 2.0.0
    private static final File EASYTRAVEL_HOME = detectEasyTravelHome(DEMOAPPS_HOME);	// i.e. C:\Users\cwat-abcdef\.dynaTrace\easyTravel 2.0.0\easyTravel
    private static final File LOG_DIR = detectLogDir(EASYTRAVEL_HOME);	// i.e. C:\Users\cwat-abcdef\.dynaTrace\easyTravel 2.0.0\easyTravel\log
    private static final File CONFIG_DIR = detectConfigDir(EASYTRAVEL_HOME);	// i.e. C:\Users\cwat-abcdef\.dynaTrace\easyTravel 2.0.0\easyTravel\config
    private static final File TEMP_DIR = detectTempDir(EASYTRAVEL_HOME);	// i.e. C:\Users\cwat-abcdef\.dynaTrace\easyTravel 2.0.0\easyTravel\tmp
    private static final File DATABASE_DIR = detectDatabaseDir(EASYTRAVEL_HOME);	// i.e. C:\Users\cwat-abcdef\.dynaTrace\easyTravel 2.0.0\easyTravel\database
    private static final File TESTS_DIR = detectTestsDir(EASYTRAVEL_HOME);	// i.e. C:\Users\cwat-abcdef\.dynaTrace\easyTravel 2.0.0\easyTravel\tests
    private static final File INSTALL_DIR = detectInstallDir();	// i.e. C:\Program Files\dynaTrace\easyTravel
    private static final File WIN_DIR = detectWinDir();	// i.e. C:\WINDOWS
    private static final File WEBAPP_DIR = detectWebAppDir(INSTALL_DIR);	// i.e. C:\Program Files\dynaTrace\easyTravel\weblauncher\webapp
    private static final File PHP_DIR = detectPhpDir();	// i.e. C:\Program Files\dynaTrace\easyTravel\php
    private static final File MYSQL_DIR = detectMysqlDir();	// i.e. C:\Program Files\dynaTrace\easyTravel\mysql
    private static final File MYSQL_DATA_DIR = detectMysqlDataDir(DATABASE_DIR);	// i.e. C:\Users\cwat-abcdef\.dynaTrace\easyTravel 2.0.0\easyTravel\database\ratings
    private static final File MONGODB_DIR = detectMongodbDir();	// i.e. C:\Program Files\dynaTrace\easyTravel\mongodb
    private static final File MONGODB_DATA_DIR = detectMongodbDataDir(DATABASE_DIR);	// i.e. C:\Users\cwat-abcdef\.dynaTrace\easyTravel 2.0.0\easyTravel\database\mongodb
    private static final File COUCHDB_DATA_DIR = detectCouchDBDataDir(DATABASE_DIR);	// i.e. C:\Users\cwat-abcdef\.dynaTrace\easyTravel 2.0.0\easyTravel\database\couchdb
	private static final File EASYTRAVEL_JRE = detectEasyTravelJRE(INSTALL_DIR);
	private static final File PLUGINS_SHARED_DIR = detectSharedPluginsDir(INSTALL_DIR);
	private static final File RESOURCES_DIR = detectResourcesDir(INSTALL_DIR);	// i.e. C:\Program Files\dynaTrace\easyTravel\resources
	private static final File LIB_DIR = detectLibDir(INSTALL_DIR);
	private static final File CHROME_DIR = detectChromeDir(INSTALL_DIR);

    private Directories() {
    }

    /**
     * Detect the current working directory by using the "user.dir" system property.
     *
     * @return
     * @author martin.wurzinger
     */
    private static File detectWorkingDir() {
        File result = new File(System.getProperty(BaseConstants.SystemProperties.USER_DIR));
        if (result.exists()) {
            return result;
        } else {
            return new File(BaseConstants.WS).getParentFile();
        }
    }

    /**
     * Detect the user home directory by using the "user.home" system property.
     *
     * @return the user home directory
     * @throws IllegalStateException if no or an invalid value is set to the "user.home" property
     * @author martin.wurzinger
     */
    private static File detectUserHome() {
        final String userHomeProperty = System.getProperty(BaseConstants.SystemProperties.USER_HOME);
        if (userHomeProperty == null) {
            throw new IllegalStateException("No user home directory found");
        }

        final File userHome = new File(userHomeProperty);
        if (!userHome.exists()) {
            throw new IllegalStateException("Detect user home directory does not exist: " + userHomeProperty);
        }

        return userHome;
    }

    /**
     * @return the user home directory
     * @author martin.wurzinger
     */
    public static File getUserHome() {
        return USER_HOME;
    }

    private static File detectDynaTraceHome(final File userHome) {
        return new File(userHome, BaseConstants.SubDirectories.DYNATRACE_HOME);
    }

    public static File getDynaTraceHome() {
        return DYNATRACE_HOME;
    }

    private static File detectDemoAppsHome(final File dynaTraceHome) {
        return new File(dynaTraceHome, BaseConstants.EASYTRAVEL + BaseConstants.WS + Version.read().toRevision());
    }

    public static File getDemoAppsHome() {
        return DEMOAPPS_HOME;
    }

    private static File detectEasyTravelHome(final File demoAppsHome) {
        // Special handling if running under unit-tests to not overwrite production data, e.g. property/config files!
        String configDirCorrection = System.getProperty(BaseConstants.SystemProperties.HOME_DIR_CORRECTION);
        if (configDirCorrection != null) {
        	return new File(demoAppsHome, configDirCorrection);
        }

        return new File(demoAppsHome, BaseConstants.EASYTRAVEL);
    }

    public static File getEasyTravelHome() {
        return EASYTRAVEL_HOME;
    }

    private static File detectLogDir(final File easyTravelHome) {
        return new File(easyTravelHome, BaseConstants.SubDirectories.LOG);
    }

    private static File detectWebAppDir(final File easyTravelHome) {
    	return new File(easyTravelHome, BaseConstants.SubDirectories.WEB_APP);
    }

    private static File detectPhpDir() {
        return new File(getInstallDir().getAbsolutePath(), BaseConstants.SubDirectories.PHP);
    }

    private static File detectMysqlDir() {
    	return new File(getInstallDir().getAbsoluteFile(), BaseConstants.SubDirectories.MySQL);
    }

    private static File detectMongodbDir() {
    	return new File(getInstallDir().getAbsoluteFile(), BaseConstants.SubDirectories.MONGODB);
    }

    private static File detectMysqlDataDir(final File easyTravelHome) {
    	return new File(easyTravelHome, BaseConstants.SubDirectories.EASYTRAVEL_BLOG);
    }

    private static File detectMongodbDataDir(final File easyTravelHome) {
    	return new File(easyTravelHome, BaseConstants.SubDirectories.MONGODB);
    }
    private static File detectCouchDBDataDir(final File easyTravelHome) {
    	return new File(easyTravelHome, BaseConstants.SubDirectories.COUCHDB);
    }

	private static File detectEasyTravelJRE(final File easyTravelInstallDir) {
		return new File(easyTravelInstallDir, BaseConstants.SubDirectories.JRE_BIN);
	}

	public static File detectSharedPluginsDir(File installDir) {
		return new File(installDir, BaseConstants.PluginsDir.PLUGINS_SHARED);
	}

    /**
     * Get the log directory.
     * @return the logging directory
     * @author martin.wurzinger
     */
    public static File getLogDir() {
        return LOG_DIR;
    }

    /**
     * Get the log directory and create it if it has not existed before.
     *
     * @return the logging directory
     * @author martin.wurzinger
     */
    public static File getExistingLogDir() {
        final File logDir = Directories.getLogDir();
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        return logDir;
    }

    public static String getExistingLogDirWithForwardSlashes() {
        final File logDir = Directories.getLogDir();
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        return replaceBackslashWithSlash(logDir.getAbsolutePath());
    }

    /**
     * Get the easyTravel configuration directory.
     *
     * @param easyTravelHome
     * @return
     * @author martin.wurzinger
     */
    private static File detectConfigDir(final File easyTravelHome) {
        return new File(easyTravelHome, BaseConstants.SubDirectories.CONFIG);
    }

    public static File getConfigDir() {
        return CONFIG_DIR;
    }

    public static File getExistingConfigDir() {
        final File configDir = Directories.getConfigDir();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        return configDir;
    }


    private static File detectTempDir(final File easyTravelHome) {
        return new File(easyTravelHome, BaseConstants.SubDirectories.TEMP);
    }

    /**
     * Get the temp directory.
     * @return the directory used for temporary files
     * @author martin.wurzinger
     */
    public static File getTempDir() {
        return TEMP_DIR;
    }

    /**
     * Return the directory that should be used for the
     * Tomcat work-directory. The port is used to make it unique
     * across multiple tomcat instances (e.g. multiple frontends,
     * backend, ...)
     *
     * @param port
     * @return
     * @author dominik.stadler
     */
    public static File getWorkDir(int port) {
    	return new File(getTempDir(), Integer.toString(port));
    }

    /**
     * Get the temporary directory and create it if it has not existed before.
     *
     * @return the directory used for temporary files
     * @author martin.wurzinger
     */
    public static File getExistingTempDir() {
        final File tempDir = Directories.getTempDir();
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        return tempDir;
    }

    private static File detectDatabaseDir(final File easyTravelHome) {
        return new File(easyTravelHome, BaseConstants.SubDirectories.DATABASE);
    }

    public static File getDatabaseDir() {
        return DATABASE_DIR;
    }

    public static String getDatabaseDirWithForwardSlashes() {
        return replaceBackslashWithSlash(DATABASE_DIR.getAbsolutePath());
    }

    public static File getMysqlDataDir() {
    	return MYSQL_DATA_DIR;
    }

    public static File getMongodbDataDir() {
    	return MONGODB_DATA_DIR;
    }

    public static File getCouchDBDataDir() {
    	return COUCHDB_DATA_DIR;
    }

	public static File getEasytravelJRE() {
		return EASYTRAVEL_JRE;
	}

	public static File getPluginsSharedDir() {
		return PLUGINS_SHARED_DIR;
	}

	public static File getResourcesDir() {
		return RESOURCES_DIR;
	}

	public static File getLibDir() {
		return LIB_DIR;
	}

    /**
     * Get the database directory and create it if it has not existed before.
     *
     * @return the database directory
     * @author martin.wurzinger
     */
    public static File getExistingDatabaseDir() {
        final File dbDir = getDatabaseDir();
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }

        return dbDir;
    }

  public static File  getExistingMysqlDatabaseDir() {

	   final File mysqlDir = getMysqlDataDir();

	   if(!mysqlDir.exists()) {
		   mysqlDir.mkdirs();
	   }

	   return mysqlDir;
   }


    /**
     * Detects the installation directory which contains the demo application JARs.
     * @return the directory of the easyTravel installation.
     * @author martin.wurzinger
     */
    private static File detectInstallDir() {
        // Special handling if launcher is started via Eclipse IDE to make debugging easier.
        String installDirCorrection = System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION);
        if (installDirCorrection == null) {
            return WORKING_DIR;
        }

        File installDir;

        // check if absolute install directory is existing
        installDir = new File(installDirCorrection);
        if (installDir.isAbsolute() && installDir.exists()) {
            return installDir;
        }

        // check if self-resolved relative path is existing
        installDir = new File(WORKING_DIR, installDirCorrection);
        if (installDir.exists()) {
            return installDir;
        }

        // check if automatic-resolved relative path is existing
        installDir = new File(installDirCorrection);
        if (installDir.exists()) {
            return installDir;
        }

        return WORKING_DIR;
    }

	public static File detectResourcesDir(final File easyTravelInstallDir) {
		return new File(easyTravelInstallDir, BaseConstants.SubDirectories.RESOURCES);
	}

	public static File detectLibDir(final File easyTravelInstallDir) {
		return new File(easyTravelInstallDir, BaseConstants.SubDirectories.LIB);
	}

    public static File getInstallDir() {
        return INSTALL_DIR;
    }

    public static File detectChromeDir(final File easyTravelInstallDir) {
		return new File(easyTravelInstallDir, BaseConstants.SubDirectories.CHROME);
	}

    public static File getChromeDir() {
        return CHROME_DIR;
    }

    private static File detectWinDir() {
    	File winDir = null;
        String sWinDir = System.getenv("windir");
        if(sWinDir != null && !sWinDir.isEmpty()){
        	winDir = new File(System.getenv("windir"));
        	 // check if absolute install directory is existing
            if (winDir.isAbsolute() && winDir.exists()) {
                return winDir;
            }
        }

        return null;
    }

    public static File getWinDir() {
        return WIN_DIR;
    }

    public static File getWebAppDir() {
    	return WEBAPP_DIR;
    }

    public static File getWorkingDir() {
        return WORKING_DIR;
    }

    public static File getPhpDir() {
        return PHP_DIR;
    }


    public static File getMysqlDir() {
    	return MYSQL_DIR;
    }

    public static File getMongodbDir() {
    	return MONGODB_DIR;
    }

    private static File detectTestsDir(final File easyTravelHome) {
        return new File(easyTravelHome, BaseConstants.SubDirectories.TESTS);
    }

    public static File getTestsDir() {
        return TESTS_DIR;
    }

    /**
     * Get the directory that contains test results and create it if it has not existed before.
     *
     * @return the directory containing test results
     * @author martin.wurzinger
     */
    public static File getExistingTestsDir() {
        final File testsDir = getTestsDir();
        if (!testsDir.exists()) {
            testsDir.mkdirs();
        }

        return testsDir;
    }

    private static String replaceBackslashWithSlash(String str){
    	return str.replace("\\", "/");
    }
}
