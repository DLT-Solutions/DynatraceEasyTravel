package com.dynatrace.easytravel.spring;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

/**
 * Tests for PluginEventNotifier
 *
 * @author tomasz.wieremjewicz
 * @date 20 lis 2017
 *
 */
public class PluginEventNotifierTest {
	private final static String ApiToken = "testToken123";
	private final static String PluginName1 = "PluginName1";
	private final static String PluginName2 = "PluginName2";
	private final static String PluginName3 = "PluginName3";
	private final static String PluginName4 = "PluginName4";
	private final static String PluginName5 = "PluginName5";
	private final static String Entity1 = "Entity1";
	private final static String Entity2 = "Entity2";
	private final static String Entity3 = "Entity3";
	private final static String Entity4 = "Entity4";
	private final static String Entity5 = "Entity5";

	private Map<String, String[]> pluginToEntitiesMapping;

	private ObjectMapper mapper;

	public PluginEventNotifierTest() {
		pluginToEntitiesMapping = new HashMap<>();
		mapper = new ObjectMapper();
	}

	@Test
	public void testSendPluginStateChangeEvent() {
		MockRESTServer server = null;
		DtVersionDetector.enforceInstallationType(InstallationType.APM);

		try {
			EasyTravelConfig config = EasyTravelConfig.read();
			MyHttpRunnable runnable = new MyHttpRunnable();
			server = new MockRESTServer(runnable);
			config.apmServerWebPort= Integer.toString(server.getPort());
			config.apmTenant="test";
			config.apmServerHost="localhost";
			config.apmTenantToken = ApiToken;
			config.apmServerProtocol = "http";
			config.apmServerDefault= InstallationType.APM;

			PluginEventNotifier pen = new PluginEventNotifier();
			pen.initializeTemplatesFromJson(createTemplates());
			assertEquals(4, pen.getEventObjectTemplates().size());

			PluginChangeInfo pci1 = new PluginChangeInfo(PluginName1, true),
					pci2 = new PluginChangeInfo(PluginName2, true),
					pci3 = new PluginChangeInfo(PluginName3, true),
					pci4 = new PluginChangeInfo(PluginName4, true),
					pci5 = new PluginChangeInfo(PluginName5, true);

	    	pen.sendPluginStateChangeEvent(pci1);
	    	assertEquals(1, runnable.getServerContactCount());

	    	pen.sendPluginStateChangeEvent(pci2);
	    	assertEquals(2, runnable.getServerContactCount());

	    	pen.sendPluginStateChangeEvent(pci3);
	    	assertEquals(3, runnable.getServerContactCount());

	    	pen.sendPluginStateChangeEvent(pci4);
	    	assertEquals(3, runnable.getServerContactCount());

	    	pen.sendPluginStateChangeEvent(pci5);
	    	assertEquals(4, runnable.getServerContactCount());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			EasyTravelConfig.resetSingleton();
			if (server != null) {
				server.stop();
			}
		}
	}

	@Test
	public void testSendPluginStateChangeEventWithClassic() {
		MockRESTServer server = null;
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);

		try {
			EasyTravelConfig config = EasyTravelConfig.read();
			MyHttpRunnable runnable = new MyHttpRunnable();
			server = new MockRESTServer(runnable);
			config.apmServerWebPort= Integer.toString(server.getPort());
			config.apmTenant="test";
			config.apmServerHost="localhost";
			config.apmTenantToken = ApiToken;
			config.apmServerProtocol = "http";
			config.apmServerDefault= InstallationType.Classic;

			PluginEventNotifier pen = new PluginEventNotifier();
			pen.initializeTemplatesFromJson(createTemplates());
			assertEquals(4, pen.getEventObjectTemplates().size());

			PluginChangeInfo pci1 = new PluginChangeInfo(PluginName1, true),
					pci2 = new PluginChangeInfo(PluginName2, true);

	    	pen.sendPluginStateChangeEvent(pci1);
	    	assertEquals(0, runnable.getServerContactCount());

	    	pen.sendPluginStateChangeEvent(pci2);
	    	assertEquals(0, runnable.getServerContactCount());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			EasyTravelConfig.resetSingleton();
			if (server != null) {
				server.stop();
			}
		}
	}

	private String createTemplates() throws JsonProcessingException {
		PluginNotificationTemplate[] templates = new PluginNotificationTemplate[5];
		templates[0] = new PluginNotificationTemplate();
		templates[0].pluginNames = new String[] {PluginName1, PluginName2};
		templates[0].entityIds = new String[] {Entity1};

		templates[1] = new PluginNotificationTemplate();
		templates[1].pluginNames = new String[] {PluginName3};
		templates[1].entityIds = new String[] {Entity2, Entity3, };

		templates[2] = new PluginNotificationTemplate();
		templates[2].pluginNames = new String[] {PluginName4};
		templates[2].entityIds = new String[] {};

		templates[3] = new PluginNotificationTemplate();
		templates[3].pluginNames = new String[] {};
		templates[3].entityIds = new String[] {Entity4};

		templates[4] = new PluginNotificationTemplate();
		templates[4].pluginNames = new String[] {PluginNotificationTemplate.DEFAULT_PLUGIN_NAME};
		templates[4].entityIds = new String[] {Entity5};

		templates[0].title = templates[1].title = templates[2].title = templates[3].title = templates[4].title = "_pluginName_";
		templates[0].source = templates[1].source = templates[2].source = templates[3].source = templates[4].source = "easyTravel";
		templates[0].version = templates[1].version = templates[2].version = templates[3].version = templates[4].version = "version";
		templates[0].deploymentProject = templates[1].deploymentProject = templates[2].deploymentProject = templates[3].deploymentProject = templates[4].deploymentProject = "action";
		templates[0].ciBackLink = templates[1].ciBackLink = templates[2].ciBackLink = templates[3].ciBackLink = templates[4].ciBackLink = "ciBackLink";
		templates[0].remediationAction = templates[1].remediationAction = templates[2].remediationAction = templates[3].remediationAction = templates[4].remediationAction = "remediationAction";

		pluginToEntitiesMapping.put(PluginName1,  new String[] {Entity1});
		pluginToEntitiesMapping.put(PluginName2,  new String[] {Entity1});
		pluginToEntitiesMapping.put(PluginName3,  new String[] {Entity2, Entity3});
		pluginToEntitiesMapping.put(PluginName5,  new String[] {Entity5}); //default will be used with PluginName5 plugin

		return mapper.writeValueAsString(templates);
	}

	class MyHttpRunnable implements HTTPResponseRunnable {
		int serverContactCount = 0;

		@Override
		public Response run(String uri, String method, Properties header, Properties parms) {
			if (method.equals("POST") && uri.equals("/api/v1/events/")){
				try {
					String body = parms.getProperty("postBody");
					ObjectMapper mapper = new ObjectMapper();
					EventObject event = mapper.readValue(body, EventObject.class);
					String auth = header.getProperty("authorization");

					if (!Strings.isNullOrEmpty(auth) && auth.equals("Api-Token " + ApiToken)
							&& event != null && event.attachRules != null && event.attachRules.entityIds != null
							&& Arrays.equals(event.attachRules.entityIds, pluginToEntitiesMapping.get(event.deploymentName))) {
						serverContactCount++;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK.");
		}

		int getServerContactCount() {
			return serverContactCount;
		}

	}
}
