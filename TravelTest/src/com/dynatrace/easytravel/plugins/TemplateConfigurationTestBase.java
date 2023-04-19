/**
 *
 */
package com.dynatrace.easytravel.plugins;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.spring.EventObject;
import com.dynatrace.easytravel.spring.PluginNotificationTemplate;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author tomasz.wieremjewicz
 * @date 6 gru 2017
 *
 */
public class TemplateConfigurationTestBase {
	protected static final String ENTITY = "test entity";
	protected static final String TITLE = "test title";
	protected static final String VERSION = "test version";
	protected static final String SOURCE = "test source";
	protected static final String DEPLOYMENTPROJECT="test deploymentProject";
	protected static final String CIBACKLINK="test ciBackLink";
	protected static final String REMEDIATIONACTION="test remediationAction";

	public class MyHttpRunnable implements HTTPResponseRunnable {
		int serverContactCount = 0;

		@Override
		public Response run(String uri, String method, Properties header, Properties parms) {
			if (method.equals("POST") && uri.equals("/api/v1/events/")){
				try {
					String body = parms.getProperty("postBody");
					ObjectMapper mapper = new ObjectMapper();
					EventObject event = mapper.readValue(body, EventObject.class);

					if (event != null && event.attachRules != null && event.attachRules.entityIds != null
							&& Arrays.equals(event.attachRules.entityIds, new String[] {ENTITY})
							&& event.source.equals(SOURCE)
							&& event.deploymentName.equals(TITLE)
							&& event.deploymentVersion.equals(VERSION)) {
						serverContactCount++;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK.");
		}

		public int getServerContactCount() {
			return serverContactCount;
		}
	}

	public String createTemplateConfigurationJson(String pluginName) {
		String templateJson = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			PluginNotificationTemplate[] templates = new PluginNotificationTemplate[1];
			templates[0] = new PluginNotificationTemplate();
			templates[0].pluginNames = new String[] { pluginName };
			templates[0].entityIds = new String[] {ENTITY};
			templates[0].title = TITLE;
			templates[0].source = SOURCE;
			templates[0].version = VERSION;
			templates[0].deploymentProject = DEPLOYMENTPROJECT;
			templates[0].ciBackLink = CIBACKLINK;
			templates[0].remediationAction = REMEDIATIONACTION;
			templateJson = mapper.writeValueAsString(templates);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return templateJson;
	}

	public void configureEasytravel(MockRESTServer server) {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.apmServerWebPort= Integer.toString(server.getPort());
		config.apmTenant="test";
		config.apmServerHost="localhost";
		config.apmServerProtocol = "http";
		config.apmServerDefault= InstallationType.APM;
	}
}
