package com.dynatrace.easytravel.metrics;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.dynatrace.easytravel.metrics.MetricsServletInit;



public class MetricsServletInitTest {
	@Test
	public void testInit() {
		assertNotNull(new MetricsServletInit().getMetricRegistry());
	}
}
