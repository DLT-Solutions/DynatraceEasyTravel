package com.dynatrace.easytravel.launcher.engine;


public abstract class AbstractStopListener implements StopListener {

    /* (non-Javadoc)
	 * @see com.dynatrace.easytravel.launcher.engine.StopListener#notifyProcessStopped()
	 */
    @Override
	public void notifyProcessStopped() {}

    /* (non-Javadoc)
	 * @see com.dynatrace.easytravel.launcher.engine.StopListener#notifyProcessFailed()
	 */
    @Override
	public void notifyProcessFailed() {}
}
