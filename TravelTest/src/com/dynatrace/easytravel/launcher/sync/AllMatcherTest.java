package com.dynatrace.easytravel.launcher.sync;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dynatrace.easytravel.launcher.sync.Predicate.AllMatcher;


public class AllMatcherTest {

	@Test
	public void testShouldStop() {
		AllMatcher<String> matcher = new AllMatcher<String>();
		assertFalse(matcher.shouldStop());
	}

	@Test
	public void testEval() {
		AllMatcher<String> matcher = new AllMatcher<String>();
		assertTrue(matcher.eval("somestring"));
		assertTrue(matcher.eval(null));
		assertTrue(matcher.eval(""));
		assertTrue(matcher.eval("2349872397"));
	}
}
