/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BrowserScenario.java
 * @date: 20.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload;


/**
 * Abstract base class for all browser based scenarios. This class is responsible for
 * creating an appropriate Simulator for this scenario.
 *
 * @author peter.lang
 */
public abstract class BrowserScenario extends UEMLoadScenario {

	@Override
	final public Simulator createSimulator() {
		return new BrowserSimulator(this);
	}

}
