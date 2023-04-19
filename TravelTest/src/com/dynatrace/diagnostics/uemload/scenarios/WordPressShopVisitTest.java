package com.dynatrace.diagnostics.uemload.scenarios;

import static org.junit.Assert.*;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.Location.LocationType;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravel.VisitLength;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class WordPressShopVisitTest {
	
	@Test
	public void testShortVisit() {
		WordPressShopVisit visit = new WordPressShopVisit("http://127.0.0.1:8080/", VisitLength.SHORT);
		Location location = new Location("Atlantis", "Meropis", "0.0.0.0");
		
		Location synthetic = new Location(null, null, "1.1.1.1", 0, LocationType.DynatraceSynthetic);
		int maxValue = 0;
		int minValue = 1000;
		for(int i=0; i<1000; i++) {
			int actionsCount = visit.getActions(ExtendedDemoUser.MARIA_USER, location).length;
			maxValue = Math.max(maxValue, actionsCount);
			minValue = Math.min(minValue, actionsCount);
		}
		assertTrue(maxValue == 10);
		assertTrue(minValue == 6);
		
		maxValue = 0;
		minValue = 1000;
		for(int i=0; i<1000; i++) {
			int actionsCount = visit.getActions(ExtendedDemoUser.MARIA_USER, synthetic).length;
			maxValue = Math.max(maxValue, actionsCount);
			minValue = Math.min(minValue, actionsCount);
		}
		assertTrue(maxValue == 11);
		assertTrue(minValue == 7);
	}
	
	@Test
	public void testLongVisit() {
		WordPressShopVisit visit = new WordPressShopVisit("http://127.0.0.1:8080/", VisitLength.LONG);
		Location location = new Location("Atlantis", "Meropis", "0.0.0.0");
		
		Location synthetic = new Location(null, null, "1.1.1.1", 0, LocationType.DynatraceSynthetic);
		int maxValue = 0;
		int minValue = 1000;
		for(int i=0; i<1000; i++) {
			int actionsCount = visit.getActions(ExtendedDemoUser.MARIA_USER, location).length;
			maxValue = Math.max(maxValue, actionsCount);
			minValue = Math.min(minValue, actionsCount);
		}
		assertTrue(maxValue == 22);
		assertTrue(minValue == 12);
		
		maxValue = 0;
		minValue = 1000;
		for(int i=0; i<1000; i++) {
			int actionsCount = visit.getActions(ExtendedDemoUser.MARIA_USER, synthetic).length;
			maxValue = Math.max(maxValue, actionsCount);
			minValue = Math.min(minValue, actionsCount);
		}
		assertTrue(maxValue == 23);
		assertTrue(minValue == 13);
	}

}
