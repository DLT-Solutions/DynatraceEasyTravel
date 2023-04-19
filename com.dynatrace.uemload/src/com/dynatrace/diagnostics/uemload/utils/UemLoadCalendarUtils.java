package com.dynatrace.diagnostics.uemload.utils;

import java.time.YearMonth;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.EasytravelStartPage;
import com.google.common.collect.Lists;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class UemLoadCalendarUtils {
	
	private static final Random random = new Random();

	private static final String[] MONTHS = new String[]{"January", "February", "March", 
			"April", "May", "June", 
			"July", "August", "September",
            "October", "November", "December"};
	
	private int tripYear;
	private int tripMonth;
	private int tripDay;
	private boolean isTripThisYear;
	private boolean isTripThisMonth;
	
	public UemLoadCalendarUtils() {
		setTripDate();
	}
	
	public String getYearXhrActionName() {		
		return (isTripThisYear()) ? "This year: " + Integer.toString(tripYear) : "Show next year: " + Integer.toString(tripYear);
	}
	
	public String getMonthXhrActionName() {
		if(isTripThisMonth) {
			return "This month: " + MONTHS[tripMonth];
		} else if(isTripThisYear()) {
			return "Show next mounth: " + MONTHS[tripMonth];
		}
		return "Show previous mounth: " + MONTHS[tripMonth];
	}
	
	public String getDayXhrActionName() {
		return "From day: " + Integer.toString(tripDay);
	}
	
	private final void setTripDate(int currentYear, int currentMonth, int year, int month, int day) {
		this.tripYear = year;
		this.tripMonth = month;
		this.tripDay = day;
		this.isTripThisYear = (currentYear == year);
		this.isTripThisMonth = (currentMonth == month);
	}
	
	/**
	 * Set date of the trip used in search.
	 * If chosen date is during current year, XHR action for choosing year is skipped.
	 * If chosen date is during current month, XHR action for choosing month is also skipped.
	 * XHR action for choosing day is always performed.
	 * 
	 * @author Michal.Bakula
	 */
	public void setTripDate() {		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int month = random.nextInt(12);
		setTripDate(cal, month);
	}
	
	/** 
	 * Method just for testing purpose.
	 * @param calendar
	 * @param month
	 */
	public void setTripDate(Calendar calendar, int month) {
		int currentYear = calendar.get(Calendar.YEAR);
		int currentMonth = calendar.get(Calendar.MONTH);
		int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
		
		YearMonth ym = YearMonth.of(tripYear, tripMonth + 1);
		int maximumDays = ym.lengthOfMonth();
		
		
		int year = (month < currentMonth) ? currentYear + 1 : currentYear;
		int day;
		if(month == currentMonth) {
			day = (maximumDays - currentDay == 0) ? currentDay : random.nextInt(maximumDays - currentDay) + currentDay;
		} else {
			day = random.nextInt(maximumDays) + 1;
		}
		
		setTripDate(currentYear, currentMonth, year, month, day);
	}
	
	public boolean isTripThisYear() {
		return isTripThisYear;
	}
	
	public boolean isTripThisMonth() {
		return isTripThisMonth;
	}
	
	public List<Action> getCalendarActions(CustomerSession session) {
		List<Action> actions = Lists.newArrayList();
		
		if(!this.isTripThisYear()) {
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.CALENDAR_YEAR, this));
		}
		if(!this.isTripThisMonth()) {
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.CALENDAR_MONTH, this));
		}
		actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.CALENDAR_DAY, this));
		
		return actions;
	}
}
