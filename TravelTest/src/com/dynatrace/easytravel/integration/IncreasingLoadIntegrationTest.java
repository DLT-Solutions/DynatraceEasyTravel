/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BasicIntegrationTest.java
 * @date: 14.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.integration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.BrowserWindowSize;
import com.dynatrace.diagnostics.uemload.DemoUserData;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.EasytravelStartPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.EasytravelStartPage.State;
import com.dynatrace.diagnostics.uemload.utils.Journeys;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.CommonUser;


/**
 * Integration Test which executes the HtmlUnitTest testcase
 *
 * @author dominik.stadler
 */
public class IncreasingLoadIntegrationTest {
	private static final Logger log = LoggerFactory.make();

	@BeforeClass
	public static void setUpClass() throws Exception {
		IntegrationTestBase.cleanupAndStart(false, MessageConstants.SCENARIO_GROUP_TESTCENTER_TITLE, MessageConstants.TESTCENTER_SCENARIO_INCREASING_LOAD_TITLE,
				Arrays.asList(new String[] {
						"All easyTravel procedures started successfully",
						"Increasing load feature started",
				}));
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		log.info("Finished testing, now shutting down launcher");
		IntegrationTestBase.stop();
	}

	@Test
	public void test() throws Exception {
		// wait some time to get some increasing load
		//Thread.sleep(10*60*1000);
	}

	@Test
	public void testSearch() throws IOException, InterruptedException {
		Browser browser = new Browser(BrowserType.FF_530, new Location("Asia", "Tokio", "192.182.0.22"), 0, Bandwidth.BROADBAND, BrowserWindowSize._1024x768);
		CustomerSession session = new CustomerSession("http://localhost:" + EasyTravelConfig.read().frontendPortRangeStart,
				new CommonUser(DemoUserData.MARIA, DemoUserData.MARIA),
				new Location("Asia", "Tokio", "192.182.0.22"),
				false);

		final CountDownLatch latch = new CountDownLatch(1);
		EasytravelStartPage page = new EasytravelStartPage(session, State.SEARCH);

		page.enablePartialResponseLogging();

		page.visit(browser, new UEMLoadCallback() {

			@Override
			public void run() throws IOException {
			}
		});

		page.login(browser, "user1", "user1", 0, new UEMLoadCallback() {

			@Override
			public void run() throws IOException {
			}

		});

		page.search(browser, "New London", 0, new UEMLoadCallback() {

			@Override
			public void run() throws IOException {
				latch.countDown();
			}
		});

		browser.close();

		latch.await();
		assertThat(session.getJourneyId(), is(not(Journeys.NO_JOURNEY_FOUND)));
	}
}
