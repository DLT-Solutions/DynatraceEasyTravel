/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BasicIntegrationTest.java
 * @date: 14.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.integration;

import static com.dynatrace.easytravel.html.helpers.HtmlUnitHelpers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.plugin.RemotePluginController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * Integration Test which executes the HtmlUnitTest testcase
 *
 * @author dominik.stadler
 */
public class PluginsIntegrationTest {
	private static final Logger log = LoggerFactory.make();

	private RemotePluginController controller = new RemotePluginController();

	// some plugins do not allow a successful booking any more or fail for some other reason, see TODOs below
	private static Set<String> exlcudedPlugins = new HashSet<String>();
	static {
		exlcudedPlugins.add("DeadlockInFrontend");
		exlcudedPlugins.add("LargeMemoryLeak");
		exlcudedPlugins.add("DatabaseAccessExceedPoolThreaded");
		exlcudedPlugins.add("CTGNativeApplication");
		exlcudedPlugins.add("CreditCardCheckError500");
		exlcudedPlugins.add("DotNetPaymentService");
		exlcudedPlugins.add("DummyPaymentService");
		exlcudedPlugins.add("HiddenIframeAmazonTracking");		// JavaScript error in Amazon related code: Cannot read property "length" from undefined (https://images-na.ssl-images-amazon.com/images/G/01/browser-scripts/site-wide-js-1.2.6-beacon/site-wide-11313043667._V1_.js#2749)
		exlcudedPlugins.add("IncludeSocialMedia"); 				// TODO: fails with: 403 Forbidden for http://localhost:8380/js/GlobalProd.js
		exlcudedPlugins.add("InfiniteLoopInFrontend");
		exlcudedPlugins.add("JourneyUpdateFast"); 				// TODO: why does search fail here?
		exlcudedPlugins.add("JourneyUpdateSlow"); 				// TODO: why does search fail here?
		exlcudedPlugins.add("LoginProblems");
		exlcudedPlugins.add("NamedPipeNativeApplication");
		exlcudedPlugins.add("PHPEnablementPlugin");				// TODO: why does it fail with this plugin?
		exlcudedPlugins.add("SlowAuthentication");				// expected to fail while logging in due to timeout
		exlcudedPlugins.add("SlowUserLogin");					// can time out the authentication step
		exlcudedPlugins.add("ThirdPartyContent");				// Javascript/Proxy issues
		exlcudedPlugins.add("UnhealthyThirdPartyService-CDN-03-Unhealthy-Outage");
		exlcudedPlugins.add("UnhealthyThirdPartyService-SocialMedia-03-Unhealthy-Outage");
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		IntegrationTestBase.cleanupAndStart(false, null, null);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		log.info("Finished testing, now shutting down launcher");
		IntegrationTestBase.stop();
	}

	@Test
	public void testTriggerAllPlugins() throws Throwable {
		List<String> failingPlugins = new ArrayList<String>();
		List<String> unsetFailingPlugins = new ArrayList<String>();
		String[] plugins = controller.requestAllPluginNames();
		Set<String> enabledPlugins = new HashSet<String>(Arrays.asList(controller.requestEnabledPluginNames()));
		for (String plugin : plugins) {
			// skip deadlock and memory leak plugin as it will make the frontend/backend unuseable
			if (exlcudedPlugins.contains(plugin)) {
				log.info("Not running test with excluded plugin: " + plugin);
				continue;
			}

			log.info("Testing plugin " + plugin);

			// enable/disable plugin depending on it's current state
			assertNotNull("Expected to receive something back when switching state of plugin " + plugin +
					", previous state was " + enabledPlugins.contains(plugin) + ", but got back null.",
					controller.sendEnabled(plugin, !enabledPlugins.contains(plugin), null));

			// wait up to

			// run one booking to trigger the plugin
			try {
				testBook(true);
			} catch (Throwable e) {
				log.error("Failed while having plugin " + plugin + ": " + (enabledPlugins.contains(plugin) ? "disabled" : "enabled"), e);
				failingPlugins.add(plugin);
				//throw new Exception("Failed while having plugin " + plugin + ": " + (enabledPlugins.contains(plugin) ? "disabled" : "enabled"), e);
			} finally {
				// undo the plugin enable/disable again
				controller.sendEnabled(plugin, enabledPlugins.contains(plugin), null);
			}

			// run a booking without the plugin to verify that it was undone successfully to avoid reporting
			// following plugins as failures when actually undoing the plugin did not work
			try {
				testBook(true);
			} catch (Throwable e) {
				log.error("Failed after re-setting plugin " + plugin + ": " + (enabledPlugins.contains(plugin) ? "enabled" : "disabled"), e);
				unsetFailingPlugins.add(plugin);
				//throw new Exception("Failed after re-setting plugin " + plugin + ": " + (enabledPlugins.contains(plugin) ? "enabled" : "disabled"), e);
			}
		}

		// check if we had any plugins that did fail
		assertTrue("Failing: " + failingPlugins.toString() +
				"\n\nUnset-Failing: " + unsetFailingPlugins.toString() +
				"\n\nPlugins: " + Arrays.toString(plugins) +
				"\n\nEnabled Plugins: " + enabledPlugins.toString(),
				failingPlugins.isEmpty() && unsetFailingPlugins.isEmpty());

		// run one final booking at the end to see if we still are stable enough to run a booking
		testBook(true);
	}

	@Test
	public void testTriggerFailing() throws Exception {
		// run some plugins which are failing, but still should not de-stabilize the applications
		// strange Javascript error? runFailing("CTGNativeApplication");
		runFailing("CreditCardCheckError500");
		runFailing("LoginProblems");
		runFailing("NamedPipeNativeApplication");
		runFailing("SlowAuthentication");	// expected to fail while logging in due to timeout
		runFailing("UnhealthyThirdPartyService-CDN-03-Unhealthy-Outage");
	}

	private void runFailing(String plugin) throws Exception {
		// enable/disable plugin depending on it's current state
		assertNotNull("Expected to receive something back when switching state of plugin " + plugin +
				" to enabled, but got back null.",
				controller.sendEnabled(plugin, true, null));

		// run one booking to trigger the plugin, do not expect credit card check to work
		try {
			testBook(false);
		} catch (Throwable e) {
			throw new Exception("While having plugin: " + plugin, e);
		}

		// undo the plugin enable/disable again
		controller.sendEnabled(plugin, false, null);

	}

	private void testBook(boolean expectValidCard) throws Exception {
		final WebClient webClient = createWebClient();

		HtmlPage page = getInitialPage(webClient);

		page = login(webClient, page, "demouser", "demopass");

		log.info("Running test booking");

		page = search(webClient, page, "maur");

		// now we need to have found at least "Mauritius"
		assertTrue("Should find 'Mauritius', but had: \n" + page.getPage().asXml(),
				page.getPage().asXml().contains("Mauritius"));

		page = book(webClient, page, "0123456789", expectValidCard);

		logout(webClient, page);

		webClient.closeAllWindows();
	}
}
