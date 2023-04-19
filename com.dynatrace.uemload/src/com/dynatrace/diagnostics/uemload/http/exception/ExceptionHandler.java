package com.dynatrace.diagnostics.uemload.http.exception;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import ch.qos.logback.classic.Logger;

public class ExceptionHandler {

    private static final int MAX_MAP_SIZE = 1000;

    @SuppressWarnings("serial")
    private transient static final Set<String> LOGGED_EXCEPTIONS = Collections.newSetFromMap(new LinkedHashMap<String, Boolean>() {
        @Override
		protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            return size() > MAX_MAP_SIZE;
        }
    });


    public static void warn(IOException e, Logger passedLogger) {
        warn(e, passedLogger, null);
    }

	public static void warn(IOException e, Logger passedLogger, String additionalMessage) {
		String exceptionMessage = e.getMessage();
		if (exceptionMessage != null && exceptionMessage.contains(PageNotAvailableException.NO_WARNING)) {
		    return;
		}
		StackTraceElement[] stackTrace = e.getStackTrace();
		StackTraceElement first = stackTrace[0];
		StackTraceElement firstDynaTrace = null;
		for (StackTraceElement ste : stackTrace) {
		    if (ste.getClassName().contains("dynatrace")) {
		        firstDynaTrace = ste;
		        break;
		    }
		}
		String key = generateKey(e, first, firstDynaTrace);
		if (LOGGED_EXCEPTIONS.add(key)) {
		    if (additionalMessage != null) {
		        passedLogger.warn(exceptionMessage + '\n' + additionalMessage);
		    } else {
		        passedLogger.warn(exceptionMessage);
		    }
		}
    }


	private static String generateKey(Exception exception, StackTraceElement... stackTraceElements) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(exception.getClass().getName()).append('|').append(exception.getMessage());
	    for (StackTraceElement ste : stackTraceElements) {
	        if (ste != null) {
	            sb.append('|').append(ste.getClassName()).append('|').append(ste.getMethodName()).append('|').append(ste.getLineNumber());
	        }
	    }
	    return sb.toString();
	}

}
