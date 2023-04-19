package com.dynatrace.easytravel.launcher.remote;

/**
 * Extends the comon HTTPService Thread with functionality that
 * shuts down all Procedures if there are any started
 * 
 * @author cwat-rpilz
 *
 */
public class HttpServiceThread extends com.dynatrace.easytravel.remote.HttpServiceThread {
	
	public HttpServiceThread(int port, Runnable shutdownExecutor) {
		super(port, shutdownExecutor);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		// stop procedures if we started any
		RESTProcedureControl.stopAll();
	}

}
