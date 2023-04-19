/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: OperatingSystemTest.java
 * @date: 29.06.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.launcher.agent;

import static org.junit.Assert.*;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;


/**
 *
 * @author dominik.stadler
 */
public class OperatingSystemTest {

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.agent.OperatingSystem#isCurrent(com.dynatrace.easytravel.launcher.agent.OperatingSystem)}.
	 */
	@Test
	public void testIsCurrent() {
		if (SystemUtils.IS_OS_WINDOWS) {
			assertTrue(OperatingSystem.isCurrent(OperatingSystem.WINDOWS));
			assertFalse(OperatingSystem.isCurrent(OperatingSystem.LINUX));
			assertFalse(OperatingSystem.isCurrent(OperatingSystem.MAC_OS));

			assertTrue(StringUtils.isNotEmpty(OperatingSystem.pickUp().getExecutableExtension()));
			assertTrue(StringUtils.isNotEmpty(OperatingSystem.pickUp().getLibraryExtension()));
			assertTrue(StringUtils.isNotEmpty(OperatingSystem.getCurrentExecutableExtension()));
		} else if (SystemUtils.IS_OS_LINUX) {
			assertFalse(OperatingSystem.isCurrent(OperatingSystem.WINDOWS));
			assertTrue(OperatingSystem.isCurrent(OperatingSystem.LINUX));
			assertFalse(OperatingSystem.isCurrent(OperatingSystem.MAC_OS));

			assertEquals(BaseConstants.EMPTY_STRING, OperatingSystem.pickUp().getExecutableExtension());
			assertTrue(StringUtils.isNotEmpty(OperatingSystem.pickUp().getLibraryExtension()));
			assertEquals(BaseConstants.EMPTY_STRING, OperatingSystem.getCurrentExecutableExtension());
		} else if (SystemUtils.IS_OS_MAC) {
			assertFalse(OperatingSystem.isCurrent(OperatingSystem.WINDOWS));
			assertFalse(OperatingSystem.isCurrent(OperatingSystem.LINUX));
			assertTrue(OperatingSystem.isCurrent(OperatingSystem.MAC_OS));

			assertEquals(BaseConstants.EMPTY_STRING, OperatingSystem.pickUp().getExecutableExtension());
			assertTrue(StringUtils.isNotEmpty(OperatingSystem.pickUp().getLibraryExtension()));
			assertEquals(BaseConstants.EMPTY_STRING, OperatingSystem.getCurrentExecutableExtension());
		}

		assertTrue(StringUtils.isNotEmpty(OperatingSystem.pickUp().getName()));
	}
}
