package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.*;
import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;
import com.dynatrace.diagnostics.uemload.jserrors.BrowserSpecificJavaScriptError;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.google.common.collect.Lists;

public class EasyTravelContactPage extends CustomerPage {

    private static final Logger LOGGER = Logger.getLogger(EasyTravelContactPage.class.getName());

	public EasyTravelContactPage(EtPageType pageType, CustomerSession session) {
		super(pageType, session);
	}


	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws Exception {
		if(PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.JAVASCRIPT_USER_ACTIONERROR)) {
			loadContactWithJsErrorPage(browser, continuation);
		} else {
			loadPage(browser,continuation);
		}
	}


	private void loadContactWithJsErrorPage(final Browser browser, final UEMLoadCallback continuation) throws Exception {
		loadPage(browser, new UEMLoadCallback() {
			@Override
			public void run() throws IOException {
				addCustomUserAction(JavaScriptErrorActionHelper.CONTACT_PAGE_ERROR_USER_ACTION_NAME, browser, 10);

				if(JavaScriptErrorActionHelper.getNextRandom(0, 5) == 0) {
					addCustomUserAction(JavaScriptErrorActionHelper.CONTACT_PAGE_ERROR_USER_ACTION_NAME_LABEL, browser, 5);
				}

				if(JavaScriptErrorActionHelper.getNextRandom(0, 5) == 0) {
					addInlineUserActionError(JavaScriptErrorActionHelper.CONTACT_PAGE_ERROR_USER_ACTION_NAME_MOUSE, browser);
				}

				cont(continuation);
			}
		});
	}

	private synchronized void createUserActionWithErrors(String userActionName, Browser browser, List<BrowserSpecificJavaScriptError> errors) throws IOException {
		LOGGER.fine("Create user action with name: " + userActionName);
		startCustomAction(userActionName, "C", "automatically created js error on contact page", browser);
		List<String> actions = new ArrayList<String>();
		int numberOfErrorsToGenerate = errors.size();
		int i = 1;
		for (BrowserSpecificJavaScriptError error : errors) {
			JavaScriptErrorAction errorAction = JavaScriptErrorActionHelper.generateJavaScriptErrorAction(error, 2);
			actions.addAll(errorAction.getActionsWithActionIdPlaceholders());

			LOGGER.fine("[JavascriptUserActionError][" + i + "|" + numberOfErrorsToGenerate + "] Automatic js error created on contact-orange.jsf");
			i++;
		}

		finishCustomAction(browser, getProcessingTime(), false, actions);
	}

	private void addCustomUserAction(String userActionName, Browser browser, int errorLiklihood) throws IOException {
		List<BrowserSpecificJavaScriptError> errors = new ArrayList<BrowserSpecificJavaScriptError>();
		if(JavaScriptErrorActionHelper.getNextRandom(0, errorLiklihood) == 0) {
			int numberOfErrorsToGenerate = JavaScriptErrorActionHelper.getNumberOfUserActionTriggeredJavaScriptErrors();
			for (int i = 0; i < numberOfErrorsToGenerate; i++) {
				errors.add(getUserActionErrorMessage(browser, browser.getType()));
			}
		} 
		createUserActionWithErrors(userActionName, browser, errors);
	}

	private void addInlineUserActionError(String userActionName, Browser browser) throws IOException {
		BrowserFamily browserFamily = browser.getType().getBrowserFamily();
		if (browserFamily != BrowserFamily.Robot && browserFamily != BrowserFamily.Unknown) {
			createUserActionWithErrors(userActionName, browser, Lists.newArrayList(getInlineJavascriptError(browser, browser.getType())));
		} else {
			LOGGER.fine("[JavascriptUserActionError] Inline user action error NOT create due to unsupported browser family: " + browserFamily);
		}
	}

	private BrowserSpecificJavaScriptError getUserActionErrorMessage(Browser browser, BrowserType browserType) {
		BrowserSpecificJavaScriptError error = JavaScriptErrorActionHelper.getRandomUserActionErrorMessage(browser.getCurrentPage(), browserType);
		LOGGER.fine("[JavascriptUserActionError] Generated user action error message: " + error.getMessage());
		return error;
	}

	private BrowserSpecificJavaScriptError getInlineJavascriptError(Browser browser, BrowserType browserType) {
		String url = getHost() + getPage().getPath();
		BrowserSpecificJavaScriptError error = JavaScriptErrorActionHelper.getInlineUserActionErrorMessages(browser.getCurrentPage(), browserType, url);
		LOGGER.fine("[JavascriptUserActionError] Generated inline js error message: " + error.getMessage());
		return error;
	}

}
