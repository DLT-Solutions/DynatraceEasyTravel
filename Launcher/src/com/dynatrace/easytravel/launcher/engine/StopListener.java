package com.dynatrace.easytravel.launcher.engine;



public interface StopListener {

	void notifyProcessStopped();

	void notifyProcessFailed();

}