package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.scenarios.HeadlessAngularScenario;
import com.dynatrace.diagnostics.uemload.utils.UserFileGenerator;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.dynatrace.easytravel.util.SpecialUserData;
import com.dynatrace.easytravel.util.SpecialUserDataRow;

public class SimulatorTest {

	public static final String TEST_DATA_PATH = "../TravelTest/testdata";
	
	@BeforeClass
	public static void setup() {
		UserFileGenerator generator = new UserFileGenerator();
		generator.generateUserFile();
	}

	@AfterClass
	public static void cleanup() {
		File f = new File(Directories.getConfigDir(), ResourceFileReader.EXTENDEDUSERS);
		f.delete();
	}
	
	@Test
	public void checkUsersForEachCountry() throws NumberFormatException, IOException, InterruptedException {
		
		File source = new File(TEST_DATA_PATH, "ExtendedUsers.txt");
		File dest = new File(Directories.getConfigDir(), "ExtendedUsers.txt");
		FileUtils.copyFile(source, dest); 
		
		Set<String> countries = getCountries();
		UEMLoadScenario scenario = new HeadlessAngularScenario();

		ExecutorService executor = Executors.newSingleThreadScheduledExecutor(); 
		
		Runnable task = () -> {
			for(String country : countries) {				
				ExtendedCommonUser user = scenario.getRandomUser(country, false, false);
			}
		};
		executor.submit(task);
		executor.shutdown();
		assertTrue("Getting users for all countries didn't finish in 60s", executor.awaitTermination(60, TimeUnit.SECONDS));
	
	}
		
	@Test
	public void testGettingWeeklyAndMonthlyUsers() throws InterruptedException {		
		UEMLoadScenario scenario = new HeadlessAngularScenario();
		Simulator simulator = scenario.createSimulator();
		Calendar cal = Calendar.getInstance();
		SpecialUserData data = SpecialUserData.getInstance();
		ExtendedCommonUser user;
		
		int currentDay = cal.get(Calendar.DAY_OF_WEEK);
		int differentDay = currentDay == 3 ? 4 : 3;
		
		data.usedUsers = new ArrayList<SpecialUserDataRow>();
		
		SpecialUserData.getInstance().setWeeklyUserDay(differentDay);
		for (int i = 0; i < 20000; i++) {
			user = simulator.getUserForVisit();
			assertFalse("We got a weekly user on a wrong day of the week", user.isSpecialWeeklyUser());
		}
		assertTrue("We did not get any monthly users, when they all were not used yet", data.usedUsers.size() > 0);
		
		data.usedUsers = new ArrayList<SpecialUserDataRow>();
		addAllMonthlyUserData(data, cal);
		
		SpecialUserData.getInstance().setWeeklyUserDay(currentDay);
		boolean gotAtLeastOneWeeklyUser = false;
		for (int i = 0; i < 20000; i++) {
			user = simulator.getUserForVisit();
			gotAtLeastOneWeeklyUser = gotAtLeastOneWeeklyUser || user.isSpecialWeeklyUser();
			assertFalse("We got a monthly user which was already used!", user.isSpecialMonthlyUser());
		}
		assertTrue("We don't get a weekly user on a proper day of the week", gotAtLeastOneWeeklyUser);
	}
	
	private void addAllMonthlyUserData(SpecialUserData data, Calendar cal) {
		List<ExtendedCommonUser> users = Arrays.asList(
			ExtendedDemoUser.MONTHLY_USER_1, ExtendedDemoUser.MONTHLY_USER_2, ExtendedDemoUser.MONTHLY_USER_3,
			ExtendedDemoUser.MONTHLY_USER_4, ExtendedDemoUser.MONTHLY_USER_5, ExtendedDemoUser.MONTHLY_USER_6,
			ExtendedDemoUser.MONTHLY_USER_7, ExtendedDemoUser.MONTHLY_USER_8, ExtendedDemoUser.MONTHLY_USER_9,
			ExtendedDemoUser.MONTHLY_USER_10, ExtendedDemoUser.MONTHLY_USER_11);
		
		for (ExtendedCommonUser extendedCommonUser : users) {
			data.usedUsers.add(new SpecialUserDataRow(
					extendedCommonUser.getVisitorInfo().getVisitorID(), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)));
		}
	}
	
	private Set<String> getCountries() throws NumberFormatException, IOException{
		try (BufferedReader br = new BufferedReader(new InputStreamReader(ResourceFileReader.getInputStream(ResourceFileReader.GEO), BaseConstants.UTF8));){
			return br.lines().map(line -> line.split(";")[0]).collect(Collectors.toSet());
		}

	}
}
