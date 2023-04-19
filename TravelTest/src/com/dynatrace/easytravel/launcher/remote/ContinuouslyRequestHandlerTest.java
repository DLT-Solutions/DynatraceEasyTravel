package com.dynatrace.easytravel.launcher.remote;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.launcher.ant.AntController;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;


public class ContinuouslyRequestHandlerTest {
	@Before
	public void setUp() throws IOException {
		TestEnvironment.createOrClearRuntimeData();
	}

	@Test
	public void test() throws IOException {
		ContinuouslyRequestHandler handler = new ContinuouslyRequestHandler();

		try {
			handler.setContinuously(false);
			fail("Should throw an exception initially");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "must not be null");
		}

		try {
			ContinuouslyRequestHandler.setAntController(null);
			fail("Should throw an exception initially");
		} catch (NullPointerException e) {
			TestHelpers.assertContains(e, "must not be null");
		}

		File file = File.createTempFile("antcontroller", ".xml", new File(TestEnvironment.ABS_RUNTIME_DATA_PATH));
		AntController antController = new AntController(file, "test") {

			@Override
			public void stopSoft() {
			}

			@Override
			public void stopHard() {
			}

			@Override
			public void start() {
			}

			@Override
			public boolean isProcessing() {
				return false;
			}
		};
		ContinuouslyRequestHandler.setAntController(antController);

		handler.setContinuously(false);
		handler.setContinuously(true);
	}
}
