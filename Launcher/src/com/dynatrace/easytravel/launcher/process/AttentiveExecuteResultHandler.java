package com.dynatrace.easytravel.launcher.process;

import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;



public class AttentiveExecuteResultHandler extends DefaultExecuteResultHandler {
    private static final Logger LOGGER = LoggerFactory.make();
    private String command;
	private FailureListener failureListener;


    public AttentiveExecuteResultHandler(String command, FailureListener failureListener) {
        this.command = command;
        this.failureListener = failureListener;
    }

    @Override
    public void onProcessFailed(ExecuteException e) {
        super.onProcessFailed(e);
        LOGGER.warn("Process did not stop gracefully, had exception '" + e.getMessage() + "' while executing process with command: " + command, e);
        if(LOGGER.isDebugEnabled()) {
        	LOGGER.debug("Exception details: ", e);
        }
        failureListener.notifyFailureOccured(e);
    }
 
}
