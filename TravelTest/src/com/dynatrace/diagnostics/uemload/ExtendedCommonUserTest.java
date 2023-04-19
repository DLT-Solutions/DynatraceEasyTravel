package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.util.ResourceFileReader;
import com.dynatrace.easytravel.util.TextUtils;
import com.dynatrace.easytravel.utils.TestEnvironment;

/**
 * 
 * @author Michal.Bakula
 *
 */

public class ExtendedCommonUserTest {
	
	private UEMLoadScenario scenario;
	
	private static String DIRECTORY = TestEnvironment.ABS_TEST_DATA_PATH+"/staticusers";
	
	private static final Map<String, RandomSet<ExtendedCommonUser>> USERS_DISTRIBUTION = createUsersFromCountries();
	
	@Before
	public void setup(){
		
		scenario = new UEMLoadScenario() {
			
			@Override
			protected IterableSet<Visit> createVisits() {
				return null;
			}
			
			@Override
			public Simulator createSimulator() {
				return null;
			}
			
			@Override
			public ExtendedCommonUser getRandomUser(String country){
				return USERS_DISTRIBUTION.get(country).getNext();
			}			
		};
		
		scenario.init();
	}

	private static Map<String, RandomSet<ExtendedCommonUser>> createUsersFromCountries(){				
		Map<String, RandomSet<ExtendedCommonUser>> tmpUsers = new HashMap<>();
		
		InputStream stream = null;
		try {
			stream = new FileInputStream(new File(DIRECTORY, ResourceFileReader.EXTENDEDUSERS));
		} catch (FileNotFoundException e) {
			fail(TextUtils.merge("Could not load {0} file from {1}", ResourceFileReader.EXTENDEDUSERS, DIRECTORY));
		}
		
		for(ExtendedCommonUser user : ExtendedCommonUser.getExtendedUsers(stream)) {
			String country = user.getLocation().getCountry();
			RandomSet<ExtendedCommonUser> rs = tmpUsers.get(country);
			if(rs == null){
				rs = new RandomSet<>();
				rs.add(user, user.getWeight());
			} else {
				rs.add(user, user.getWeight());
			}
			tmpUsers.put(country, rs);
		}

		return tmpUsers;
	}
	
	@Test
	public void staticUserParameteresTest(){
		ExtendedCommonUser user = scenario.getRandomUser("Poland");
		
		assertTrue("Extended user's full name wasn''t correct.", "Aleksandra Kucharska".equals(user.getFullName()));
		Location loc = user.getLocation();
		assertTrue("Extended user's location wasn''t correct.", "Europe".equals(loc.getContinent()) && "Poland".equals(loc.getCountry()) && "80.50.24.192".equals(loc.getIp()));
		assertTrue("Extended user's browsers wasn''t correct.", BrowserType.FF_520.equals(user.getRandomDesktopBrowser()) && BrowserType.MOBILE_SAFARI_8.equals(user.getMobileBrowser()));
		assertTrue("Extended user's resolution wasn''t correct.", BrowserWindowSize._1024x768.equals(user.getDesktopBrowserWindowSize()) && BrowserWindowSize._m800x480.equals(user.getMobileBrowserWindowSize()));
		assertTrue("Extended user's bandwidth wasn''t correct.", Bandwidth.DSL_MED.equals(user.getBandwidth()));
		
	}
	
	@Test
	public void visitorsIdTest(){
		ExtendedCommonUser user = scenario.getRandomUser("United Kingdom");
		
		Set<String> ids = new HashSet<>();
		for(int i=0;i<10;i++){
			ids.add(scenario.getVisitorInfo(user).createVisitorID().getVisitorId());
		}
		assertTrue("Returning user with static VisitorID assigned had more than one ID.", ids.size() == 1);
		ids.clear();
		
		user = scenario.getRandomUser("Japan");
		for(int i=0;i<10;i++){
			ids.add(scenario.getVisitorInfo(user).createVisitorID().getVisitorId());
		}
		assertTrue("Returning user without static VisitorID assigned didn''t have more than one ID even after 10 draws.", ids.size() > 1 && ids.size() < 10);
		ids.clear();
		
		user = scenario.getRandomUser("Poland");
		for(int i=0;i<10;i++){
			ids.add(scenario.getVisitorInfo(user).createVisitorID().getVisitorId());
		}
		assertTrue("New user didn''t always have new VisitorID.", ids.size() == 10);
	}
	
	@Ignore("Integration test for checking ExtendedUsers.txt file from deployment scripts ")
	@Test
	public void loadExtendedUsersFile() throws FileNotFoundException {
		assertTrue("Extended users list is empty", ExtendedCommonUser.getExtendedUsers(ResourceFileReader.getInputStream(ResourceFileReader.EXTENDEDUSERS)).size()>0);		
	}
}
