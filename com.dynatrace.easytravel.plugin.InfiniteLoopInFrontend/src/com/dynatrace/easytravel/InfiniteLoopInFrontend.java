package com.dynatrace.easytravel;

import java.util.Calendar;
import java.util.Date;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

/**
 * Causes an infinite loop in the frontend when searching trips with a from date
 * past the to date.
 * 
 * @author Peter Hofer
 */
public class InfiniteLoopInFrontend extends AbstractGenericPlugin {

	@Override
	public Object doExecute(String location, Object... context) {
		Date from = (Date) context[1];
		Date to = (Date) context[2];

		int days = 0;
		if (from != null && to != null && from.after(to)) {
			// Call another method where we want the infinite loop to be found
			days = getDaysBetween(from, to);
		}
		return days;
	}

	private int getDaysBetween(Date fromDate, Date toDate) {
		Calendar from = Calendar.getInstance();
		from.setTime(fromDate);
		Calendar to = Calendar.getInstance();
		to.setTime(toDate);

		// Do all of the loop's work in compareAndAdvance() to reduce stack
		// samples with getDaysBetween() on top and make detection suspect
		// that the busy loop originates in compareAndAdvance()
		int ndays = 0;
		while (!compareAndAdvance(from, to, 1)) {
			ndays++;
		}
		return ndays;
	}

	private boolean compareAndAdvance(Calendar from, Calendar to, int ndays) {
		if (!from.equals(to)) {
			advanceByDays(from, ndays);
			return false;
		}
		return true;
	}

	private void advanceByDays(Calendar from, int ndays) {
		from.add(Calendar.DAY_OF_MONTH, ndays);
	}
}
