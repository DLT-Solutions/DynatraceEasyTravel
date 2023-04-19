package com.dynatrace.diagnostics.uemload.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.SampledRandomLocation;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.UEMLoadScenario;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.VisitorInfo;
import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

/**
 *
 * @author Michal.Bakula
 *
 */

public class UserFileGenerator {

	private static final Logger LOGGER = LoggerFactory.make();

	private SampledRandomLocation location;
	private UEMLoadScenario scenario;
	private Random random;

	public UserFileGenerator() {
		location = new SampledRandomLocation();

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
		random = new Random();
	}

	public void generateUserFile() {
		LOGGER.info("Creating Extended Users file.");

		BufferedReader br = null;
		InputStream is = null;
		PrintWriter writer = null;
		try {
			int noOfUsers = getNumberOfLines(ResourceFileReader.USERS);
			Map<String, Location> exampleLocation = getLocationFromEachCountry(ResourceFileReader.GEO);

			is = ResourceFileReader.getInputStream(ResourceFileReader.USERS);
			br = new BufferedReader(new InputStreamReader(is, BaseConstants.UTF8));

			File file = new File(Directories.getConfigDir(), ResourceFileReader.EXTENDEDUSERS);
			file.getParentFile().mkdirs();

			try {
				writer = new PrintWriter(file);
				String baseInfo;

				int count = 0;
				long lastDeviceId = 100001L;
				while ((baseInfo = br.readLine()) != null) {
					baseInfo = baseInfo.replace(",", ";");

					String[] bases = baseInfo.split(";");
					int weight = Integer.parseInt(bases[4]);

					Location loc = location.getRandomLocation();

					if(noOfUsers - count == exampleLocation.size()){
						Map.Entry<String, Location> e = exampleLocation.entrySet().iterator().next();
						loc = e.getValue();
					}
					if(exampleLocation.containsKey(loc.getCountry())){
						exampleLocation.remove(loc.getCountry());
					}

					String country = loc.getCountry();
					String continent = loc.getContinent();
					String timezone = Integer.toString(loc.getTimezone());
					String ip = loc.getIp();
					String browser = getRandomBrowsers(3, loc);
					String mobileDevice = MobileDeviceType
							.getDeviceFieldName(scenario.getRandomMobileDevice());
					String mobileBrowser = BrowserType
							.getBrowserFieldName(scenario.getRandomMobileBrowser(loc));
					String bandwidth = scenario.getRandomBandwidth(loc).name();
					String desktopResolution = scenario.getRandomBrowserWindowSize().name();
					String mobileResolution = scenario.getRandomMobileBrowserWindowSize().name();
					String slowdown = Integer.toString(scenario.getDNSSlowdownFactor(loc));

					String visitorInfo;
					if(weight > 5){
						visitorInfo = TextUtils.merge("{0},{1}", Boolean.TRUE, VisitorInfo.generateVisitorID());
					} else {
						visitorInfo = (random.nextInt(5)>1) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
					}

					lastDeviceId += count;
					String deviceId = Long.toString(lastDeviceId);

					String newLine = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s", baseInfo, country, continent, timezone,
							ip, browser, mobileDevice, mobileBrowser, bandwidth, desktopResolution, mobileResolution,
							slowdown, visitorInfo, deviceId);
					writer.println(newLine);

					count++;
				}
			} finally {
				if(br != null){
					br.close();
				}
				if(is != null){
					is.close();
				}
			}
		} catch (Exception e) {
			LOGGER.error("Cannot create Extended Users file.", e);
		} finally {
			if(writer != null){
				writer.close();
			}
		}

	}

	private String getRandomBrowsers(int bound, Location loc)
			throws IllegalArgumentException, IllegalAccessException {
		Random r = new Random();
		int n = r.nextInt(bound) + 1;
		List<String> browsers = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			String b;
			do {
				b = BrowserType.getBrowserFieldName(scenario.getRandomDesktopBrowser(loc));
			} while (browsers.contains(b));
			browsers.add(b);
		}
		return browsers.stream().collect(Collectors.joining(","));
	}

	private int getNumberOfLines(String name) throws IOException {
		int count = 0;
		InputStream is= ResourceFileReader.getInputStream(name);
		BufferedReader br = new BufferedReader(new InputStreamReader(is, BaseConstants.UTF8));
		try {
			while (br.readLine() != null) {
				count++;
			}
		} finally {
			if(br != null)
				br.close();
			if(is != null)
				is.close();
		}
		return count;
	}

	private Map<String, Location> getLocationFromEachCountry(String name) throws NumberFormatException, IOException{
		InputStream is = ResourceFileReader.getInputStream(name);
		BufferedReader br = new BufferedReader(new InputStreamReader(is, BaseConstants.UTF8));

		Map<String, Location> locations = new HashMap<String, Location>();
		try {
			String line;
			while ((line = br.readLine()) != null) {
				String[] columns = line.split(";");
				String country = columns[0];
				String continent = columns[1];
				Integer timezone = Integer.parseInt(columns[2]);
				String[] ips = columns[columns.length - 1].split(",");

				locations.put(country, new Location(continent, country, ips[0], timezone));
			}
		} finally {
			if(br != null)
				br.close();
			if(is != null)
				is.close();
		}

		return locations;
	}
}
