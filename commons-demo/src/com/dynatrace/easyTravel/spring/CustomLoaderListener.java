
package com.dynatrace.easytravel.spring;

import javax.servlet.ServletContextEvent;

import org.springframework.web.context.ContextLoaderListener;

import com.dynatrace.easytravel.util.EnvVariablesParser;
import com.dynatrace.easytravel.util.MySqlVcapService;


public class CustomLoaderListener extends ContextLoaderListener {
	/**
	 * Initialize the root web application context.
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {

		EnvVariablesParser.parseEnv();
		
		MySqlVcapService.parseEnv("easyTravel-Business*");
		
		super.contextInitialized(event);
	}
}