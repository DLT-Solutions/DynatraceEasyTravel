package com.dynatrace.easytravel.selenium;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


public class EasyTravelSearchResult extends PageObjectBase {
	
	public EasyTravelSearchResult(WebDriver driver) {
		super(driver);
	}

	public int getSearchResultCount() {
		// first we wait for the first result to show up
		WebElement firstResultRow = waitForElement(By.xpath("//div[@id='iceform:dataList:0:result']"));
		if(firstResultRow == null)
			return 0;

		// need to iterature through all TRs - seems the contains on an @id doesnt work
		int count = 0;
		List<WebElement> searchResult = waitForElements(By.tagName("div"));
		for(WebElement divTag : searchResult) {
			try {
				String id = divTag.getAttribute("id");
				if(id.startsWith("iceform:dataList:") && id.endsWith(":result")) {
					count++;
				}
			} catch (StaleElementReferenceException e) {
				// this is reported for some elements in Selenium 2.25 now, but we are not overly interested in why this happens...
				e.printStackTrace();
			}
		}

		return count;
	}

	public void clickOnBookLink(int resultIndex) throws Exception {
		verifyElementAndClick(By.xpath("//a[@id='iceform:dataList:" + resultIndex + ":bookLink']"), "Book Link", true);
	}

	public void clickOnBookReviewNextLink() throws Exception {
		java.lang.Thread.sleep(2000);
		verifyElementAndClick(By.xpath("//a[@id='iceform:bookReviewNext']"), "Booking Review Next Link", true);
	}

	public void completeBuy(String creditCard) throws Exception {
		// make sure everyting is loaded correctly
		java.lang.Thread.sleep(2000);

		// fill mock data
		verifyElementAndClick(By.xpath("//a[@id='iceform:fillMock']"), "Fill Mock Data Link", true);

		// make sure everyting is loaded correctly
		java.lang.Thread.sleep(2000);

		// enter credit card information
		WebElement creditCardNumber = verifyElementAndClick(By.xpath("//input[@id='iceform:creditCardNumber']"), "Credit Card Field", false);
		creditCardNumber.sendKeys(creditCard);

        // now the Verification Button
        verifyElementAndClick(By.xpath("//input[@id='iceform:bookPaymentNext']"), "Booking Payment Next Button", true);

        // check if there is no error
        verifyErrorLabel(By.xpath("//div[@class='bookPaymentInputError']"), true);

		// make sure everyting is loaded correctly
		java.lang.Thread.sleep(2000);

        // click on confirm and wait for the close button
		verifyElementAndClick(By.xpath("//input[@id='iceform:bookFinishFinish']"), "Booking Finish Finish Button", true);

		// make sure there is no error on the page
        verifyErrorLabel(By.xpath("//div[@class='bookPaymentInputError']"), true);

		// make sure everyting is loaded correctly
		java.lang.Thread.sleep(2000);
	}
}
