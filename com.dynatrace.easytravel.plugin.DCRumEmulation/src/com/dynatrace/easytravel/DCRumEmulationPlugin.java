package com.dynatrace.easytravel;


import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

/**
 * This plugin causes an emulation of DC-RUM data.
 *
 * @author stefan.moschinski
 */
public class DCRumEmulationPlugin extends AbstractGenericPlugin {


	/* (non-Javadoc)
	 * We have nothing to do here, the launcher regularly asks the backend whether
	 * this plugin is enabled or not.
	 */
	@Override
	public Object doExecute(String location, Object... context) {
		return null;
	}
}
