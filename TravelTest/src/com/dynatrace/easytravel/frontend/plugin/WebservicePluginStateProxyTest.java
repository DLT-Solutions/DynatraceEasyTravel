/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: WebservicePluginStateProxyTest.java
 * @date: 07.01.2012
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.frontend.plugin;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;

/**
 *
 * @author dominik.stadler
 */
public class WebservicePluginStateProxyTest {
	private static final Logger LOGGER = LoggerFactory.make();

	private final AtomicReference<String> failed = new AtomicReference<String>("");

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

    @After
    public void tearDown() throws InterruptedException {
    	// ensure we do not leave a thread running in one of the tests
    	//ThreadTestHelper.waitForThreadToFinish("Cyclic-Plugin-Manager-Thread");

    	// make sure we undo any changes to the in-memory config-settings
		EasyTravelConfig.resetSingleton();
	}


	/**
	 * Test method for {@link com.dynatrace.easytravel.frontend.plugin.WebservicePluginStateProxy#registerPlugins(java.lang.String[])}.
	 */
	@Test
	public void testRegisterPluginsWithoutServer() {
		WebservicePluginStateProxy proxy = new WebservicePluginStateProxy();

		// these fail with log-output
		assertNull(proxy.getAllPluginNames());
		assertNull(proxy.getAllPlugins());
		assertNull(proxy.getEnabledPluginNames());
		assertNull(proxy.getEnabledPluginNamesForHost("somehost"));
		assertNull(proxy.getEnabledPlugins());
		assertNull(proxy.getEnabledPluginsForHost("somehost"));

		proxy.registerPlugins(new String[] {});
		proxy.setPluginEnabled("plugin", true);
		proxy.setPluginHosts("name", new String[] {"somehost"});
		proxy.setPluginTemplateConfiguration("someconfiguration");
	}

	@Test
	public void testRegisterPlugins() throws IOException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", "registerPlugins", header, null, null, "");
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", "plugin1", header, null, null, "");
				} else if(count == 2) {
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", null, header, "content-type", "action=\"urn:getAllPluginNames\"", "getAllPluginNamesResponse");
				} else if(count == 3) {
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", null, header, "content-type", "action=\"urn:getAllPlugins\"", "getAllPluginsResponse");
				} else if(count == 4) {
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", null, header, "content-type", "action=\"urn:getEnabledPluginNames\"", "getEnabledPluginNamesResponse");
				} else if(count == 5) {
					expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", "getEnabledPluginNamesForHost", header, null, null, null);
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", "somehost", header, null, null, "getEnabledPluginNamesForHostResponse");
				} else if(count == 6) {
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", null, header, "content-type", "action=\"urn:getEnabledPlugins\"", "getEnabledPluginsResponse");
				} else if(count == 7) {
					expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", "getEnabledPluginsForHost", header, null, null, "");
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", "somehost", header, null, null, "getEnabledPluginsForHostResponse");
				} else if(count == 8) {
					expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", "setPluginEnabled", header, null, null, "");
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", "plugin123", header, null, null, "setPluginEnabledResponse");
				} else if(count == 9) {
					expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", "setPluginHosts", header, null, null, "");
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", "somehost", header, null, null, "setPluginHostsResponse");
				} else if(count == 10) {
					expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", "setPluginTemplateConfiguration", header, null, null, "");
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/", "someconfiguration", header, null, null, "setPluginTemplateConfigurationResponse");
				} else {
					failed.set(failed.get() + "\nUnexpected uri: " + uri);
					return null;
				}
			}
		});

		try {
			// adjust the port in the config
			EasyTravelConfig.read().backendPort=server.getPort();
			EasyTravelConfig.read().webServiceBaseDir="http://localhost:" + server.getPort() + "/services/";

			WebservicePluginStateProxy proxy = new WebservicePluginStateProxy();
			assertNotNull(proxy);

			// hangs! both sides expect to read more data!?
			proxy.registerPlugins(new String[] {"plugin1"});
			assertTrue("Should not have failed in the HTTPResponseRunnable, but did:" + failed.get(), failed.get().isEmpty());

			assertNotNull(proxy.getAllPluginNames());
			assertTrue("Should not have failed in the HTTPResponseRunnable, but did:" + failed.get(), failed.get().isEmpty());

			assertNotNull(proxy.getAllPlugins());
			assertTrue("Should not have failed in the HTTPResponseRunnable, but did:" + failed.get(), failed.get().isEmpty());

			assertNotNull(proxy.getEnabledPluginNames());
			assertTrue("Should not have failed in the HTTPResponseRunnable, but did:" + failed.get(), failed.get().isEmpty());

			assertNotNull(proxy.getEnabledPluginNamesForHost("somehost"));
			assertTrue("Should not have failed in the HTTPResponseRunnable, but did:" + failed.get(), failed.get().isEmpty());

			assertNotNull(proxy.getEnabledPlugins());
			assertTrue("Should not have failed in the HTTPResponseRunnable, but did:" + failed.get(), failed.get().isEmpty());

			assertNotNull(proxy.getEnabledPluginsForHost("somehost"));
			assertTrue("Should not have failed in the HTTPResponseRunnable, but did:" + failed.get(), failed.get().isEmpty());

			proxy.setPluginEnabled("plugin123", true);
			assertTrue("Should not have failed in the HTTPResponseRunnable, but did:" + failed.get(), failed.get().isEmpty());

			proxy.setPluginHosts("name", new String[] {"somehost"});
			assertTrue("Should not have failed in the HTTPResponseRunnable, but did:" + failed.get(), failed.get().isEmpty());

			proxy.setPluginTemplateConfiguration("someconfiguration");
			assertTrue("Should not have failed in the HTTPResponseRunnable, but did:" + failed.get(), failed.get().isEmpty());
		} finally {
			server.stop();

			EasyTravelConfig.resetSingleton();
		}
	}

	private Response expectUriContains(String uri, String params, String contains, String paramsContain,
			Properties header, String headerContains, String headerContainsValue, String responseElement) {
		try {
			LOGGER.debug("Had: " + uri + " and params " + params + ", expecting " + contains + " and " + paramsContain);
			assertTrue("Expected string '" + contains + "' not found in uri: " + uri,
					uri.contains(contains));
			assertTrue("Expected params '" + paramsContain + "' not found in params: " + params,
					paramsContain == null || params.contains(paramsContain));
			assertTrue("Had headers: " + header + ", expected " + headerContains + " = " + headerContainsValue,
					headerContains == null ||
						(header.containsKey(headerContains) && header.getProperty(headerContains).contains(headerContainsValue)));

			return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT,
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body>" +
							"<web:" + responseElement + " xmlns:web=\"http://webservice.business.easytravel.dynatrace.com\"><web:host>somehost</web:host></web:" + responseElement + ">" +
					"</soapenv:Body></soapenv:Envelope>");
		} catch (Throwable e) {
			failed.set(failed.get() + "\n" + e.toString());
			throw new RuntimeException(e);
		}
	}

}
