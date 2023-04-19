package com.dynatrace.diagnostics.uemload.openkit;

import org.slf4j.LoggerFactory;
import com.dynatrace.easytravel.constants.BaseConstants.OpenkitLogs;
import com.dynatrace.easytravel.logging.DefaultFormatter;
import com.dynatrace.easytravel.logging.LoggerConfig;
import com.dynatrace.openkit.api.LogLevel;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;


public class OpenKitLoggerProxy implements com.dynatrace.openkit.api.Logger {
	private final Logger LOGGER;
	
	public OpenKitLoggerProxy(Logger logger, LoggerConfig loggerConfig){
		this.LOGGER = logger;	
		LoggerContext logCtx = getLoggerContext();
		LayoutWrappingEncoder<ILoggingEvent> logEncoder = getLogEncoder(logCtx);
		RollingFileAppender<ILoggingEvent> appender = getFileAppender(logCtx, logEncoder, loggerConfig.getLogFilePath());

		int minLogFileIndex = loggerConfig.getMinFileIndex();
		addRollingPolicy(appender, logCtx, loggerConfig.getLogFileRollingPattern(), minLogFileIndex, getMaxLogFileIndex(minLogFileIndex, loggerConfig.getMaxFileIndex()));
		addTriggeringPolicy(appender, logCtx, loggerConfig.getMaxBytes());
		appender.start();

		
		logger.addAppender(appender);
	}
	
	private int getMaxLogFileIndex(int minFileIndex, int defaultMaxLogFileIndex) {
		String property = System.getProperty(OpenkitLogs.LOG_FILE_COUNT_PROPERTY_NAME, "0");
		try {
			int countFromProperty = Integer.parseInt(property);
			if (countFromProperty > 0) {
				return countFromProperty + minFileIndex - 1;
			}
			return defaultMaxLogFileIndex;
		} catch (NumberFormatException e) {
			LOGGER.warn("While creating OpenkitLoggerProxy: Value '" + property + "' in '" + OpenkitLogs.LOG_FILE_COUNT_PROPERTY_NAME + "' couldn't be parsed, using default instead.");
			return defaultMaxLogFileIndex;
		}
	}
	
	private void addRollingPolicy(RollingFileAppender<ILoggingEvent> appender, LoggerContext logCtx, String rollPattern, int minIndex, int maxIndex) {
		FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
		rollingPolicy.setFileNamePattern(rollPattern);
		rollingPolicy.setParent(appender);
		rollingPolicy.setContext(logCtx);
		rollingPolicy.setMinIndex(minIndex);
		rollingPolicy.setMaxIndex(maxIndex);
		rollingPolicy.start();
		appender.setRollingPolicy(rollingPolicy);
	}
	
	private void addTriggeringPolicy(RollingFileAppender<ILoggingEvent> appender, LoggerContext logCtx, int maxSize) {
		SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
		triggeringPolicy.setMaxFileSize(new FileSize(maxSize));
		triggeringPolicy.start();
		appender.setTriggeringPolicy(triggeringPolicy);
	}
	
	private LoggerContext getLoggerContext() {
		return (LoggerContext) LoggerFactory.getILoggerFactory();
	}
	
	private LayoutWrappingEncoder<ILoggingEvent> getLogEncoder(LoggerContext logCtx){
		LayoutWrappingEncoder<ILoggingEvent> logEncoder = new LayoutWrappingEncoder<>();
		logEncoder.setContext(logCtx);
		logEncoder.setLayout(new DefaultFormatter());
		logEncoder.start();
		return logEncoder;
	}
	
	private RollingFileAppender<ILoggingEvent> getFileAppender(LoggerContext logCtx, LayoutWrappingEncoder<ILoggingEvent> logEncoder, String fileName){
		RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
		appender.setContext(logCtx);
		appender.setName(LOGGER.getName() + "_appender");
		appender.setEncoder(logEncoder);
		appender.setAppend(true);
		appender.setFile(fileName);
		return appender;
	}

	@Override
	public void log(LogLevel level, String message) {
		log(level, message, null);
	}

	@Override
	public void log(LogLevel level, String message, Throwable throwable) {
		switch(level) {
		case DEBUG:
			LOGGER.debug(message, throwable);
			break;
		case INFO:
			LOGGER.info(message, throwable);
			break;
		case WARN:
			LOGGER.warn(message, throwable);
			break;
		case ERROR:
			LOGGER.error(message, throwable);
			break;
		default:
			break;
		}
	}

	@Override
	public void error(String message) {
		LOGGER.error(message);
	}

	@Override
	public void error(String message, Throwable t) {
		LOGGER.error(message, t);
	}

	@Override
	public void warning(String message) {
		LOGGER.warn(message);
	}

	@Override
	public void info(String message) {
		LOGGER.info(message);
	}

	@Override
	public void debug(String message) {
		LOGGER.debug(message);
	}

	@Override
	public boolean isErrorEnabled() {
		return LOGGER.isErrorEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return LOGGER.isWarnEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return LOGGER.isInfoEnabled();
	}

	@Override
	public boolean isDebugEnabled() {
		return LOGGER.isDebugEnabled();
	}

}
