package com.dynatrace.easytravel.selenium;

import org.junit.Test;


public class EasyTravelAdminWebDriverTest extends EasyTravelWebDriverBase {

	@Test
	public void testAdminHomePage() throws Exception {
		EasyTravelAdminHomePage.open(driver);
		initTestRunId();
		setTestName("adminPage");
		endVisit();
	}

	@Test
	public void testAllAdminLinks() throws Exception {
		EasyTravelAdminHomePage homePage = EasyTravelAdminHomePage.open(driver);
		initTestRunId();
		setTestName("allAdminLinks");
		homePage.openJourney("Personal Travel Inc.", "pti");
		homePage.openBookings(null, null);
		homePage.openLocations(null, null);
		homePage.openReports(null, null);
		homePage.doLogout();
		endVisit();
	}
}
