package com.dynatrace.diagnostics.uemload.headless;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.DemoUserData;
import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.ExtendedCommonUser.ExtendedCommonUserBuilder;
import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.headless.HeadlessVisit.HeadlessGetAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.diagnostics.uemload.utils.AngularSearchParameters;
import com.dynatrace.easytravel.misc.CommonUser;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class HeadlessVisitTest {
	
	private HeadlessVisit visit = new HeadlessVisit(null) {
		
		@Override
		public String getVisitName() {
			return null;
		}
		
		@Override
		public Action[] getActions(CommonUser user, Location location) {
			return null;
		}
	};

	@Test
	public void testCorrectAngularSearchActions() {
		List<Action> actions = visit.getAngularSearchActions("Paris", "2018-06-05", "2018-06-19", "1");
		Assert.assertTrue(actions.size() == 5);
	}
	
	@Test
	public void testNullParamsAngularSearchActions() {
		List<Action> actions = visit.getAngularSearchActions("Paris", null, null, null);
		Assert.assertTrue(actions.size() == 2);
	}
	
	@Test
	public void testWrongAngularSearchActions() {
		List<Action> actions;	
		actions = visit.getAngularSearchActions("Paris", "20189-06-05", "2018-066-19", "5");
		Assert.assertTrue(actions.size() == 2);
		actions = visit.getAngularSearchActions("Paris", "2018-30-05", "2018-06-60", "1");
		Assert.assertTrue(actions.size() == 3);
	}
	
	@Test
	public void testGetAngularCreditCardActions() {
		AngularSearchParameters params = new AngularSearchParameters();
		CommonUser user;
		List<Action> actions;
		boolean stopBookingProcess;
		
		//normal user, plugin off, booking being stopped does not matter
		testCreditCardActionsCombination(params, false, ExtendedDemoUser.MARIA_USER, false, 6);
		testCreditCardActionsCombination(params, false, ExtendedDemoUser.MARIA_USER, true, 6);
		
		//normal user, plugin on, booking stopped or not
		testCreditCardActionsCombination(params, true, ExtendedDemoUser.MARIA_USER, false, 8);
		testCreditCardActionsCombination(params, true, ExtendedDemoUser.MARIA_USER, true, 5);
		
		//special user, booking stopped, plugin does not matter
		testCreditCardActionsCombination(params, true, ExtendedDemoUser.MONTHLY_USER_1, true, 5);
		testCreditCardActionsCombination(params, false, ExtendedDemoUser.MONTHLY_USER_1, true, 5);
		
		//special user, booking not stopped, plugin does not matter
		testCreditCardActionsCombination(params, true, ExtendedDemoUser.MONTHLY_USER_1, false, 5);
		testCreditCardActionsCombination(params, false, ExtendedDemoUser.MONTHLY_USER_1, false, 5);
	}
	
	private void testCreditCardActionsCombination(
			AngularSearchParameters params, boolean pluginState, CommonUser user, boolean stopBookingProcess, int expectedResult) {
		PluginChangeMonitor.setupPlugin(PluginChangeMonitor.Plugins.ANGULAR_BIZ_EVENTS_PLUGIN, pluginState);
		List<Action> actions = visit.getAngularCreditCardActions(params, stopBookingProcess, user);
		Assert.assertTrue(
				"The number of actions added by getAngularCreditCardActions is wrong. It should be " + expectedResult + ", but it is: " + actions.size(), 
				actions.size() == expectedResult);
	}
	
	@Test
	public void testUTMParamsUrl(){
		String host = "http://localhost:9079";
		String urlParamsRegex = "http://localhost:9079\\?utm_source=[0-9a-zA-Z]+&utm_medium=[0-9a-zA-Z]+&utm_campaign=[0-9a-zA-Z&=_-]+&gclid=[0-9A-Za-z]+";
		
		int plainUrlCount = 0;
		int urlWithParamsCount = 0;
		int n = 40;
		for(int i=0; i<n; i++) {
			String url = new HeadlessGetAction(host).getUrl();
			if(url.contentEquals(host)) {
				plainUrlCount++;
			} else if (url.matches(urlParamsRegex)) {
				urlWithParamsCount++;
			}
		}

		int totalURLs = plainUrlCount + urlWithParamsCount;
		assertEquals("Invalid total number of URLs: " + totalURLs, totalURLs, n);
		assertTrue("Invalid number of plain URLs: " + plainUrlCount, plainUrlCount >= 10);
		assertTrue("Invalid number of URLs with params: " + urlWithParamsCount, urlWithParamsCount >= 10);
	}	
}
