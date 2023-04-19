package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.misc.ProcedureControlPluginListener;
import com.dynatrace.easytravel.util.TextUtils;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;
import com.google.common.collect.Lists;


/**
 * Test for {@link AbstractUEMLoadSession}.
 * @author rafal.psciuk
 *
 */
// TBD:
// This is now de facto an PluginChangeMonitor test and no longer AbstractUEMLoadSessionTest
// To be tidy, we should strictly speaking do some re-architecture.
public class AbstractUEMLoadSessionTest {

	private static final Logger LOGGER = Logger.getLogger(AbstractUEMLoadSessionTest.class.getName());

	//lists of plugins monitored by PluginChangeMonitor
	private static final String[] ENABLED_PLUGINS_ALL = {
		BaseConstants.Plugins.DC_RUM_EMULATOR,
		BaseConstants.Plugins.TABLET_CRASHES,
		BaseConstants.Plugins.SLOW_TRANSACTION_FOR_PHP_BLOG,
		BaseConstants.Plugins.ADS_ENABLEMENT_PLUGIN,
		BaseConstants.Plugins.PHP_ENABLEMENT_PLUGIN,
		"WorldMapDNSFailsAsia",
		"StreamingMediaTraffic",
		"WorldMapDNSFailsEurope",
		"WorldMapDNSFailsUnitedStates",
		"SlowApacheWebserver",
		"NetworkPacketDrop",
		"CrashCouchDB",
		"AngularJSGalleryApplication",
		"MagentoShop",
		"LoadChange",
	};

	private static final String[] ENABLED_PLUGINS_SHORTLIST_NET = {
		"NetworkPacketDrop"
	};

	private static final String[] ENABLED_PLUGINS_SHORTLIST_COUCH1 = {
		// We need another plugin here, in addition to CrashCouchDB, so there is a
		// callback on plugin change, even if the CrashCouchDB plugin has not changed.
		"NetworkPacketDrop",
		"CrashCouchDB"
	};

	private static final String[] ENABLED_PLUGINS_SHORTLIST_COUCH2 = {
		// We need another plugin here, in addition to CrashCouchDB, so there is a
		// callback on plugin change, even if the CrashCouchDB plugin has not changed.
		"SlowApacheWebserver",
		"CrashCouchDB"
	};

	/**
	 * Response that contains no plugins.
	 */
	private final static String[] NO_PLUGINS = {};

	/**
	 * @author rafal.psciuk
	 * HTTPResponseRunnable implementation for MockRESTServer.
	 * The run method simulates REST service responses with different plugins enabled: ConfigurationService/getEnabledPluginNames
	 */
	private class PluginsHttpRunnable implements HTTPResponseRunnable {
		//list of responses. Each response contains list of plugin names.
		private final List<String[]> responses;
		//current response id
		private int idx = 0;

		//Counts number of calls to run method. Used to stop test after certain number of calls.
		private final AtomicInteger runCnt = new AtomicInteger(0);

		private PluginsHttpRunnable(List<String[]> responses) {
			assertFalse("List of responses cannot be empty", responses.isEmpty());
			this.responses = responses;
		}

		@Override
		public Response run(String uri, String method, Properties header, Properties parms) {
			LOGGER.fine("Response number: "+ idx);
			String[] pluginNames = responses.get(idx);

			//get next response position. If this is last response use it again
			if(idx < responses.size()-1) {
				idx++;
			}

			String xmlResponse = getResponseXml(pluginNames);
			LOGGER.info("xml response: " + xmlResponse);
			runCnt.incrementAndGet();
			return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, xmlResponse);
		}

		/**
		 * @return number of calls to run method
		 */
		public int getRunCnt() {
			return runCnt.get();
		}

		/**
		 * Generate xml for given list of plugins
		 * @param pluginNames
		 * @return
		 */
		private String getResponseXml(String[] pluginNames) {
			StringBuffer sb = new StringBuffer();
			for(String name : pluginNames) {
				sb.append(TextUtils.merge("<ns:return>{0}</ns:return>", name));
			}

			String xmlResponse = TextUtils.merge("<xml>{0}</xml>", sb.toString());
			return xmlResponse;
		}
	}

	/**
	 * Mock class implementing PluginChangeListener
	 *
	 * Listens to whether specific procedure-control plugins are ON or OFF.
	 * At the moment we only operate it for CouchDB, as this is currently
	 * the only procedure we can turn on and off by means of a plugin.
	 */
	private class PluginChangeListenerImpl extends ProcedureControlPluginListener {

		@Override
		protected StatefulProcedure getCouchDBProcedure() {
			// Return our mock StatefulProcedure, instead of the real
		 	// CouchDB procedure.
			// This way, if the listener detects the CrashCouchDB plugin being off,
			// it will start the procedure that this method returns, and vice versa.
			return myStatefulProcedure;
		}
		
		 //Should be atomic, since it is updated/read from different threads
		private final AtomicInteger counter = new AtomicInteger();

		@Override
		public void pluginsChanged() {
			counter.incrementAndGet();
			super.pluginsChanged();
		}

		/**
		 * @return number of calls to pluginsChanged method
		 */
		int getCounter() {
			return counter.get();
		}
	}
	
	/**
	 * Mock stateful procedure.
	 *
	 */
	private StatefulProcedure myStatefulProcedure = new StatefulProcedure(null) {
		
		State myState = State.OPERATING;
		
		@Override
		protected void addDefaultStopListener() {
			// mock noop
		}

		@Override
		public Feedback stop() {
			setState(State.STOPPING);
			return Feedback.Success;
		}
		
		@Override
		public Feedback run() {
			setState(State.OPERATING);
			return Feedback.Success;
		}
		
		@Override
		public boolean isRunning() {
			return (getState() == State.OPERATING);
		}
		
		@Override
		public void setState(State newState) {
			myState = newState;
		}
		
		@Override
		public State getState() {
			return myState;
		}
		
		@Override
		public String getName() {
			return "unitTest";
		}
	};
				
			
	@Before
	public void setup() throws IOException, InterruptedException {
		//clear list of plugins for initial state
		List<String[]> responses = Lists.newArrayList();
		responses.add(NO_PLUGINS);

		PluginChangeListenerImpl listener = new PluginChangeListenerImpl();
		
		PluginChangeMonitor.registerForPluginChanges(listener);

		//wait for plugin list to be refreshed
		testPluginChangeNotifications(responses);
	}
	
	/**
	 * Simulate enabling of each plugin from ENABLED_PLUGINS_ALL list.
	 * Each call to mocked REST service will return one plugin name.
	 * Test: verify how many times PluginChangeListener was called
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testAllPluginStateChanges() throws IOException, InterruptedException {
		//create responses for PluginsHttpRunnable
		List<String[]> responses = Lists.newArrayList();
		responses.add(NO_PLUGINS); //no plugins enabled
		responses.add(new String[]{"somePlugin"}); //dummy plugin, no notification expeced
		for(String name : ENABLED_PLUGINS_ALL){ //each response will contain one plugin enabled
			responses.add(new String[] {name});
		}
		responses.add(NO_PLUGINS); //no plugins enabled
		PluginChangeListenerImpl  listener = new PluginChangeListenerImpl ();
		PluginChangeMonitor.registerForPluginChanges(listener);

		//wait for plugin list to be refreshed
		testPluginChangeNotifications(responses);

		//check how many times PluginChangeListenerImpl was called
		assertEquals("PluginChangeListenerImpl should be called exact number of times", ENABLED_PLUGINS_ALL.length+1, listener.getCounter());
	}

	/**
	 * Test: verify all paths of enabling/disabling CouchDB, using the
	 * ProcedureControl plugin. We need one additional plugin in the list, so that we
	 * can test the following scenario:
	 * 1. Change the state of the ProcedureControl procedure manually
	 *    (by directly accessing the running flag)
	 * 2. Change another plugin,
	 * 3. See that the ProcedureControl plugin will not try to undo the manual
	 *    changes we did, i.e. see that CouchDB is not started or stopped as a result
	 *    of step 2.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testProcedureControlPluginStateChanges() throws IOException, InterruptedException {
	
		//create responses for PluginsHttpRunnable
		List<String[]> responsesNet = Lists.newArrayList();
		List<String[]> responsesCouch1 = Lists.newArrayList();
		List<String[]> responsesCouch2 = Lists.newArrayList();
		List<String[]> responsesEmpty = Lists.newArrayList();
		
		responsesNet.add(ENABLED_PLUGINS_SHORTLIST_NET);
		responsesCouch1.add(ENABLED_PLUGINS_SHORTLIST_COUCH1);
		responsesCouch2.add(ENABLED_PLUGINS_SHORTLIST_COUCH2);
		responsesEmpty.add(NO_PLUGINS);
	
		//=================================================
		// IMPORTANT:
		//
		// re. testing plugin list:
		// It is essential that the list is every time different than it was
		// the last time, else there will be no callback to the listener
		// and we will be testing nothing.
		//=================================================
		
		// Test notifications for plugin list: NetworkPacketDrop.
		// At the start of the test, CouchDB is simulated as running
		// (our mock Stateful procedure will return isrunning() as true).
		// If we now simulate a single NetworkPackageDrop plugin as being ON,
		// then we are at the same time saying that CrashCouchDB is OFF,
		// and so CouchDB should be running.  However, it already IS running,
		// so no change in state is expected.
		assertTrue(myStatefulProcedure.isRunning());
		testPluginChangeNotifications(responsesNet);
		assertTrue(myStatefulProcedure.isRunning());
		
		// Test notifications for plugin list: NetworkPacketDrop + CrashCouchDB.
		// CrashCouchDB plugin will be activated, thus CouchDB procedure should stop.
		testPluginChangeNotifications(responsesCouch1);
		assertFalse(myStatefulProcedure.isRunning());
		
		// Test notifications for plugin list: NetworkPacketDrop.
		// CouchDB should start again.
		testPluginChangeNotifications(responsesNet);
		assertTrue(myStatefulProcedure.isRunning());
		
		// Test notifications for plugin list: NetworkPacketDrop + CrashCouchDB.
		// CrashCouchDB should stop again.
		testPluginChangeNotifications(responsesCouch1);
		assertFalse(myStatefulProcedure.isRunning());

		// Manually starting CouchDB
		myStatefulProcedure.run();
		assertTrue(myStatefulProcedure.isRunning());
		
		// Test notifications for plugin list: SlowApacheWebserver + CrashCouchDB.
		// CrashCouchDB will be active, however, we are not required to stop
		// CouchDB if it was started manually since the last time
		// it was stopped by the plugin.
		testPluginChangeNotifications(responsesCouch2);
		assertTrue(myStatefulProcedure.isRunning());
		
		// Test notifications for plugin list: NetworkPacketDrop.
		// Nothing should change, as CouchDB was running
		// and now it should also be running, as
		// the CrashCouchDB plugin is OFF.
		testPluginChangeNotifications(responsesNet);
		assertTrue(myStatefulProcedure.isRunning());

		// Manually stopping CouchdB.
		myStatefulProcedure.stop(); // stop CouchDB
		assertFalse(myStatefulProcedure.isRunning());
		
		// Test notifications for plugin list: Empty.
		// The CrashCouchDB plugin is not ON, however CouchDB
		// was stopped manually, so we do not restart it.
		testPluginChangeNotifications(responsesEmpty);
		assertFalse(myStatefulProcedure.isRunning());
	}

	/**
	 * Run MockRESTServer with given {@link PluginsHttpRunnable}.
	 * This method blocks until MockRESTServer is called number of times equal to responses.size()
	 * There is also global timeout after which this method will return anyway.
	 * @param responses - responses to be returned by MockRESTServer
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void testPluginChangeNotifications(List<String[]> responses) throws IOException, InterruptedException {
		PluginsHttpRunnable httpRunnable = new PluginsHttpRunnable(responses);
		MockRESTServer server = new MockRESTServer(httpRunnable);
		try {
			EasyTravelConfig.read().webServiceBaseDir = "http://localhost:" + server.getPort() + "/";

			//wait for thread that checks plugin state to execute several times
			waitForRunnable(httpRunnable, responses.size()+1, 2*60*1000);
			
		} finally {
			server.stop();
		}
	}

	/**
	 * Waits for given PluginsHttpRunnable to be completed.
	 * @param runnable
	 * @param numberOfCalls - after this number of calls to runnable method will return
 	 * @param timeout - maximum time to wait for runnable
	 */
	private void waitForRunnable(PluginsHttpRunnable runnable, int numberOfCalls, long timeout) throws InterruptedException{
		long start = System.currentTimeMillis();
		while(runnable.getRunCnt()<numberOfCalls || numberOfCalls == 0){
			Thread.sleep(5*1000);
			if(System.currentTimeMillis()-start > timeout) {
				fail(TextUtils.merge("PluginsHttpRunnable was not called {0} times in {1} seconds", numberOfCalls, timeout/1000));
			}
		}
	}
}
