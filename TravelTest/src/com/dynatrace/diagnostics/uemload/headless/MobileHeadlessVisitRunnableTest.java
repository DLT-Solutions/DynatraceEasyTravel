/**
 *
 */
package com.dynatrace.diagnostics.uemload.headless;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author tomasz.wieremjewicz
 * @date 22 sty 2019
 *
 */
public class MobileHeadlessVisitRunnableTest {
	Logger logger = LoggerFactory.make();
	HeadlessVisitTestUtil visitTestUtil;
	private static final String easytravelUrl = "SECRET";

	@Before
	public void setUp() {
 		visitTestUtil = new HeadlessVisitTestUtil();
 		HeadlessVisitTestUtil.setup(true);
	}

	@After
	public void tearDown() throws InterruptedException {
		visitTestUtil.tearDown(true);
	}

	@Ignore("Integration test")
	@Test
	public void testMobileHeadlessRunnable() {
		HeadlessAngularAlmostConvertedVisit visit = new HeadlessAngularAlmostConvertedVisit(easytravelUrl);
		ExtendedCommonUser user = ExtendedDemoUser.getUserWithGivenMobileDevice(MobileDeviceType.SAMSUNG_GALAXY_S9);
		HeadlessVisitTestUtil.runHeadlessVisit(visit, user, true);
	}
}
