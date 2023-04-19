package com.dynatrace.easytravel.rest.tomcat;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.frontend.tomcat.FrontendTomcatStarter;

public class RunFrontendRESTTomcat {
	
	private static final String LOGGER_TAG = BaseConstants.LoggerNames.ANGULAR_FRONTEND;
	
	private RunFrontendRESTTomcat() {
		throw new IllegalAccessError();
	}

    public static void main(String[] args) throws Exception {
    	FrontendTomcatStarter frontendTomcatStarter = new FrontendTomcatStarter(LOGGER_TAG);
    	frontendTomcatStarter.start(args);
    }
}
