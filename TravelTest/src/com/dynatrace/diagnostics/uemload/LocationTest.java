package com.dynatrace.diagnostics.uemload;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class LocationTest {

	@Test
	public void rushHourCheckTest() {
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

		calendar.set(Calendar.HOUR_OF_DAY, 12);

		// Location without time zone check
		Location location = new Location("Norhaven", "Fayfall", "1.1.1.1");
		assertFalse("Locations without time zone assigned should always return false.", location.isRushHourNow());

		// Daily rush hours check
		for (int i = -11; i <= 12; i++) {
			calendar.set(Calendar.HOUR_OF_DAY, 12);
			if ((i > -6 && i < -2) || (i > 4 && i < 8)) {
				location = new Location("Norhaven", "Fayfall", "1.1.1.1", i);
				assertTrue("Rush hour check returned wrong value. If rush hours were changed, please update test.",
						location.isRushHourNow(calendar));
			} else {
				location = new Location("Norhaven", "Fayfall", "1.1.1.1", i);
				assertFalse("Rush hour check returned wrong value.  If rush hours were changed, please update test.",
						location.isRushHourNow(calendar));
			}
		}
	}
}
