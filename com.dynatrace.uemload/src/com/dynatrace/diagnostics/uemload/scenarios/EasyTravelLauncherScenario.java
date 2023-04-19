/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: EasyTravelLauncherScenario.java
 * @date: 25.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.Simulator;


/**
 * Interface for scenarios to integrate into easyTravel launcher.
 *
 * @author peter.lang
 */
public interface EasyTravelLauncherScenario {

	/**
	 *
	 * @param taggedWebRequest wether use tagged webrequest or not.
	 * @author peter.lang
	 */
	public void init(boolean taggedWebRequest);

	/**
	 * @return simulator needed to run this scenario. never returns null
	 * @author peter.lang
	 */
	public Simulator createSimulator();

	/**
	 *
	 * @param host
	 * @author peter.lang
	 */
	public EasyTravelHostManager getHostsManager();

	/**
	 *
	 * @return true if the necessary host(s) for this scenario are available
	 * @author peter.lang
	 */
	public boolean hasHosts();
	
	/**
	 * Method called when base load value has been changed 
	 * @param value - base load value
	 * @author rafal.psciuk
	 */
	public void setLoad(int value);
}
