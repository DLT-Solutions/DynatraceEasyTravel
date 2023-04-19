package com.dynatrace.easytravel.launcher.misc;

import org.eclipse.rap.rwt.testfixture.TestContext;
import org.eclipse.swt.widgets.Display;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.RESTConstants;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Logger;

public class ConstantsTest {
    private static final Logger LOGGER = LoggerFactory.make();

	@Rule
	public TestContext context = new TestContext();

    @Test
	public void testInstantiate() {
		TestHelpers.assumeCanUseDisplay();
    	Display display = new Display();

    	// skip test if we do not have a graphical Display
    	Assume.assumeNotNull(Display.getCurrent());

		LOGGER.info("Constants: " + Constants.Colors.WHITE);
		LOGGER.info("Cursors: " + Constants.Cursors.ARROW);
		LOGGER.info("Modules: " + Constants.Modules.B2B_BACKEND);
		LOGGER.info("Images: " + Constants.Images.FANCY_MENU_BUTTON_BG);
		LOGGER.info("InternalMessages: " + Constants.InternalMessages.MENU_ACTION_WAS_NULL);
		LOGGER.info("HTML: " + Constants.Html.BEGIN_BOLD);
		LOGGER.info("Misc: " + Constants.Misc.CMD_PARAM_ADK_AGENT_LIB);
		LOGGER.info("Procedures: " + Constants.Procedures.ANT_ID);
		LOGGER.info("ConfigurationXML: " + Constants.ConfigurationXml.ATTRIBUTE_ENABLED);
		LOGGER.info("CmdArguments: " + Constants.CmdArguments.NO_AUTOSTART);
		LOGGER.info("REST: " + Constants.REST.PING);
		LOGGER.info("REST: " + RESTConstants.AGENT_FOUND);

		display.dispose();
	}

	 // helper method to get coverage of the unused constructor
	 @Test
	 public void testPrivateConstructor() throws Exception {
    	// skip test if we do not have a graphical Display
    	Assume.assumeNotNull(Display.getCurrent());

    	PrivateConstructorCoverage.executePrivateConstructor(Constants.class);
	 	PrivateConstructorCoverage.executePrivateConstructor(Constants.Colors.class);
	 	PrivateConstructorCoverage.executePrivateConstructor(Constants.Cursors.class);
	 	PrivateConstructorCoverage.executePrivateConstructor(Constants.Modules.class);
	 	PrivateConstructorCoverage.executePrivateConstructor(Constants.Images.class);
	 	PrivateConstructorCoverage.executePrivateConstructor(Constants.InternalMessages.class);
	 	PrivateConstructorCoverage.executePrivateConstructor(Constants.Html.class);
	 	PrivateConstructorCoverage.executePrivateConstructor(Constants.Misc.class);
	 	PrivateConstructorCoverage.executePrivateConstructor(Constants.Procedures.class);
	 	PrivateConstructorCoverage.executePrivateConstructor(Constants.ConfigurationXml.class);
	 	PrivateConstructorCoverage.executePrivateConstructor(Constants.CmdArguments.class);
	 	PrivateConstructorCoverage.executePrivateConstructor(Constants.REST.class);
	 	PrivateConstructorCoverage.executePrivateConstructor(RESTConstants.class);
	 }
}
