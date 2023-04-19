package com.dynatrace.easytravel.frontend;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.frontend.tomcat.FrontendTomcatStarter;

public class RunFrontendTomcat {
	
	private static final String LOGGER_TAG = BaseConstants.LoggerNames.CUSTOMER_FRONTEND;
	
	private RunFrontendTomcat() {
		throw new IllegalAccessError();
	}
	
    public static void main(String[] args) throws Exception {
    	FrontendTomcatStarter frontendTomcatStarter = new FrontendTomcatStarter(LOGGER_TAG);
    	frontendTomcatStarter.start(args);
    }
}
