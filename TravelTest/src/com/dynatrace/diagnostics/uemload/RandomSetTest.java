package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


public class RandomSetTest {

	private static final int TEST_COUNT = 1000000;
	
	@Test
	public void test() {
		RandomSet<String> s = new RandomSet<String>();
		
		//should should be empty
		assertTrue(s.isEmpty());

		// nothing happens on empty set
		s.setWeightInPercent("bar", 150);
		
		//should should be empty
		assertTrue(s.isEmpty());
		
		s.add("foo", 3);
		s.add("bar", 3);
		s.add("donkey", 3);

		//should not be empty
		assertFalse(s.isEmpty());

		s.setWeightInPercent("foo", 50);

		// no effect for unknown item
		s.setWeightInPercent("unknown", 150);

		// Map<String, Integer> counts = new HashMap<String, Integer>();
		Map<String, Integer> counts = new HashMap<>(); // Java 7 should pass this OK
		for(int i = 0; i < TEST_COUNT; i++) {
			String key = s.getRandom();
			Integer cnt = counts.get(key);
			if(cnt == null) cnt = 0;
			counts.put(key, cnt + 1);
		}
		System.err.println("foo: " + counts.get("foo"));
		System.err.println("bar: " + counts.get("bar"));
		System.err.println("bar: " + counts.get("donkey"));

		// check that the results are not too far off
		assertEquals((double)TEST_COUNT/2, counts.get("foo"), 2000);
		assertEquals((double)TEST_COUNT/4, counts.get("bar"), 2000);
		assertEquals((double)TEST_COUNT/4, counts.get("donkey"), 2000);
	}
}
