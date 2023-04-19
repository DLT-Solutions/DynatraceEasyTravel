package com.dynatrace.easytravel.html;

import static com.dynatrace.easytravel.html.helpers.HtmlUnitHelpers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.html.helpers.HtmlUnitHelpers;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * This Test class is experimenting with the icefaces application
 */
public class HtmlUnitTest {
	private final static Logger logger = LoggerFactory.make();

    private static final int TEST_COUNT = 5;

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
	}

	@Ignore("Only for local testing...")
    @Test
    public void testHtmlUnit() throws Exception {
		final WebClient webClient = new WebClient();
		HtmlPage page = webClient.getPage("http://localhost:8011/easyTravel.htm");
        assertNotNull(page);

        webClient.closeAllWindows();
    }

    @Test
    /**
     * Fetch the main page and enter into a bidding loop
     */
    public void testMainPage() throws Exception {
        WebClient webClient = HtmlUnitHelpers.createWebClient();

        logger.info("Loading page");
        HtmlPage page = webClient.getPage(TestUtil.getCustomerFrontendUrl());
        logger.info("Page title = " + page.getTitleText());

        /*webClient.setAjaxController(new MyAjaxController());
        page.addDomChangeListener( new MyDomChangeListener());*/

        logger.info("Page contents: " + page.getPage().asText());

        // Click the search button
        page = enterData(page, "iceform:destination", "m", webClient);

        logger.info("Page contents after enter: " + page.getPage().asText());

        //logger.info("XML: " + page.getPage().asXml());
    }

    @Test
    public void testDisableFacebook() throws Exception {
        final WebClient webClient = createWebClient();

        HtmlPage page = disableFacebookScripts(webClient);

        //logger.info("XML contents: " + page.getPage().asXml());

        // fetch page again to see if the change did happen then...
        logger.info("Retrieving page with info about facebook state");
        page = webClient.getPage(TestUtil.getCustomerFrontendUrl() + "/facebook-orange.jsf");

        logger.info("Verifying content");
        // <label class="iceOutLbl" id="iceform:facebookDisabled" style="color: red;">disabled</label>:
        assertNotNull("Should find the element that indicates that facebook scripts are disabled now",
        		page.getElementById("iceform:facebookDisabled"));
    }

    @Test
    public void testSearchByLocation() throws Exception {
        final WebClient webClient = createWebClient();

        HtmlPage page = getInitialPage(webClient);

        /*logger.info("XML contents: " + page.getPage().asXml());
        logger.info("Page contents: " + page.getPage().asText());
        logger.info("Forms: " + page.getForms());*/

        for(int i = 0;i < TEST_COUNT;i++) {
        	logger.info("Running test number: " + i);

        	HtmlPage page2 = search(webClient, page, "maur");

	        /*for(HtmlElement element : page2.getAllHtmlChildElements()) {
	        	logger.info("Element: " + element.getId() + ": " + element.toString() + "-" + element.getClass().getName());
	        	if(element instanceof HtmlBold) {
	        		for(HtmlElement elem : element.getAllHtmlChildElements())  {
	        			logger.info("Value: " + elem.toString());
	        		};

	        	}
	        }*/

	        //logger.info("XML contents: " + page2.getPage().asXml());
	        //logger.info("Page contents: " + page2.getPage().asText());

	        // now we need to have found at least "Mauritius"
	        HtmlUnitHelpers.assertContainsAndLog("Searching for 'maur'", page2.asXml(), "Mauritius");
        }

        webClient.closeAllWindows();
    }

    @Test
    public void testLogin() throws Exception {
        final WebClient webClient = createWebClient();

        HtmlPage page = getInitialPage(webClient);

        /*logger.info("XML contents: " + page.getPage().asXml());
        logger.info("Page contents: " + page.getPage().asText());
        logger.info("Forms: " + page.getForms());*/

        for(int i = 0;i < TEST_COUNT;i++) {
        	logger.info("Running login test number: " + i);

			page = login(webClient, page, "demouser", "demopass");

	        //logger.info("XML contents: " + page2.getPage().asXml());
	        //logger.info("Page contents: " + page2.getPage().asText());

        	page = logout(webClient, page);
        }

        webClient.closeAllWindows();
    }

    @Test
    public void testLoginFailed() throws Exception {
        final WebClient webClient = createWebClient();

        HtmlPage page = getInitialPage(webClient);

		// verify that we are not logged in at the beginning
		assertFalse("Should not be logged in when trying to log in with an invalid user", loggedIn(page));

        for(int i = 0;i < TEST_COUNT;i++) {
        	logger.info("Running failed login test number: " + i);

			page = login(webClient, page, "invaliduser", "invalidpass", false);

			// verify that we are still not logged in
			assertFalse("Should not be logged in when trying to log in with an invalid user", loggedIn(page));

        	//page = logout(webClient, page);
        }

        webClient.closeAllWindows();
    }

    @Test
    public void testBook() throws Exception {
        final WebClient webClient = createWebClient();

        HtmlPage page = getInitialPage(webClient);

		page = login(webClient, page, "demouser", "demopass");

        for(int i = 0;i < TEST_COUNT;i++) {
        	logger.info("Running book test number: " + i);

        	page = search(webClient, page, "maur");

	        // now we need to have found at least "Mauritius"
	        assertTrue("Should find 'Mauritius'", page.getPage().asXml().contains("Mauritius"));

	        page = book(webClient, page, "0123456789" + i, true);
        }

        logout(webClient, page);

        webClient.closeAllWindows();
    }

    @Test
	public void testLoginSearchBookAndLogout() throws Exception {
        final WebClient webClient = createWebClient();

        executeFullBooking(webClient, "demouser", "demopass", "maur", "Mauritius", "0123456789", true);

        webClient.closeAllWindows();
    }

    /* Disabled because we could not get it to work
    @Test
	public void testLoginSearchBookAndLogoutWithDelayEnabled() throws Exception {
        final WebClient webClient = createWebClient();

        // enable delay
        setPageOption(webClient, 3000, null);

		executeFullBooking(webClient, "demouser", "demouser", "maur", "Mauritius", "0123456789", true);

        webClient.closeAllWindows();
    }*/

	private HtmlPage executeFullBooking(final WebClient webClient, String user, String pwd, String search, String result, String creditCard, boolean shouldSucceed) throws IOException, MalformedURLException {
		HtmlPage page = getInitialPage(webClient);
		page = login(webClient, page, user, pwd);

		page = search(webClient, page, search);

		// now we need to have found at least "Mauritius"
		assertTrue("Should find '" + result + "'", page.getPage().asXml().contains(result));

		page = book(webClient, page, creditCard, shouldSucceed);

		page = logout(webClient, page);

		return page;
	}

    @Test
	public void testCreateAccount() throws Exception {
        final WebClient webClient = createWebClient();

        HtmlPage page = webClient.getPage(TestUtil.getCustomerFrontendUrl() + "/orange-newaccount.jsf");

        // first create a new account with some generated user-name
        // use the current time to create unique users always
        String user = createNewAccount(webClient, page, "password");

        // now perform a booking with the existing user
        logger.info("Perform a booking with the default user");
        executeFullBooking(webClient, "demouser", "demopass", "maur", "Mauritius", "0123456789", true);

        // and now one with the new user
        logger.info("Perform a booking with the new user " + user);
        executeFullBooking(webClient, user, "password", "maur", "Mauritius", Long.toString(System.currentTimeMillis()), true);

        /* only works when Payment Service is running
        // finally run a booking with the new user and a credit card which was used before, this should fail!
        logger.info("Perform a booking with a credit card which was used before, this one will fail");
        page = executeFullBooking(webClient, user, "password", "maur", "Mauritius", "0123456789", false);

        TestHelpers.assertContainsMsg("Should still find 'Booking' in the name of the page-title as it should have stopped with an error here.",
        		page.getTitleText(), "Booking");
        */

        webClient.closeAllWindows();
    }

    @Test
    public void testPaymentFrontend() throws Exception {
    	// .NET Frontend is only available on Windows when not disabled in scenario configuration
    	if(!TestHelpers.isDotNetEnabled()) {
    		logger.info("Not running .NET tests either because this is not a Windows machine or .NET is disabled in the scenario.xml");
    		return;
    	}

        final WebClient webClient = createWebClient();

        HtmlPage page = webClient.getPage(TestUtil.getB2BFrontendUrl() + "/Account/LogOn?ReturnUrl=%2fJourney");

        page = loginB2B(webClient, page, "Personal Travel Inc.", "pti");

        String xml = page.asXml();
		logger.debug("Xml: \n" + xml);
        assertTrue("Had: " + xml, xml.contains("showing entries 1 to "));
    }
}
