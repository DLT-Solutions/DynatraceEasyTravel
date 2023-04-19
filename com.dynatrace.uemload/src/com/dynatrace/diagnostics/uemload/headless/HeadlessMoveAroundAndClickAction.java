/**
 *
 */
package com.dynatrace.diagnostics.uemload.headless;

import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.ActionExecutor;
import com.dynatrace.diagnostics.uemload.HeadlessBySelectors;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author tomasz.wieremjewicz
 * @date 17 gru 2018
 *
 */
public class HeadlessMoveAroundAndClickAction extends Action {
	private static final Logger LOGGER = LoggerFactory.make();

	private By elementToClick;
	private By elementToMoveAround;
	private By logoutElement;
	private static final int failRate = EasyTravelConfig.read().headlessAngularUsabilityClickPayFailRate;
	private static final Random random = new Random();

	public HeadlessMoveAroundAndClickAction(By elementToClick, By elementToMoveAround, By logoutElement) {
		this.elementToClick = elementToClick;
		this.elementToMoveAround = elementToMoveAround;
		this.logoutElement = logoutElement;
	}

	@Override
	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
		HeadlessActionExecutor exec = (HeadlessActionExecutor) browser;
		WebElement webElementToMoveAround = null;
		WebElement webElementToClick = null;

		try {
			webElementToMoveAround = exec.getWait().until(ExpectedConditions.visibilityOfElementLocated(elementToMoveAround));
  		} catch (Exception e) {
			LOGGER.error( "Web element [" + elementToMoveAround + "] not found", e);
		}

		try {
			webElementToClick = exec.getWait().until(ExpectedConditions.visibilityOfElementLocated(elementToClick));
  		} catch (Exception e) {
			LOGGER.error( "Web element [" + elementToClick + "] not found", e);
		}

		if (webElementToMoveAround != null && webElementToClick != null) {
			try {
				Point start = webElementToMoveAround.getLocation();
				Point end = webElementToMoveAround.getLocation();

				Actions builder = new Actions(exec.getDriver());
				builder.moveToElement(webElementToMoveAround, webElementToMoveAround.getSize().width/2, -1 * webElementToMoveAround.getSize().height)
				.build().perform();

				Point origin = new Point(webElementToMoveAround.getLocation().x + webElementToMoveAround.getSize().width/2,
						webElementToMoveAround.getLocation().y - webElementToMoveAround.getSize().height);

				for (int i=0; i < 5 + random.nextInt(5); i++) {
					start = end;
					end = getNewPoint(origin.x, origin.y, webElementToClick.getSize().width, webElementToClick.getSize().height, i%4);

					moveFromPointToPoint(start, end, exec.getDriver());

					Thread.sleep(250);
				}

				int doWeClickPay = random.nextInt(101);
				try {
					JavascriptExecutor javascriptExecutor = exec.getDriver();
		    		javascriptExecutor.executeScript(String.format("window.scrollBy(0,%d)", (doWeClickPay > failRate ? 2000 : 0)));
		    		javascriptExecutor.executeScript(String.format("document.body.scrollTop = %d", (doWeClickPay > failRate ? 2000 : 0)));
		    		Thread.sleep(50);

	    			webElementToClick = exec.getWait().until(ExpectedConditions.elementToBeClickable(
	    					doWeClickPay > failRate ? elementToClick : logoutElement));
	    			webElementToClick.click();
	      		} catch (Exception e) {
	      			if (doWeClickPay > failRate) {
	      				LOGGER.error( "Web element [" + elementToClick + "] not found", e);
	      			}
	      			else {
	      				tryClickingInMobileWay(exec);
	      			}
	    		}
			} catch (Exception e) {
				LOGGER.error( "A problem occured when dragging the mouse", e);
			}
		}
	}

	private void tryClickingInMobileWay(HeadlessActionExecutor exec) {
		try {
			WebElement webElementToClick = exec.getWait().until(ExpectedConditions.elementToBeClickable(HeadlessBySelectors.AngularNavigationWhenMobile.get()));
			exec.getDriver().executeScript("arguments[0].click();", webElementToClick);
			Thread.sleep(1000);
			webElementToClick = exec.getWait().until(ExpectedConditions.elementToBeClickable(logoutElement));
			exec.getDriver().executeScript("arguments[0].click();", webElementToClick);
		}
		catch (Exception ex) {
			LOGGER.error( "Web element [" + logoutElement + "] not found", ex);
		}
	}

	private void moveFromPointToPoint(Point start, Point end, ChromeDriver driver) throws InterruptedException {
		int steps = 30;
		float xOffset = (end.x - start.x) * 1.0f / steps;
		float yOffset = (end.y - start.y) * 1.0f / steps;
		Actions builder = new Actions(driver);

		for (int i=1; i < steps; i++) {
			builder = new Actions(driver);
			builder.moveByOffset(Math.round(xOffset*i) - Math.round(xOffset*(i-1)), Math.round(yOffset*i) - Math.round(yOffset*(i-1)))
			.build().perform();
			Thread.sleep(1);
		}
	}

	private Point getNewPoint(int x, int y, int width, int height, int mode) {
		switch (mode) {
		case 0:
			return new Point(x + (2*width) + random.nextInt(width), y + (4*height) + random.nextInt(height));
		case 1:
			return new Point(x - (2*width) - random.nextInt(width), y - (4*height) - random.nextInt(height));
		case 2:
			return new Point(x + (2*width) + random.nextInt(width), y - (2*height) - random.nextInt(height));
		case 3:
			return new Point(x - (2*width) - random.nextInt(width), y + (2*height) + random.nextInt(height));
		default:
			return new Point(x, y);
		}
	}
}
