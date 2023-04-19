package com.dynatrace.diagnostics.uemload.scenarios;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.DemoUserData;
import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.SampledRandomLocation;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.UEMLoadScenario;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.utils.UserFileGenerator;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.google.common.collect.Sets;

/**
 * Test for {@link UEMLoadScenario} class.
 * 
 * @author rafal.psciuk
 *
 */
public class UEMLoadScenarioTest {
	private UEMLoadScenario scenario;

	private SampledRandomLocation locations;
	
	@BeforeClass
	public static void setup(){
		UserFileGenerator generator = new UserFileGenerator();
		generator.generateUserFile();
	}
	
	@AfterClass
	public static void cleanup(){
		File f = new File(Directories.getConfigDir(), ResourceFileReader.EXTENDEDUSERS);
		f.delete();
	}

	@Before
	public void init() {
		locations = new SampledRandomLocation();
		scenario = new UEMLoadScenario() {

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
	 * Test that locations returned for demo users are always the same.
	 */
	@Test
	public void testGetLocation() {
		// check that different locations are returned
		Set<String> continentAndCountry = Sets.newHashSet();
		Set<String> users = Sets.newHashSet();
		for(int i=0;i<24;i++){
			for (int j = 0; j < 5; j++) {
				Location loc = locations.get(i, 0);
				continentAndCountry.add(loc.getCountryAndContinent());
				ExtendedCommonUser user = scenario.getRandomUser(loc.getCountry());
				users.add(user.getName());
			}
		}

		assertTrue("There should be more than 2 users", users.size() > 2);
		assertTrue("There should be more than 2 locations", continentAndCountry.size() > 2);

		// check that the same location is returned for demo users MARIA and MONICA
		int mariaAppearance=0, monicaAppearance=0;
		for (int i = 0; i < 24; i++) {
			for (int j = 0; j < 500; j++) {
				Location loc = locations.get(i, 0);
				ExtendedCommonUser user = scenario.getRandomUser(loc.getCountry());
				if(user.equals(ExtendedDemoUser.MARIA_USER)){
					mariaAppearance++;
					assertSame("Maria's location is wrong", DemoUserData.MARIA_USER.getLocation(), user.getLocation());
				}
				if(user.equals(ExtendedDemoUser.MONICA_USER)){
					monicaAppearance++;
					assertSame("Monica's location is wrong", DemoUserData.MONICA_USER.getLocation(), user.getLocation());
				}
			}
		}
		assertTrue("Maria didn't appear in test.", mariaAppearance > 0);
		assertTrue("Monica didn't appear in test.", monicaAppearance > 0);
	}

	/**
	 * Test if demo users are always using the same {@link BrowserType}
	 */
	@Test
	public void testGetBrowser() {
		int mariaAppearance=0, monicaAppearance=0;
		for (int i = 0; i < 24; i++) {
			for (int j = 0; j < 500; j++) {
				Location loc = locations.get(i, 0);
				ExtendedCommonUser user = scenario.getRandomUser(loc.getCountry());
				if(user.equals(ExtendedDemoUser.MARIA_USER)){
					mariaAppearance++;
					assertSame("Maria's browser is wrong", DemoUserData.MARIA_USER.getBrowser(), user.getRandomDesktopBrowser());
				}
				if(user.equals(ExtendedDemoUser.MONICA_USER)){
					monicaAppearance++;
					assertSame("Monica's browser is wrong", DemoUserData.MONICA_USER.getBrowser(), user.getRandomDesktopBrowser());
				}
			}
		}
		assertTrue("Maria didn't appear in test.", mariaAppearance > 0);
		assertTrue("Monica didn't appear in test.", monicaAppearance > 0);
	}

	/**
	 * Test if demo user are always using the same mobile device type
	 */
	@Test
	public void testGetMobileDeviceType() {
		int mariaAppearance=0, monicaAppearance=0;
		for (int i = 0; i < 24; i++) {
			for (int j = 0; j < 500; j++) {
				Location loc = locations.get(i, 0);
				ExtendedCommonUser user = scenario.getRandomUser(loc.getCountry());
				if(user.equals(ExtendedDemoUser.MARIA_USER)){
					mariaAppearance++;
					assertSame("Maria's mobile device is wrong", DemoUserData.MARIA_USER.getMobileDevice(), user.getMobileDevice());
				}
				if(user.equals(ExtendedDemoUser.MONICA_USER)){
					monicaAppearance++;
					assertSame("Monica's mobile device is wrong", DemoUserData.MONICA_USER.getMobileDevice(), user.getMobileDevice());
				}
			}
		}
		assertTrue("Maria didn't appear in test.", mariaAppearance > 0);
		assertTrue("Monica didn't appear in test.", monicaAppearance > 0);
	}

	/**
	 * Test if demo users are always using the same browser window size
	 */
	@Test
	public void testGetBrowserWindowSize() {
		int mariaAppearance=0, monicaAppearance=0;
		for (int i = 0; i < 24; i++) {
			for (int j = 0; j < 500; j++) {
				Location loc = locations.get(i, 0);
				ExtendedCommonUser user = scenario.getRandomUser(loc.getCountry());
				if(user.equals(ExtendedDemoUser.MARIA_USER)){
					mariaAppearance++;
					assertSame("Maria's screen resolution is wrong", DemoUserData.MARIA_USER.getBrowserWindowSize(), user.getDesktopBrowserWindowSize());
				}
				if(user.equals(ExtendedDemoUser.MONICA_USER)){
					monicaAppearance++;
					assertSame("Monica's screen resolution is wrong", DemoUserData.MONICA_USER.getBrowserWindowSize(), user.getDesktopBrowserWindowSize());
				}
			}
		}
		assertTrue("Maria didn't appear in test.", mariaAppearance > 0);
		assertTrue("Monica didn't appear in test.", monicaAppearance > 0);
	}

	/**
	 * Test user distribution with different loads
	 */
	@Test
	public void testUserDistribution() {
		HashMap<String, Integer> users = new HashMap<>();

		/*
		 * 100 visits/min load during 1 hour test
		 */
		users.put("maria", 0);
		users.put("monica", 0);
		users.put("demouser", 0);
		users.put("demouser2", 0);

		for (int i = 0; i < 24; i++) {
			for (int j = 0; j < 250; j++) {
				Location l = locations.get(i, 0);
				ExtendedCommonUser user = scenario.getRandomUser(l.getCountry());
				String name = user.getName();
				if (users.containsKey(name)) {
					int val = users.get(name);
					val++;
					users.put(name, val);
				} else {
					users.put(name, 1);
				}
			}
		}
		int sum = 0;
		sum = users.get("maria") + users.get("monica") + users.get("demouser") + users.get("demouser2");
		int usersLastSize = users.size();
		assertTrue("Demousers didn't appeared enough times.", sum > 110);

		/*
		 * 1000 visits/min load during 1 hour test
		 */
		users.clear();
		users.put("maria", 0);
		users.put("monica", 0);
		users.put("demouser", 0);
		users.put("demouser2", 0);
		for (int i = 0; i < 24; i++) {
			for (int j = 0; j < 2500; j++) {
				Location l = locations.get(i, 0);
				ExtendedCommonUser user = scenario.getRandomUser(l.getCountry());
				String name = user.getName();
				if (users.containsKey(name)) {
					int val = users.get(name);
					val++;
					users.put(name, val);
				} else {
					users.put(name, 1);
				}
			}
		}
		sum = 0;
		sum = users.get("maria") + users.get("monica") + users.get("demouser") + users.get("demouser2");
		assertTrue("Demousers didn't appeared enough times.", sum > 1100);
		assertTrue("Increased load didn't generate significat increase in number of different users.",
				3 * usersLastSize < users.size());
	}
}
