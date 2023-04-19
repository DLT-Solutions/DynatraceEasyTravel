package com.dynatrace.easytravel.components;

import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;

public class RemoteComponentManagerTest {
	private MockRESTServer server;
	
	@Test
	public void testSettingAndGettingComponent() throws Exception{
		final String type = "frontend";
		final String ip = "172.18.18.18";
		server = new MockRESTServer(new HTTPResponseRunnable(){
			boolean isComponentsListEmpty = true;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				if(uri.equals("/PluginService/setComponent/"+ip)){
					isComponentsListEmpty = false;
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK.");
				} else if(uri.equals("/PluginService/getComponentsIPList/"+type)){
					if(!isComponentsListEmpty){		
						return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "["+ip+"]");
					}
				}
				return new Response(NanoHTTPD.HTTP_INTERNALERROR, NanoHTTPD.MIME_PLAINTEXT, "ERROR.");		
			}
			
		});
		RemoteComponentManager rcm = new RemoteComponentManager("localhost", server.getPort());
		rcm.setComponent(ip, new String[]{"Vagrant", type});
		
		String[] components = rcm.getComponentsIPList(type);
		
		assertTrue(components[0].equals(ip));
	}
}
