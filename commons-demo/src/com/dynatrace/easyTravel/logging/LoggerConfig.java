package com.dynatrace.easytravel.logging;


import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

/**
 * Configuration interface for root logger initialization.
 *
 * @author martin.wurzinger
 */
public interface LoggerConfig {

    public LayoutBase<ILoggingEvent> getFormatter();
    
    public String getLogFileName();
    
    public String getLogFilePath();

    public String getLogFileRollingPattern();

    public int getMaxBytes();
    
    public int getMinFileIndex();

    public int getMaxFileIndex();

    public boolean doAppend();   

    public Logger getRootLogger();
}