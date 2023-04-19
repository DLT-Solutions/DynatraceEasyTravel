package com.dynatrace.easytravel.selenium;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.dynatrace.webautomation.DynaTraceWebDriverHelper;

public class PageObjectBase {

	public static final String EASYTRAVEL_HOME;
	public static final String EASYTRAVELADMIN_HOME;

	static {
	    String customerHost = System.getProperty("customerFrontendHost");
	    String customerPort = System.getProperty("customerFrontendPort");
	    String b2bHost = System.getProperty("b2bFrontendHost");
        String b2bPort = System.getProperty("b2bFrontendPort");

        if (customerHost != null && customerPort != null
                && !customerHost.isEmpty() && !customerPort.isEmpty()) {
            EASYTRAVEL_HOME = "http://" + customerHost + ":" + customerPort;
        } else {
            EASYTRAVEL_HOME = "http://localhost:8080";
        }
        if (b2bHost != null && b2bPort != null
                && !b2bHost.isEmpty() && !b2bPort.isEmpty()) {
            EASYTRAVELADMIN_HOME = "http://" + b2bHost + ":" + b2bPort;
        } else {
            EASYTRAVELADMIN_HOME = "http://localhost:9000";
        }
	}

	public static int DEFAULT_TIMEOUT = 10000;
	public static int DEFAULT_PAGE_READY_TIMEOUT = 30000;

	protected WebDriver driver;
	protected DynaTraceWebDriverHelper dynaTrace;
	protected boolean isFirefox = false;

	public PageObjectBase(WebDriver driver) {
		this.driver = driver;
		isFirefox = driver instanceof FirefoxDriver;
		dynaTrace = DynaTraceWebDriverHelper.forDriver(driver);
	}

	public WebElement waitForElement(By object) {
		return waitForElement(object, DEFAULT_TIMEOUT);
	}

	public WebElement waitForElement(By object, long timeout) {
		List<WebElement> elements = waitForElements(object, timeout);
		return elements == null || elements.size() == 0 ? null : elements.get(0);
	}

	public List<WebElement> waitForElements(By object) {
		return waitForElements(object, DEFAULT_TIMEOUT);
	}

	public List<WebElement> waitForElements(By object, long timeout) {
		while(timeout >= 0) {
			List<WebElement> elements = driver.findElements(object);
			if(elements != null && elements.size() > 0){
				return elements;
			}
			try {
				if(timeout >= 0) {
					timeout-=250;
					java.lang.Thread.sleep(250);
				}
			} catch (InterruptedException e) {
			}
		}

		return null;
	}

	/**
	 * Searches for tags that match the passed text, e.g.: link names
	 * @param tagName
	 * @param text
	 * @return
	 */
	public WebElement waitForTagByText(String tagName, String text) {
		return waitForTagByText(tagName, text, DEFAULT_TIMEOUT);
	}
	public WebElement waitForTagByText(String tagName, String text, long timeout) {
		while(timeout >= 0) {
			List<WebElement> elements = driver.findElements(By.tagName(tagName));
			if(elements != null) {
				for(WebElement element : elements) {
					String textValue = element.getText();
					if(textValue != null && textValue.equalsIgnoreCase(text))
						return element;
				}
			}

			try {
				if(timeout >= 0) {
					timeout-=250;
					java.lang.Thread.sleep(250);
				}
			} catch (InterruptedException e) {
			}
		}

		return null;
	}

	/**
	 * Searches for tags that match the passed name, e.g.: username
	 * @param tagName
	 * @param text
	 * @return
	 */
	public WebElement waitForTagByName(String tagName, String elementName) {
		return waitForTagByName(tagName, elementName, DEFAULT_TIMEOUT);
	}
	public WebElement waitForTagByName(String tagName, String elementName, long timeout) {
		while(timeout >= 0) {
			List<WebElement> elements = driver.findElements(By.tagName(tagName));
			if(elements != null) {
				for(WebElement element : elements) {
					String nameValue = element.getAttribute("name");
					if(nameValue != null && nameValue.equalsIgnoreCase(elementName))
						return element;
				}
			}

			try {
				if(timeout >= 0) {
					timeout-=250;
					java.lang.Thread.sleep(250);
				}
			} catch (InterruptedException e) {
			}
		}

		return null;
	}

	/**
	 * Searches for Tags by Id -> need that for IE as WebDriver seems to have a problem on IE with By.id("")
	 * @param tagName
	 * @param id
	 * @return
	 */
	public WebElement waitForTagById(String tagName, String id) {
		return waitForTagById(tagName, id, DEFAULT_TIMEOUT);
	}
	public WebElement waitForTagById(String tagName, String id, long timeout) {
		while(timeout >= 0) {
			List<WebElement> elements = driver.findElements(By.tagName(tagName));
			if(elements != null) {
				for(WebElement element : elements) {
					String idValue = element.getAttribute("id");
					if(idValue != null && idValue.equalsIgnoreCase(id))
						return element;
				}
			}

			try {
				if(timeout >= 0) {
					timeout-=250;
					java.lang.Thread.sleep(250);
				}
			} catch (InterruptedException e) {
			}
		}

		return null;
	}

	public boolean waitForCssName(WebElement element, String cssClassName) {
		return waitForCssName(element, cssClassName, DEFAULT_TIMEOUT);
	}

	public boolean waitForCssName(WebElement element, String cssClassName, long timeout) {
		String className = element.getAttribute("class");
		while(!className.contains(cssClassName) && timeout >= 0) {
			try {
				timeout-=250;
				java.lang.Thread.sleep(250);
			} catch (InterruptedException e) {
			}

			className = element.getAttribute("class");
		}

		return timeout >= 0;
	}

	public WebElement verifyErrorLabel(By element, boolean throwException) throws Exception {
		// make sure there is no error on the page
		WebElement errorLabel = waitForElement(element, 1000);
		if(errorLabel != null && throwException)
			throw new Exception("Found an error label: " + errorLabel.getText());

		return errorLabel;
	}

	/**
	 * Locates the webelement and - if not found - throws an exception. If found - clicks on it
	 * @param button
	 * @param elementNameForError
	 * @throws Exception
	 */
	public WebElement verifyElementAndClick(By element, String elementNameForError, boolean doClick) throws Exception {
		WebElement webElement = waitForElement(element);
		if(webElement == null)
			throw new Exception("Can't find " + elementNameForError);
		if(doClick)
			webElement.click();
		return webElement;
	}

	/**
	 * Types keys with a delay
	 * @param element
	 * @param keys
	 * @throws Exception
	 */
	public void typeKeys(WebElement element, String keys) throws Exception {
		for(char key : keys.toCharArray()) {
			element.sendKeys(Character.toString(key));
			java.lang.Thread.sleep(150);
		}
	}
}