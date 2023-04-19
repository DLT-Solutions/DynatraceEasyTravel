package com.dynatrace.easytravel.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.constants.BaseConstants;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.LayoutBase;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class DefaultFormatter extends LayoutBase<ILoggingEvent> {
	
	private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String LINE_SEPARATOR = BaseConstants.CRLF_OS_INDEPENDENT;

    private static final StringBuilder BUILDER = new StringBuilder();
    private static final Date DATE = new Date();
    
    private static final int LEVEL_DEFAULT_LENGTH = 5;
    private static final int APP_DEFAULT_LENGTH = 10;
    private static final int CLASS_DEFAULT_LENGTH = 12;
    
    private static String appId = null;

    /** Allows to provide an application id for cases
     * where multiple applications log to the same output, e.g.
     * in the Launcher output window.
     *
     * @param appId
     * @author dominik.stadler
     */
	public static void setAppId(String appId) {
		DefaultFormatter.appId = appId;
	}

	@Override
	public synchronized String doLayout(ILoggingEvent event) {
		BUILDER.setLength(0);

        // build log message
        appendDateTime(event);
        appendApplication();
        appendLevel(event);
        appendClass(event);
        appendMessage(event);
        appendThrowable(event);
        appendNewLine();
        
	    return BUILDER.toString();
	}
	
	private void appendDateTime(ILoggingEvent event) {
        DATE.setTime(event.getTimeStamp());
        BUILDER.append(DATE_TIME_FORMAT.format(DATE));
        BUILDER.append(BaseConstants.WS);
    }
	
	private void appendApplication() {
		if(appId != null) {
			BUILDER.append(appId.substring(0, appId.length() > APP_DEFAULT_LENGTH ? APP_DEFAULT_LENGTH : appId.length()));
	        BUILDER.append(StringUtils.repeat(BaseConstants.WS, APP_DEFAULT_LENGTH - appId.length()));
	        BUILDER.append(BaseConstants.WS);
		}
	}
	
	private void appendLevel(ILoggingEvent event) {
        String levelName = event.getLevel().toString();

        BUILDER.append(levelName);
        BUILDER.append(StringUtils.repeat(BaseConstants.WS, LEVEL_DEFAULT_LENGTH - levelName.length()));
        BUILDER.append(BaseConstants.WS);
    }
	
	private void appendClass(ILoggingEvent event) {
        String className = event.getLoggerName();
        int lastdot = className.lastIndexOf(BaseConstants.DOT);
        if (lastdot > 0) {
            className = className.substring(lastdot + 1);
        }

        BUILDER.append(BaseConstants.BRACKET_LEFT);
        BUILDER.append(className);
        BUILDER.append(BaseConstants.BRACKET_RIGHT);

        BUILDER.append(StringUtils.repeat(BaseConstants.WS, CLASS_DEFAULT_LENGTH - className.length()));

        BUILDER.append(BaseConstants.WS);
    }
	
	private void appendMessage(ILoggingEvent event) {
        BUILDER.append(event.getFormattedMessage());
    }
	
	private void appendThrowable(ILoggingEvent event) {
		ThrowableProxy throwableProxy = (ThrowableProxy) event.getThrowableProxy();
		if (throwableProxy == null) {
			return;
		}

		Throwable throwable = throwableProxy.getThrowable();
		if (throwable == null) {
			return;
		}
		appendNewLine();
		appendStackTrace(throwable);
	}

	private void appendStackTrace(Throwable throwable) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            pw.close();
            BUILDER.append(sw.toString());
        } catch (Exception ex) {
            // NOSONAR - ok to ignore this
        }
    }
	
	private void appendNewLine() {
        BUILDER.append(LINE_SEPARATOR);
    }

}
