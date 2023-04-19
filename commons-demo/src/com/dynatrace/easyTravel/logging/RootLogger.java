package com.dynatrace.easytravel.logging;

import java.io.File;
import java.util.Date;

import org.slf4j.LoggerFactory;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.Version;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.jackson.JacksonJsonFormatter;
import ch.qos.logback.contrib.json.classic.JsonLayout;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;

/**
 * Class to initialize the easyTravel loggers.
 *
 * @author martin.wurzinger
 * @author Michal.Bakula
 */
public class RootLogger {

	// private static final Logger LOGGER = LoggerFactory.make();

	public static void setup(String logFileName, String logFileNameSuffix) {
		setup(new BasicLoggerConfig(logFileName, logFileNameSuffix));
	}

	public static void setup(String logFileName) {
		setup(logFileName, null);
	}

	public static void setup(LoggerConfig config) {
		try {
			DefaultFormatter.setAppId(config.getLogFileName());
			
			File logDir = Directories.getLogDir();
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
			
			LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
			
			LayoutWrappingEncoder<ILoggingEvent> logEncoder = new LayoutWrappingEncoder<>();
			logEncoder.setContext(logCtx);
			if(Boolean.parseBoolean(System.getProperty("useJSONLogging", "false"))) {
				JsonLayout layout = new JsonLayout();
				layout.setJsonFormatter(new JacksonJsonFormatter());
				layout.setAppendLineSeparator(true);
				layout.setTimestampFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
				logEncoder.setLayout(layout);
			} else {
				logEncoder.setLayout(config.getFormatter());
			}			
			logEncoder.start();

			ConsoleAppender<ILoggingEvent> logConsoleAppender = new ConsoleAppender<>();
			logConsoleAppender.setContext(logCtx);
			logConsoleAppender.setName(config.getLogFileName());
			logConsoleAppender.setEncoder(logEncoder);
			logConsoleAppender.start();

			RollingFileAppender<ILoggingEvent> logFileAppender = new RollingFileAppender<>();
			logFileAppender.setContext(logCtx);
			logFileAppender.setName(config.getLogFileName());
			logFileAppender.setEncoder(logEncoder);
			logFileAppender.setAppend(config.doAppend());
			logFileAppender.setFile(config.getLogFilePath());
			
			FixedWindowRollingPolicy logFilePolicy = new FixedWindowRollingPolicy();
			logFilePolicy.setContext(logCtx);
			logFilePolicy.setParent(logFileAppender);
			logFilePolicy.setFileNamePattern(config.getLogFileRollingPattern());
			logFilePolicy.setMinIndex(config.getMinFileIndex());
			logFilePolicy.setMaxIndex(config.getMaxFileIndex());
			logFilePolicy.start();
			
			SizeBasedTriggeringPolicy<ILoggingEvent> logSizePolicy = new SizeBasedTriggeringPolicy<>();
			logSizePolicy.setMaxFileSize(new FileSize(config.getMaxBytes()));
			logSizePolicy.start();

			logFileAppender.setRollingPolicy(logFilePolicy);
			logFileAppender.setTriggeringPolicy(logSizePolicy);
			logFileAppender.start();

			Logger log = config.getRootLogger();
			log.addAppender(logConsoleAppender);
			log.addAppender(logFileAppender);
		} catch (Exception e) {
            System.err.println(TextUtils.merge("Unable to setup log file for ''{0}''. Reason: {1}", config.getLogFileName(), e.getLocalizedMessage())); // NOSONAR - Using System.err because logger could not be initialized
        }
		

		// report this even when in WARN-loglevel
		Logger initLOGGER = (Logger) LoggerFactory.getLogger("Init");
		Level level = initLOGGER.getLevel();
		try {
			initLOGGER.setLevel(Level.INFO);
			initLOGGER.info("-----------------------------------------------------------------------------");
			initLOGGER.info(TextUtils.merge("easyTravel Demo Application - Copyright (C) 2010-{0,date,yyyy} dynaTrace software GmbH", new Date()));
			initLOGGER.info("-----------------------------------------------------------------------------");
			initLOGGER.info("Procedure: " + config.getLogFileName());
			Version version = Version.read();
			initLOGGER.info("Version: " + version.toString());
			initLOGGER.info("Build Date: " + version.getBuilddate());
			initLOGGER.info("Platform: " + getPlatformDescription());
		} finally {
			// restore previous level
			initLOGGER.setLevel(level);
		}
	}
	

	private static String getPlatformDescription() {
		return System.getProperty(BaseConstants.SystemProperties.OS_NAME) + BaseConstants.WS
				+ System.getProperty(BaseConstants.SystemProperties.OS_VERSION) + ", "
				+ System.getProperty(BaseConstants.SystemProperties.OS_ARCH);
	}
}
