package com.dynatrace.diagnostics.uemload.mobileopenkit.visit;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.UemLoadScheduler;
import com.dynatrace.diagnostics.uemload.mobileopenkit.MobileSimulator;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.search.MobileSearchTouchCrashActionSet;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.parameters.MobileOpenKitParams;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitSimulator;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.OpenKitMobileAppScenario;

@Ignore("Integreation test")
public class MobileVisitIntegrationTest {
	private static final String apiUrl = "http://localhost:9080/easytravel/rest";
	private static final ExtendedCommonUser user = ExtendedDemoUser.DEMOUSER2;
	
	private OpenKitMobileAppScenario scenario = new OpenKitMobileAppScenario();
	private MobileOpenKitParams params = scenario.getRandomParams(user);
	private MobileSimulator simulator = new MobileSimulator(scenario);
	
	@Test
	public void testBookingVisit() throws InterruptedException {
		BookingJourneyVisit bookJourneyVisit = new BookingJourneyVisit(params, user, apiUrl);
		OpenKitSimulator.ActionRunner<MobileActionType> actionRunner = new OpenKitSimulator.ActionRunner<MobileActionType>(bookJourneyVisit, simulator);
		actionRunner.run();	
		waitForUemLoadFinish();
	}
	
	@Test
	public void testSearchVisitWithError() throws InterruptedException {
		MobileSearchTouchCrashActionSet.eachVisitShoudGenerateMobileError();
		generateSearchVisit();
	}
	
	@Test 
	public void testSearchVisitWithCrash() throws InterruptedException {
		MobileSearchTouchCrashActionSet.eachVisitShoudGenerateMobileOrTabletCrash();
		generateSearchVisit();
	} 
	
	@Test
	public void testContinously() throws Exception {
		int cnt = 0;
		while(true) {
			System.out.println("Visit nr: " + (++cnt));
			generateSearchVisit();
		}
	}
	
	private void generateSearchVisit() throws InterruptedException {
		SearchJourneyVisit searchVisit = new SearchJourneyVisit(params, user, apiUrl);
		OpenKitSimulator.ActionRunner<MobileActionType> actionRunner = new OpenKitSimulator.ActionRunner<MobileActionType>(searchVisit, simulator);
		actionRunner.run();	
		waitForUemLoadFinish();		
	}
	
	private void waitForUemLoadFinish() throws InterruptedException {
		while(UemLoadScheduler.getActiveCount() > 0 || UemLoadScheduler.getQueueSize() > 0 ) {
			Thread.sleep(1000);
			System.out.println("getActiveCount " + UemLoadScheduler.getActiveCount() + " getQueueSize " + UemLoadScheduler.getQueueSize());
		}
		Thread.sleep(5000);
	}	
}
