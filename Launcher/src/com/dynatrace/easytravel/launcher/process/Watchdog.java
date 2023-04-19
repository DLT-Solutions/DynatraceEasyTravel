package com.dynatrace.easytravel.launcher.process;

import java.util.List;

import org.apache.commons.exec.ExecuteWatchdog;

import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.engine.StopListener;



public class Watchdog extends ExecuteWatchdog implements FailureListener {

	private static final String ACCESS_DENIED = "Access is denied";

    private final List<StopListener> stopListeners;

    private final Runnable stopRunnable;

    private boolean stopRunnableRan;

    private java.lang.Process process;

    public Watchdog(long timeout, List<StopListener> stopListeners, Runnable stopRunnable) {
        super(timeout);
        this.stopListeners = stopListeners;
        this.stopRunnable = stopRunnable;
        this.stopRunnableRan = false;
    }


    @Override
    public synchronized void stop() {
        if (stopRunnable != null && !stopRunnableRan) {
            stopRunnable.run();
            stopRunnableRan = true;
        }
        super.stop();
        notifyProcessStopped();
    }


	protected void notifyProcessStopped() {
		for (StopListener stopListener : stopListeners) {
            stopListener.notifyProcessStopped();
        }
	}


    @Override
	public synchronized void destroyProcess() {
        this.timeoutOccured(null);
        if (stopRunnable != null && !stopRunnableRan) {
            stopRunnable.run();
            stopRunnableRan = true;
        }
        super.stop();
    }


    @Override
	public synchronized void start(java.lang.Process process) {
        this.process = process;
        super.start(process);
    }


    public synchronized int getExitValue() throws IllegalStateException {
        try {
            return process.exitValue();
        } catch (IllegalThreadStateException itse) {
            throw new IllegalStateException(itse);
        }
    }

    @Override
    public void notifyFailureOccured(Exception e) {
		if(isAccessDeniedAndOsWindows(e)) {
			fireProcessFailedEvent();
		}
    }

	private boolean isAccessDeniedAndOsWindows(Exception e) {
		String errorMessage = e.getMessage();
		if(errorMessage.contains(ACCESS_DENIED) && OperatingSystem.isCurrent(OperatingSystem.WINDOWS)) {
			return true;
		}

		return false;
	}

    private void fireProcessFailedEvent() {
        for (StopListener stopListener : stopListeners) {
        	stopListener.notifyProcessFailed();
        }
    }

	public java.lang.Process getProcess() {
		return process;
	}

}
