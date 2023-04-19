package com.dynatrace.easytravel.config;

import org.junit.Assert;
import org.junit.Test;

public class InstallationTypeTest {

	@Test
	public void testCompatibility() throws Exception {
		// Both matches with everything except Unknown and null
		Assert.assertTrue(InstallationType.Both.matches(InstallationType.Both));
		Assert.assertTrue(InstallationType.Both.matches(InstallationType.Classic));
		Assert.assertTrue(InstallationType.Both.matches(InstallationType.APM));

		Assert.assertFalse(InstallationType.Both.matches(InstallationType.Unknown));
		Assert.assertFalse(InstallationType.Both.matches(null));

		// Classic matches with Classic and Both
		Assert.assertTrue(InstallationType.Classic.matches(InstallationType.Classic));
		Assert.assertTrue(InstallationType.Classic.matches(InstallationType.Both));

		Assert.assertFalse(InstallationType.Classic.matches(InstallationType.Unknown));
		Assert.assertFalse(InstallationType.Classic.matches(InstallationType.APM));
		Assert.assertFalse(InstallationType.Classic.matches(null));

		// APM matches with APM and Both
		Assert.assertTrue(InstallationType.APM.matches(InstallationType.APM));
		Assert.assertTrue(InstallationType.APM.matches(InstallationType.Both));

		Assert.assertFalse(InstallationType.APM.matches(InstallationType.Unknown));
		Assert.assertFalse(InstallationType.APM.matches(InstallationType.Classic));
		Assert.assertFalse(InstallationType.APM.matches(null));

		// Unknown will not match with anything
		Assert.assertFalse(InstallationType.Unknown.matches(InstallationType.Unknown));
		Assert.assertFalse(InstallationType.Unknown.matches(InstallationType.Both));
		Assert.assertFalse(InstallationType.Unknown.matches(InstallationType.Classic));
		Assert.assertFalse(InstallationType.Unknown.matches(InstallationType.APM));
		Assert.assertFalse(InstallationType.Unknown.matches(null));
	}
}
