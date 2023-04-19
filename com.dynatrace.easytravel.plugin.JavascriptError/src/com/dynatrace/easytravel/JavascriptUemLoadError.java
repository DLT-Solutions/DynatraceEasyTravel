package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;


/**
 * 
 * There is nothing to do for the plugin when called as all the uemload
 * magic is done in EasyTravelContactPage and JavaScriptAgent.
 * 
 * @author cwat-slexow
 *
 */
public class JavascriptUemLoadError extends AbstractGenericPlugin {

	@Override
	protected Object doExecute(String location, Object... context) {
		return null;
	}

}
