package com.dynatrace.easytravel.logging;

import java.util.Collection;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


/**
 * Convenience class that let you filter unwanted log output.
 * 
 * @author cwat-smoschin
 */
public class LoggingSuppresser {
	private final Collection<Pattern> patterns;

	private final Logger logger;
	private final Filter originalFilter;

	public LoggingSuppresser(String loggerClassName) {
		this.patterns = Lists.newArrayListWithExpectedSize(1);
		this.logger = Preconditions.checkNotNull(Logger.getLogger(loggerClassName), "Cannot get logger for class '%s'",
				loggerClassName);
		this.originalFilter = logger.getFilter();
	}

	/**
	 * Using this method you can specify which log messages should be suppressed.
	 * 
	 * @param logMsgPattern if the log message <b>contains</b> the given regular expression, it is suppressed
	 * @return the current instance (i.e., {@code this})
	 * @author stefan.moschinski
	 */
	public LoggingSuppresser addLogPatternToSuppress(String logMsgPattern) {
		patterns.add(Pattern.compile(logMsgPattern));
		return this;
	}

	/**
	 * Starts to suppress the log messages matching the patterns previously given by the {@link #addLogPatternToSuppress(String)}
	 * method.
	 * A log message is filtered if <b>any</b> of the given pattens matches!
	 * 
	 * @author stefan.moschinski
	 */
	public void suppressLogging() {
		logger.setFilter(new Filter() {

			@Override
			public boolean isLoggable(LogRecord record) {
				if (record == null || StringUtils.isEmpty(record.getMessage())) {
					return true;
				}

				if (originalFilter != null && !isLoggableAccordingToOriginalFilter(record)) {
					return false;
				}

				for (Pattern pattern : patterns) {
					if (pattern.matcher(record.getMessage()).find()) {
						return false;
					}
				}
				return true;
			}

			private boolean isLoggableAccordingToOriginalFilter(LogRecord record) {
				return originalFilter.isLoggable(record);
			}
		});
	}

	/**
	 * Ends the log suppression and sets the filter to the original filter if the logger had one.
	 * 
	 * @author stefan.moschinski
	 */
	public void endSuppressLogging() {
		logger.setFilter(originalFilter);
	}
}
