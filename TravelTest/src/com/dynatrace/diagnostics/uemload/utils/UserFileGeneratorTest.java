package com.dynatrace.diagnostics.uemload.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.SampledRandomLocation;
import com.dynatrace.diagnostics.uemload.UEMLoadScenario;
import com.dynatrace.diagnostics.uemload.VisitorInfo;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelCustomer;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelCustomerMobile;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.EasyTravelMobileAppScenario;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.dynatrace.easytravel.util.TextUtils;

/**
 *
 * @author Michal.Bakula
 *
 */

public class UserFileGeneratorTest {

	private static int NO_OF_LINES;
	private static int NO_OF_COUNTRIES;
	private static final String MOST_POPULAR_COUNTRY = "United States";
	private static final int NO_OF_FIELDS = 18;
	private static final String MOST_POPULAR_BROWSER = "CHROME_55";
	private static final int NO_OF_BROWSERS = 50;
	private static final int NO_OF_MOBILE_DEVICES = 41;
	private static final int NO_OF_MOBILE_BROWSERS = 40;
	private static final int NO_OF_BANDWIDTH_TYPES = Bandwidth.values().length - 1;

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
	public void userFileTest() throws Exception {
		InputStream is = ResourceFileReader.getInputStream(ResourceFileReader.EXTENDEDUSERS);
		BufferedReader br = new BufferedReader(new InputStreamReader(is, BaseConstants.UTF8));

		int lineCount = 0;
		boolean lineError = false;
		int corruptedLine = 0;
		Map<String, Integer> countries = new HashMap<>();
		Map<String, Integer> differentBrowsers = new HashMap<>();
		Set<String> mobileDevices = new HashSet<>();
		Set<String> differentMobileBrowsers = new HashSet<>();
		Set<String> bandwidthTypes = new HashSet<>();

		try {
			NO_OF_LINES = getNumberOfLines(ResourceFileReader.USERS);
			NO_OF_COUNTRIES = getNumberOfLines(ResourceFileReader.GEO);

			String line;
			while ((line = br.readLine()) != null) {
				lineCount++;

				String[] tokens = line.split(";");
				if (tokens.length != NO_OF_FIELDS) {
					lineError = true;
					corruptedLine = lineCount;
				}

				String country = tokens[5];
				Integer count = countries.get(country);
				if (count == null) {
					countries.put(country, 1);
				} else {
					countries.put(country, count + 1);
				}
				String[] browsers = tokens[9].split(",");
				for (int i = 0; i < browsers.length; i++) {
					count = differentBrowsers.get(browsers[i]);
					if (count == null) {
						differentBrowsers.put(browsers[i], 1);
					} else {
						differentBrowsers.put(browsers[i], count + 1);
					}
				}
				String mobileDevice = tokens[10];
				mobileDevices.add(mobileDevice);
				String mobileBrowser = tokens[11];
				differentMobileBrowsers.add(mobileBrowser);
				String bandwidth = tokens[12];
				bandwidthTypes.add(bandwidth);
			}
		} finally {
			is.close();
			br.close();
		}

		assertTrue(TextUtils.merge("Not enough lines in {0} file.", ResourceFileReader.EXTENDEDUSERS),
				lineCount == NO_OF_LINES);

		assertFalse(TextUtils.merge("Line {0} did not have {1} fields.", corruptedLine, NO_OF_FIELDS), lineError);

		String mostPopularCountry = getMostPopularKey(countries);
		assertTrue(
				TextUtils.merge("Country {0} wasn''t most popular. {1} was.", MOST_POPULAR_COUNTRY, mostPopularCountry),
				MOST_POPULAR_COUNTRY.equals(mostPopularCountry));

		assertTrue(TextUtils.merge("There were not every contries from {0} in file.", ResourceFileReader.GEO),
				countries.size() == NO_OF_COUNTRIES);

		String mostPopularBrowser = getMostPopularKey(differentBrowsers);
		assertTrue(
				TextUtils.merge("{0} wasn''t most popular browser. {1} was.", MOST_POPULAR_BROWSER, mostPopularBrowser),
				MOST_POPULAR_BROWSER.equals(mostPopularBrowser));

		assertTrue("There were not enough different browsers in file.", differentBrowsers.size() > NO_OF_BROWSERS);

		assertTrue(
				TextUtils.merge("Not all types of Mobile Devices appeared in {0} file. (was {1})",
						ResourceFileReader.EXTENDEDUSERS, mobileDevices.size()),
				mobileDevices.size() == NO_OF_MOBILE_DEVICES);

		assertTrue("There were not enough different mobile browsers in file.",
				differentMobileBrowsers.size() > NO_OF_MOBILE_BROWSERS);

		assertTrue(
				TextUtils.merge("Not all types of Bandwidth appeared in {0} file.", ResourceFileReader.EXTENDEDUSERS),
				bandwidthTypes.size() == NO_OF_BANDWIDTH_TYPES);
	}

	private String getMostPopularKey(Map<String, Integer> map) {
		Entry<String, Integer> maxEntry = null;
		for (Entry<String, Integer> entry : map.entrySet()) {
			if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
				maxEntry = entry;
			}
		}

		return (maxEntry != null) ? maxEntry.getKey() : "";
	}

	private int getNumberOfLines(String name) throws IOException {
		int count = 0;
		InputStream is = ResourceFileReader.getInputStream(name);
		BufferedReader br = new BufferedReader(new InputStreamReader(is, BaseConstants.UTF8));
		try {
			while (br.readLine() != null) {
				count++;
			}
		} finally {
			br.close();
			if (is != null)
				is.close();
		}
		return count;
	}

	@Test
	public void newToReturningUsersTest() {
		SampledRandomLocation locations = new SampledRandomLocation();

		EasyTravelCustomer web = new EasyTravelCustomer(false);
		web.init();
		EasyTravelCustomerMobile webMobile = new EasyTravelCustomerMobile(false);
		webMobile.init();
		EasyTravelMobileAppScenario app = new EasyTravelMobileAppScenario();
		app.init();

		final int WEB_LOAD = 13;
		final int MOBILE_WEB_LOAD = 13;
		final int MOBILE_APP_LOAD = 13;

		Set<String> weeklyNewVisitorID = new HashSet<>();
		Set<String> weeklyRetVisitorIDs = new HashSet<>();

		final List<Set<String>> dailyNewVisitorIDs = new ArrayList<>();
		final List<Set<String>> dailyRetVisitorIDs = new ArrayList<>();

		final List<Set<String>> hourlyNewVisitorIDs = new ArrayList<>();
		final List<Set<String>> hourlyRetVisitorIDs = new ArrayList<>();
		int newUserActions = 0;
		int retUserActions = 0;

		for (int d = 0; d < 7; d++) {
			dailyNewVisitorIDs.add(new HashSet<String>());
			dailyRetVisitorIDs.add(new HashSet<String>());
			for (int h = 0; h < 24; h++) {
				hourlyNewVisitorIDs.add(new HashSet<String>());
				hourlyRetVisitorIDs.add(new HashSet<String>());
				for (int v = 0; v < (WEB_LOAD + MOBILE_WEB_LOAD + MOBILE_APP_LOAD) * 60; v++) {
					UEMLoadScenario loadScenario = (v < WEB_LOAD) ? web
							: ((v < WEB_LOAD + MOBILE_WEB_LOAD) ? webMobile : app);
					Location location = locations.get(h, d);
					if (location.getCountry() == null) {
						String id = VisitorInfo.generateVisitorID();
						weeklyNewVisitorID.add(id);
						dailyNewVisitorIDs.get(d).add(id);
						hourlyNewVisitorIDs.get(h + 24 * d).add(id);
						newUserActions++;
					} else {
						ExtendedCommonUser user = loadScenario.getRandomUser(location.getCountry());
						if (user.getVisitorInfo().createVisitorID().isNewVisitor()) {
							String id = loadScenario.getVisitorInfo(user).createVisitorID().getVisitorId();
							weeklyNewVisitorID.add(id);
							dailyNewVisitorIDs.get(d).add(id);
							hourlyNewVisitorIDs.get(h + 24 * d).add(id);
							newUserActions++;
						} else {
							String id = loadScenario.getVisitorInfo(user).createVisitorID().getVisitorId();
							weeklyRetVisitorIDs.add(id);
							dailyRetVisitorIDs.get(d).add(id);
							hourlyRetVisitorIDs.get(h + 24 * d).add(id);
							retUserActions++;
						}
					}
				}
			}
		}

		assertTrue("Returning users didn''t generate more than 80% of traffic.",
				percentage(retUserActions, newUserActions) > 80.0f);

		float weeklyPerc = percentage(weeklyRetVisitorIDs.size(), weeklyNewVisitorID.size());
		assertTrue("Weekly percent of returning users wasn''t beetwen 15-25%.",
				weeklyPerc > 15.0f && weeklyPerc < 25.0f);

		float dailyPerc = 0.0f;
		for (int i = 0; i < 7; i++) {
			dailyPerc += percentage(dailyRetVisitorIDs.get(i).size(), dailyNewVisitorIDs.get(i).size());
		}
		dailyPerc = dailyPerc / 7;
		assertTrue("Average daily percent of returning users wasn''t beetwen 35-45%.",
				dailyPerc > 35.0f && dailyPerc < 45.0f);

		float hourlyPerc = 0.0f;
		for (int i = 0; i < 24 * 7; i++) {
			hourlyPerc += percentage(hourlyRetVisitorIDs.get(i).size(), hourlyNewVisitorIDs.get(i).size());
		}
		hourlyPerc = hourlyPerc / (7 * 24);
		assertTrue("Average hourly percent of returning users wasn''t beetwen 55-65%.",
				hourlyPerc > 55.0f && hourlyPerc < 70.0f);
	}

	private float percentage(int firstValue, int secondValue) {
		return (firstValue * 100.0f) / (firstValue + secondValue);
	}
}
