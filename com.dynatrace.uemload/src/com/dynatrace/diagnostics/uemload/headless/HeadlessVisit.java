package com.dynatrace.diagnostics.uemload.headless;

import static com.dynatrace.diagnostics.uemload.HeadlessBySelectors.*;
import static java.lang.String.format;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.ActionExecutor;
import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.HeadlessBySelectors;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CreditCard;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PseudoRandomJourneyDestination;
import com.dynatrace.diagnostics.uemload.utils.AngularSearchParameters;
import com.dynatrace.diagnostics.uemload.utils.AngularUTMParamsDistribution;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

/**
 * Created by Kacper.Olszanski on 17.07.2017.
 */
public abstract class HeadlessVisit implements Visit {
    private static final Logger LOGGER = LoggerFactory.make();

	private static final int RAGE_CLICKS_COUNT = 5;
	private static final int WAIT_BEFORE_RAGE_CLICK_TIME = 3000;
	private static final Duration WAIT_FOR_CLICK = Duration.ofSeconds(5);
	private static final Duration CLICK_AND_SEARCH_TIMEOUT = Duration.ofSeconds(15);

    private static final int MAX_SEARCH_TRIES = PseudoRandomJourneyDestination.LOCATION_COUNT;
    
    protected static final double BOOK_FAILURE_RATE = 3;

	protected String host;

	public HeadlessVisit(String host) {
		this.host = host;
	}

    protected  void doStuff(ChromeDriver driver) {}

    protected List<Action> getCreditCardActions(CommonUser user) {
		CreditCard card = new CreditCard(user.name);
		List<Action> actions = new LinkedList<>();
		actions.add(new HeadlessSelectAction(CustomerIceFormCreditCardType.get(), card.getType()));
		actions.add(new HeadlessSendKeysAction(CustomerIceFormCredidCardNumber.get(), String.valueOf(card.getNumber())));
		actions.add(new HeadlessSendKeysAction(CustomerIceFormCreditCardOwner.get(), card.getOwner()));
		actions.add(new HeadlessSelectAction(CustomerIceFormCreditCardExpirationMonth.get(), card.getExpirationMonth()));
		actions.add(new HeadlessSelectAction(CustomerIceFormCreditCardExpirationYear.get(), String.valueOf(card.getExpirationYear())));
		actions.add(new HeadlessSendKeysAction(CustomerIceFormCVCVerification.get(), String.valueOf(card.getVerificationNumber())));
		actions.add(new HeadlessClickAction(CustomerIceFormPaymentNext.get()));
		actions.add(new HeadlessClickAction(CustomerIceFormBookFinish.get()));
		return actions;
	}

	protected List<Action> getBookingSummaryButtonActions() {
		List<Action> actions = new LinkedList<>();
		actions.add(new HeadlessClickAction(AngularBookingSummaryButton.get()));
		if(PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.ANGULAR_BOOKING_ERROR_500)) {
			actions.add(new HeadlessWaitAction(WAIT_BEFORE_RAGE_CLICK_TIME));
			for (int i = 0; i < RAGE_CLICKS_COUNT; i++) {
				actions.add(new HeadlessClickAction(AngularBookingSummaryButton.get()));
			}
		}
		actions.add(new HeadlessWaitAction(1000));
		return actions;
	}

    protected List<Action> getAngularCreditCardActions(AngularSearchParameters params, boolean stopBookingProcess, CommonUser user) {
		List<Action> actions = new LinkedList<>();
		actions.add(new HeadlessSendKeysAction(AngularCreditCardFirst4Digits.get(), params.getFirstFourDigits()));
		actions.add(new HeadlessSendKeysAction(AngularCreditCardSecond4Digits.get(), params.getSecondFourDigits()));
		actions.add(new HeadlessSendKeysAction(AngularCreditCardThird4Digits.get(), params.getThirdFourDigits()));
		
		boolean generateFail = false;
		if ((user instanceof ExtendedCommonUser)) {
			ExtendedCommonUser extendedCommonUser = (ExtendedCommonUser) user;
			if (extendedCommonUser.isSpecialMonthlyUser() || extendedCommonUser.isSpecialWeeklyUser()) {
				generateFail = true;
			}
		}
		
		if(PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.ANGULAR_BIZ_EVENTS_PLUGIN) || generateFail) {
			boolean forgetToInputCVC = UemLoadUtils.randomInt(2) == 1;
			
			if (forgetToInputCVC) {
				actions.add(new HeadlessSendKeysAction(AngularCreditCardFourth4Digits.get(), params.getFourthFourDigits()));
				actions.add(new HeadlessClickAction(AngularCreditCardSubmitButton.get()));
				if (generateFail == false && stopBookingProcess == false) {
					actions.add(new HeadlessWaitAction(2500));
					actions.add(new HeadlessSendKeysAction(AngularCreditCardCVC.get(), params.getCVC()));
					actions.add(new HeadlessClickAction(AngularCreditCardSubmitButton.get()));
				}
			}
			else {
				actions.add(new HeadlessSendKeysAction(AngularCreditCardCVC.get(), params.getCVC()));
				actions.add(new HeadlessClickAction(AngularCreditCardSubmitButton.get()));
				if (generateFail == false && stopBookingProcess == false) {
					actions.add(new HeadlessWaitAction(2500));
					actions.add(new HeadlessSendKeysAction(AngularCreditCardFourth4Digits.get(), params.getFourthFourDigits()));
					actions.add(new HeadlessClickAction(AngularCreditCardSubmitButton.get()));
				}
			}
		}
		else {
			actions.add(new HeadlessSendKeysAction(AngularCreditCardFourth4Digits.get(), params.getFourthFourDigits()));
			actions.add(new HeadlessSendKeysAction(AngularCreditCardCVC.get(), params.getCVC()));
			actions.add(new HeadlessClickAction(AngularCreditCardSubmitButton.get()));
		}
		
		return actions;
	}

	protected List<Action> getSearchActions(String from, String to) {
		List<Action> actions = new LinkedList<>();
		actions.add(new HeadlessSendKeysAction(CustomerIceFormDestination.get(), PseudoRandomJourneyDestination.get()));
		if(from != null) {
			actions.add(new HeadlessSendKeysAction(CustomerIceFormDateFrom.get(), from));
		}
		if(to != null) {
			actions.add(new HeadlessSendKeysAction(CustomerIceFormDateTo.get(), to));
		}
		actions.add(new HeadlessClickAction(CustomerIceFormSearch.get(), true));
		if(PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.JAVASCRIPT_ERROR_ONLABEL_CLICK) && Math.random() < 0.5) {
			actions.add(new HeadlessJsErrorAction(host));
		}
		if(PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.NODEJS_WEATHER_APPLICATION)) {
			actions.add(new HeadlessSwitchWindowAction(new HeadlessClickAction(CustomerWeatherForecast.get(), true)));
		}
		if(PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.PHP_ENABLEMENT_PLUGIN)){
			actions.add(new HeadlessClickAction(CustomerPHPPlugin.get(), CustomerPHPPLuginAlternative.get(), true));
		}
		return actions;
	}

	protected List<Action> getAngularSearchActions(String destination, String from, String to, String travellers) {
		List<Action> actions = new LinkedList<>();
		actions.add(new HeadlessSendKeysAction(AngularSearchDestinationField.get(), destination));
		if(checkAngularDateFormat(from)) {
			actions.add(new HeadlessSendKeysAction(AngularSearchFromDateField.get(), from));
		}
		if(checkAngularDateFormat(to)) {
			actions.add(new HeadlessSendKeysAction(AngularSearchToDateField.get(), to));
		}
		if(checkAngularTravellersFormat(travellers)) {
			actions.add(new HeadlessSendKeysAction(AngularSearchTravellersField.get(), travellers));
		}
		actions.add(new HeadlessClickAction(AngularSearchButton.get(), true));
		return actions;
	}

	private boolean checkAngularDateFormat(String date) {
		if(date == null) {
			return false;
		} else if(!date.matches("^\\d{4}-\\d{2}-\\d{2}")) {
			LOGGER.warn(TextUtils.merge("Wrong format of date String. Was: {0}. Correct format would be: yyyy-mm-dd.", date));
			return false;
		} else {
			try {
				LocalDateTime.parse(date, AngularSearchParameters.FORMATTER);
			} catch (DateTimeParseException e) {
				LOGGER.warn(TextUtils.merge("Provided date String does not represent real date. Was: {0}.", date), e); // NOSONAR
				return false;
			}
		}
		return true;
	}

	private boolean checkAngularTravellersFormat(String travellers) {
		if(travellers == null) {
			return false;
		} else if(!travellers.matches("^[1-2]{1}")) {
			LOGGER.warn(TextUtils.merge("Wrong format of travellers String. Was: {0}. Should be string representation of number in range [1-2].", travellers));
			return false;
		}
		return true;
	}

	protected static List<Action> getLoginActions(CommonUser user) {
		List<Action> actions = new LinkedList<>();
		actions.add(new HeadlessClickAction(CustomerLoginFormLoginLink.get(), true));
		actions.add(new HeadlessSendKeysAction(CustomerLoginFormUsername.get(), user.getName()));
		actions.add(new HeadlessSendKeysAction(CustomerLoginFormPassword.get(), user.getPassword()));
		actions.add(new HeadlessClickAction(CustomerLoginFormSubmit.get(), true));
		return actions;
	}

	protected static List<Action> getAngularLoginActions(CommonUser user) {
		List<Action> actions = new LinkedList<>();
		actions.add(new HeadlessSendKeysAction(AngularLoginFormUsername.get(), user.getName()));
		actions.add(new HeadlessWaitAction(1000));
		actions.add(new HeadlessSendKeysAction(AngularLoginFormPassword.get(), user.getPassword()));
		actions.add(new HeadlessWaitAction(2000));
		actions.add(new HeadlessClickAction(AngularLoginFormSubmit.get(), true));
		return actions;
	}

	protected static List<Action> getBlogVisitActions(String host) {
		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));
		if(PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.PHP_ENABLEMENT_PLUGIN)) {
			actions.add(new HeadlessClickAction(HeadlessBySelectors.BlogLink.get(), true));
			actions.add(new HeadlessClickAction(HeadlessBySelectors.BlogLatrobePost.get(), true));
			actions.add(new HeadlessClickAction(HeadlessBySelectors.BlogArchive2013Link.get(), true));
			actions.add(new HeadlessClickAction(HeadlessBySelectors.BlogItalyPost.get(), true));
		}
		return actions;
	}

	protected String getBounceUrl(String path) {
		StringBuilder sb = new StringBuilder();
		String slash = host.charAt(host.length()-1) == '/' ? "" : "/";
		sb.append(host).append(slash).append(path);
		return sb.toString();
	}

	protected List<Action> getUsabilityClickPayButtonActions() {
		List<Action> actions = new LinkedList<>();

		if(PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.USABILITY_ISSUE)) {
			actions.add(new HeadlessWaitAction(2000));
			actions.add(new HeadlessCustomPageDownPageUpAction(3, 400));
			actions.add(new HeadlessMoveAroundAndClickAction(
					HeadlessBySelectors.AngularPay2Button.get(),
					HeadlessBySelectors.AngularDateParagraph.get(),
					HeadlessBySelectors.AngularLogout.get()));
		}
		else {
			actions.add(new HeadlessClickAction(HeadlessBySelectors.AngularPayButton.get()));
		}

		return actions;
	}

    protected static class HeadlessGetAction extends Action {

    	private String url;

    	public HeadlessGetAction(String url) {
    		this.url = url;
    		
    		Random random = new Random();
    		int r = random.nextInt(10);
			if (r > 4) {
				this.url += AngularUTMParamsDistribution.getRandomParams();
			}
    	}

    	@Override
    	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
    		ChromeDriver driver = ((HeadlessActionExecutor) browser).getDriver();
    		driver.get(url);
    		LOGGER.trace("Loading url in an angular visit: " + url);
    	}

    	// returns the URL of the headless action (for debugging if necessary)
    	public String getUrl( ) {
    		return url;
    	}

    }

    protected static class HeadlessClickAction extends Action {

    	private By by;
    	private boolean clickByJS;
    	private By alternativeBy;

    	public HeadlessClickAction(By by) {
    		this(by, false);
    	}

    	public HeadlessClickAction(By by, boolean clickByJS) {
    		this(by, null, clickByJS);
    	}

    	public HeadlessClickAction(By by, By alternativeBy) {
    		this(by, alternativeBy, false);
    	}

    	public HeadlessClickAction(By by, By alternativeBy, boolean clickByJs) {
    		this.by = by;
    		this.alternativeBy = alternativeBy;
    		this.clickByJS = clickByJs;
    	}

    	@Override
    	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
    		HeadlessActionExecutor exec = (HeadlessActionExecutor) browser;
    		WebElement webElement;

    		try {
    			webElement = exec.getWait().withTimeout(WAIT_FOR_CLICK).until(ExpectedConditions.elementToBeClickable(by));
      		} catch (Exception e) {
    			LOGGER.info( "Web element [" + by +"] not found", e);
    			LOGGER.trace(exec.getDriver().getPageSource());
    			// if web element don't exist
    			if(alternativeBy != null) {
    				try {
    	    			LOGGER.info( "Trying alternative Web element [" + alternativeBy +"] ");
    					webElement = exec.getWait().until(ExpectedConditions.elementToBeClickable(alternativeBy));
    				} catch (Exception ex) {
    					LOGGER.error( "Alternative Web element [" + alternativeBy +"] not found", ex);
    					return;
    				}
    			} else {
    				return;
    			}
    		}
    		
    		clickElement(webElement, exec);
    	}
    	
    	private void clickElement(WebElement webElement, HeadlessActionExecutor exec) {
    		if(clickByJS) {
    			// sometimes webElement.click() fails for some reason I don't know
    			clickElementByJs(webElement, exec);
    		} else { 
    			clickElementByWebdriver(webElement, exec);
    		}
    	}
    	
        private void clickElementByWebdriver(WebElement webElement, HeadlessActionExecutor exec) {
        	try {
        		webElement.click();
        	} catch(Exception e) {
        		LOGGER.error("Cannot click element " + webElement + " try to click by js" + e.getMessage());
        		//use fallback click by js
        		exec.getDriver().executeScript("arguments[0].click();", webElement);
        	}
        }
        
        private void clickElementByJs(WebElement webElement, HeadlessActionExecutor exec) {
			exec.getDriver().executeScript("arguments[0].click();", webElement);        	
        }
    }
    
    protected static class HeadlessClickMobileAction extends Action {

    	private By by;
    	private boolean clickByJS;

    	public HeadlessClickMobileAction(By by) {
    		this(by, false);
    	}

    	public HeadlessClickMobileAction(By by, boolean clickByJS) {
    		this.by = by;
    		this.clickByJS = clickByJS;
    	}

    	@Override
    	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
    		HeadlessActionExecutor exec = (HeadlessActionExecutor) browser;
    		WebElement webElement;

    		try {
    			webElement = exec.getWait().until(ExpectedConditions.elementToBeClickable(by));
    			if(clickByJS) {
        			exec.getDriver().executeScript("arguments[0].click();", webElement);
        		} else {
        			webElement.click();
        		}
      		} catch (Exception e) {
    			LOGGER.trace( "Web element [" + by +"] not found - trying the mobile fix", e);
    			tryClickingInMobileWay(exec);
    		}
    	}

    	private void tryClickingInMobileWay(HeadlessActionExecutor exec) {
    		try {
    			WebElement webElement = exec.getWait().until(ExpectedConditions.elementToBeClickable(HeadlessBySelectors.AngularNavigationWhenMobile.get()));
				exec.getDriver().executeScript("arguments[0].click();", webElement);
				Thread.sleep(1000);
				webElement = exec.getWait().until(ExpectedConditions.elementToBeClickable(by));
    			if(clickByJS) {
        			exec.getDriver().executeScript("arguments[0].click();", webElement);
        		} else {
        			webElement.click();
        		}
			}
			catch (Exception ex) {
				LOGGER.error("Web element [" + by +"] not found - could not click after using mobile fix", ex);
			}
    	}
    }

    protected static class HeadlessSendKeysAction extends Action {

    	private By by;
    	private String text;

    	public HeadlessSendKeysAction(By by, String text) {
    		this.by = by;
    		this.text = text;
    	}

    	@Override
    	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
    		HeadlessActionExecutor exec = (HeadlessActionExecutor) browser;
    		// If it can't find the element then we want to stop execution of this scenario.
    		// The exception will be caught by HeadlessVisitRunnable.run() which will stop the driver
    		WebElement webElement = exec.getWait().until(ExpectedConditions.visibilityOfElementLocated(by));
    		try {
        		webElement.clear();
    		} catch(StaleElementReferenceException e) {
    			LOGGER.info( "Unable to clear element [" + by +"]");
    			if(LOGGER.isDebugEnabled()) {
					LOGGER.debug(e.getMessage(), e);
				}
    			webElement = exec.getWait().until(ExpectedConditions.visibilityOfElementLocated(by));
    			webElement.clear();
    		}
    		try {
    			webElement.sendKeys(text);
    		} catch(StaleElementReferenceException e) {
    			LOGGER.info( "Unable to type [" + text +"] into element [" + by + "]" );
    			if(LOGGER.isDebugEnabled()) {
					LOGGER.debug(e.getMessage(), e);
				}
    			webElement = exec.getWait().until(ExpectedConditions.visibilityOfElementLocated(by));
    			webElement.sendKeys(text);
    		}
    	}
    }

    protected static class HeadlessCustomPageDownPageUpAction extends Action {
    	private int repeatNumber;
    	private int sleepMillis;

    	public HeadlessCustomPageDownPageUpAction(int repeatNumber, int sleepMillis) {
    		this.repeatNumber = repeatNumber;
    		this.sleepMillis = sleepMillis;
    	}

    	@Override
    	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
    		HeadlessActionExecutor exec = (HeadlessActionExecutor) browser;
    		WebElement webElement = null;

    		try {
    			webElement = exec.getDriver().findElement(HeadlessBySelectors.AngularBody.get());
    			if (webElement != null) {
    				webElement.click();
    				for (int i = 0; i < repeatNumber; i++) {
    					webElement.sendKeys(Keys.PAGE_DOWN);
    					Thread.sleep(sleepMillis);
    					webElement.sendKeys(Keys.PAGE_UP);
    					Thread.sleep(sleepMillis);
    				}
        		}
      		} catch (Exception e) {
    			LOGGER.error( "Web element [" + HeadlessBySelectors.AngularBody.get() + "] not found", e);
    		}
    	}
    }

    protected static class HeadlessWaitAction extends Action {
    	private long millis;

    	public HeadlessWaitAction(long millis) {
    		this.millis = millis;
    	}

    	@Override
    	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
    		Thread.sleep(millis);
    	}
    }

    protected static class HeadlessSelectAction extends Action {

    	private By by;
    	private String text;

    	public HeadlessSelectAction(By by, String text) {
    		this.by = by;
    		this.text = text;
    	}

    	@Override
    	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
    		HeadlessActionExecutor exec = (HeadlessActionExecutor) browser;
    		Select select = new Select(exec.getWait().until(ExpectedConditions.visibilityOfElementLocated(by)));
    		select.selectByVisibleText(text);
    	}
    }

    protected static class HeadlessJsErrorAction extends Action {

    	private String homeUrl;

    	public HeadlessJsErrorAction(String homeUrl) {
			this.homeUrl = homeUrl;
		}

		@Override
		public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
			ChromeDriver driver = ((HeadlessActionExecutor) browser).getDriver();
			if(!(driver.getCurrentUrl().equals(homeUrl) || driver.getCurrentUrl().equals(homeUrl + "orange.jsf"))) {
				driver.get(homeUrl);
			}
			((HeadlessActionExecutor) browser).getWait().until(ExpectedConditions.elementToBeClickable(By.id("loginForm:logoutLink"))).click();
		}
    }

    protected static class HeadlessXhrErrorAction extends Action {

    	private static final Cookie xhrFailCookie = new Cookie("xhr_fail", "xhr_fail");

    	private String homeUrl;

    	public HeadlessXhrErrorAction(String homeUrl) {
			this.homeUrl = homeUrl;
		}

		@Override
		public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
			HeadlessActionExecutor exec = (HeadlessActionExecutor) browser;
			ChromeDriver driver = exec.getDriver();
			if(!(driver.getCurrentUrl().equals(homeUrl) || driver.getCurrentUrl().equals(homeUrl + "orange.jsf"))) {
				driver.get(homeUrl);
			}
			// Proxy then checks if xhr fail cookie exists
    		driver.manage().addCookie(xhrFailCookie);
   			exec.getWait().until(ExpectedConditions.visibilityOfElementLocated(CustomerIceFormSearch.get())).click();
   			driver.manage().deleteCookie(xhrFailCookie);
		}
    }

    protected static class HeadlessSwitchWindowAction extends Action {

    	List<Action> actions = new ArrayList<>();

    	public HeadlessSwitchWindowAction(Action a) {
			actions.add(a);
		}

    	public HeadlessSwitchWindowAction(List<Action> actions) {
			this.actions.addAll(actions);
		}

		@Override
		public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
			ChromeDriver driver = ((HeadlessActionExecutor) browser).getDriver();
			String window = driver.getWindowHandle();
			for(Action a : actions) {
				a.run(browser, continuation);
			}
			driver.switchTo().window(window);
		}

    }

    protected static class HeadlessWaitForElement extends Action {

    	private By by;
    	private long timeoutInSeconds;

    	public HeadlessWaitForElement(By by, long timeoutInSeconds) {
			this.by = by;
			this.timeoutInSeconds = timeoutInSeconds;
		}

		@Override
		public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
			HeadlessActionExecutor exec = (HeadlessActionExecutor) browser;

    		try {
    			exec.getWait().withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.visibilityOfElementLocated(by));
      		} catch (Exception e) {
    			LOGGER.info( "Web element [" + by +"] not found");
    			if(LOGGER.isDebugEnabled()) {
    				LOGGER.debug(e.getMessage(), e);
    			}
    			return;
    		}
		}

    }

    protected static class HeadlessPauseAction extends Action {

    	int pause=0;	// seconds

    	public HeadlessPauseAction( int pause) {
    		this.pause = pause;
    	}

		@Override
		public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
			Thread.sleep(pause * 1000);
		}

    }

    protected static class HeadlessAngularSearchWithRetry extends Action {

    	private AngularSearchParameters params;
    	private boolean clickByJS;

    	public HeadlessAngularSearchWithRetry(AngularSearchParameters params, boolean clickByJS) {
			this.params = params;
			this.clickByJS = clickByJS;
		}

		@Override
		public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
			HeadlessActionExecutor exec = (HeadlessActionExecutor) browser;
			WebElement webElement = exec.getWait().until(ExpectedConditions.visibilityOfElementLocated(AngularDestinationField.get()));
			int tryCnt = 0;
			while (!search(exec, webElement)) {
				tryCnt++;
				if (tryCnt == MAX_SEARCH_TRIES) {
					throw new IllegalStateException(
							format("Performed %d searches without any results, some component of easyTravel seems to be broken",
									MAX_SEARCH_TRIES));
				}

				if (!DriverEntryPoolSingleton.getInstance().getPool().isVisitGenerationEnabled() &&
						!MobileDriverEntryPoolSingleton.getInstance().getPool().isVisitGenerationEnabled()) {
					LOGGER.warn("DriverEntryPool is stopping. Action interrupted");
					break;
				}
			}
		}

		private boolean search(HeadlessActionExecutor exec, WebElement webElement) {
			fillDestinationField(exec, webElement);
    		return clickSearchAndCheckForResults(exec, webElement);
		}

		private void fillDestinationField(HeadlessActionExecutor exec, WebElement webElement) {
			webElement = exec.getWait().until(ExpectedConditions.visibilityOfElementLocated(AngularDestinationField.get()));
    		webElement.clear();
    		webElement.sendKeys(params.getDestination());
		}

		private boolean clickSearchAndCheckForResults(HeadlessActionExecutor exec, WebElement webElement) {
			webElement = exec.getWait().until(ExpectedConditions.elementToBeClickable(AngularSearchButton.get()));
    		click(exec, webElement);
    		try {
	    		webElement = exec.getWait().withTimeout(CLICK_AND_SEARCH_TIMEOUT).until(ExpectedConditions.elementToBeClickable(AngularSearchResult.get()));
	    		click(exec, webElement);
	    		return true;
    		} catch (TimeoutException|NoSuchElementException e) {
    			LOGGER.info(format("No journey found for destination '%s'", params.getDestination())); // NOSONAR - catching exception as a way to check if journey was found
    			webElement = exec.getWait().withTimeout(CLICK_AND_SEARCH_TIMEOUT).until(ExpectedConditions.elementToBeClickable(AngularClearButton.get()));
	    		click(exec, webElement);
	    		params.findNextRandomDestination();
				return false;
			}
		}

		private void click(HeadlessActionExecutor exec, WebElement webElement) {
			if(clickByJS) {
    			exec.getDriver().executeScript("arguments[0].click();", webElement);
    		} else {
    			webElement.click();
    		}
		}

    }
}
