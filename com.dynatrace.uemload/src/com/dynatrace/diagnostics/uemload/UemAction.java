package com.dynatrace.diagnostics.uemload;


public interface UemAction extends Comparable<UemAction> {

	void sendActionPreview();

	long elapsedMillis();

	void resetTimer();

	ActionType getTye();

	enum ActionType {
		CUSTOM_ACTION,
		PAGE_LOAD;
	}

	/**
	 * 
	 * @return the start time of the action in millis
	 * @author stefan.moschinski
	 */
	long getStartTime();
}

