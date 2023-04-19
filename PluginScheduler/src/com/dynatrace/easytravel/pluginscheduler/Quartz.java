package com.dynatrace.easytravel.pluginscheduler;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Singleton class used for creating instance of Quartz-Scheduler
 * cwpl-rorzecho
 */
public class Quartz {
	private static final Logger LOGGER = LoggerFactory.make();

	private static String schedulerInstanceName;

	private static volatile Scheduler scheduler = null;
	private static volatile SchedulerFactory schedulerFactory = null;
	private static final Properties quartzProperties =  new Properties();

	private static final String QUARTZ_PROPERTIES = "quartz.properties";

	/**
	 * Default initialization method for SchedulerFacoty
	 */
	public static void initialize()  {
		synchronized (Quartz.class) {
			loadQuartzProperties(getQuartzPropertyFilePath());
			try {
				schedulerFactory = new StdSchedulerFactory(quartzProperties);
			} catch (SchedulerException e) {
				throw new RuntimeException("Scheduler Factory cannot be properly initialized.", e);
			}
		}
	}

	/**
	 * Initialize SchedulerFactory with different configuration files
	 * @param quartzPropertiesFilePath
	 * @param quartzDataFilePath
	 */
	public static void initialize(String quartzPropertiesFilePath, String quartzDataFilePath) {
		synchronized (Quartz.class) {
			try {
				loadQuartzProperties(quartzPropertiesFilePath);
				schedulerFactory = new StdSchedulerFactory(quartzProperties);
			} catch (Exception e) {
				throw new RuntimeException("Scheduler Factory cannot be properly initialized.", e);
			}
		}
	}

	private Quartz() {
	}

	/**
	 * Get Scheduler instance
	 * @return Scheduler
	 */
	public static Scheduler getScheduler() {
		synchronized (Quartz.class) {
			if (scheduler == null && schedulerFactory != null) {
				try {
					scheduler = schedulerFactory.getScheduler();
					schedulerInstanceName = scheduler.getSchedulerName();
					LOGGER.info(TextUtils.merge("Scheduler {0} has been initialized", schedulerInstanceName));
				} catch (SchedulerException e) {
					throw new RuntimeException(TextUtils.merge("Scheduler cannot be initialized {0}", e));
				}
			}
		}
		return scheduler;
	}
	
	public static void shutDown() throws SchedulerException {
		synchronized (Quartz.class) {
			if (scheduler != null) {
				scheduler.shutdown();
			}
		}
	}

	/**
	 * Read quartz.properties file
	 */
	private static void loadQuartzProperties(String quartzPropertiesFilePath) {
		FileInputStream fileInputStream = null;

		try {
			fileInputStream = new FileInputStream(quartzPropertiesFilePath);
			quartzProperties.load(fileInputStream);
		} catch (IOException e) {
			LOGGER.error("Cannot load quartz.properties file", e);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					LOGGER.error("Cannot close FileInputStream", e);
				}
			}
		}
	}

	/**
	 * Get Quartz initialization properties
	 * @return
	 */
	public static Properties getQuartzProperties() {
		return quartzProperties;
	}

	/**
	 * Get Scheduler instance name
	 * @return
	 */
	public static String getSchedulerInstanceName() {
		return schedulerInstanceName;
	}

	/**
	 * Get properties file for Quartz
	 * @return
	 */
	private static String getQuartzPropertyFilePath() {
		File quartzProperties = new File(Directories.getResourcesDir().getAbsolutePath() + File.separator + QUARTZ_PROPERTIES);
		if (quartzProperties.exists()) {
			return quartzProperties.getPath();
		} else {
			throw new RuntimeException(TextUtils.merge("Cannot find {0} file", QUARTZ_PROPERTIES));
		}
	}

	enum QuartzProperties {
		FILE_NAMES("org.quartz.plugin.jobInitializer.fileNames");

		private String name;

		QuartzProperties(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

}
