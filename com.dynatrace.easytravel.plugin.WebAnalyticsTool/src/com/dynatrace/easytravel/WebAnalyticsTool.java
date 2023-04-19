package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractPagePlugin;

/**
 * This plugin adds third party social media content to all easyTravel pages.
 * The content is added to the footer.
 *
 * @author peter.lang
 *
 */
public class WebAnalyticsTool extends AbstractPagePlugin {

	// injected by the PluginServlet
	public boolean showFooter = true;

	@Override
	public String getFooter() {
		if (showFooter) {
			
			//NOTE: This is not the default code suggested by statcounter,
			//because it would use document.write, which is not detected by our uemload simulator
			String statCounterCode =
					"<!-- Start of StatCounter Code for Default Guide -->\r\n"+
					"<script type=\"text/javascript\">\r\n" + 
					"var sc_project=9804080; var sc_invisible=0; var sc_security=\"d2167d38\";\r\n" + 
					"</script>\r\n"+
					"<script type='text/javascript' src='https://secure.statcounter.com/counter/counter.js'></script>";
			
			return statCounterCode;
		}
		return null;
	}

	@Override
	public String getHeadInjection() {
		return "";
	}

	@Override
	public String toString() {
		return "WebAnalyticsTool [showFooter=" + showFooter + "]";
	}
}
