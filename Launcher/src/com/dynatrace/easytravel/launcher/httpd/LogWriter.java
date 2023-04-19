package com.dynatrace.easytravel.launcher.httpd;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.google.common.collect.Lists;

/**
 * Small helper-class for writing the log-part of the http.conf
 *
 */
public class LogWriter {
	private static final Logger logger = LoggerFactory.make();
	
	private static final String EASYTRAVEL_LOG_PATH = Directories.getLogDir().getAbsolutePath();

	// rotate every hour to only keep one days worth of data...
	private static int DURATION_OF_ONE_LOG_FILE_IN_SECONDSS =86400/24;

	private static final String ERROR_LOG_PATH_WIN =
			"ErrorLog \"%s\\error.log\"";

	private static final String ERROR_LOG_PATH_LINUX =
			"ErrorLog '|\"%s/rotatelogs\" \"%s/error_%%H.log\" %d'";
	
	//file names to be deleted on apache startup
	private static final String ACCESS_LOG_WILDCARD = "access_??.log";
	private static final String ERROR_LOG_WILDCARD = "error_??.log";
	private static final List<String> LOG_NAMES_TO_DELETE = Lists.newArrayList(ACCESS_LOG_WILDCARD, ERROR_LOG_WILDCARD);			
			

	public static void addModLogRotateToModules(PrintWriter writer) {
		if (isNotWindows()) {
			return;
		}
		final String logRotateModuleEntry = "LoadModule log_rotate_module modules/mod_log_rotate.so";
		writer.println(logRotateModuleEntry);
	}

	public static void writeModLogRotateConfigToHttpdConf(PrintWriter writer) {
		setModLogRotatePropertiesOnWindows(writer);
		writeErrorLog(writer);
	}

	private static void setModLogRotatePropertiesOnWindows(PrintWriter writer) {
		if (isNotWindows()) {
			return;
		}
		writer.println("RotateLogs On");
		writer.println("RotateLogsLocalTime On");
		writer.println("RotateInterval " + getLogDurationInSeconds());
	}

	private static boolean isNotWindows() {
		return !OperatingSystem.IS_WINDOWS;
	}

	public static void deleteOldLog() {
		FileUtils.deleteQuietly(new File(EASYTRAVEL_LOG_PATH + File.separator + "error.log"));
		FileUtils.deleteQuietly(new File(EASYTRAVEL_LOG_PATH + File.separator + "access.log"));
		
		//delete all acess_xx.log and error_xx.log files
		File logDirectory = new File(EASYTRAVEL_LOG_PATH);
		Collection<File> logFiles = FileUtils.listFiles (logDirectory, new WildcardFileFilter(LOG_NAMES_TO_DELETE), null);
		logger.debug("Delete existing log files: " + logFiles);
		for (File file : logFiles) {
			FileUtils.deleteQuietly(file);
		}
	}

	private static int getLogDurationInSeconds() {
		return DURATION_OF_ONE_LOG_FILE_IN_SECONDSS;
	}

	private static void writeErrorLog(PrintWriter writer) {
		if (OperatingSystem.IS_WINDOWS) {
			writeErrorLogWindows(writer);
		} else {
			writeErrorLogLinux(writer);
		}
	}

	private static void writeErrorLogWindows(PrintWriter writer) {
		writeToLogWindows(ERROR_LOG_PATH_WIN, writer);
	}

	private static void writeToLogWindows(String logEntry, PrintWriter writer) {
		writer.println(String.format(logEntry, EASYTRAVEL_LOG_PATH));
	}

	private static void writeErrorLogLinux(PrintWriter writer) {
		writeToLogLinux(ERROR_LOG_PATH_LINUX, writer);
	}

	private static void writeToLogLinux(String logEntry, PrintWriter writer) {
		writer.println(String.format(logEntry, ApacheHttpdUtils.APACHE_BIN_PATH, EASYTRAVEL_LOG_PATH, getLogDurationInSeconds()));
	}

	/**
	 * Helper method which searches for all files matching the wildcard and returning the file
	 * which was last modified. Returns "new File(dir, wildcard).getAbsolutePath()" if no file
	 * matches!
	 *
	 * @param dir
	 * @param wildcard
	 * @return The last modified file or "new File(dir, wildcard).getAbsolutePath()" if no file
	 * 			matches!
	 * @author cwat-dstadler
	 */
	public static String getLastModifiedFile(File dir, String wildcard) {
		// use the wildcard to filter for all matching files
		FilenameFilter filter = new WildcardFileFilter(wildcard);
		File[] listFiles = dir.listFiles(filter);
		if(listFiles == null || listFiles.length < 1) {
			// none found? => no such logfile available, then just return the wildcard itself to indicate that there was no file found
			return new File(dir, wildcard).getAbsolutePath();
		}

		// sort by lastModified() to return the latest available one
		Set<File> files = new TreeSet<File>(new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				// reverse o1 and o2 here to get the latest listed first!
				return Long.valueOf(o2.lastModified()).compareTo(o1.lastModified());
			}
		});
		for(File file : listFiles) {
			files.add(file);
		}

		// now the first one in the sorted set is the latest one
		return files.iterator().next().getAbsolutePath();
	}

}
