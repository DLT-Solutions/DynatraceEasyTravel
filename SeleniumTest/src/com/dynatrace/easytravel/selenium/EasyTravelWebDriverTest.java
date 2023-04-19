package com.dynatrace.easytravel.selenium;

import org.junit.Test;

public class EasyTravelWebDriverTest extends EasyTravelWebDriverBase {

	@Test
	public void testHomePage() throws Exception {
		EasyTravelHomePage.open(driver);
		initTestRunId();
		endVisit();
	}

	@Test
	public void testPurchase() throws Exception {
		EasyTravelHomePage homePage = EasyTravelHomePage.open(driver);
		initTestRunId();
		setTestName("doLogin");
		EasyTravelLoginDialog loginDialog = homePage.loginDialog();
		loginDialog.login("hainer", "hainer");
		endVisit();

		setTestName("doSearch");
		// search for New York and verify that there are results
		EasyTravelSearchResult result = homePage.search("New York");
		int resultCount = result.getSearchResultCount();
		if(resultCount == 0)
			throw new Exception("No Search Results for New York");
		endVisit();

		setTestName("selectAndBuy");
		System.out.println("Result count: " + resultCount);
		// click on a booklink
		int randomIndex = new java.util.Random().nextInt(resultCount);
		result.clickOnBookLink(randomIndex);
		// now we are on booking page 2 (since already logged in)
		result.clickOnBookReviewNextLink();
		// now we are on booking page 3 (credit card)
		// now purchase the flight
		result.completeBuy("4111111111111111");
		endVisit();

		setTestName("doLogout");
		homePage.logout();
		endVisit();
	}
}
