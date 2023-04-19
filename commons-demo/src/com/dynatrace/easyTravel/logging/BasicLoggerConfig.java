package com.dynatrace.easytravel.logging;

import java.io.File;

import org.slf4j.LoggerFactory;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;


/**
 * Configuration class for root logger.
 *
 * @author martin.wurzinger
 * @author Michal.Bakula
 */
public class BasicLoggerConfig implements LoggerConfig {

    public static final int DEFAULT_MAX_BYTES = 2 * 1024 * 1024;
    public static final int DEFAULT_MIN_FILE_INDEX = 1;
    public static final int DEFAULT_MAX_FILE_INDEX = 8;
    private static final boolean DEFAULT_DO_APPEND = true;

    private final DefaultFormatter formatter;
    private final int maxBytes;
    private final int minFileIndex;
    private final int maxFileIndex;
    private final boolean doAppend;
    private final String logFileName;
    private final String logFilePath;
    private final String logFileRollingPattern;

    /**
     * Create a configuration for a logger that logs to console and a file. The
     * <code>logFileName</code> parameter specifies the filename.
     *
     * @param logFileName the name of the log file
     * @author martin.wurzinger
     */
    public BasicLoggerConfig(String logFileName, String logFileNameSuffix) {
        this.formatter = new DefaultFormatter();
        this.logFileName = logFileName;
        this.logFilePath = getLogFilePath(logFileName, logFileNameSuffix);
        this.logFileRollingPattern = getLogFilePath(logFileName, logFileNameSuffix, BaseConstants.LOG_FILENAME_PATTERN);

        this.maxBytes = DEFAULT_MAX_BYTES;
        this.minFileIndex = DEFAULT_MIN_FILE_INDEX;
        this.maxFileIndex = DEFAULT_MAX_FILE_INDEX;
        this.doAppend = DEFAULT_DO_APPEND;
    }


    public BasicLoggerConfig(String logFileName) {
        this(logFileName, null);
    }


    public static String getLogFilePath(String logFileName) {
        return getLogFilePath(logFileName, null, null);
    }
    
    public static String getLogFilePath(String logFileName, String logFileNameSuffix) {
        return getLogFilePath(logFileName, logFileNameSuffix, null);
    }

    public static String getLogFilePath(String logFileName, String logFileNameSuffix, String rollingPattern) {
        if (logFileName == null || logFileName.isEmpty()) {
            logFileName = BaseConstants.EASYTRAVEL;
        }
        if (logFileNameSuffix != null) {
            logFileName = logFileName + '_' + logFileNameSuffix;
        }

        StringBuilder result = new StringBuilder();

        result.append(Directories.getExistingLogDir().getAbsolutePath());
        result.append(File.separatorChar);
        result.append((rollingPattern == null) ? TextUtils.merge("{0}.log", logFileName) : TextUtils.merge(rollingPattern, logFileName)); // log file name pattern

        return result.toString();
    }

    /** {@inheritDoc} */
    @Override
    public LayoutBase<ILoggingEvent> getFormatter() {
        return formatter;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxBytes() {
    	String property = System.getProperty(BaseConstants.LOG_SIZE_SYSTEM_PROPERTY_NAME, "0.0");
    	try {
    		float bytesFromProperty = Float.parseFloat(property);
    		if (bytesFromProperty > 0.0) {				
    			return (int)(bytesFromProperty * 1024.0 * 1024.0);
    		}
    		return maxBytes;
    	} catch (NumberFormatException e) {
    		// logging may not be started yet so using println
    		System.out.println("Value '" + property + "' passed to '" + BaseConstants.LOG_SIZE_SYSTEM_PROPERTY_NAME + "' couldn't be parsed, using default instead.");
    		return maxBytes;
    	}		
    }

    /** {@inheritDoc} */
    @Override
    public boolean doAppend() {
        return doAppend;
    }

    /** {@inheritDoc} */
    @Override
    public String getLogFileName() {
        return logFileName;
    }

    /** {@inheritDoc} */
    @Override
    public Logger getRootLogger() {
        return (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    }

    /** {@inheritDoc} */
	@Override
	public int getMinFileIndex() {
		return minFileIndex;
	}

	/** {@inheritDoc} */
	@Override
	public int getMaxFileIndex() {
		return maxFileIndex;
	}

	@Override
	public String getLogFilePath() {
		return logFilePath;
	}

	@Override
	public String getLogFileRollingPattern() {
		return logFileRollingPattern;
	}
}
