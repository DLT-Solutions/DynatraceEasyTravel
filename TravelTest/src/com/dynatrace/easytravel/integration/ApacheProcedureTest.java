package com.dynatrace.easytravel.integration;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.NavigationTiming;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadHttpClient;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.engine.ApacheHttpdProcedure;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;

import ch.qos.logback.classic.Logger;

public class ApacheProcedureTest {
	private static final Logger LOGGER = LoggerFactory.make();

	private static MockRESTServer server;

	@BeforeClass
	public static void setUpClass() throws IOException {
		fail("Cannot run without adjusting HttpdConfSetup!");
		/*
				newLines.add(line
						//.replace("ajp://", "http://")
						);

				// replace localhost with remote customer frontend host if necessary
				if(line.contains("ajp://localhost:")) {
					// TODO: is this useful for anything except Customer Frontend? No other procedure uses the ajp-protocol
					for(String host : remoteHosts) {
						newLines.add(line.replace(
								"ajp://localhost:",
								//"http://" + host + ":"));
								"ajp://" + host + ":"));
					}
				}
		 */

		LoggerFactory.initLogging();

		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");
		//System.setProperty("com.dynatrace.easytravel.host.additional", "dynasprint,notexisting");
		System.setProperty("com.dynatrace.easytravel.host.additional", "dynasprint.lab.dynatrace.org");

	    final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		IntegrationTestBase.checkPort(CONFIG.apacheWebServerPort);
		IntegrationTestBase.checkPort(CONFIG.apacheWebServerB2bPort);
		IntegrationTestBase.checkPort(CONFIG.apacheWebServerProxyPort);
		if(CONFIG.apacheWebServerStatusPort != -1) {
			IntegrationTestBase.checkPort(CONFIG.apacheWebServerStatusPort);
		}

		server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		EasyTravelConfig.read().frontendPortRangeStart = server.getPort();
		EasyTravelConfig.read().frontendPortRangeEnd = server.getPort()+10;

		EasyTravelConfig.read().frontendAjpPortRangeStart = server.getPort();
		EasyTravelConfig.read().frontendAjpPortRangeEnd = server.getPort()+10;
	}

	@AfterClass
	public static void tearDownClass() {
		if(server != null) {
			server.stop();
		}
	}

	//private boolean stop = false;

	@Test
	public void test() throws Exception {
		ApacheHttpdProcedure proc = new ApacheHttpdProcedure(new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_ID));

		proc.run();

		try {
			LOGGER.info("Waiting for Apache to fully start up");
			while(!proc.isOperating()) {
				Thread.sleep(100);
			}

			LOGGER.info("Apache is operating, now starting to run requests");

			EasyTravelConfig config = EasyTravelConfig.read();
			Random random = new Random(System.currentTimeMillis());

			// warm up request
			runRequest(config, random);

			for(int i = 0;i < 1000;i++) {
				if(i % 30 == 0) {
					System.out.println();
					LOGGER.info(i + ": ");
				}

				long duration = runRequest(config, random);
				assertTrue("Expected duration lower than 10000, but had: " + duration,
						duration < 10000);

			}
		} finally {
			System.out.println();

			proc.stop();
		}
		LOGGER.info("Done");
	}

	protected long runRequest(EasyTravelConfig config, Random random) throws IOException {
		long start = System.currentTimeMillis();

		UemLoadHttpClient client = new UemLoadHttpClient(Bandwidth.values()[random.nextInt(Bandwidth.values().length)], BrowserType.FF_530);
		HttpRequest request = new HttpRequest("http://localhost:" + config.apacheWebServerPort);

		client.execute(request, NavigationTiming.start(), new HttpResponseCallback() {
			@Override
			public void readDone(HttpResponse response) throws IOException {
			}
		}, null);

		client.close();

		long duration = System.currentTimeMillis() - start;
		System.out.print(duration + "ms ");

		return duration;
	}
}
