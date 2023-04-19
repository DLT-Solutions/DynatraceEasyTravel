package com.dynatrace.easytravel.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class EasyTravelLoginDialog extends PageObjectBase {

	public EasyTravelLoginDialog(WebDriver driver) {
		super(driver);
	}

	public void login(String username, String password) throws Exception {

		// make the login popup visible
		verifyElementAndClick(By.xpath("//a[@id='loginForm:loginLink']"), "Login Link", true);

		// the login-form can be slow to open...
		Thread.sleep(3000);

		// enter username
		WebElement usernameField = verifyElementAndClick(By.xpath("//input[@id='loginForm:username']"), "Username Field", false);
		usernameField.clear();
		usernameField.sendKeys(username);

		// enter password
		WebElement passwordField = verifyElementAndClick(By.xpath("//input[@id='loginForm:password']"), "Password Field", false);
		passwordField.clear();
		passwordField.sendKeys(password);

		// click submit
		verifyElementAndClick(By.xpath("//input[@id='loginForm:loginSubmit']"), "Login Button", true);

		// make sure everything is loaded correctly
		Thread.sleep(3000);
	}
}
