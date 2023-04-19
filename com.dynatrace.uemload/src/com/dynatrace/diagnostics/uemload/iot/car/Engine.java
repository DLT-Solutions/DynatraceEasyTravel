package com.dynatrace.diagnostics.uemload.iot.car;

/**
 * @author Michal.Bakula
 */
public class Engine {

	public void fail() throws EngineFailure {
		throw new EngineFailure();
	}
}
