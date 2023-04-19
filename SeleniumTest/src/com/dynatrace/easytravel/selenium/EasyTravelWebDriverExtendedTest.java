package com.dynatrace.easytravel.selenium;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class EasyTravelWebDriverExtendedTest extends EasyTravelWebDriverBase {

	@Test
	public void testLogin() throws Exception {
		EasyTravelHomePage homePage = EasyTravelHomePage.open(driver);
		initTestRunId();
		setTestName("testLogin");
		try {
			EasyTravelLoginDialog loginDialog = homePage.loginDialog();
			loginDialog.login("hainer", "hainer");
			Assert.assertTrue("Expecting to be logged in", homePage.isLoggedIn());
			homePage.logout();
			Assert.assertFalse("Expecting to be NOT logged in", homePage.isLoggedIn());
		} finally {
			endVisit();
		}
	}

	@Test
	public void testSearch() throws Exception {
		EasyTravelHomePage homePage = EasyTravelHomePage.open(driver);
		initTestRunId();
		setTestName("testSearch");

		try {
			String searchStrings[] = {"Paris", "Athens", "Boston"}; // NOSONAR
			int totalCount = 0;

			for(String searchString : searchStrings) {
				EasyTravelSearchResult result = homePage.search(searchString);
				int count = result.getSearchResultCount();
				totalCount += count;
				System.out.println("Search results for '" + searchString + "': " + count);
			}
			Assert.assertTrue("Expecting to find something", totalCount > 0);
		} finally {
			endVisit();
		}
	}

	@Test
	public void testSuggestions() throws Exception {
		EasyTravelHomePage homePage = EasyTravelHomePage.open(driver);
		initTestRunId();
		setTestName("testSuggestions");

		try {
			String searchStrings[] = {"Pa", "At", "Bos"}; // NOSONAR
			int totalCount = 0;

			for(String searchString : searchStrings) {
				EasyTravelSuggestions result = homePage.searchSuggestions(searchString);
				List<String> suggestions = result.getSuggestions();
				totalCount += suggestions.size();
				System.out.println("Suggestions for '" + searchString + "': " + suggestions);
			}
			Assert.assertTrue("Expecting to find some suggestions", totalCount > 0);
		} finally {
			endVisit();
		}
	}
}
