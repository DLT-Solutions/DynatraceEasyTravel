package com.dynatrace.easytravel.pluginscheduler;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.quartz.xml.XMLSchedulingDataProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Class is executed when Quartz-Scheduler instance is initialized.
 *
 * Property org.quartz.plugin.jobInitializer.fileNames in quartz.properties file defines {@link JobFile}
 * specified with:
 * - file names contained in user config directory
 * - file names with specified path - for instance C:\\dir\\jobfile.xml
 *
 * cwpl-rorzecho
 */
public class JobSchedulingDataProcessorPlugin implements SchedulerPlugin {

	private static final Logger LOGGER = LoggerFactory.make();

	private ClassLoadHelper classLoadHelper;
	private Scheduler scheduler;

	private static final String FILE_NAME_DELIMITERS = ",";

	// fileNames read from quartz.properties file
	private String fileNames;

	private List<JobFile> jobFiles = new ArrayList<JobFile>();

	@Override
	public void initialize(String name, Scheduler scheduler, ClassLoadHelper loadHelper) throws SchedulerException {
		this.classLoadHelper = loadHelper;
		this.scheduler = scheduler;

		StringTokenizer fileNamesTokens = new StringTokenizer(fileNames, FILE_NAME_DELIMITERS);
		while (fileNamesTokens.hasMoreElements()) {
			final JobFile jobFile = new JobFile(fileNamesTokens.nextToken());
			if (jobFile.file != null) {
				jobFiles.add(jobFile);
			}
		}

	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	@Override
	public void start() {
		if (!jobFiles.isEmpty()) {
			// for every JobFile
			for (JobFile jobFile : jobFiles) {
				// find job-scheduling-data
				JobFileProcessor jobFileProcessor = new JobFileProcessor(jobFile.getFile());
					// process job-scheduling-data if schedule element exists
					for (JobSchedulingData jobSchedulingData : jobFileProcessor.getJobSchedulingData()) {
						if (jobSchedulingData.containsScheduleElement()) {
							processJobSchedulingData(jobSchedulingData);
						}
					}
			}
		}
	}

	@Override
	public void shutdown() {
		// initially empty
	}

	/**
	 * Initialize Quartz-Scheduler instance with defined jobs.
	 * @param JobSchedulingData
	 */
	private void processJobSchedulingData(JobSchedulingData jobSchedulingData) {
		try {
			XMLSchedulingDataProcessor processor = new XMLSchedulingDataProcessor(this.classLoadHelper);
			processor.processStreamAndScheduleJobs(
					jobSchedulingData.getJobSchedulingDataStream(),
					TextUtils.merge("{0}|{1}", jobSchedulingData.getFileName(), jobSchedulingData.getParentName()),
					getScheduler());
		} catch (Exception e) {
			LOGGER.error(TextUtils.merge("Cannot Schedule Jobs from systemId {0}", jobSchedulingData.getFileName()), e);
		}
	}

	public String getFileNames() {
		return fileNames;
	}

	public void setFileNames(String fileNames) {
		this.fileNames = fileNames;
	}

	/**
	 * Inner class specifying file with Job Scheduling Data
	 */
	class JobFile {
		private String fileName;
		private File file;

		public JobFile(String fileName) {
			this.fileName = fileName;
			initialize();
		}

		/**
		 *  Check if fileName can be found in the following locations:
		 *  - in the specified absolute path
		 *  - in the user config directory
		 */
		private void initialize() {
			File f = getJobFileFromCurrentDir();

			if (f == null) {
				f = getJobFileFromConfigDir();
			} if (f == null) {
				LOGGER.error(TextUtils.merge("JobFile {0} can not be found", fileName));
			}
				file = f;
		}

		/**
		 * Get JobFile from specified file path
		 * @return File
		 */
		private File getJobFileFromCurrentDir() {
			File tmp = new File(fileName);
			File dir = tmp.getParentFile();
			File f = new File(dir, tmp.getName());
			return f.exists() ? f : null;
		}

		/**
		 * Get JobFile from user config directory
		 * @return File
		 */
		private File getJobFileFromConfigDir() {
			File f = new File(Directories.getConfigDir(), getFileName());
			return f.exists() ? f : null;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public File getFile() {
			return file;
		}

		public void setFile(File file) {
			this.file = file;
		}
	}
}
