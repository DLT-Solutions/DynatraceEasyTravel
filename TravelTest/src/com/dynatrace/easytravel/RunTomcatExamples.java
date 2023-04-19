package com.dynatrace.easytravel;

import java.net.MalformedURLException;
import java.util.logging.Logger;

import org.apache.catalina.LifecycleException;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.tomcat.Tomcat7Config;
import com.dynatrace.easytravel.tomcat.Tomcat7Starter;

public class RunTomcatExamples {
    private static final Logger log = Logger.getLogger(RunTomcatExamples.class.getName());

    private static final int PORT = 8080;

    private final String webAppBase = "webapp";

    /**
     * Starts the embedded Tomcat server.
     *
     * @throws LifecycleException
     * @throws MalformedURLException if the server could not be configured
     * @throws LifecycleException if the server could not be started
     * @throws MalformedURLException
     * @throws Exception
     */
    public void run() throws Exception {
    	Tomcat7Config config = new Tomcat7Config.Tomcat7ConfigBuilder()
    			.withPort(PORT)
    			.withWebappBase(webAppBase)
    			.withCookies(true)
    			.withParentClass(this.getClass())
    			.build();
    	new Tomcat7Starter().run(config);

    	log.info("Application fully started.");
        while(true) {
        	Thread.sleep(50000);
        }
    }

    public static void main(String[] args) throws Exception {
        new RunTomcatExamples().run();
    }
}
