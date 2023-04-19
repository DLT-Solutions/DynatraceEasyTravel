package com.dynatrace.easytravel.webfonts;

import com.dynatrace.easytravel.spring.AbstractPagePlugin;
import com.dynatrace.easytravel.spring.PluginConstants;


public abstract class WebFont extends AbstractPagePlugin {

	protected abstract String getWebFontScript();

	@Override
	public Object doExecute(String location, Object... context) {
	
		if (PluginConstants.FRONTEND_WEBFONTS.equals(location)) {
			return getWebFontScript();
		}
		return super.doExecute(location, context);
	}
}
