package com.dynatrace.diagnostics.uemload.utils;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.easytravel.misc.CommonUser;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class UemLoadCalendarUtilsTest {
	
	@Test
	public void numberOfCalendarActionsTest() {
		CommonUser user = new CommonUser("dummy", "user");
		Location location = new Location("Europe", "Poland", "1.1.1.1", 2);
		CustomerSession session = new CustomerSession("http://localhost", user, location, false);
		
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.set(2017, 5, 26);
		
		UemLoadCalendarUtils calUtils = new UemLoadCalendarUtils();
		calUtils.setTripDate(calendar, 5);			
		List<Action> list = calUtils.getCalendarActions(session);
		assertTrue("There should be 1 calendar action.", list.size() == 1);
		
		calUtils.setTripDate(calendar, 6);
		list = calUtils.getCalendarActions(session);
		assertTrue("There should be 2 calendar action.", list.size() == 2);
		
		calUtils.setTripDate(calendar, 4);
		list = calUtils.getCalendarActions(session);
		assertTrue("There should be 3 calendar action.", list.size() == 3);
	}

	@Test
	public void xhrActionNameTest() {		
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.set(2017, 5, 26);
		
		UemLoadCalendarUtils calUtils = new UemLoadCalendarUtils();
		calUtils.setTripDate(calendar, 4);
		assertTrue("Wrong xhr action name.", calUtils.getYearXhrActionName().contains("Show next year: "));
		assertTrue("Wrong xhr action name.", calUtils.getMonthXhrActionName().contains("Show previous mounth: "));
		
		calUtils.setTripDate(calendar, 6);
		assertTrue("Wrong xhr action name.", calUtils.getYearXhrActionName().contains("This year: "));
		assertTrue("Wrong xhr action name.", calUtils.getMonthXhrActionName().contains("Show next mounth: "));
		
		calUtils.setTripDate(calendar, 5);
		assertTrue("Wrong xhr action name.", calUtils.getYearXhrActionName().contains("This year: "));
		assertTrue("Wrong xhr action name.", calUtils.getMonthXhrActionName().contains("This month: "));
	}
	
}
