package com.dynatrace.easytravel;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Level;


public class DotNetPaymentServiceTest {
	@Test
	public void testCallPaymentService() throws IOException {
		DotNetPaymentService service = new DotNetPaymentService();

		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "VALID:");
		try {
			EasyTravelConfig.read().dotNetBackendWebServiceBaseDir = "http://localhost:" + server.getPort() + "/";
			service.callPaymentService("123", "234235", "asasdf", 23.223, "someloc", "tenant");
		} finally {
			server.stop();
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testCallPaymentServiceNoHost() throws IOException {
		DotNetPaymentService service = new DotNetPaymentService();

		int port = SocketUtils.reserveNextFreePort(9000, 9010, null);
		try {
			EasyTravelConfig.read().dotNetBackendWebServiceBaseDir = "http://localhost:" + port + "/";
			service.callPaymentService("123", "234235", "asasdf", 23.223, "someloc", "tenant");
			fail("Should throw exception here because we cannot connect to the .NET payment backend");
		} catch (IOException e) {
			// the information should be included in the exception
			TestHelpers.assertContains(e, "localhost", Integer.toString(port));
		} finally {
			SocketUtils.freePort(port);
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testWithDifferentLogLevel() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {
			@Override
			public void run() {
				try {
					testCallPaymentService();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}, DotNetPaymentService.class.getName(), Level.DEBUG);
	}
}
