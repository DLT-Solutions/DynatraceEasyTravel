package com.dynatrace.easytravel.logging;

import static org.junit.Assert.assertNotNull;

import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.ThrowableProxy;


public class DefaultFormatterTest {

	@Test
	public void testFormatLogRecord() {
		DefaultFormatter formatter = new DefaultFormatter();
		
		DefaultFormatter.setAppId("newapp");
		
		ILoggingEvent event = createLoggingEvent("logger", Level.INFO, new Date(1525699205), null, new Exception());
		assertNotNull(formatter.doLayout(event));

		DefaultFormatter.setAppId("newappwithmorelengthwhichiscutofflater");
		
		event = createLoggingEvent("logger", Level.INFO, new Date(1525699205), "somemessage", new Exception());
		assertNotNull(formatter.doLayout(event));

		// use classname with packagename 
		event = createLoggingEvent("com.dstadler.logger", Level.INFO, new Date(1525699205), "somemessage", new Exception());
		assertNotNull(formatter.doLayout(event));

		event = createLoggingEvent("logger", Level.INFO, new Date(1525699205), "somemessage", new Exception());
		assertNotNull(formatter.doLayout(event));
	}
	
	@Test
	public void testNullAppId() {
		DefaultFormatter formatter = new DefaultFormatter();
		
		DefaultFormatter.setAppId(null);
		
		ILoggingEvent event = createLoggingEvent("logger", Level.INFO, new Date(1525699205), "somemessage", new Exception());
		assertNotNull(formatter.doLayout(event));
	}

	@Test
	public void testThrowableThrowsInAppendStackTrace() {
		DefaultFormatter formatter = new DefaultFormatter();
		
		ILoggingEvent event = createLoggingEvent("logger", Level.INFO, new Date(1525699205), "somemessage", new Exception() {
			private static final long serialVersionUID = 1L;

			@Override
			public void printStackTrace(PrintWriter s) {
				throw new IllegalStateException("testexception");
			}});

		assertNotNull(formatter.doLayout(event));
	}
	
	ILoggingEvent createLoggingEvent(String loggerName, Level logLevel, Date date, String msg, Throwable throwable)
    {
        return new ILoggingEvent()
        {
            @Override
            public String getThreadName()
            {
                return "thread";
            }

            @Override
            public Level getLevel()
            {
                return logLevel;
            }

            @Override
            public String getMessage()
            {
                return msg;
            }

            @Override
            public Object[] getArgumentArray()
            {
                return new Object[ 0 ];
            }

            @Override
            public String getFormattedMessage()
            {
                return msg;
            }

            @Override
            public String getLoggerName()
            {
                return loggerName;
            }

            @Override
            public LoggerContextVO getLoggerContextVO()
            {
                return null;
            }

            @Override
            public IThrowableProxy getThrowableProxy()
            {
                return new ThrowableProxy(throwable);
            }

            @Override
            public StackTraceElement[] getCallerData()
            {
                return null;
            }

            @Override
            public boolean hasCallerData()
            {
                return false;
            }

            @Override
            public Marker getMarker()
            {
                return null;
            }

            @Override
            public Map<String, String> getMDCPropertyMap()
            {
                return null;
            }

            @Override
            public Map<String, String> getMdc()
            {
                return null;
            }

            @Override
            public long getTimeStamp()
            {
                return date.getTime();
            }

            @Override
            public void prepareForDeferredProcessing()
            {

            }
        };

    }
}
