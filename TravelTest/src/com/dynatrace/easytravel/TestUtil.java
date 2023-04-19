package com.dynatrace.easytravel;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import org.junit.Assert;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.BasicLoggerConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.logging.RootLogger;

public class TestUtil {
	private static final Logger LOGGER = LoggerFactory.make();
	
	public static void setInstallDirCorrection() {
	    System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");
        LOGGER.warn("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));    	
	}

    public static String getCustomerFrontendUrl() {
        return "http://" + getCustomerFrontendHost() + ":" + getCustomerFrontendPort();
    }


    public static String getCustomerFrontendHost() {
        String host = System.getProperty("customerFrontendHost");
        if (host == null || host.isEmpty()) {
            return "localhost";
        }
        return host;
    }


    public static String getCustomerFrontendPort() {
        String port = System.getProperty("customerFrontendPort");
        if (port == null || port.isEmpty()) {
            return Integer.toString(EasyTravelConfig.read().frontendPortRangeStart);
        }
        return port;
    }


    public static String getBusinessBackendUrl() {
        return "http://" + getBusinessBackendHost() + ":" + getBusinessBackendPort();
    }


    public static String getBusinessBackendHost() {
        String host = System.getProperty("businessBackendHost");
        if (host == null || host.isEmpty()) {
            return EasyTravelConfig.read().backendHost;
        }
        return host;
    }


    public static String getBusinessBackendPort() {
        String port = System.getProperty("businessBackendPort");
        if (port == null || port.isEmpty()) {
            return Integer.toString(EasyTravelConfig.read().backendPort);
        }
        return port;
    }

    public static String getB2BFrontendUrl() {
        return "http://" + getB2BFrontendHost() + ":" + getB2BFrontendPort();
    }


    public static String getB2BFrontendHost() {
        String host = System.getProperty("b2bFrontendHost");
        if (host == null || host.isEmpty()) {
            return "localhost";
        }
        return host;
    }


    public static String getB2BFrontendPort() {
        String port = System.getProperty("b2bFrontendPort");
        if (port == null || port.isEmpty()) {
            return Integer.toString(EasyTravelConfig.read().b2bFrontendPortRangeStart);
        }
        return port;
    }

	public static File detectTravelTestSrcDir() {
		File src = new File("../TravelTest/src");
		if (src.isDirectory()) {
			return src;
		}
		src = new File("../../../TravelTest/src"); // if it's executed in Distribution/dist/test
		if (src.isDirectory()) {
			return src;
		}
		// detection failed - where am I?
		System.out.println("Can't find TravelTest/src");
		try {
			System.out.println("I am here: " + new File(".").getCanonicalPath());
		} catch (IOException e) {
			System.out.println("I am here: " + new File(".").getAbsolutePath());
		}
		Assert.fail("Cannot find TravelTest src dir");
		return null;
	}

	public static String getJavaFileName(Class<?> clazz) {
		return clazz.getName().replace('.', '/') + ".java";
	}
	
	/**
	 * Call this in setupClass to enable logging in locally executed tests.  
	 * Additionally add  -Dlogback.configurationFile=path_to_logback.xml to the VM arguments in run configuration of the test
	 * @throws IOException 
	 */
	public static void setupLocalLogging() {
		try {
			LoggerFactory.initLogging();
			RootLogger.setup(new BasicLoggerConfig("test"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
    public static void waitWhileTrue( Supplier<Boolean> s, int retries) throws InterruptedException {
    	int cnt = 0;
    	while( s.get() && cnt++ < retries) {
    		Thread.sleep(500);
     	}
    	
    	if(cnt > retries) {
    		throw( new IllegalStateException("waitWhileTrue didn't finished in " + retries + " retries"));
    	}
    }
    
    public static void waitWhileNotTrue( Supplier<Boolean> s, int retries) throws InterruptedException {
    	Supplier<Boolean> s1 = () -> !s.get();
    	waitWhileTrue(s1, retries);
    }
}
