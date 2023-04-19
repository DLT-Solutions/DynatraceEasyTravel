package com.dynatrace.easytravel.business;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.config.BusinessBackendReservation;
import com.dynatrace.easytravel.utils.TestHelpers;

public class RunTomcatTest {

	@Test
	public void testCreate() throws Exception {
		RunTomcat run = new RunTomcat();
		assertNotNull(run);
	}

	@Ignore("Tries to start Tomcat, runs out of permgen in Eclipse?")
	@Test
	public void testRun() throws Exception {
		RunTomcat run = new RunTomcat();
		run.run();
	}

	@Ignore("Tries to start Tomcat, runs out of permgen in Eclipse?")
	@Test
	public void testRunBusinessBackendReservation() throws Exception {
		RunTomcat run = new RunTomcat();
		run.run(new BusinessBackendReservation(0, 0, 0, "/", "/"));
	}

	@Ignore("Tries to start Tomcat, runs out of permgen in Eclipse?")
	@Test
	public void testMain() throws Exception {
		RunTomcat.main(new String[] {});
	}

	@Test
	public void testMainInvalidOption() throws Exception {
		try {
			RunTomcat.main(new String[] {"-invalidoption"});
			fail("Should catch exception");
		} catch (UnrecognizedOptionException e) {
			TestHelpers.assertContains(e, "-invalidoption");
		}
	}

	@Test
	public void testCreateOptions() {
		assertNotNull(RunTomcat.createOptions());
	}	
}
