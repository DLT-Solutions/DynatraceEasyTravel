package com.dynatrace.easytravel.plugin;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.integration.IntegrationTestBase;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.RESTConstants;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.plugins.PluginService;
import com.dynatrace.easytravel.spring.PluginList;
import com.dynatrace.easytravel.util.DtVersionDetector;


public class PluginTestBase {
    @BeforeClass
    public static void setUpPluginTestBase() throws IOException {
		LoggerFactory.initLogging();

		DtVersionDetector.enforceInstallationType(InstallationType.Classic);

		// if configured, start up the plugin service
    	EasyTravelConfig config = EasyTravelConfig.read();

		if(config.pluginServiceHost != null) {
    		IntegrationTestBase.checkPort(config.pluginServicePort);

    		try {
				PluginService.main(new String[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }

    @AfterClass
    public static void tearDownPluginTestBase() throws IOException {
    	// ensure that we stop to request plugin state now
    	PluginList.stopRefreshThread();

    	// if configured, tear down the plugin service
    	EasyTravelConfig config = EasyTravelConfig.read();
		if(config.pluginServiceHost != null && !SocketUtils.isPortAvailable(config.pluginServicePort, null)) {
			UrlUtils.retrieveData("http://localhost:" + config.pluginServicePort + "/" + RESTConstants.SHUTDOWN);

			while(!SocketUtils.isPortAvailable(config.pluginServicePort, null)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new IOException(e);
				}
			}

    		IntegrationTestBase.checkPort(config.pluginServicePort);
		}
    }
}
