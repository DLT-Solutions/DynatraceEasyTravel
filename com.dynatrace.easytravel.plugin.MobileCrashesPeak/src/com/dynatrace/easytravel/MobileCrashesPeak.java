package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

public class MobileCrashesPeak extends AbstractGenericPlugin {

	/* (non-Javadoc)
	 * We have nothing to do here, the launcher regularly asks the backend whether
	 * this plugin is enabled or not.
	 */
	@Override
	protected Object doExecute(String location, Object... context) {
		//this plugin does nothing - logic is in MobileCrashAction - only needed as on/off switch
		return null;
	}
}
