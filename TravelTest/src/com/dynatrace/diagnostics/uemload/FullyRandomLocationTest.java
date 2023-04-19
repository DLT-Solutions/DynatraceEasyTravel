package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FullyRandomLocationTest {

	@Test
	public void testIpv6Generator() {
		float testAmount = 100;
		FullyRandomLocation fullyRandomLocation = new FullyRandomLocation();
		float ipV6Amount = 0;
		for (int i = 0; i < testAmount; i++) {
			if (!fullyRandomLocation.get().getIp().matches(
					"^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
				ipV6Amount++;
			}
		}
		assertTrue("There are too many IPv6 in comparison to IPv4", (ipV6Amount / testAmount) < 0.25f);

	}
}
