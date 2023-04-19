package com.dynatrace.diagnostics.uemload;

import java.io.*;
import java.util.*;

import com.dynatrace.easytravel.util.ResourceFileReader;

/**
 * 
 * @author Michal.Bakula
 *
 */

public class SampledRandomLocation implements RandomLocation {

	private final RandomSet<String> countries = new RandomSet<String>();
	private final Map<String, List<Location>> locationsByCountry = new HashMap<String, List<Location>>();

	private final Random random = new Random();

	private static final int HOURS_IN_DAY = 24;

	private final List<RandomSet<String>> countriesWeekdays = new ArrayList<RandomSet<String>>(HOURS_IN_DAY);
	private final List<RandomSet<String>> countriesWeekends = new ArrayList<RandomSet<String>>(HOURS_IN_DAY);

	//										  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23
	private final double[] weekdayProfile = { 1, 1, 1, 1, 1, 1, 2, 2, 3, 4, 4, 3, 3, 3, 3, 4, 5, 6, 6, 5, 4, 3, 2, 1 };
	private final double[] weekendProfile = { 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 4, 4, 4, 3, 3, 3, 3, 4, 4, 3, 3, 2, 1, 1 };

	public SampledRandomLocation() {
		InputStream is = null;
		BufferedReader in = null;
		try {
			for (int i = 0; i < HOURS_IN_DAY; i++) {
				countriesWeekdays.add(new RandomSet<String>());
				countriesWeekends.add(new RandomSet<String>());
			}
			is = ResourceFileReader.getInputStream(ResourceFileReader.GEO);
			in = new BufferedReader(new InputStreamReader(is));
			try {
				String line;
				while ((line = in.readLine()) != null) {
					String[] columns = line.split(";");
					String country = columns[0];
					String continent = columns[1];
					int timeZone = Integer.parseInt(columns[2]);
					String[] ips = columns[columns.length - 1].split(",");

					List<Location> locationsForCountry = new ArrayList<Location>();
					for (String ip : ips) {
						Location loc = new Location(continent, country, ip, timeZone);
						locationsForCountry.add(loc);
					}
					
					countries.add(country, ips.length);
					locationsByCountry.put(country, locationsForCountry);

					for (int i = 0; i < HOURS_IN_DAY; i++) {
						int hour = getLocalTime(i, timeZone);

						countriesWeekdays.get(i).add(country, (int) (ips.length * weekdayProfile[hour]));
						countriesWeekends.get(i).add(country, (int) (ips.length * weekendProfile[hour]));
					}
				}
			} finally {
				if(is != null)
					is.close();
				if(in != null)
					in.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Transforms time given in UTC to local time in given time zone.
	 * @param utcHour
	 * @param timeZone
	 * @return local time in given time zone
	 */
	private int getLocalTime(int utcHour, int timeZone){
		int hour = utcHour + timeZone;
		hour = (hour < 0) ? hour + HOURS_IN_DAY
				: ((hour > HOURS_IN_DAY - 1) ? hour - HOURS_IN_DAY : hour);
		return hour;
	}

	public SampledRandomLocation(boolean highLoadFromAsia) {
		this();
		if (highLoadFromAsia) {
			setCountryPercent("China", 9);
			setCountryPercent("India", 8);
			setCountryPercent("Japan", 5);
			setCountryPercent("Singapore", 2);
			setCountryPercent("Vietnam", 4);
			setCountryPercent("Thailand", 3);
		}
	}

	@Override
	public Location get() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		return get(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.DAY_OF_WEEK));
	}

	/**
	 * Used just for test purpose, for scenarios use get()
	 * 
	 * @param hourOfDay
	 * @param dayOfWeak
	 * @return
	 */
	public Location get(int hourOfDay, int dayOfWeak) {
		String randomCountry;
		if (dayOfWeak == Calendar.SATURDAY || dayOfWeak == Calendar.SUNDAY) {
			randomCountry = countriesWeekends.get(hourOfDay).getRandom();
		} else {
			randomCountry = countriesWeekdays.get(hourOfDay).getRandom();
		}
		List<Location> locationsForCountry = locationsByCountry.get(randomCountry);
		return locationsForCountry.get(random.nextInt(locationsForCountry.size()));
	}

	public Location getRandomLocation() {
		String randomCountry = countries.getRandom();
		List<Location> locationsForCountry = locationsByCountry.get(randomCountry);
		return locationsForCountry.get(random.nextInt(locationsForCountry.size()));
	}

	public void setCountryPercent(String country, int percent) {
		countries.setWeightInPercent(country, percent);
	}
}