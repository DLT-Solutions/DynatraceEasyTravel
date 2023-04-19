package com.dynatrace.easytravel.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class EasyTravelAdminHomePage extends PageObjectBase {

	public EasyTravelAdminHomePage(WebDriver driver) {
		super(driver);
	}

	public static EasyTravelAdminHomePage open(WebDriver driver) throws Exception {
		EasyTravelAdminHomePage homePage = new EasyTravelAdminHomePage(driver);
		homePage.openHomePage();

		return homePage;
	}

	public void openHomePage() throws Exception {
		driver.navigate().to(PageObjectBase.EASYTRAVELADMIN_HOME);

		// now we wait for the links
		WebElement link = waitForElement(By.className("etNoLink"), 30000);
		if(link == null) {
			throw new Exception("Could not open home page: " + PageObjectBase.EASYTRAVELADMIN_HOME);
		}
	}

	protected void doLogin(String username, String password) throws Exception {
		WebElement userField = waitForTagById("input", "UserName");
		if(userField == null) return;
		WebElement passField = waitForTagById("input", "Password");
		
		userField.clear();
		passField.clear();
		java.lang.Thread.sleep(1000);
		
		typeKeys(userField, username);
		typeKeys(passField, password);

		// press submit
		verifyElementAndClick(By.xpath("//input[@type='submit']"), "Submit Button", true);

		// check if we have a field validation error
		WebElement validationError = waitForElement(By.className("field-validation-error"), 2000);
		if(validationError != null)
			throw new Exception("Logon failed");
	}

	public void doLogout() throws Exception {
		verifyElementAndClick(By.xpath("//div[@id='logoffmenu']"), "Logout Menu", true);
	}

	private void openLink(String username, String password, String linkText, String headlineText) throws Exception {
		// find the Journey link
		verifyElementAndClick(By.xpath("//a[text()='" + linkText + "']"), linkText + " Link", true);

		// need to logon?
		if(username != null)
			doLogin(username, password);

		// now verify we are on the correct page
		verifyElementAndClick(By.xpath("//h2[text()='" + headlineText + "']"), linkText + " Headline", false);

		// wait an additional second to make sure the page got fully loaded
		java.lang.Thread.sleep(1000);
	}

	public void openJourney(String username, String password) throws Exception {
		openLink(username, password, "Journeys", "Journeys");
	}

	public void openLocations(String username, String password) throws Exception {
		openLink(username, password, "Locations", "Locations");
	}

	public void openBookings(String username, String password) throws Exception {
		openLink(username, password, "Bookings", "Bookings");
	}

	public void openReports(String username, String password) throws Exception {
		openLink(username, password, "Booking Report", "Reports");
	}
}
