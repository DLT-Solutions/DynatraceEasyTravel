package com.dynatrace.easytravel.plugins;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.components.ComponentController;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.pluginagentbb.PluginAgentBB;
import com.dynatrace.easytravel.pluginagentcf.PluginAgentCF;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.SpringTestBase;
import com.dynatrace.easytravel.spring.pluginagent.PluginAgent;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

/**
 * @author wojtek.jarosz
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class PluginAgentTest extends SpringTestBase {

	private static final EasyTravelConfig config = EasyTravelConfig.read();

	@After
	public void tearDown() {
		EasyTravelConfig.resetSingleton();
	}
	
	@Mock
	public ComponentController componentController;

	@SuppressWarnings("serial")
	@Test
	public void testPluginAgent() throws Exception {
		VagrantBoxHTTPRunnable response = new VagrantBoxHTTPRunnable();
		MockRESTServer server = new MockRESTServer( response);

		try {
			config.pluginAgentURLCF = "http://localhost:"+ server.getPort() + "/" + config.pluginAgentContext;

			when(componentController.getEnabledComponents()).thenReturn(new ArrayList<String>() {{
			    add(config.pluginAgentURLCF);
			}});
			
			PluginAgentCF pluginAgentCF = new PluginAgentCF();
			pluginAgentCF.setComponentController(componentController);
			
			pluginAgentCF.doExecute( PluginConstants.LIFECYCLE_PLUGIN_ENABLE,(Object[]) null);
			pluginAgentCF.doExecute(PluginConstants.FRONTEND_JOURNEY_SEARCH, (Object[]) null);
			pluginAgentCF.doExecute( PluginConstants.LIFECYCLE_PLUGIN_DISABLE,(Object[]) null);
			pluginAgentCF.waitForShutdown();
			assertTrue( response.getNumberOfRequests() > 0 );						
									
			config.pluginAgentURLBB = "http://localhost:"+ server.getPort() + "/" + config.pluginAgentContext;

			when(componentController.getEnabledComponents()).thenReturn(new ArrayList<String>() {{
			    add(config.pluginAgentURLBB);
			}});
			
			PluginAgentBB pluginAgentBB = new PluginAgentBB();
			pluginAgentBB.setComponentController(componentController);
			
			response.resetRequestsNumber();
			pluginAgentBB.doExecute( PluginConstants.LIFECYCLE_PLUGIN_ENABLE,(Object[]) null);
			pluginAgentBB.doExecute(PluginConstants.BACKEND_JOURNEY_SEARCH, (Object[]) null);
			pluginAgentBB.doExecute( PluginConstants.LIFECYCLE_PLUGIN_DISABLE,(Object[]) null);
			pluginAgentBB.waitForShutdown();
			assertTrue( response.getNumberOfRequests() > 0 );
									
		} finally {
			server.stop();
		}
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testMultipleThreads() throws IOException, InterruptedException {
		VagrantBoxHTTPRunnable response = new VagrantBoxHTTPRunnable( 5000 );
		MockRESTServer server = new MockRESTServer( response);

		try {
			config.pluginAgentURLCF = "http://localhost:"+ server.getPort() + "/" + config.pluginAgentContext;
			
			when(componentController.getEnabledComponents()).thenReturn(new ArrayList<String>() {{
			    add(config.pluginAgentURLCF);
			}});
			
			PluginAgentCF pluginAgentCF = new PluginAgentCF();
			pluginAgentCF.setComponentController(componentController);
			
			pluginAgentCF.doExecute( PluginConstants.LIFECYCLE_PLUGIN_ENABLE,(Object[]) null);
			
			for( int i=0; i<PluginAgent.MAXIMUM_POOL_SIZE*2; i++ ) {
				pluginAgentCF.doExecute(PluginConstants.FRONTEND_JOURNEY_SEARCH, (Object[]) null);	
			}
			
			Thread.sleep( 1000 );
			int activeRequests = getNumberOfPluginThreads();
			assertTrue( activeRequests > 0 );
			assertTrue( activeRequests <= PluginAgent.MAXIMUM_POOL_SIZE );
			
			pluginAgentCF.doExecute( PluginConstants.LIFECYCLE_PLUGIN_DISABLE,(Object[]) null);
			pluginAgentCF.waitForShutdown();
			
		} finally {
			server.stop();
		}
	}
	
	private int getNumberOfPluginThreads() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		FluentIterable<Thread> pluginThraeds = FluentIterable.from(threadSet).filter( new Predicate<Thread>() {

			@Override
			public boolean apply(Thread thread) {				
				return thread.getName().startsWith("PluginAgentThread-");
			}
			
			@Override
			public boolean test(Thread thread) {
				return apply(thread);
			}
		});
		return pluginThraeds.size();
	}

		
	private static class VagrantBoxHTTPRunnable implements HTTPResponseRunnable {

		// Note: To see the complete response as transmitted to the socket,
		// look at NanoHTTPD.sendResponse().

		// ==================================================================
		// Set up mock server responses
		// ==================================================================

		AtomicInteger requestsNumber = new AtomicInteger(0);
		private long processingDealy = 0;
		
		VagrantBoxHTTPRunnable() {			
		}
		
		VagrantBoxHTTPRunnable( long processingDelay ) {
			this.processingDealy = processingDelay;
		}

		@Override
		public Response run(String uri, String method, Properties header, Properties parms) {

			if (method.equals("GET") && uri.contains("load/gateway")) {

				requestsNumber.incrementAndGet();
				process();				
				
				// This is the transmission we are waiting for.
				// We return an acknowledgement.
				// It does not really matter what we reply right now, as the
				// plugin will just read it and throw it away.
				return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, "application/json",
						"[\"Three rings for the elven kings.\"]");

			} else {
				throw new IllegalStateException("Unexpected REST-call: <" + uri + ">");
			}
		}
		
		int getNumberOfRequests() {
			return requestsNumber.get();
		}
		
		void resetRequestsNumber() {
			requestsNumber.set(0);
		}
		
		private void process() {
			if (processingDealy > 0 ) {
				try {
					Thread.sleep(processingDealy);
				} catch (InterruptedException e) {					
					e.printStackTrace();
				}
			}
		}
	}		
}
