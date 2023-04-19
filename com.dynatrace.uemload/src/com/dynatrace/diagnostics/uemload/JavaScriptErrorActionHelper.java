package com.dynatrace.diagnostics.uemload;

import java.util.Random;

import com.dynatrace.diagnostics.uemload.jserrors.*;
import com.dynatrace.easytravel.util.DtVersionDetector;

/**
*
* @author cwat-slexow
*
*/
public class JavaScriptErrorActionHelper {

	public static final String CONTACT_PAGE_ERROR_USER_ACTION_NAME = "Contact-Page JS Error Button";
	public static final String CONTACT_PAGE_ERROR_USER_ACTION_NAME_LABEL = "Contact-Page JS Error Label";
	public static final String CONTACT_PAGE_ERROR_USER_ACTION_NAME_MOUSE = "Contact-Page JS Error Mouseover";

	private static final int VERSION_63 = 63;
	private static final Random randomNumberGenerator = new Random();

	public static JavaScriptErrorAction generateVersionSpecificErrorAction(String version, String currentPageUrl, BrowserType browserType) {
		// expects the version string to appear as "1.x" where x is the minor that gets specific errors
		JavaScriptError jsError = null;
		switch(version.charAt(version.length() - 1)) {
			case '3' : jsError = new ChipDeInspiredError1(currentPageUrl);
				break;
			case '4' : jsError = new ChipDeInspiredError1(currentPageUrl);
				break;
			case '7' : jsError = new ChipDeInspiredError2(currentPageUrl);
				break;
			case '8' : jsError = new ChipDeInspiredError2(currentPageUrl);
				break;
			default:
				break;
		}
		if (jsError == null) {
			return null;
		}
		JavaScriptErrorAction errorAction = generateJavaScriptErrorAction(jsError.getBrowserSpecificJavaScriptError(browserType), 1);
		return errorAction;
	}

	public static JavaScriptErrorAction generateRandomJavascriptAction(String currentPageUrl, BrowserType browserType) {
		return generateRandomJavascriptAction(currentPageUrl, browserType, 1);
	}

	public static JavaScriptErrorAction generateRandomJavascriptAction(String currentPageUrl, BrowserType browserType, int depth) {
		JavaScriptError error = getRandomJavaScriptError(currentPageUrl);
		return generateJavaScriptErrorAction(error.getBrowserSpecificJavaScriptError(browserType), depth);
	}

	public static JavaScriptErrorAction generateSpecificJavascriptAction(String currentPageUrl,
			BrowserType browserType, int depth) {
		JavaScriptError error = new ChipDeInspiredError1(currentPageUrl);
		return generateJavaScriptErrorAction(error.getBrowserSpecificJavaScriptError(browserType), depth);
	}

	private static JavaScriptError getRandomJavaScriptError(
			String currentPageUrl) {
		int randomNumber = getNextRandom(0, 11);
		JavaScriptError error = null;
		switch (randomNumber) {
			case 1:
				error = new ChipDeInspiredError1(currentPageUrl);
				break;
			case 2:
				error = new ChipDeInspiredError2(currentPageUrl);
				break;
			case 3:
				error = new ChipDeInspiredError3(currentPageUrl);
				break;
			case 4:
				error = new ChipDeInspiredError4(currentPageUrl);
				break;
			case 5:
				error = new ChipDeInspiredError5(currentPageUrl);
				break;
			case 6:
				error = new ChipDeInspiredError6(currentPageUrl);
				break;
			case 7:
				error = new ChipDeInspiredError7(currentPageUrl);
				break;
			case 8:
				error = new GeneratedStackTraceError(currentPageUrl);
				break;
			case 9:
				error = new PizzaHutInspiredError1(currentPageUrl);
				break;
			case 10:
				error = new SourceMapTestError(currentPageUrl);
				break;
			default:
				error = new ChipDeInspiredError1(currentPageUrl);
		}
		return error;
	}

	public static BrowserSpecificJavaScriptError getRandomUserActionErrorMessage(String currentPageUrl, BrowserType browserType) {
		int randomNumber = getNextRandom(0,4);
		JavaScriptError error = null;
		switch (randomNumber) {
			case 1:
				error = new UserActionError2(currentPageUrl);
				break;
			case 2:
				error = new UserActionError3(currentPageUrl);
				break;
			default:
				error = new UserActionError1(currentPageUrl);
		}

		return error.getBrowserSpecificJavaScriptError(browserType);
	}

	public static JavaScriptErrorAction generateChangeDetectionJavaScriptErrorAction(String currentPageUrl, BrowserType browserType, int depth) {
		JavaScriptError error = new ChangeDetectionJavaScriptError(currentPageUrl);
		return generateJavaScriptErrorAction(error.getBrowserSpecificJavaScriptError(browserType), depth);
	}

	public static JavaScriptErrorAction generateErrorOnLabelClick(Browser browser, BrowserType browserType) {
		JavaScriptError error = new CurCSSNotDefinedError(browser.getCurrentPage());
		return generateJavaScriptErrorAction(error.getBrowserSpecificJavaScriptError(browserType), 1);
	}

	public static BrowserSpecificJavaScriptError getInlineUserActionErrorMessages(String currentPageUrl, BrowserType browserType, String url) {

		JavaScriptError error = new InlineJavaScriptError(currentPageUrl);

		// set file & line number (random)
		int line = getNextRandom(1, 100);
		int column = getNextRandom(1, 200);

		error.setFile(url);
		error.setLine(line);
		error.setColumn(column);


		BrowserSpecificJavaScriptError browserSpecificError = error.getBrowserSpecificJavaScriptError(browserType);
		return browserSpecificError;
	}

	public static JavaScriptErrorAction generateJavaScriptErrorAction(BrowserSpecificJavaScriptError error, int depth) {
		if (error == null) {
			return null;
		}
		JavaScriptErrorAction action = new JavaScriptErrorAction();
		long time = System.currentTimeMillis();
		action.setStartTime(time);
		action.setDepth(depth);
		action.setErrorMessage(error.getMessage());
		action.setLine(error.getLine());
		action.setColumn(error.getColumn());
		action.setStackTrace(error.getStackTrace());
		action.setFile(error.getFile());
		action.setUserAction(error.getUserAction());
		action.setCode(error.getCode());

		// Dynatrace and Ruxit use the same way of sending stack traces.
		if (DtVersionDetector.isDetectedVersionGreaterOrEqual(VERSION_63) || DtVersionDetector.isAPM()) {
			action.setNewFormat(true);
		} else {
			action.setNewFormat(false);
		}
		return action;
	}

	public static int getNextRandom(int min, int max) {
		return randomNumberGenerator.nextInt(max - min) + min;
	}

	/**
	 * returns true at approximately every tenth call
	 * @return
	 */
	public static boolean shouldJavascriptErrorActionBeAdded(boolean increasedErrorActionCount) {
		if (increasedErrorActionCount) {
			return getNextRandom(1, 10) == 1;
		}
		return getNextRandom(1, 25) == 1;
	}

	/**
	 * returns true at approximately every 10th call
	 * @return
	 */
	public static boolean shouldUserActionTriggerdJavaScriptErrorBeGenerated() {
		return getNextRandom(1, 10) == 1;
	}

	/**
	 * returns the number of client errors which are generated if the javascript
	 * user action error problem pattern is active.
	 * @return
	 */
	public static int getNumberOfUserActionTriggeredJavaScriptErrors() {
		return getNextRandom(1, 3);
	}

	public static int getJavaScriptErrorChildActionCount(String action) {
		int numberOfChildActions = 0;

		if (action.contains("_useraction_")) {
			numberOfChildActions++;
		}

		if (action.contains("_code_")) {
			numberOfChildActions++;
		}

		if (action.contains("_location_")) {
			numberOfChildActions++;
		}

		if (action.contains("_stack_")) {
			numberOfChildActions++;
		}

		return numberOfChildActions;
	}

}
