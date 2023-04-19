package com.dynatrace.easytravel.launcher.process;

import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;


/**
 * In Windows, we have to kill the Apache web server because a graceful stop would require administrator rights.
 * Due to that fact, the FailureTolerantResultHandler ignores the return value that indicates that the Apache
 * web server was not terminated in the right way.
 * @author stefan.moschinski
 */
public class FailureTolerantResultHandler extends DefaultExecuteResultHandler {

	private final FailureListener failureListener;

	public FailureTolerantResultHandler(String command, FailureListener failureListener) {
		this.failureListener = failureListener;
	}

	@Override
    public void onProcessFailed(ExecuteException e) {
		failureListener.notifyFailureOccured(e);
		super.onProcessFailed(e);
    }

}
