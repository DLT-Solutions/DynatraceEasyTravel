/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ApacheHttpdUtils.java
 * @date: 17.10.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.launcher.httpd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.utils.ExecutableUtils;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import ch.qos.logback.classic.Logger;

/**
 * Some helpers for the Apache HTTPD procedure.
 *
 * @author stefan.moschinski
 * @author dominik.stadler
 */
public class ApacheHttpdUtils {
	private static final Logger LOGGER = LoggerFactory.make();
	
	private static final OperatingSystem USED_OS = OperatingSystem.pickUp();

	public static final String APACHE_VERSION = "apache2.4";
	
	public static final String INSTALL_APACHE_PATH = Paths.get( Directories.getInstallDir().getAbsolutePath(), APACHE_VERSION ).toString();
	public static final String APACHE_OS_SPECIFIC_PATH = Paths.get( INSTALL_APACHE_PATH, getApacheInstallDirForUsedOs() ).toString(); 
	public static final String APACHE_BIN_PATH = Paths.get( APACHE_OS_SPECIFIC_PATH, "bin" ).toString();	
	public static final String HTTPD_EXE_PATH = Paths.get( APACHE_BIN_PATH, "httpd" ).toString();
	public static final String APACHE_PLAIN_CONF = Paths.get( INSTALL_APACHE_PATH, "plain_conf").toString();

	public static final String APACHE_CONF = Paths.get( Directories.getConfigDir().getAbsolutePath(), "httpd.conf" ).toString();
	public static final String APACHE_RUNTIME_DIR = Directories.getTempDir().getAbsolutePath(); 
	public static final String PHP_INI = Paths.get( Directories.getConfigDir().getAbsolutePath(), "php.ini" ).toString();
	
	private static final String[] WINDOWS_KILL_INSTRUCTION = { "taskkill", "/F", "/t", "/PID" };
	
	public static String getLibraryPath() {
		return ApacheHttpdUtils.APACHE_OS_SPECIFIC_PATH + File.separator + "lib" + ":" + System.getenv(Constants.Misc.ENV_VAR_WEBSERVER_LIBRARY_PATH);
	}
   	    
    public static String getExecutableDependingOnOs() {
		return APACHE_VERSION + File.separator + getApacheInstallDirForUsedOs() + File.separator + "bin"+ File.separator + "httpd";
	}

	public static void killProcess(String[] killInstructions) throws IOException {
		exec(killInstructions);
	}

	private static void exec(String[] instructions) throws IOException {
		Runtime.getRuntime().exec(instructions);
	}

	public static void killIfNotTerminatedLinux() throws IOException {
		LOGGER.debug("killIfNotTerminatedLinux - entered");
		if (!isUsedOsWindows()) {
			LOGGER.debug("killIfNotTerminatedLinux - the OS is not windows, executing the command");
			exec(getLinuxKillInstructionUsingPid());
			LOGGER.debug("killIfNotTerminatedLinux - command run");
		}
	}

	public static String[] getKillInstruction() throws IOException {
		String[] killInstruction;
		killInstruction =
				isUsedOsWindows() ?
						getWindowsKillInstruction() :
						getLinuxKillInstruction();
		return killInstruction;
	}

	public static boolean isUsedOsWindows() {
		return USED_OS == OperatingSystem.WINDOWS;
	}

	private static String[] getLinuxKillInstruction() {
		final String[] LINUX_KILL_INSTRUCTION = { HTTPD_EXE_PATH, "-k", "stop", "-f", APACHE_CONF };
		return LINUX_KILL_INSTRUCTION;
	}

	private static String[] getLinuxKillInstructionUsingPid() throws IOException {
		return new String[] { "kill", String.valueOf(getPid()) };
	}

	private static String[] getWindowsKillInstruction() throws IOException {
		final int pid = getPid();

		if (pid > 0) {
			return (String[]) ArrayUtils.add(WINDOWS_KILL_INSTRUCTION, String.valueOf(pid));
		}

		throw new IllegalArgumentException("The PID " + pid + " is invalid. It must be greater than 0.");
	}


	private static int getPid() throws IOException {
		int pid = 0;
        final String httpdPidName = "httpd.pid";

        String pidString = null;

        for (File httpdPidLocation : httpdPidLocations()) {
            File httpdPidFile = new File(httpdPidLocation, httpdPidName);
            if (httpdPidFile.exists()) {
                pidString = FileUtils.readFileToString(new File(httpdPidLocation, httpdPidName)).trim();
            }
        }

        if (pidString == null) {
            throw new IllegalStateException(TextUtils.merge("The file {0} does not exist in the following locations: {1}", httpdPidName, httpPidLocations()));
        }

		try {
			pid = Integer.parseInt(pidString);
		} catch (NumberFormatException e) {
			throw new IOException("Could not convert the PID " + pidString + " to an integer value.", e);
		}
		return pid;
	}

	public static String getApacheInstallDirForUsedOs() {
		return ExecutableUtils.getInstallDirDependingOs(INSTALL_APACHE_PATH);
	}
	
	public static String getPhpInstallDirForUsedOs() {
		return getApacheInstallDirForUsedOs();
	}
	
	
	public static String getPhpExtPathForUsedOs() {
		return Paths.get(Directories.getPhpDir().getAbsolutePath(), getPhpInstallDirForUsedOs(), "ext").toString();
	}
	
	public static String getPhpTmpPathForUsedOs() {
		return Paths.get(Directories.getPhpDir().getAbsolutePath(), getPhpInstallDirForUsedOs(), "tmp").toString();
	}
	
    private static File[] httpdPidLocations() {
        final File[] httpdPidLocations = {Directories.getLogDir(), Directories.getTempDir()};
        return httpdPidLocations;
    }

    private static ImmutableList<String> httpPidLocations() {
        return FluentIterable.from(Arrays.asList(httpdPidLocations()))
                .transform(new Function<File, String>() {
                    @Override
                    public String apply(File input) {
                        return input.getAbsolutePath();
                    }
                }).toList();
    }
}
