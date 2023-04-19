package com.dynatrace.easytravel.amazon;

import com.dynatrace.easytravel.spring.AbstractPagePlugin;

/**
 * This plugin is used for showing how a hidden iframe with
 * background loading is not noticeable for the user, but detectable
 * with dynaTrace.
 *
 * @author dominik.stadler
 *
 */
public class AmazonTrackingPlugin extends AbstractPagePlugin {
	@Override
	public String getFooterScript() {
		return "<iframe id=\"trackingFrame\" style=\"display:none\" src=\"https://www.amazon.de/dp/3868020403?tag=dynatrace-21\" />\n" +
				"<script type=\"text/javascript\">\n" +
				"var iframe = document.getElementById('trackingFrame'), src = iframe.src;\n" +
				"iframe.src = '';\n" +
				"document.onload =  function(){iframe.src = src;})\n" +
				"</script>\n" +
				"test";
	}
}
