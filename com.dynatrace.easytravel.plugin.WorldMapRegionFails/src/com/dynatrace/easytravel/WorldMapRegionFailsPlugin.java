package com.dynatrace.easytravel;


import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

/**
 * This plugin causes a region in the World Map to fail completely (i.e. all visits frustrating)
 *
 * @author stefan.moschinski
 */
public class WorldMapRegionFailsPlugin extends AbstractGenericPlugin {


	/* (non-Javadoc)
	 * We have nothing to do here, the launcher regularly asks the backend whether
	 * this plugin is enabled or not.
	 */
	@Override
	public Object doExecute(String location, Object... context) {
		return null;
	}
}
