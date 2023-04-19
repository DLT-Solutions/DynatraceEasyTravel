package com.dynatrace.diagnostics.uemload.headless;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.junit.Ignore;
import org.junit.Test;
import org.littleshoot.proxy.HttpProxyServer;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;

public class DriverEntryFactoryTest {
	
	@Test
	public void testHttpClientWithLocalServer() throws IOException {
		AtomicBoolean called = new AtomicBoolean(false);
		HTTPRunnable httpRunnable = (uri, method, header, params) -> called.set(true);
		final MockRESTServer server = new MockRESTServer(httpRunnable, NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		
		try {
			EasyTravelConfig config  = EasyTravelConfig.read();
			config.thirdPartyDomains = new String[] {"localhost"};			
						
			HttpProxyServer proxyServer = new DriverEntryFactory().runProxyServer( new DriverEntry("1.1.1.1") );
			HttpHost proxy = new HttpHost(proxyServer.getListenAddress().getAddress().getHostAddress(), proxyServer.getListenAddress().getPort());
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);			
			CloseableHttpClient httpClient = HttpClients.custom().setRoutePlanner(routePlanner).build();
			
			String url = String.format("http://localhost:%d", server.getPort());
			HttpGet request = new HttpGet(url);
			CloseableHttpResponse response = httpClient.execute(request);
			
			assertTrue(Arrays.stream(response.getAllHeaders()).filter( h -> h.getName().equals("Content-Length") && h.getValue().equals("54015")).findAny().isPresent());
			assertFalse("Server called", called.get());
			response.close();
			httpClient.close();
		} finally {
			EasyTravelConfig.resetSingleton();
			server.stop();
		}
	}	
	
	
	@Test
	public void testGetEmptyBytes() {
		assertTrue("Empty image size is 0", DriverEntryFactory.EMPTY_IMAGE_BYTES.length > 0);
	}
	
	@Test
	@Ignore("run this for manual testing of prox server. Set proxy host and port in the system settings")
	public void runProxy() throws InterruptedException {
		HttpProxyServer proxyServer = new DriverEntryFactory().runProxyServer( new DriverEntry("1.1.1.1") );
		System.out.println(proxyServer.getListenAddress().getPort());
		Thread.sleep(5*60*1000);
	}
	
}
