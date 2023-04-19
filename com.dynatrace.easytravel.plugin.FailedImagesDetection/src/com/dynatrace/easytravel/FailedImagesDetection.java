package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;


/**
 * 
 * There is nothing to do for the plugin
 * ThirdPartyContentSummary checks if the plugin is enabled
 * The failed images are shown in the waterfall view
 * 
 * @author markus.wagner
 *
 */
public class FailedImagesDetection extends AbstractGenericPlugin {

	@Override
	protected Object doExecute(String location, Object... context) {
		return null;
	}

}
