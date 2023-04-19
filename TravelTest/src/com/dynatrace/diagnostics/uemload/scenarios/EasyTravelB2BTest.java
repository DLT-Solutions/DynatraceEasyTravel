package com.dynatrace.diagnostics.uemload.scenarios;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.utils.UserFileGenerator;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.google.common.collect.Sets;

/**
 * Test for {@link EasyTravelB2B} class
 * 
 * @author rafal.psciuk
 *
 */
public class EasyTravelB2BTest {

	private EasyTravelB2B scenario;

	private static final Set<String> b2bUserNames = Sets.newHashSet("Speed Travel Agency", "Personal Travel Inc.", "Thomas Chef", "TravelNiche Ltd.");

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
		scenario = new EasyTravelB2B(false);
		scenario.init();
	}

	/**
	 * Test if getRandomUser method returns correct values for {@link EasyTravelB2B} sceanrio
	 */
	@Test
	public void testGetUsers() {

		Set<String> names = Sets.newHashSet();
		for(int i=0; i<100; i++) {
			CommonUser user = scenario.getRandomUser("whatever");
			assertTrue("Unexpected user returned",b2bUserNames.contains(user.getName()));
			names.add(user.getName());
		}

		assertTrue("Not all b2b users returned",names.size() == b2bUserNames.size());
	}
}
