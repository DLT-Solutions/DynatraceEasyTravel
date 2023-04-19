package com.dynatrace.easytravel.frontend;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.config.CustomerFrontendReservation;
import com.dynatrace.easytravel.frontend.tomcat.FrontendTomcatStarter;
import com.dynatrace.easytravel.utils.TestHelpers;


public class RunFrontendTomcatTest {
	
	private static final String LOGGER_TAG = "Test";

	@Test
	public void testCreate() throws Exception {
		FrontendTomcatStarter fts = new FrontendTomcatStarter(LOGGER_TAG);
		assertNotNull(fts);
	}

	@Ignore("Tries to start Tomcat, runs out of permgen in Eclipse?")
	@Test
	public void testRun() throws Exception {
		FrontendTomcatStarter fts = new FrontendTomcatStarter(LOGGER_TAG);
		fts.run();
	}

	@Ignore("Tries to start Tomcat, runs out of permgen in Eclipse?")
	@Test
	public void testRunCustomerFrontendReservation() throws Exception {
		FrontendTomcatStarter fts = new FrontendTomcatStarter(LOGGER_TAG);
		fts.run(new CustomerFrontendReservation(0, 0, 0, "/", "/"));
	}

	@Ignore("Tries to start Tomcat, runs out of permgen in Eclipse?")
	@Test
	public void testMain() throws Exception {
		RunFrontendTomcat.main(new String[] {});
	}

	@Ignore("Even tries to start Tomcat when commandline parsing fails!?")
	@Test
	public void testMainInvalidOption() throws Exception {
		try {
			RunFrontendTomcat.main(new String[] {"-invalidoption"});
			fail("Should catch exception");
		} catch (UnrecognizedOptionException e) {
			TestHelpers.assertContains(e, "-invalidoption");
		}
	}

	@Test
	public void testCreateOptions() {
		assertNotNull(FrontendTomcatStarter.createOptions());
	}
	
}
