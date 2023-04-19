package com.dynatrace.easytravel.components;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;

public class ComponentManagerAccessTest {
	private MockRESTServer server;

	@Test
	public void testSettingAndGettingComponent() throws Exception {
		final String type = "frontend";
		final String ip = "172.18.18.18";
		
		server = new MockRESTServer(new HTTPResponseRunnable() {
			boolean isComponentsListEmpty = true;

			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {

				if (uri.startsWith(("/services/ConfigurationService/setComponent"))) {
					isComponentsListEmpty = !isExpectedParameterFound(ip, parms);

					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK.");
				} else if (uri.startsWith("/services/ConfigurationService/getComponentsIPList")) {

					if (isExpectedParameterFound(type, parms) && !isComponentsListEmpty) {
						return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "<ns:return>" + ip + "</ns:return>");
					}
				}
				return new Response(NanoHTTPD.HTTP_INTERNALERROR, NanoHTTPD.MIME_PLAINTEXT, "ERROR.");
			}

			private boolean isExpectedParameterFound(String param, Properties parms) {
				for (Map.Entry<Object, Object> entry : parms.entrySet()) {
					System.out.println("Entry: "+entry.getKey()+" "+entry.getValue());
					if (entry.getValue().toString().equals(param)) {
						return true;
					}
				}
				return false;
			}
		}

		);
		
		EasyTravelConfig.read().webServiceBaseDir="http://localhost:"+server.getPort()+"/services/";
		ComponentManagerAccess rcm = new ComponentManagerAccess();
		rcm.setComponent(ip, new String[] { "Vagrant", type });

		String[] components = rcm.getComponentsIPList(type);

		assertTrue(components[0].equals(ip));
		
		EasyTravelConfig.resetSingleton();
	}
}
