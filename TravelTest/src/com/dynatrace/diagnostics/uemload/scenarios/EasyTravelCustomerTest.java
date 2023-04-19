package com.dynatrace.diagnostics.uemload.scenarios;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.utils.UserFileGenerator;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * 
 * @author Michal.Bakula
 *
 */

public class EasyTravelCustomerTest {

	private static final Location location = new Location("Norhaven", "Fayfall", "1.1.1.1");
	private static Location rushHourLocation;
	private static final CommonUser dummyUser = new CommonUser("user", "test");

	private static final int DRAW = 1000;
	private static final int TEST_RUSH_HOUR = 18;

	@BeforeClass
	public static void setup() {
		UserFileGenerator generator = new UserFileGenerator();
		generator.generateUserFile();

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		rushHourLocation = new Location("Norhaven", "Fayfall", "1.1.1.1",
				TEST_RUSH_HOUR - calendar.get(Calendar.HOUR_OF_DAY));
	}

	@AfterClass
	public static void cleanup() {
		File f = new File(Directories.getConfigDir(), ResourceFileReader.EXTENDEDUSERS);
		f.delete();
	}

	@Test
	public void syntheticAndRobotsTest() throws InterruptedException {
		int dtSyntheticCount = 0;
		int syntheticCount = 0;
		int robotCount = 0;

		double dtSyntheticPerc;
		double syntheticPerc;
		double robotPerc;

		final int DRAWS = 100000;

		double LOAD = 10.0;
		final double MARGIN = 0.5;

		EasyTravelConfig config = EasyTravelConfig.read();
		InstallationType saveState = DtVersionDetector.getInstallationType();
		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		try {
			EasyTravel web = new EasyTravelCustomer("http://localhost", "http://localhost", false);
			web.init(true);

			config.baseDynatraceSyntheticLoad = LOAD;
			config.baseSyntheticLoad = LOAD;
			config.baseRobotLoad = LOAD;

			for (int i = 0; i < DRAWS; i++) {
				Location loc = web.getRandomLocation();
				if (loc.isRuxitSynthetic()) {
					dtSyntheticCount++;
				} else if (loc.isSynthetic()) {
					syntheticCount++;
				} else if (loc.isRobot()) {
					robotCount++;
				}
			}

			dtSyntheticPerc = dtSyntheticCount * 100.0 / DRAWS;
			syntheticPerc = syntheticCount * 100.0 / DRAWS;
			robotPerc = robotCount * 100.0 / DRAWS;

			assertTrue(TextUtils.merge("Dynatrace Synthetic location wasn''t drawn in about {0}% cases.", LOAD),
					dtSyntheticPerc > LOAD - MARGIN && dtSyntheticPerc < LOAD + MARGIN);
			assertTrue(TextUtils.merge("Synthetic location wasn''t drawn in about {0}% cases.", LOAD),
					syntheticPerc > LOAD - MARGIN && dtSyntheticPerc < LOAD + MARGIN);
			assertTrue(TextUtils.merge("Robot location wasn''t drawn in about {0}% cases.", LOAD),
					robotPerc > LOAD - MARGIN && robotPerc < LOAD + MARGIN);

			config = EasyTravelConfig.read();
			LOAD = 5.0;
			config.baseDynatraceSyntheticLoad = LOAD;
			config.baseSyntheticLoad = LOAD;
			config.baseRobotLoad = LOAD;

			dtSyntheticCount = 0;
			syntheticCount = 0;
			robotCount = 0;

			for (int i = 0; i < DRAWS; i++) {
				Location loc = web.getRandomLocation();
				if (loc.isRuxitSynthetic()) {
					dtSyntheticCount++;
				} else if (loc.isSynthetic()) {
					syntheticCount++;
				} else if (loc.isRobot()) {
					robotCount++;
				}
			}

			dtSyntheticPerc = dtSyntheticCount * 100.0 / DRAWS;
			syntheticPerc = syntheticCount * 100.0 / DRAWS;
			robotPerc = robotCount * 100.0 / DRAWS;

			assertTrue(TextUtils.merge("Dynatrace Synthetic location wasn''t drawn in about {0}% cases.", LOAD),
					dtSyntheticPerc > LOAD - MARGIN && dtSyntheticPerc < LOAD + MARGIN);
			assertTrue(TextUtils.merge("Synthetic location wasn''t drawn in about {0}% cases.", LOAD),
					syntheticPerc > LOAD - MARGIN && dtSyntheticPerc < LOAD + MARGIN);
			assertTrue(TextUtils.merge("Robot location wasn''t drawn in about {0}% cases.", LOAD),
					robotPerc > LOAD - MARGIN && robotPerc < LOAD + MARGIN);

			dtSyntheticCount = 0;
			syntheticCount = 0;
			robotCount = 0;
			DtVersionDetector.enforceInstallationType(InstallationType.Classic);

			for (int i = 0; i < DRAWS; i++) {
				Location loc = web.getRandomLocation();
				if (loc.isRuxitSynthetic()) {
					dtSyntheticCount++;
				} else if (loc.isSynthetic()) {
					syntheticCount++;
				} else if (loc.isRobot()) {
					robotCount++;
				}
			}

			assertTrue("Dynatrace Synthetic location was drawn in Classic Mode.", dtSyntheticCount == 0);
			assertTrue("Synthetic location wasn't drawn in Classic Mode.", syntheticCount > 0);
			assertTrue("Robot location wasn't drawn in Classic Mode.", robotCount > 0);
		} finally {
			DtVersionDetector.enforceInstallationType(saveState);
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void avgNumberOfActionTest() {
		EasyTravel web = new EasyTravelCustomer("http://localhost", "http://localhost", false);
		web.init(true);

		int sum = 0;

		for (int i = 0; i < DRAW; i++) {
			Visit visit = web.getRandomVisit(location);
			sum += visit.getActions(dummyUser, location).length;
		}
		double notRushHourAverage = (double) sum / DRAW;
		sum = 0;

		for (int i = 0; i < DRAW; i++) {
			Visit visit = web.getRandomVisit(rushHourLocation);
			sum += visit.getActions(dummyUser, rushHourLocation).length;
		}
		double rushHourAverage = (double) sum / DRAW;

		assertTrue(
				"Average number of actions per session did not increased during rush hours. "
						+ "Check createVisits() and createRushHourVisits() in EasyTravelCustomer class.",
				rushHourAverage * 0.95 > notRushHourAverage);
	}

	@Test
	public void pageWandererVisitTest() {

		EasyTravel web = new EasyTravelCustomer("http://localhost", "http://localhost", false) {
			@Override
			protected IterableSet<Visit> createVisits() {
				VisitsModel visits = new VisitsModel.VisitsBuilder().setWandererShort(20).build();
				return createVisits(visits);
			}

			@Override
			protected IterableSet<Visit> createRushHourVisits() {
				VisitsModel visits = new VisitsModel.VisitsBuilder().setWandererLong(20).build();
				return createVisits(visits);
			}
		};
		web.init(true);

		int sum = 0;
		int longestShortSession = 0;
		int shortestShortSession = 100;

		for (int i = 0; i < DRAW; i++) {
			Visit visit = web.getRandomVisit(location);
			int lenght = visit.getActions(dummyUser, location).length;
			sum += lenght;
			longestShortSession = (lenght > longestShortSession) ? lenght : longestShortSession;
			shortestShortSession = (lenght < shortestShortSession) ? lenght : shortestShortSession;
		}
		double notRushHourAverage = (double) sum / DRAW;

		sum = 0;
		int longestLongSession = 0;
		int shortestLongSession = 100;

		for (int i = 0; i < DRAW; i++) {
			Visit visit = web.getRandomVisit(rushHourLocation);
			int lenght = visit.getActions(dummyUser, rushHourLocation).length;
			sum += lenght;
			longestLongSession = (lenght > longestLongSession) ? lenght : longestLongSession;
			shortestLongSession = (lenght < shortestLongSession) ? lenght : shortestLongSession;
		}
		double rushHourAverage = (double) sum / DRAW;

		assertTrue(
				"Average number of actions per session did not increased sugnificantly during rush hours. "
						+ "Check createVisits() and createRushHourVisits() in EasyTravelCustomer class.",
				rushHourAverage * 0.7 > notRushHourAverage);
		assertTrue("Shortest shortMagentoVisit was not shorter than shortest longMagentoVisit.",
				shortestShortSession < shortestLongSession);
		assertTrue("Longest shortMagentoVisit was not shorter than longest longMagentoVisit.",
				longestShortSession < longestLongSession);
	}

	@Test
	public void magentoShopVisitTest() {

		EasyTravel web = new EasyTravelCustomer("http://localhost", "http://localhost", false) {
			@Override
			protected IterableSet<Visit> createVisits() {
				VisitsModel visits = new VisitsModel.VisitsBuilder().setMagentoShort(20).build();
				return createVisits(visits);
			}

			@Override
			protected IterableSet<Visit> createRushHourVisits() {
				VisitsModel visits = new VisitsModel.VisitsBuilder().setMagentoLong(20).build();
				return createVisits(visits);
			}

			@Override
			protected IterableSet<Visit> createVisits(VisitsModel visits) {
				RandomSet<Visit> res = new RandomSet<Visit>();
				res.add(new WordPressShopVisit("http://localhost", VisitLength.SHORT), visits.getMagentoShort());
				res.add(new WordPressShopVisit("http://localhost", VisitLength.LONG), visits.getMagentoLong());
				return res;
			}
		};
		web.init(true);

		int sum = 0;
		int longestShortSession = 0;
		int shortestShortSession = 100;

		for (int i = 0; i < DRAW; i++) {
			Visit visit = web.getRandomVisit(location);
			int lenght = visit.getActions(dummyUser, location).length;
			sum += lenght;
			longestShortSession = (lenght > longestShortSession) ? lenght : longestShortSession;
			shortestShortSession = (lenght < shortestShortSession) ? lenght : shortestShortSession;
		}
		double notRushHourAverage = (double) sum / DRAW;

		sum = 0;
		int longestLongSession = 0;
		int shortestLongSession = 100;

		for (int i = 0; i < DRAW; i++) {
			Visit visit = web.getRandomVisit(rushHourLocation);
			int lenght = visit.getActions(dummyUser, rushHourLocation).length;
			sum += lenght;
			longestLongSession = (lenght > longestLongSession) ? lenght : longestLongSession;
			shortestLongSession = (lenght < shortestLongSession) ? lenght : shortestLongSession;
		}
		double rushHourAverage = (double) sum / DRAW;

		assertTrue(
				"Average number of actions per session did not increased sugnificantly during rush hours. "
						+ "Check createVisits() and createRushHourVisits() in EasyTravelCustomer class.",
				rushHourAverage * 0.7 > notRushHourAverage);
		assertTrue("Shortest shortMagentoVisit was not shorter than shortest longMagentoVisit.",
				shortestShortSession < shortestLongSession);
		assertTrue("Longest shortMagentoVisit was not shorter than longest longMagentoVisit.",
				longestShortSession < longestLongSession);
	}
}
