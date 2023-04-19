package com.dynatrace.diagnostics.uemload.openkit.action.definition;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class BetweenMatcher extends TypeSafeMatcher<Long> {
	private final DurationBounds duration;

	private BetweenMatcher(DurationBounds duration) {
		this.duration = duration;
	}

	public static BetweenMatcher isBetween(DurationBounds duration) {
		return new BetweenMatcher(duration);
	}

	@Override
	protected boolean matchesSafely(Long value) {
		return value >= duration.min && value <= duration.max;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Number between " + duration.min + " and " + duration.max);
	}
}
