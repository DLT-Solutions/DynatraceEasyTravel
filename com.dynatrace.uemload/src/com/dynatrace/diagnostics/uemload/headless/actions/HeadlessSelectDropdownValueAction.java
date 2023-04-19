package com.dynatrace.diagnostics.uemload.headless.actions;

import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.ActionExecutor;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.headless.HeadlessActionExecutor;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HeadlessSelectDropdownValueAction extends Action {
	
	private static final Logger LOGGER = LoggerFactory.make();

	private By by;
	private static final int errorRate = EasyTravelConfig.read().headlessAngularJsErrorRate;
	private static final Random random = new Random();

	public HeadlessSelectDropdownValueAction(By by) {
		this.by = by;
	}

	@Override
	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
		HeadlessActionExecutor exec = (HeadlessActionExecutor) browser;
		WebElement webElement = null;

		try {
			webElement = exec.getWait().until(ExpectedConditions.visibilityOfElementLocated(by));
  		} catch (Exception e) {
			LOGGER.error( "Web element [" + by +"] not found", e);
		}

		if (webElement != null) {    			
			int value = getRandomValue();
			try {
				Select select = new Select(webElement);    				
				selectEntryByIndex(webElement, value);
				select.selectByValue(Integer.toString(value));
			} catch (Exception e) {
				LOGGER.error( "A problem occured when changing value of dropdown identified with: [" + by +"] to: " + value, e);
			}
		}
	}
	
	private int getRandomValue() {
		return random.nextInt(101) > errorRate ? random.nextInt(5) + 1 : 6;
	}

	private void selectEntryByIndex(WebElement webElement, int index) throws InterruptedException {
		for(int i=0 ;i<index; i++) {
			webElement.sendKeys(Keys.DOWN);
		}
		Thread.sleep(1000);
		webElement.sendKeys(Keys.ENTER);
	}
}
