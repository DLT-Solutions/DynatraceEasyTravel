package com.dynatrace.easytravel.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class EasyTravelHomePage extends PageObjectBase {

	public EasyTravelHomePage(WebDriver driver) {
		super(driver);
	}

	public static EasyTravelHomePage open(WebDriver driver) throws Exception {
		EasyTravelHomePage homePage = new EasyTravelHomePage(driver);
		homePage.openHomePage();

		return homePage;
	}

	public void openHomePage() throws Exception {
		driver.navigate().to(PageObjectBase.EASYTRAVEL_HOME);

		// now we wait for the search tag
		WebElement search = waitForElement(By.className("iceCmdBtn"));
		if(search == null)
			throw new Exception("Could not open home page: " + PageObjectBase.EASYTRAVEL_HOME);
	}

	public void disableFacebook(boolean enable) throws Exception {
		driver.navigate().to(PageObjectBase.EASYTRAVEL_HOME + "/facebook.jsf");

		verifyElementAndClick(By.xpath("//input[@id='iceform:facebookStateRadio:_" + (enable ? "1" : "2") + "']"), "Facebook Disable Link", true);
	}

	/**
	 * Returns true if we are currently logged in - verified by checking for the logout button
	 * @return
	 */
	public boolean isLoggedIn() {
		WebElement logoutLink = waitForElement(By.xpath("//a[@id='loginForm:logoutLink']"));
		return (logoutLink != null);
	}

	public EasyTravelLoginDialog loginDialog() throws Exception {
		// lets click on the Login Button
		verifyElementAndClick(By.xpath("//a[@id='loginForm:loginLink']"), "Login Link", false);
		return new EasyTravelLoginDialog(driver);
	}

	public void logout() throws Exception {
		verifyElementAndClick(By.xpath("//a[@id='loginForm:logoutLink']"), "Logout Link", true);

		// make sure everyting is loaded correctly
		java.lang.Thread.sleep(1000);
	}

	/**
	 * Executes a search and hits enter
	 * @param searchString
	 * @throws Exception
	 */
	public EasyTravelSearchResult search(String searchString) throws Exception {
		// enter search value
		WebElement searchField = verifyElementAndClick(By.xpath("//input[@id='iceform:destination']"), "Search Field", false);
		searchField.clear();
		typeKeys(searchField, searchString);
		driver.findElement(By.id("iceform:destination")).sendKeys(Keys.TAB);
		java.lang.Thread.sleep(4000);
		// click search button
		verifyElementAndClick(By.xpath("//input[@id='iceform:search']"), "Search Button", true);

		// how long to wait for the search result to be back?
		java.lang.Thread.sleep(2000);

		return new EasyTravelSearchResult(driver);
	}

	/**
	 * types keys in the search field which opens the suggestions text box
	 * @param searchString
	 * @return
	 * @throws Exception
	 */
	public EasyTravelSuggestions searchSuggestions(String searchString) throws Exception {
		WebElement searchField = verifyElementAndClick(By.xpath("//input[@id='iceform:destination']"), "Search Field", false);
		searchField.clear();
		typeKeys(searchField, " ");
		searchField = verifyElementAndClick(By.xpath("//input[@id='iceform:destination']"), "Search Field", false);
		typeKeys(searchField, searchString);

		// how long to wait for the search result to be back?
		java.lang.Thread.sleep(5000);

		return new EasyTravelSuggestions(driver);
	}
}
