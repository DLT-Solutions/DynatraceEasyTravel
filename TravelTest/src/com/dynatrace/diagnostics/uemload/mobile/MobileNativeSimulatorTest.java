package com.dynatrace.diagnostics.uemload.mobile;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.UEMLoadScenario;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelCustomer;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.EasyTravelMobileAppScenario;
import com.dynatrace.diagnostics.uemload.utils.UserFileGenerator;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.TestHelpers;

public class MobileNativeSimulatorTest {
	public static final String TEST_DATA_PATH = "../TravelTest/testdata";
	
	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
		
		File source = new File(TEST_DATA_PATH, "Users.txt");
		File dest = new File(Directories.getConfigDir(), "Users.txt");
		FileUtils.copyFile(source, dest);
		UserFileGenerator generator = new UserFileGenerator();
		generator.generateUserFile();
	}
	
	@AfterClass
	public static void tearDownClass() {
		File f = new File(Directories.getConfigDir(), ResourceFileReader.EXTENDEDUSERS);
		f.delete();
		f = new File(Directories.getConfigDir(), ResourceFileReader.USERS);
		f.delete();
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
			EasyTravelMobileAppScenario scenario = new EasyTravelMobileAppScenario();
			scenario.getHostsManager().addCustomerFrontendHost("http://localhost");
			runWithScenario(scenario);
		} finally {
			server.stop();
		}
	}

	@Test
	public void testWrongActionExecutor() throws Exception {
		try {
			runWithScenario(new EasyTravelCustomer("http://localhost", "http://localhost", false));
			fail("Should catch exception here");
		} catch (UnsupportedOperationException e) {
			TestHelpers.assertContains(e, "MobileDevice", "not supported");
		}
	}

	protected void runWithScenario(UEMLoadScenario scenario) throws Exception {
		scenario.init();

		MobileNativeSimulator simulator = new MobileNativeSimulator(scenario);
		simulator.warmUp();

		Runnable runner = simulator.createActionRunnerForVisit();
		assertNotNull(runner);
		runner.run();
	}
}
