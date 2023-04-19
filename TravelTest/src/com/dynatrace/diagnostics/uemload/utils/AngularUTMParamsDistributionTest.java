package com.dynatrace.diagnostics.uemload.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AngularUTMParamsDistributionTest {

	@Test
	public void createAngularUTMParamsString() {
		AngularUTMParams params = AngularUTMParamsDistribution.getRandomParams();
		String urlParamsRegexp = "(\\?utm_source=[0-9a-zA-Z]+&utm_medium=[0-9a-zA-Z]+&utm_campaign=[0-9a-zA-Z&=_-]+&gclid=[0-9A-Za-z]+)";

		assertTrue(params.toString().matches(urlParamsRegexp));
	}
}
