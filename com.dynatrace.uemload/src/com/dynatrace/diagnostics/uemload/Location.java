package com.dynatrace.diagnostics.uemload;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

/**
 * 
 * @author Michal.Bakula
 *
 */

public class Location {

	private final String continent;
	private final String country;
	private final String ip;
	private final String coordinates;
	private final LocationType locationType;
	private final Integer timezone;
	
	private static final Set<Integer> rushHours = new HashSet<>(Arrays.asList(7, 8, 9, 17, 18, 19));
	
	public enum LocationType {
		User, DynatraceSynthetic, Synthetic, Robot
	}

	public Location(String continent, String country, String ip) {
		this(continent, country, ip, null, null, LocationType.User);
	}
	
	public Location(String continent, String country, String ip, Integer timezone) {
		this(continent, country, ip, null, timezone, LocationType.User);
	}
	
	public Location(String continent, String country, String ip, String coordinates, Integer timezone) {
		this(continent, country, ip, coordinates, timezone, LocationType.User);
	}

	public Location(String continent, String country, String ip, Integer timezone, LocationType locationType) {
		this(continent, country, ip, null, timezone, locationType);
	}
	
	public Location(Location location, LocationType locationType) {
			this(location.getContinent(), location.getCountry(), location.getIp(), location.getCoordinates(), location.getTimezone(), locationType);
	}
	
	public Location(String continent, String country,  String ip, String coordinates, Integer timezone, LocationType locationType){
		this.continent = continent;
		this.country = country;
		this.ip = ip;
		this.coordinates = coordinates;
		this.timezone = timezone;
		this.locationType = locationType;		
	}

	public String getContinent() {
		return continent;
	}


	public String getCountry() {
		return country;
	}


	public String getIp() {
		return ip;
	}

	public String getCoordinates() {
		return coordinates;
	}
	
	public String getCountryAndContinent() {
		return country + ", " + continent;
	}
	
	public int getTimezone() {
		return timezone;
	}

	public boolean isRuxitSynthetic() {
		return (locationType == LocationType.DynatraceSynthetic);
	}
	
	public boolean isSynthetic() {
		return (locationType == LocationType.Synthetic);
	}

	public boolean isRobot() {
		return (locationType == LocationType.Robot);
	}
	
	public boolean isUser() {
		return (locationType == LocationType.User);
	}
	
	/**
	 * Used only for test purpose. In UEM scenarios use isRushHourNow().
	 * @param calendar
	 * @return
	 */
	public boolean isRushHourNow(final Calendar calendar) {
		if (timezone != null) {
			calendar.add(Calendar.HOUR, timezone);
			return rushHours.contains(calendar.get(Calendar.HOUR_OF_DAY));
		}
		return false;
	}
	
	public boolean isRushHourNow(){
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		return isRushHourNow(calendar);
	}
}