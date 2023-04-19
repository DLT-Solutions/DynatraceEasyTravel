package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.visit.BookingJourneyVisit;
import com.dynatrace.diagnostics.uemload.mobileopenkit.visit.MobileVisits;
import com.dynatrace.diagnostics.uemload.openkit.visit.OpenKitVisit;
import com.dynatrace.diagnostics.uemload.utils.UserFileGenerator;
import com.dynatrace.diagnostics.uemload.Location;
import com.google.common.collect.Sets;

/**
 * Test for OpenKitMobileAppScenario class.
 * 
 * @author maria.rolbiecka
 *
 */
public class OpenKitMobileAppScenarioTest {
	private OpenKitMobileAppScenario scenario;
	private static final Logger LOGGER = Logger.getLogger(OpenKitMobileAppScenario.class.getName());
	
	@BeforeClass
	public static void setup(){
		UserFileGenerator generator = new UserFileGenerator();
		generator.generateUserFile();
	}
	
	@Before
	public void init() {
		scenario = new OpenKitMobileAppScenario() {

			@Override
			protected IterableSet<Visit> createVisits() {
				return null;
			}

			@Override
			public Simulator createSimulator() {
				return null;
			}
		};
		scenario.init();
	}
	
	/**
	 * Check that for Hainer user BookingJourneyVisit is always generated.
	 */
	@Test
	public void testGetRandomVisitTypeForHainerUser() {
		ExtendedCommonUser user = ExtendedDemoUser.HAINER_USER;
		MobileVisits visitType = scenario.getRandomVisitType(user);
		assertTrue(visitType == MobileVisits.BOOKING_JOURNEY_VISIT);
	}
	
	/**
	 * Check that for random user any mobile visit type is generated.
	 */
	@Test
	public void testGetRandomVisitTypesForRandomUsers() {
		Set<ExtendedCommonUser> users = getRandomUsers();
		Set<MobileVisits> visits = Sets.newHashSet();
		for(ExtendedCommonUser randomUser : users) {
			visits.add(scenario.getRandomVisitType(randomUser));
		}

		assertTrue("Only one or none visit types found ", visits.size() > 1);
		LOGGER.warning("Found visits types: " + visits);		
	}

	/**
	 * Check that users in OpenKit mobile load generator are returned correctly.
	 */
	@Test
	public void testGetRandomMobileUser() {
		Set<ExtendedCommonUser> users = null;
		
		PluginChangeMonitor.setupPlugin(PluginChangeMonitor.Plugins.ANGULAR_BOOKING_ERROR_500, Boolean.FALSE);
		users = getRandomUsers();
		assertFalse("Hainer user should not be returned by getRandomMobileUser(), when Angular booking error plugin is disabled", 
				users.contains(ExtendedDemoUser.HAINER_USER));
		
		PluginChangeMonitor.setupPlugin(PluginChangeMonitor.Plugins.ANGULAR_BOOKING_ERROR_500, Boolean.TRUE);
		users = getRandomUsers();
		assertTrue("Hainer user should be returned by getRandomMobileUser(), when Angular booking error plugin is enabled", 
				users.contains(ExtendedDemoUser.HAINER_USER));
	}
	
	private Set<ExtendedCommonUser> getRandomUsers() {
		Location location = null;
		Set<ExtendedCommonUser> users = Sets.newHashSet();
		for(int i=0;i<50;i++) {
			location = scenario.getRandomLocation();
			users.add(scenario.getRandomMobileUser(location.getCountry()));
		}
		return users;
	}
}
