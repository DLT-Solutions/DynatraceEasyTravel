package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.util.TextUtils;

/**
 *
 * @author Michal.Bakula
 *
 */

public class SampledRandomLocationTest
{
	private SampledRandomLocation location;

	private boolean weekday;

	private final String USA = "United States";
	private final String CHINA = "China";
	private final String JAPAN = "Japan";
	private final String UK = "United Kingdom";
	private final String AUSTRALIA = "Australia";

	private final String dailyPattern = "{0} did not have more daily visists than {1}.";
	private final String hourlyPattern = "{0} did not have more visits at {2} UTC than {1}.";
	private final String specialPattern = "{0} did not have {2} times more visits at {3} UTC than {1}.";
	private final String comparisonPattern = "{0} did not have {1} times more visits at {2} UTC than at {3} UTC.";

	@Before
	public void setup() {
		location = new SampledRandomLocation();
	}

	/**
	 * Tests location distribution during weekday
	 */
	@Test
	public void rushHourWeekdaysTest() {
		Map<Integer, Integer> usa = getData(location, USA, Calendar.TUESDAY);
		Map<Integer, Integer> china = getData(location, CHINA, Calendar.TUESDAY);
		Map<Integer, Integer> japan = getData(location, JAPAN, Calendar.TUESDAY);
		Map<Integer, Integer> uk = getData(location, UK, Calendar.TUESDAY);
		Map<Integer, Integer> australia = getData(location, AUSTRALIA, Calendar.TUESDAY);

		int sUSA = sumDailyVisits(usa);
		int sChina = sumDailyVisits(china);
		int sJapan = sumDailyVisits(japan);
		int sUK = sumDailyVisits(uk);
		int sAustralia = sumDailyVisits(australia);

		// Check sum of visits for different countries throughout the day
		assertTrue(TextUtils.merge(dailyPattern, USA, CHINA), sUSA > sChina);
		assertTrue(TextUtils.merge(dailyPattern, CHINA, JAPAN), sChina > sJapan);
		assertTrue(TextUtils.merge(dailyPattern, JAPAN, UK), sJapan > sUK);
		assertTrue(TextUtils.merge(dailyPattern, UK, AUSTRALIA), sUK > sAustralia);

		// Check visits during different hours of day
		double multiplier = 5;
		int hour = 3;
		assertTrue(TextUtils.merge(hourlyPattern, JAPAN, UK, hour), japan.get(hour) > uk.get(hour));
		assertTrue(TextUtils.merge(specialPattern, USA, CHINA, multiplier, hour),
				usa.get(hour) > multiplier * china.get(hour));
		hour = 11;
		assertTrue(TextUtils.merge(hourlyPattern, JAPAN, UK, hour), japan.get(hour) > uk.get(hour));
		hour = 15;
		assertTrue(TextUtils.merge(hourlyPattern, USA, CHINA, hour), usa.get(hour) > china.get(hour));
		hour = 18;
		assertTrue(TextUtils.merge(hourlyPattern, UK, AUSTRALIA, hour), uk.get(hour) > australia.get(hour));
		assertTrue(TextUtils.merge(specialPattern, USA, CHINA, multiplier, hour),
				usa.get(hour) > multiplier * china.get(hour));

		// Country traffic comparison during day and night
		multiplier = 2;
		hour = 1;
		assertTrue(TextUtils.merge(comparisonPattern, UK, multiplier, hour + 12, hour),
				uk.get(hour + 12) > multiplier * uk.get(hour));
		hour = 7;
		assertTrue(TextUtils.merge(comparisonPattern, JAPAN, multiplier, hour, hour + 12),
				japan.get(hour) > multiplier * japan.get(hour + 12));
		hour = 9;
		assertTrue(TextUtils.merge(comparisonPattern, USA, multiplier, hour + 12, hour),
				usa.get(hour + 12) > multiplier * usa.get(hour));
		hour = 10;
		assertTrue(TextUtils.merge(comparisonPattern, CHINA, multiplier, hour, hour + 12),
				china.get(hour) > multiplier * china.get(hour + 12));
	}

	/**
	 * Tests location distribution during weekend
	 */
	@Test
	public void rushHourWeekendsTest() {
		Map<Integer, Integer> usa = getData(location, USA, Calendar.SATURDAY);
		Map<Integer, Integer> china = getData(location, CHINA, Calendar.SATURDAY);
		Map<Integer, Integer> japan = getData(location, JAPAN, Calendar.SATURDAY);
		Map<Integer, Integer> uk = getData(location, UK, Calendar.SATURDAY);
		Map<Integer, Integer> australia = getData(location, AUSTRALIA, Calendar.SATURDAY);

		int sUSA = sumDailyVisits(usa);
		int sChina = sumDailyVisits(china);
		int sJapan = sumDailyVisits(japan);
		int sUK = sumDailyVisits(uk);
		int sAustralia = sumDailyVisits(australia);

		// launch();

		// Check sum of visits for different countries throughout the day
		assertTrue(TextUtils.merge(dailyPattern, USA, CHINA), sUSA > sChina);
		assertTrue(TextUtils.merge(dailyPattern, CHINA, JAPAN), sChina > sJapan);
		assertTrue(TextUtils.merge(dailyPattern, JAPAN, UK), sJapan > sUK);
		assertTrue(TextUtils.merge(dailyPattern, UK, AUSTRALIA), sUK > sAustralia);

		// Check visits during different hours of day
		double multiplier = 3;
		int hour = 3;
		assertTrue(TextUtils.merge(hourlyPattern, JAPAN, UK, hour), japan.get(hour) > uk.get(hour));
		assertTrue(TextUtils.merge(specialPattern, USA, CHINA, multiplier, hour),
				usa.get(hour) > multiplier * china.get(hour));
		hour = 6;
		assertTrue(TextUtils.merge(hourlyPattern, JAPAN, UK, hour), japan.get(hour) > uk.get(hour));
		assertTrue(TextUtils.merge(hourlyPattern, USA, CHINA, hour), usa.get(hour) > china.get(hour));
		multiplier = 5;
		hour = 18;
		assertTrue(TextUtils.merge(hourlyPattern, UK, JAPAN, hour), uk.get(hour) > japan.get(hour));
		assertTrue(TextUtils.merge(specialPattern, USA, CHINA, multiplier, hour),
				usa.get(hour) > multiplier * china.get(hour));

		// Country traffic comparison during day and night
		multiplier = 2;
		hour = 1;
		assertTrue(TextUtils.merge(comparisonPattern, UK, multiplier, hour + 12, hour),
				uk.get(hour + 12) > multiplier * uk.get(hour));
		multiplier = 1.5;
		hour = 6;
		assertTrue(TextUtils.merge(comparisonPattern, AUSTRALIA, multiplier, hour, hour + 12),
				australia.get(hour) > multiplier * australia.get(hour + 12));
		multiplier = 2;
		hour = 9;
		assertTrue(TextUtils.merge(comparisonPattern, USA, multiplier, hour, hour + 12),
				usa.get(hour + 12) > multiplier * usa.get(hour));
		assertTrue(TextUtils.merge(comparisonPattern, CHINA, multiplier, hour, hour + 12),
				china.get(hour) > multiplier * china.get(hour + 12));
		hour = 8;
		assertTrue(TextUtils.merge(comparisonPattern, JAPAN, multiplier, hour, hour + 12),
				japan.get(hour) > multiplier * japan.get(hour + 12));
	}

	private Map<Integer, Integer> getData(SampledRandomLocation location, String name, int day) {
		Map<Integer, Integer> hour = new HashMap<>();
		for (int i = 0; i < 24; i++) {
			hour.put(i, 0);
			for (int j = 0; j < 2280; j++) {
				String c = location.get(i, day).getCountry();
				if (c.equals(name)) {
					int tmp = hour.get(i);
					tmp++;
					hour.put(i, tmp);
				}
			}
		}
		return hour;
	}

	private int sumDailyVisits(Map<Integer, Integer> visits) {
		int sum = 0;
		for (Integer i : visits.values()) {
			sum += i;
		}
		return sum;
	}
}
