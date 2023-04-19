package com.dynatrace.easytravel.html.helpers;

import static java.lang.String.format;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;

/**
 * Small helper class which provides methods that allow to run HtmlUnit
 * against easyTravel. A typical unit test can look as follows:
 *
 * <pre>
 *
 * &#064;Test
 * public void testBook() throws Exception {
 * 	final WebClient webClient = createWebClient();
 *
 * 	HtmlPage page = disableFacebookScripts(webClient);
 *
 * 	page = webClient.getPage(&quot;http://localhost:8080/&quot;);
 * 	logger.info(&quot;Page title = &quot; + page.getTitleText());
 *
 * 	page = login(webClient, page, &quot;demouser&quot;, &quot;demopass&quot;);
 *
 * 	page = search(webClient, page, &quot;maur&quot;);
 *
 * 	// now we need to have found at least &quot;Mauritius&quot;
 * 	assertTrue(&quot;Should find 'Mauritius'&quot;, page.getPage().asXml().contains(&quot;Mauritius&quot;));
 *
 * 	page = book(webClient, page, &quot;0123456789&quot;);
 *
 * 	logout(webClient, page);
 *
 * 	webClient.closeAllWindows();
 * }
 * </pre>
 *
 * The following methods provide an "interface" to the main easyTravel page:
 * <ul>
 * <li>login()</li>
 * <li>logout()</li>
 * <li>search()</li>
 * <li>book()</li>
 * </ul>
 *
 * The method "disableFacebookScripts" is used to avoid problems when loading facebook Javascript.
 *
 * @author dominik.stadler
 */
public class HtmlUnitHelpers {
	private final static Logger logger = LoggerFactory.make();

	private static final String ID_LOGIN_FORM = "loginForm";
	private static final String ID_LOGIN_PASSWORD = "loginForm:password";
	private static final String ID_LOGIN_USERNAME = "loginForm:username";
	private static final String ID_LOGIN_BUTTON = "loginForm:loginSubmit";
	private static final String ID_LOGOUT_LINK = "loginForm:logoutLink";
	private static final String ID_ICEFORM_FIRST_BOOK_LINK = "iceform:dataList:0:bookLink";
	private static final String ID_ICEFORM_DESTINATION = "iceform:destination";
	private static final String ID_ICEFORM_SEARCH = "iceform:search";
	private static final String ID_ICEFORM = "iceform";
	private static final String ID_FACEBOOK_DISABLE_RADIO = "iceform:facebookStateRadio:_2";

	private static final String ID_ICEFORM_BOOK_REVIEW_NEXT_BUTTON = "iceform:bookReviewNext";
	private static final String ID_ICEFORM_BOOK_PAYMENT_NEXT_BUTTON = "iceform:bookPaymentNext";
	private static final String ID_ICEFORM_BOOK_PAYMENT_BACK_BUTTON = "iceform:bookPaymentBack";
	private static final String ID_ICEFORM_BOOK_FINISH_FINISH_BUTTON = "iceform:bookFinishFinish";
	private static final String ID_ICEFORM_BOOK_FINISH_NEW_SEARCH_BUTTON = "iceform:bookFinishNewSearch";

	private static final String ID_ICEFORM_CREDIT_CARD_NUMBER = "iceform:creditCardNumber";
	private static final String ID_ICEFORM_CREDIT_CARD_OWNER = "iceform:creditCardOwner";
	private static final String ID_ICEFORM_CREDIT_CARD_TYPE = "iceform:creditCardType";
	private static final String ID_ICEFORM_VERIFICATION_NUMBER = "iceform:verificationNumber";
	private static final String ID_ICEFORM_EXPIRATION_MONTH = "iceform:expirationMonth";
	private static final String ID_ICEFORM_EXPIRATION_YEAR = "iceform:expirationYear";

	private static final String ID_ICEFORM_BOOKING_STEP_INDICATOR = "bookStepBar";

	private static final String BOOKING_DONE_TEXT = "Have a nice trip";

	public static WebClient createWebClient() {
		logger.info("Creating client");

		final WebClient webClient = new WebClient();
		webClient.waitForBackgroundJavaScriptStartingBefore(10000);
		webClient.getOptions().setTimeout(30000);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setCssEnabled(true);
		webClient.getOptions().setAppletEnabled(true);
		webClient.getOptions().setRedirectEnabled(true); // follow old-school HTTP 302 redirects - standard behaviour

		webClient.setHTMLParserListener(null);
		webClient.setIncorrectnessListener(new IncorrectnessListener() {
			@Override
			public void notify(String message, Object origin) {
				// Swallow for now, but maybe collect it for optional retrieval?
			}
		});
		webClient.setCssErrorHandler(new SilentCssErrorHandler());
		webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {

			@Override
			public void timeoutError(HtmlPage htmlPage, long allowedTime, long executionTime) {
			}

			@Override
			public void scriptException(HtmlPage htmlPage, ScriptException scriptException) {
			}

			@Override
			public void malformedScriptURL(HtmlPage htmlPage, String url, MalformedURLException malformedURLException) {
			}

			@Override
			public void loadScriptError(HtmlPage htmlPage, URL scriptUrl, Exception exception) {
			}
		});

		// enforce english to avoid problems with german umlauts
		//webClient.addRequestHeader("Accept-Language", "Accept-Language: en-US");

		// set a proxy if one is configured in the properties
		EasyTravelConfig config = EasyTravelConfig.read();
		if (StringUtils.isNotEmpty(config.proxyHost)) {
			if (UrlUtils.checkServiceAvailability(config.proxyHost, config.proxyPort)) {
				ProxyConfig conf = new ProxyConfig(config.proxyHost, config.proxyPort, false);
				conf.addHostsToProxyBypass("localhost");
				webClient.getOptions().setProxyConfig(conf);
			}
		}

		return webClient;
	}

	public static HtmlPage getInitialPage(final WebClient webClient) throws IOException, MalformedURLException {
		HtmlPage page = disableFacebookScripts(webClient);

		page = webClient.getPage(TestUtil.getCustomerFrontendUrl());
		logger.info("Page title = " + page.getTitleText());

		/*
		 * webClient.setAjaxController(new MyAjaxController());
		 * page.addDomChangeListener( new MyDomChangeListener());
		 */
		return page;
	}

	public static HtmlPage disableFacebookScripts(WebClient webClient) throws IOException {
		logger.info("Disable Facebook scripts");

		HtmlPage page = webClient.getPage(TestUtil.getCustomerFrontendUrl() + "/facebook-orange.jsf");

		page = clickElement(page, ID_FACEBOOK_DISABLE_RADIO, webClient);

		logger.info("Waiting for Javascript to finish");
		// webClient.waitForBackgroundJavaScript(10000);

		return page;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getElementById(final HtmlForm form, String id, Class<T> type) {
		DomElement element = ((HtmlPage)form.getPage()).getElementById(id);

		assertNotNull("Should find element with id '" + id + "'", element);

		assertTrue("Should find an element of type '" + type.getName() + "', but had: " + element.getClass().getName(),
				type.isInstance(element));

		return (T) element;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getElementById(final HtmlPage page, String id, Class<T> type) {
		DomElement element = page.getElementById(id);

		assertNotNull("Should find element with id '" + id + "'", element);

		assertTrue("Should find an element of type '" + type.getName() + "', but had: " + element.getClass().getName(),
				type.isInstance(element));

		return (T) element;
	}


	public static HtmlSubmitInput getSubmit(HtmlForm form) {
		return (HtmlSubmitInput) (form.getElementsByAttribute("input", "type", "submit").get(0));
	}


	/**
	 * Utility method for finding and clicking an element on a page.
	 *
	 * @param page Existing HtmlPage
	 * @param elementId Id of clickable element, fully qualified containing form id's
	 * @param client WebClient instance
	 * @return new copy of HtmlPage
	 * @throws IOException from underlying test framework
	 */
	public static HtmlPage clickElement(HtmlPage page, String elementId, WebClient client) throws IOException {

		HtmlElement element = (HtmlElement) page.getElementById(elementId);
		assertNotNull("Clickable element: " + elementId + " is not found", element);

		page = (HtmlPage) element.click();
		client.waitForBackgroundJavaScript(2000);
		return page;
	}

	public static HtmlPage enterData(HtmlPage page, String elementId, String data, WebClient client) {

		HtmlElement element = (HtmlElement) page.getElementById(elementId);
		assertNotNull("Clickable element: " + elementId + " is not found", element);

		assertTrue(page.setFocusedElement(element));

		client.waitForBackgroundJavaScript(2000);

		return page;
	}

	public static HtmlForm getFormById(HtmlPage page, String elementId) {
		StringBuilder builder = new StringBuilder();
		for (HtmlForm form : page.getForms()) {
			if (form.getId().equals(elementId)) {
				return form;
			}
			builder.append("'").append(form).append("',");
		}

		fail("Expected to find form with id '" + elementId + "', available forms: " + builder.toString());
		return null;
	}

	/**
	 * Fetch the value from an input text field
	 *
	 * @param page HtmlPage
	 * @param id id of input text field
	 * @return Value attribute of field
	 */
	public static String getHtmlInputValue(HtmlPage page, String id) {
		HtmlInput element = (HtmlInput) page.getElementById(id);
		assertNotNull("Input element: " + id + " not found", element);
		return element.getValueAttribute();
	}

	public static void setInputTextData(HtmlForm form, String id, String value) {
		HtmlTextInput textInput = getElementById(form, id, HtmlTextInput.class);
		textInput.setValueAttribute(value);
	}

	public static void setInputSelectData(HtmlForm form, String id, String value) {
		HtmlSelect selectInput = getElementById(form, id, HtmlSelect.class);
		selectInput.setSelectedAttribute(value, true);
	}

	public static HtmlPage login(final WebClient webClient, HtmlPage page, String user, String password) throws IOException {
		return login(webClient, page, user, password, true);
	}

	public static HtmlPage login(final WebClient webClient, HtmlPage page, String user, String password, boolean expectSuccess)
			throws IOException {
		logger.info("Logging in as " + user);
		// Get the form that we are dealing with and within that form,
		// find the submit button and the field that we want to change.
		final HtmlForm form = getFormById(page, ID_LOGIN_FORM);

		final HtmlSubmitInput button = getElementById(form, ID_LOGIN_BUTTON, HtmlSubmitInput.class);
		final HtmlTextInput userElement = getElementById(form, ID_LOGIN_USERNAME, HtmlTextInput.class);
		final HtmlPasswordInput passwordElement = getElementById(form, ID_LOGIN_PASSWORD, HtmlPasswordInput.class);

		// Change the value of the text field
		userElement.setValueAttribute(user);
		passwordElement.setValueAttribute(password);

		// Now submit the form by clicking the button and get back the second page.
		page = button.click();
		webClient.waitForBackgroundJavaScript(2000);

		if (expectSuccess) {
			// wait some more if we are not yet logged in as the 2 seconds did not suffice sometimes when running tests locally
			if (!loggedIn(page)) {
				webClient.waitForBackgroundJavaScript(5000);
			}

			if(!loggedIn(page)) {
				String error =
						format("After logging in, we should find a logout link (i.e. an element with id '%s'), but didn't, did login fail?\n%s\nThe credentials were: user: '%s'; pw: '%s'",
								ID_LOGOUT_LINK,
								getIceFacesErrorMessage(page),
								user,
								password);
				logger.info(error + ", XML: \n" + page.asXml());
				fail(error);
			}
		}

		return page;
	}

	public static HtmlPage loginB2B(final WebClient webClient, HtmlPage page, String user, String password/*, boolean expectSuccess*/)
			throws IOException {
		logger.info("Logging in as " + user);
		// Get the form that we are dealing with and within that form,
		// find the submit button and the field that we want to change.
		final HtmlForm form = page.getForms().get(0);

		final HtmlSubmitInput button = getSubmit(form);
		final HtmlTextInput userElement = getElementById(form, "UserName", HtmlTextInput.class);
		final HtmlPasswordInput passwordElement = getElementById(form, "Password", HtmlPasswordInput.class);

		// Change the value of the text field
		userElement.setValueAttribute(user);
		passwordElement.setValueAttribute(password);

		// Now submit the form by clicking the button and get back the second page.
		page = button.click();
		webClient.waitForBackgroundJavaScript(2000);

		/*if (expectSuccess) {
			if(!loggedIn(page)) {
				String error = "After logging in, we should find a logout link ('" + ID_LOGOUT_LINK + "'), but didn't, did login fail?\n" + getIceFacesErrorMessage(page);
				logger.info(error + ", XML: \n" + page.asXml());
				fail(error);
			}
		}*/

		return page;
	}

	public static boolean loggedIn(HtmlPage page) {
		final HtmlForm form = getFormById(page, ID_LOGIN_FORM);

		// now we need to find the logout link
		try {
			DomElement element = ((HtmlPage)form.getPage()).getElementById(ID_LOGOUT_LINK);

			// return true if we find the logout-link (i.e. we are logged in)
			return element != null;
		} catch (ElementNotFoundException e) {
			return false;
		}
	}

	public static HtmlPage logout(final WebClient webClient, HtmlPage page) throws IOException {
		logger.info("Logging out");
		final HtmlForm form = getFormById(page, ID_LOGIN_FORM);

		final HtmlAnchor link = getElementById(form, ID_LOGOUT_LINK, HtmlAnchor.class);

		// Now submit the form by clicking the button and get back the second page.
		page = link.click();
		// webClient.waitForBackgroundJavaScript(5000);

		return page;
	}

	public static HtmlPage search(WebClient webClient, HtmlPage page, String search) throws IOException {
		logger.info("Searching for '" + search + "'");
		// Get the form that we are dealing with and within that form,
		// find the submit button and the field that we want to change.
		final HtmlForm form = getFormById(page, ID_ICEFORM);

		/*
		 * for(HtmlElement element : form.getAllHtmlChildElements()) {
		 * logger.info("Element: " + element.getId() + ": " + element.getClass().getName());
		 * }
		 */

		// final HtmlSubmitInput button = form.getInputByName("search");
		final HtmlSubmitInput button = getElementById(form, ID_ICEFORM_SEARCH, HtmlSubmitInput.class);
		final HtmlTextInput destField = getElementById(form, ID_ICEFORM_DESTINATION, HtmlTextInput.class);

		// Change the value of the text field
		destField.setValueAttribute(search);

		// Now submit the form by clicking the button and get back the second page.
		final HtmlPage page2 = button.click();
		webClient.waitForBackgroundJavaScript(3000);

		return page2;
	}

	// assert that the correct booking step html list item has class "active_true", the others "active_false"
	private static void assertBookingStep(HtmlPage page, int stepNo) {
		final HtmlOrderedList bookStepBar = getElementById(page, ID_ICEFORM_BOOKING_STEP_INDICATOR, HtmlOrderedList.class);
		int i = 1;

		for (DomElement elem : bookStepBar.getChildElements()) {
			String expectedClass = "active_" + (stepNo == i);
			assertTrue("Expecting: " + expectedClass, expectedClass.equals(elem.getAttribute("class")));
			i++;
		}
	}

	public static HtmlPage book(WebClient webClient, HtmlPage pageIn, String creditCardNr, boolean expectValidCard)
			throws IOException {
		logger.info("Booking with Credit Card '" + creditCardNr + "'");
		HtmlForm form = getFormById(pageIn, ID_ICEFORM);

		final HtmlAnchor bookNowLink  = getElementById(form, ID_ICEFORM_FIRST_BOOK_LINK, HtmlAnchor.class);

		HtmlPage page2 = bookNowLink.click();
		webClient.waitForBackgroundJavaScript(3000);

		// now we should booking step 2
		assertContainsAndLog("Should find page step 2 for booking" + getIceFacesErrorMessage(page2), page2.getPage().asXml(), "Trip details");
		assertBookingStep(page2, 2);

		logger.info("Booking step 2");
		form = getFormById(page2, ID_ICEFORM);
		HtmlAnchor nextLink = getElementById(form, ID_ICEFORM_BOOK_REVIEW_NEXT_BUTTON, HtmlAnchor.class);

		HtmlPage page3 = nextLink.click();
		webClient.waitForBackgroundJavaScript(3000);

		// now we should booking step 3
		assertContainsAndLog("Should find page step 3 for booking" + getIceFacesErrorMessage(page3), page3.getPage().asXml(), "Credit Card Information");
		assertBookingStep(page3, 3);

		logger.info("Booking step 3");
		form = getFormById(page3, ID_ICEFORM);
		HtmlSubmitInput nextButton = getElementById(form, ID_ICEFORM_BOOK_PAYMENT_NEXT_BUTTON, HtmlSubmitInput.class);

		// set the credit card number
		try {
			setInputTextData(form, ID_ICEFORM_CREDIT_CARD_NUMBER, creditCardNr);
			setInputSelectData(form, ID_ICEFORM_CREDIT_CARD_TYPE, "VISA");
			setInputTextData(form, ID_ICEFORM_VERIFICATION_NUMBER, "1234");
			setInputTextData(form, ID_ICEFORM_CREDIT_CARD_OWNER, "John Doe");
			setInputSelectData(form, ID_ICEFORM_EXPIRATION_MONTH, "December");
			setInputSelectData(form, ID_ICEFORM_EXPIRATION_YEAR, "2012");
		} catch (ElementNotFoundException e) {
			logger.warn("Failed with XML: " + form.asXml(), e);
			throw e;
		}

		HtmlAnchor backLink = getElementById(page3, ID_ICEFORM_BOOK_PAYMENT_BACK_BUTTON, HtmlAnchor.class);
		HtmlPage page4 = nextButton.click();
		webClient.waitForBackgroundJavaScript(3000);

		// leave if booking will fail, i.e. invalid credit card or problem pattern is active
		if (!expectValidCard) {
			return page4;
		}

		// hack to handle correctly the JSF redirect which is NOT a 302 redirect, but simply POST, then GET
		page4 = webClient.getPage(TestUtil.getCustomerFrontendUrl() + "/orange-booking-finish.jsf" +
				extractQueryString(backLink.getHrefAttribute()));

		// now we should booking step 4
		logger.info("Expecting step 4");
		assertContainsAndLog("Should find page step 4 for booking" + getIceFacesErrorMessage(page4), page4.getPage().asXml(), "The booking procedure is almost complete");
		assertBookingStep(page4, 4);

		page4 = clickAndWaitForBookingSuccess(webClient, page4);

		// click a second time as sometimes it fails to do it correctly the first time
		if (!page4.getPage().asXml().contains(BOOKING_DONE_TEXT)) {
			page4 = clickAndWaitForBookingSuccess(webClient, page4);
		}

		// now we should booking step 4 success page
		assertContainsAndLog("Should find success page step 4 for booking" + getIceFacesErrorMessage(page4),
				page4.getPage().asXml(), BOOKING_DONE_TEXT);
		assertBookingStep(page4, 4);

		logger.info("Booking done, go back to main page");
		form = getFormById(page4, ID_ICEFORM);
		final HtmlAnchor newSearchLink = getElementById(form, ID_ICEFORM_BOOK_FINISH_NEW_SEARCH_BUTTON, HtmlAnchor.class);

		HtmlPage homePage = newSearchLink.click();
		webClient.waitForBackgroundJavaScript(3000);

		logger.info("Booking success");
		return homePage;
	}

	private static HtmlPage clickAndWaitForBookingSuccess(WebClient webClient, HtmlPage page4)
			throws IOException {
		logger.info("Booking step 4");
		HtmlForm form = getFormById(page4, ID_ICEFORM);
		final HtmlSubmitInput finishButton = getElementById(form, ID_ICEFORM_BOOK_FINISH_FINISH_BUTTON, HtmlSubmitInput.class);

		page4 = finishButton.click();

		// look in second intervals to not always wait the full 15sec even if we finish earlier
		logger.info("Waiting for booking to be done");
		for (int i = 0; i < 20; i++) {
			if (page4.getPage().asXml().contains(BOOKING_DONE_TEXT)) {
				break;
			}
			webClient.waitForBackgroundJavaScript(1000);
		}
		return page4;
	}

	private static String getIceFacesErrorMessage(HtmlPage page) {
		// try to extract error message
		/*
		<span class="iceMsgsInfo">
        Error while booking: Error invoking PaymentService: Connection refused
      	</span>
		 */
		String error = "";
		DomNodeList<DomElement> spans = page.getElementsByTagName("span");
		for(DomElement span : spans) {
			String clazz = span.getAttribute("class");
			if("iceMsgsInfo".equals(clazz)) {
				error += span.getTextContent();
			} else if (clazz != null && clazz.contains("orangeLoginMessage")) {
				error += span.asXml().replace("<strong>", "").replace("</strong>", "").
						replace("\n", "").replace("\r", "").replace("  ", " ");
			} else if (clazz != null && clazz.contains("iceOutTxt")) {
				error += span.asXml().replace("<strong>", "").replace("</strong>", "").
						replace("\n", "").replace("\r", "").replace("  ", " ");
			}
		}
		return error.length() == 0 ? "" : ", had error: " + error;
	}

	private static String extractQueryString(String link) {
		int i = link.indexOf("?");
		if (i == -1) {
			fail("Expecting query string for link: " + link);
		}
		return link.substring(i);
	}

	public static void assertContainsAndLog(String string, String xml, String expect) {
		if (!xml.contains(expect)) {
			logger.warn("HTML of failing test: \n" + xml);
			fail("The HTML-page did not contain '" + expect + "': " + string);
		}
	}

	/* I could not get the form to submit as it does not have a "submit" button...
	public static void setPageOption(final WebClient webClient, int delayMS, String waitingOption) throws IOException, MalformedURLException {
		{ // first enable some of the delay options
	        HtmlPage page = webClient.getPage(TestUtil.getCustomerFrontendUrl() + "/about-orange.jsf");
			logger.info("About title = " + page.getTitleText());

			final HtmlForm form = getFormById(page, ID_ICEFORM);
			final HtmlTextInput delay = getElementById(form, "iceform:delaytime", HtmlTextInput.class);
			if(waitingOption != null) {
				final HtmlSelect waitingStrategy = getElementById(form, "iceform:waitingStrategy", HtmlSelect.class);
				waitingStrategy.setSelectedAttribute(waitingOption, true);
			}

			delay.setText(Integer.toString(delayMS));

			logger.info("Trigger script to submit the updated text");
			/*ScriptResult result =*/ /*page.executeJavaScriptFunctionIfPossible(
					new EventHandler(page, "onKeyPress", "var evt = new Event();evt.keyCode=Event.KEY_RETURN;iceSubmit(window.document.getElementById('" + ID_ICEFORM + "'),this,evt);"),
					delay.getScriptObject(), new Object[] {}, page);
			//assertFalse(ScriptResult.isFalse(result));

			webClient.waitForBackgroundJavaScript(1000);
        }

        { // then verify that the value actually sticks
	        HtmlPage page = webClient.getPage(TestUtil.getCustomerFrontendUrl() + "/about-orange.jsf");
			logger.info("About title = " + page.getTitleText());

			final HtmlForm form = getFormById(page, ID_ICEFORM);
			final HtmlTextInput delay = getElementById(form, "iceform:delaytime", HtmlTextInput.class);
			assertEquals(Integer.toString(delayMS), delay.getText());
        }
	}*/

	public static String createNewAccount(WebClient webClient, HtmlPage page, String pwdStr) throws IOException {
		// make the username/email unique
		String prefix = "testuser-" + System.currentTimeMillis();

		logger.info("Creating new test-user-account for " + prefix + "@example.com");

		HtmlForm form = getFormById(page, ID_ICEFORM);
        HtmlAnchor fillButton = getElementById(form, "iceform:fillMock", HtmlAnchor.class);
        page = fillButton.click();

        // re-read form after button-click
        form = getFormById(page, ID_ICEFORM);

        HtmlTextInput first = getElementById(form, "iceform:firstName", HtmlTextInput.class);
        first.setText(prefix);
        HtmlTextInput last = getElementById(form, "iceform:lastName", HtmlTextInput.class);
        last.setText(prefix);

        HtmlTextInput email = getElementById(form, "iceform:email", HtmlTextInput.class);
        email.setText(prefix + "@example.com");
        HtmlTextInput emailConfirm = getElementById(form, "iceform:emailConfirm", HtmlTextInput.class);
        emailConfirm.setText(prefix + "@example.com");
        HtmlPasswordInput pwd = getElementById(form, "iceform:password", HtmlPasswordInput.class);
        pwd.setText(pwdStr);
        HtmlPasswordInput pwdConfirm = getElementById(form, "iceform:passwordConfirm", HtmlPasswordInput.class);
        pwdConfirm.setText(pwdStr);

        HtmlSubmitInput button = getElementById(form, "iceform:newAccountCreate", HtmlSubmitInput.class);
        page = button.click();

		// look in second intervals to not always wait the full 5sec even if we finish earlier
		for (int i = 0; i < 5; i++) {
			if (page.getPage().asXml().contains("Account Created")) {
				break;
			}
			webClient.waitForBackgroundJavaScript(1000);
		}

		assertContainsAndLog("Should find success page for new Account" + getIceFacesErrorMessage(page),
				page.getPage().asXml(), "Account Created");

		// return the resulting email-address
		return prefix + "@example.com";
	}

	/**
	 * We need to convert some requests to synchronous in order not to miss
	 * some updates.
	 */
	/*
	 * public static class MyAjaxController extends AjaxController {
	 *
	 * public boolean processSynchron(HtmlPage page,
	 * WebRequestSettings settings,
	 * boolean async) {
	 *
	 *
	 * // It seems that if the send-updated-views Ajax requests can be handled
	 * // asynchronously, but that the User interface interaction works best
	 * // if run synchronously. Running both synchronously causes a deadlock.
	 * // Running both asynchronously lets some updates get skipped.
	 *
	 * if (settings.getUrl().toString().indexOf("auction.jsf") > -1) {
	 * // System.out.println("Ajax request to: " + settings.getUrl() + " type: " + settings.getHttpMethod());
	 * // System.out.println(page.getPage().asText());
	 * return super.processSynchron( page, settings, false);
	 * } else {
	 * return super.processSynchron( page, settings, async );
	 * }
	 * }
	 *
	 * }
	 */

	// Not used.
	/*
	 * public static class MyWindowListener extends WebWindowAdapter {
	 *
	 * @Override
	 * public void webWindowContentChanged(WebWindowEvent event) {
	 * System.out.println("WebWindowContent changed! " + event);
	 * }
	 * }
	 */

	/*
	 * public static class MyDomChangeListener implements DomChangeListener {
	 *
	 * @Override
	 * public void nodeAdded(DomChangeEvent event) {
	 * // System.out.println("Node added: " + event);
	 * }
	 *
	 * @Override
	 * public void nodeDeleted(DomChangeEvent event) {
	 * // System.out.println("Node deleted: " + event.getChangedNode().getNodeName());
	 * }
	 * }
	 */
}
