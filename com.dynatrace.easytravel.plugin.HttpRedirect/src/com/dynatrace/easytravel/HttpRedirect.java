package com.dynatrace.easytravel;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.spring.AbstractPagePlugin;

public class HttpRedirect extends AbstractPagePlugin {
	@Override
	public String getFooter() {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();

		String redirect = CONFIG.frontendContextRoot + "redirect/plugins/HttpRedirect/error.html";

		return "<div style='float:right'>" +
				"<a id='popularCities' style='text-decoration:none;color:black;margin-top:10px;margin-left:10px;'" +
				"href='" + redirect + "'>Popular Cities</a><br/>" +
				"<a id='popularCities1' style='text-decoration:none;color:black;font-size:8px;margin-left:20px;'" +
				"href='" + redirect + "'>Madrid for $310</a><br/>" +
				"<a id='popularCities2' style='text-decoration:none;color:black;font-size:8px;margin-left:20px;'" +
				"href='" + redirect + "'>London for $260</a><br/>" +
				"<a id='popularCities3' style='text-decoration:none;color:black;font-size:8px;margin-left:20px;'" +
				"href='" + redirect + "'>New York for $230</a>" +
				"</div>";
	}
}
