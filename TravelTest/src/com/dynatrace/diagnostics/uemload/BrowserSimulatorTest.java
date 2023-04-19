package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.*;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import ch.qos.logback.classic.Logger;

import com.dynatrace.diagnostics.uemload.http.base.HostAvailability;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelB2B;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelCustomer;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelFixedCustomer;
import com.dynatrace.diagnostics.uemload.scenarios.VisitsModel;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.EasyTravelMobileAppScenario;
import com.dynatrace.diagnostics.uemload.utils.UserFileGenerator;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.TestHelpers;

public class BrowserSimulatorTest {
	protected static final Logger LOGGER = LoggerFactory.make();
	
	public static final String TEST_DATA_PATH = "../TravelTest/testdata";

	@BeforeClass
	public static void setUpClass() throws IOException, InterruptedException {
		LoggerFactory.initLogging();
		
		File source = new File(TEST_DATA_PATH, "Users.txt");
		File dest = new File(Directories.getConfigDir(), "Users.txt");
		FileUtils.copyFile(source, dest);
		UserFileGenerator generator = new UserFileGenerator();
		generator.generateUserFile();
		
		// make sure this is running to get the number of scheduled actions right
		assertNotNull(LoadTimeWatcher.getUnfinishedActions());
		Thread.sleep(100);

		Simulator.THINK_TIME = 100;
	}

	@AfterClass
	public static void tearDownClass() {
		File f = new File(Directories.getConfigDir(), ResourceFileReader.EXTENDEDUSERS);
		f.delete();
		f = new File(Directories.getConfigDir(), ResourceFileReader.USERS);
		f.delete();
		
		Simulator.THINK_TIME = 3000;
	}

	@Before
	public void setUp() {
		// ensure that no items are left over from previous runs
		UemLoadScheduler.cleanup();
	}

	@Test
	public void testCustomer() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html><body>" +
						"</div><form action=\"/orange.jsf\" class=\"iceFrm\" enctype=\"application/x-www-form-urlencoded\" id=\"loginForm\" method=\"post\" onsubmit=\"return false;\"><input name=\"loginForm\" type=\"hidden\" value=\"loginForm\" /><input type=\"hidden\" name=\"javax.faces.ViewState\" id=\"javax.faces.ViewState\" value=\"-348808319978450462:-674415811729807575\" autocomplete=\"off\" /><input name=\"ice.window\" type=\"hidden\" value=\"qghdpstglc\" /><input name=\"ice.view\" type=\"hidden\" value=\"vpozvqckyb\" /><script id=\"loginForm:loginForm_captureSubmit\" type=\"text/javascript\">ice.captureSubmit('loginForm',false);ice.captureEnterKey('loginForm');</script>" +
						"	<div class=\"orangeHeaderLogin\"><a class=\"iceCmdLnk button\" href=\"javascript:;\" id=\"loginForm:loginLink\" onblur=\"setFocus('');\" onclick=\"var form=formOf(this);form['loginForm:j_idcl'].value='loginForm:loginLink';iceSubmit(form,this,event);form['loginForm:j_idcl'].value='';return false;\" onfocus=\"setFocus(this.id);\">Login</a>" +
						"	</div><div class=\"icePnlTlTip tripDetailsTip\" id=\"loginForm:userList\" name=\"loginForm:userList\" style=\"display:none;visibility:hidden;\"><table cellpadding=\"0\" cellspacing=\"0\"><tr id=\"loginForm:userList-tr\"><td class=\"icePnlTlTipBody tripDetailsTipBody\" colspan=\"2\"><div class=\"icePnlSrs tripDetailsPanel\" id=\"loginForm:j_idt44\"></div></td></tr></table><span id=\"loginForm:userListscript\"><script type=\"text/javascript\">Ice.modal.stop('loginForm:userList');; Ice.autoPosition.stop('loginForm:userList');; Ice.autoCentre.stop('loginForm:userList');; Ice.iFrameFix.start('loginForm:userList','/xmlhttp/blank');</script></span></div>" +
						"	<div class=\"orangeHeaderLoginForm\"><div class=\"icePnlPop\" id=\"loginForm:j_idt47\" name=\"loginForm:j_idt47\" style=\"display:none;\"><table cellpadding=\"0\" cellspacing=\"0\"><tr id=\"loginForm:j_idt47-tr\"><td class=\"icePnlPopBody\" colspan=\"2\"><div class=\"icePnlGrp\" id=\"loginForm:j_idt48\"><span class=\"iceOutTxt orangeLoginMessage\" id=\"loginForm:j_idt49\">" +
						"           				Please <strong>Log In</strong> or create a <strong>New Account</strong></span><label class=\"iceOutLbl orangeLoginUsername\" for=\"loginForm:username\" id=\"loginForm:j_idt53\">Username</label><input class=\"iceInpTxt orangeLoginTextbox orangeLoginUsername\" id=\"loginForm:username\" name=\"loginForm:username\" onblur=\"setFocus('');\" onfocus=\"setFocus(this.id);\" onkeypress=\"iceSubmit(form,this,event);\" onmousedown=\"this.focus();\" type=\"text\" value=\"\" /><label class=\"iceOutLbl orangeLoginPassword\" for=\"loginForm:password\" id=\"loginForm:j_idt54\">Password</label><input class=\"iceInpSecrt orangeLoginTextbox orangeLoginPassword\" id=\"loginForm:password\" name=\"loginForm:password\" onblur=\"setFocus('');\" onfocus=\"setFocus(this.id);\" onkeypress=\"iceSubmit(form,this,event);\" onmousedown=\"this.focus();\" type=\"password\" value=\"\" /><a class=\"commonButton grayButton3 orangeLoginButton orangeLoginNewAccount\" href=\"/orange-newaccount.jsf\" id=\"loginForm:newAccount\" name=\"loginForm:newAccount\">New Account</a><input class=\"iceCmdBtn loginPrivacy\" id=\"loginForm:j_idt55\" name=\"loginForm:j_idt55\" onblur=\"setFocus('');\" onclick=\"iceSubmit(form,this,event);return false;\" onfocus=\"setFocus(this.id);\" src=\"img/privacypolicy_lock.png\" type=\"image\" /><input class=\"iceCmdBtn commonButton grayButton4 orangeLoginButton orangeLoginCancel\" id=\"loginForm:loginCancel\" name=\"loginForm:loginCancel\" onblur=\"setFocus('');\" onclick=\"iceSubmit(form,this,event);return false;\" onfocus=\"setFocus(this.id);\" type=\"submit\" value=\"Cancel\" /><input class=\"iceCmdBtn commonButton orangeButton4 orangeLoginButton orangeLoginSubmit\" id=\"loginForm:loginSubmit\" name=\"loginForm:loginSubmit\" onblur=\"setFocus('');\" onclick=\"iceSubmit(form,this,event);return false;\" onfocus=\"setFocus(this.id);\" type=\"submit\" value=\"Login\" /></div></td></tr></table><span id=\"loginForm:j_idt47script\"><script type=\"text/javascript\">Ice.modal.stop('loginForm:j_idt47');; Ice.autoPosition.stop('loginForm:j_idt47');; Ice.autoCentre.stop('loginForm:j_idt47');; Ice.iFrameFix.start('loginForm:j_idt47','/xmlhttp/blank');</script></span></div>" +
						"	</div><div class=\"icePnlPop userList\" id=\"loginForm:j_idt57\" name=\"loginForm:j_idt57\" style=\"display:none;\"><table cellpadding=\"0\" cellspacing=\"0\"><tr id=\"loginForm:j_idt57-tr\"><td class=\"icePnlPopBody userListBody\" colspan=\"2\"><div class=\"icePnlSrs tripDetailsPanel\" id=\"loginForm:j_idt58\"></div></td></tr></table><span id=\"loginForm:j_idt57script\"><script type=\"text/javascript\">Ice.modal.stop('loginForm:j_idt57');; Ice.autoPosition.stop('loginForm:j_idt57');; Ice.autoCentre.stop('loginForm:j_idt57');; Ice.iFrameFix.start('loginForm:j_idt57','/xmlhttp/blank');</script></span></div><span id=\"loginFormhdnFldsDiv\"><input name=\"icefacesCssUpdates\" type=\"hidden\" value=\"\" /><input name=\"loginForm:j_idcl\" type=\"hidden\" /></span></form>" +

						"journeyId=54" +

						"</body><html>");
		try {
			ensureHostIsAvailable(server);

			runWithScenario(new EasyTravelCustomer(getAddress(server), getAddress(server), false));
		} finally {
			server.stop();
		}
	}

	private void ensureHostIsAvailable(MockRESTServer server) throws TimeoutException, InterruptedException, ExecutionException {
		if(!HostAvailability.INSTANCE.isHostAvailable(getAddress(server))) {
			Future<?> future = HostAvailability.INSTANCE.setAvailable(getAddress(server));

			// wait for the future to finish
			future.get(2000, TimeUnit.SECONDS);
		}

		assertTrue(HostAvailability.INSTANCE.isHostAvailable(getAddress(server)));
	}

	private String getAddress(MockRESTServer server) {
		return "http://localhost:" + server.getPort();
	}

	@Test
	public void testFixedCustomer() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html><body>" +
				"</div><form action=\"/orange.jsf\" class=\"iceFrm\" enctype=\"application/x-www-form-urlencoded\" id=\"loginForm\" method=\"post\" onsubmit=\"return false;\"><input name=\"loginForm\" type=\"hidden\" value=\"loginForm\" /><input type=\"hidden\" name=\"javax.faces.ViewState\" id=\"javax.faces.ViewState\" value=\"-348808319978450462:-674415811729807575\" autocomplete=\"off\" /><input name=\"ice.window\" type=\"hidden\" value=\"qghdpstglc\" /><input name=\"ice.view\" type=\"hidden\" value=\"vpozvqckyb\" /><script id=\"loginForm:loginForm_captureSubmit\" type=\"text/javascript\">ice.captureSubmit('loginForm',false);ice.captureEnterKey('loginForm');</script>" +
				"	<div class=\"orangeHeaderLogin\"><a class=\"iceCmdLnk button\" href=\"javascript:;\" id=\"loginForm:loginLink\" onblur=\"setFocus('');\" onclick=\"var form=formOf(this);form['loginForm:j_idcl'].value='loginForm:loginLink';iceSubmit(form,this,event);form['loginForm:j_idcl'].value='';return false;\" onfocus=\"setFocus(this.id);\">Login</a>" +
				"	</div><div class=\"icePnlTlTip tripDetailsTip\" id=\"loginForm:userList\" name=\"loginForm:userList\" style=\"display:none;visibility:hidden;\"><table cellpadding=\"0\" cellspacing=\"0\"><tr id=\"loginForm:userList-tr\"><td class=\"icePnlTlTipBody tripDetailsTipBody\" colspan=\"2\"><div class=\"icePnlSrs tripDetailsPanel\" id=\"loginForm:j_idt44\"></div></td></tr></table><span id=\"loginForm:userListscript\"><script type=\"text/javascript\">Ice.modal.stop('loginForm:userList');; Ice.autoPosition.stop('loginForm:userList');; Ice.autoCentre.stop('loginForm:userList');; Ice.iFrameFix.start('loginForm:userList','/xmlhttp/blank');</script></span></div>" +
				"	<div class=\"orangeHeaderLoginForm\"><div class=\"icePnlPop\" id=\"loginForm:j_idt47\" name=\"loginForm:j_idt47\" style=\"display:none;\"><table cellpadding=\"0\" cellspacing=\"0\"><tr id=\"loginForm:j_idt47-tr\"><td class=\"icePnlPopBody\" colspan=\"2\"><div class=\"icePnlGrp\" id=\"loginForm:j_idt48\"><span class=\"iceOutTxt orangeLoginMessage\" id=\"loginForm:j_idt49\">" +
				"           				Please <strong>Log In</strong> or create a <strong>New Account</strong></span><label class=\"iceOutLbl orangeLoginUsername\" for=\"loginForm:username\" id=\"loginForm:j_idt53\">Username</label><input class=\"iceInpTxt orangeLoginTextbox orangeLoginUsername\" id=\"loginForm:username\" name=\"loginForm:username\" onblur=\"setFocus('');\" onfocus=\"setFocus(this.id);\" onkeypress=\"iceSubmit(form,this,event);\" onmousedown=\"this.focus();\" type=\"text\" value=\"\" /><label class=\"iceOutLbl orangeLoginPassword\" for=\"loginForm:password\" id=\"loginForm:j_idt54\">Password</label><input class=\"iceInpSecrt orangeLoginTextbox orangeLoginPassword\" id=\"loginForm:password\" name=\"loginForm:password\" onblur=\"setFocus('');\" onfocus=\"setFocus(this.id);\" onkeypress=\"iceSubmit(form,this,event);\" onmousedown=\"this.focus();\" type=\"password\" value=\"\" /><a class=\"commonButton grayButton3 orangeLoginButton orangeLoginNewAccount\" href=\"/orange-newaccount.jsf\" id=\"loginForm:newAccount\" name=\"loginForm:newAccount\">New Account</a><input class=\"iceCmdBtn loginPrivacy\" id=\"loginForm:j_idt55\" name=\"loginForm:j_idt55\" onblur=\"setFocus('');\" onclick=\"iceSubmit(form,this,event);return false;\" onfocus=\"setFocus(this.id);\" src=\"img/privacypolicy_lock.png\" type=\"image\" /><input class=\"iceCmdBtn commonButton grayButton4 orangeLoginButton orangeLoginCancel\" id=\"loginForm:loginCancel\" name=\"loginForm:loginCancel\" onblur=\"setFocus('');\" onclick=\"iceSubmit(form,this,event);return false;\" onfocus=\"setFocus(this.id);\" type=\"submit\" value=\"Cancel\" /><input class=\"iceCmdBtn commonButton orangeButton4 orangeLoginButton orangeLoginSubmit\" id=\"loginForm:loginSubmit\" name=\"loginForm:loginSubmit\" onblur=\"setFocus('');\" onclick=\"iceSubmit(form,this,event);return false;\" onfocus=\"setFocus(this.id);\" type=\"submit\" value=\"Login\" /></div></td></tr></table><span id=\"loginForm:j_idt47script\"><script type=\"text/javascript\">Ice.modal.stop('loginForm:j_idt47');; Ice.autoPosition.stop('loginForm:j_idt47');; Ice.autoCentre.stop('loginForm:j_idt47');; Ice.iFrameFix.start('loginForm:j_idt47','/xmlhttp/blank');</script></span></div>" +
				"	</div><div class=\"icePnlPop userList\" id=\"loginForm:j_idt57\" name=\"loginForm:j_idt57\" style=\"display:none;\"><table cellpadding=\"0\" cellspacing=\"0\"><tr id=\"loginForm:j_idt57-tr\"><td class=\"icePnlPopBody userListBody\" colspan=\"2\"><div class=\"icePnlSrs tripDetailsPanel\" id=\"loginForm:j_idt58\"></div></td></tr></table><span id=\"loginForm:j_idt57script\"><script type=\"text/javascript\">Ice.modal.stop('loginForm:j_idt57');; Ice.autoPosition.stop('loginForm:j_idt57');; Ice.autoCentre.stop('loginForm:j_idt57');; Ice.iFrameFix.start('loginForm:j_idt57','/xmlhttp/blank');</script></span></div><span id=\"loginFormhdnFldsDiv\"><input name=\"icefacesCssUpdates\" type=\"hidden\" value=\"\" /><input name=\"loginForm:j_idcl\" type=\"hidden\" /></span></form>" +

				"journeyId=54" +

				"</body><html>");
		try {
			ensureHostIsAvailable(server);

			runWithScenario(new EasyTravelFixedCustomer(getAddress(server), getAddress(server), false));
		} finally {
			server.stop();
		}
	}


	@Test
	public void testB2B() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "<html><body>" +
						"</body><html>");
		try {
			ensureHostIsAvailable(server);

			EasyTravelB2B scenario = new EasyTravelB2B(false);
			scenario.getHostsManager().addB2BFrontendHost(getAddress(server));
			runWithScenario(scenario);
		} finally {
			server.stop();
		}
	}

	@Test
	public void testWrongActionExecutor() throws Exception {
		try {
			EasyTravelMobileAppScenario scenario = new EasyTravelMobileAppScenario();
			scenario.getHostsManager().addCustomerFrontendHost("http://localhost");
			runWithScenario(scenario);
			fail("Should catch exception here");
		} catch (UnsupportedOperationException e) {
			TestHelpers.assertContains(e, "Browser", "not supported");
		}
	}

	protected Simulator runWithScenario(UEMLoadScenario scenario) throws Exception {
		scenario.init();

		BrowserSimulator simulator = new BrowserSimulator(scenario);
		simulator.warmUp();

		Runnable runner = simulator.createActionRunnerForVisit();
		assertNotNull(runner);

		LOGGER.info("Starting to run using scenario " + scenario.getName() + "-" + scenario);
		runner.run();

		// sleep a bit upfront to let any schedule be put into the queue
		Thread.sleep(200);

		// 1 is always expected because LoadTimeWatcher is constantly running
		BlockingQueue<Runnable> queue = UemLoadScheduler.getQueue();
		while(UemLoadScheduler.getQueueSize() > 1 || UemLoadScheduler.getActiveCount() > 0) {
			LOGGER.info(printQueue(queue));
			Thread.sleep(500);
		}

		// we can get one here if the action already finished before
		int count = simulator.getFinishedVisits().get();
		assertTrue(printQueue(queue), count <= 1);

		simulator.incNumberOfFinishedVisits();
		assertEquals(printQueue(queue), count + 1, simulator.getFinishedVisits().get());

		simulator.runSync(1, 10);

		simulator.stop();
		simulator.stop(true);

		simulator.run(new SinusSeries(0, 1, 1), false, false);

		simulator.stop();

		return simulator;
	}

	private String printQueue(BlockingQueue<Runnable> queue) {
		Iterator<Runnable> it = queue.iterator();
		return "Having size " + UemLoadScheduler.getQueueSize() + ": " + queue + ", next: " + (it.hasNext() ? it.next() : "no-next");
	}


	@Test
	public void testMultiple() throws Exception {
		for(int i = 0;i < 10;i++) {
			testCustomer();
			testFixedCustomer();
			testB2B();
		}
	}

	@Ignore("Just for local testing of scheduler behavior")
	@Test
	public void testUEMLoadScheduler() {
		// LoadTimeWatcher always adds one
		assertEquals(1, UemLoadScheduler.getQueueSize());
		assertNotNull(UemLoadScheduler.schedule(new Runnable() {
			@Override
			public void run() {

			}
		}, Simulator.THINK_TIME, TimeUnit.SECONDS));
		assertEquals(2, UemLoadScheduler.getQueueSize());
		assertNotNull(UemLoadScheduler.schedule(new Runnable() {
			@Override
			public void run() {

			}
		}, Simulator.THINK_TIME, TimeUnit.SECONDS));
		assertEquals(3, UemLoadScheduler.getQueueSize());
	}
	
	@Test
	public void testLongSessions() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML,
				"<html><body>"
						+ "</div><form action=\"/orange.jsf\" class=\"iceFrm\" enctype=\"application/x-www-form-urlencoded\" id=\"loginForm\" method=\"post\" onsubmit=\"return false;\"><input name=\"loginForm\" type=\"hidden\" value=\"loginForm\" /><input type=\"hidden\" name=\"javax.faces.ViewState\" id=\"javax.faces.ViewState\" value=\"-348808319978450462:-674415811729807575\" autocomplete=\"off\" /><input name=\"ice.window\" type=\"hidden\" value=\"qghdpstglc\" /><input name=\"ice.view\" type=\"hidden\" value=\"vpozvqckyb\" /><script id=\"loginForm:loginForm_captureSubmit\" type=\"text/javascript\">ice.captureSubmit('loginForm',false);ice.captureEnterKey('loginForm');</script>"
						+ "	<div class=\"orangeHeaderLogin\"><a class=\"iceCmdLnk button\" href=\"javascript:;\" id=\"loginForm:loginLink\" onblur=\"setFocus('');\" onclick=\"var form=formOf(this);form['loginForm:j_idcl'].value='loginForm:loginLink';iceSubmit(form,this,event);form['loginForm:j_idcl'].value='';return false;\" onfocus=\"setFocus(this.id);\">Login</a>"
						+ "	</div><div class=\"icePnlTlTip tripDetailsTip\" id=\"loginForm:userList\" name=\"loginForm:userList\" style=\"display:none;visibility:hidden;\"><table cellpadding=\"0\" cellspacing=\"0\"><tr id=\"loginForm:userList-tr\"><td class=\"icePnlTlTipBody tripDetailsTipBody\" colspan=\"2\"><div class=\"icePnlSrs tripDetailsPanel\" id=\"loginForm:j_idt44\"></div></td></tr></table><span id=\"loginForm:userListscript\"><script type=\"text/javascript\">Ice.modal.stop('loginForm:userList');; Ice.autoPosition.stop('loginForm:userList');; Ice.autoCentre.stop('loginForm:userList');; Ice.iFrameFix.start('loginForm:userList','/xmlhttp/blank');</script></span></div>"
						+ "	<div class=\"orangeHeaderLoginForm\"><div class=\"icePnlPop\" id=\"loginForm:j_idt47\" name=\"loginForm:j_idt47\" style=\"display:none;\"><table cellpadding=\"0\" cellspacing=\"0\"><tr id=\"loginForm:j_idt47-tr\"><td class=\"icePnlPopBody\" colspan=\"2\"><div class=\"icePnlGrp\" id=\"loginForm:j_idt48\"><span class=\"iceOutTxt orangeLoginMessage\" id=\"loginForm:j_idt49\">"
						+ "           				Please <strong>Log In</strong> or create a <strong>New Account</strong></span><label class=\"iceOutLbl orangeLoginUsername\" for=\"loginForm:username\" id=\"loginForm:j_idt53\">Username</label><input class=\"iceInpTxt orangeLoginTextbox orangeLoginUsername\" id=\"loginForm:username\" name=\"loginForm:username\" onblur=\"setFocus('');\" onfocus=\"setFocus(this.id);\" onkeypress=\"iceSubmit(form,this,event);\" onmousedown=\"this.focus();\" type=\"text\" value=\"\" /><label class=\"iceOutLbl orangeLoginPassword\" for=\"loginForm:password\" id=\"loginForm:j_idt54\">Password</label><input class=\"iceInpSecrt orangeLoginTextbox orangeLoginPassword\" id=\"loginForm:password\" name=\"loginForm:password\" onblur=\"setFocus('');\" onfocus=\"setFocus(this.id);\" onkeypress=\"iceSubmit(form,this,event);\" onmousedown=\"this.focus();\" type=\"password\" value=\"\" /><a class=\"commonButton grayButton3 orangeLoginButton orangeLoginNewAccount\" href=\"/orange-newaccount.jsf\" id=\"loginForm:newAccount\" name=\"loginForm:newAccount\">New Account</a><input class=\"iceCmdBtn loginPrivacy\" id=\"loginForm:j_idt55\" name=\"loginForm:j_idt55\" onblur=\"setFocus('');\" onclick=\"iceSubmit(form,this,event);return false;\" onfocus=\"setFocus(this.id);\" src=\"img/privacypolicy_lock.png\" type=\"image\" /><input class=\"iceCmdBtn commonButton grayButton4 orangeLoginButton orangeLoginCancel\" id=\"loginForm:loginCancel\" name=\"loginForm:loginCancel\" onblur=\"setFocus('');\" onclick=\"iceSubmit(form,this,event);return false;\" onfocus=\"setFocus(this.id);\" type=\"submit\" value=\"Cancel\" /><input class=\"iceCmdBtn commonButton orangeButton4 orangeLoginButton orangeLoginSubmit\" id=\"loginForm:loginSubmit\" name=\"loginForm:loginSubmit\" onblur=\"setFocus('');\" onclick=\"iceSubmit(form,this,event);return false;\" onfocus=\"setFocus(this.id);\" type=\"submit\" value=\"Login\" /></div></td></tr></table><span id=\"loginForm:j_idt47script\"><script type=\"text/javascript\">Ice.modal.stop('loginForm:j_idt47');; Ice.autoPosition.stop('loginForm:j_idt47');; Ice.autoCentre.stop('loginForm:j_idt47');; Ice.iFrameFix.start('loginForm:j_idt47','/xmlhttp/blank');</script></span></div>"
						+ "	</div><div class=\"icePnlPop userList\" id=\"loginForm:j_idt57\" name=\"loginForm:j_idt57\" style=\"display:none;\"><table cellpadding=\"0\" cellspacing=\"0\"><tr id=\"loginForm:j_idt57-tr\"><td class=\"icePnlPopBody userListBody\" colspan=\"2\"><div class=\"icePnlSrs tripDetailsPanel\" id=\"loginForm:j_idt58\"></div></td></tr></table><span id=\"loginForm:j_idt57script\"><script type=\"text/javascript\">Ice.modal.stop('loginForm:j_idt57');; Ice.autoPosition.stop('loginForm:j_idt57');; Ice.autoCentre.stop('loginForm:j_idt57');; Ice.iFrameFix.start('loginForm:j_idt57','/xmlhttp/blank');</script></span></div><span id=\"loginFormhdnFldsDiv\"><input name=\"icefacesCssUpdates\" type=\"hidden\" value=\"\" /><input name=\"loginForm:j_idcl\" type=\"hidden\" /></span></form>"
						+

						"journeyId=54" +

						"</body><html>");
		try {
			ensureHostIsAvailable(server);
			
			EasyTravelCustomer scenario = new EasyTravelCustomer(getAddress(server), getAddress(server), false) {
				@Override
				protected IterableSet<Visit> createVisits() {
					VisitsModel visits = new VisitsModel.VisitsBuilder().setWandererShort(20).build();
					return createVisits(visits);
				}

				@Override
				protected IterableSet<Visit> createRushHourVisits() {
					VisitsModel visits = new VisitsModel.VisitsBuilder().setWandererLong(20).build();
					return createVisits(visits);
				}
			};
			scenario.init(true);

			runWithScenario(scenario);
		} finally {
			server.stop();
		}
	}
}
