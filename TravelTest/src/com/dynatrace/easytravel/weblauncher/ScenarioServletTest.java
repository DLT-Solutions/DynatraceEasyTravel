package com.dynatrace.easytravel.weblauncher;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.ssi.ByteArrayServletOutputStream;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.UemLoadScheduler;
import com.dynatrace.diagnostics.uemload.http.base.HostAvailability;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.WebUtils;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.dynatrace.easytravel.utils.ThreadTestHelper;
import com.dynatrace.easytravel.weblauncher.BaseServlet.ParamException;

import ch.qos.logback.classic.Logger;

public class ScenarioServletTest {
	private static final Logger log = LoggerFactory.make();

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
	}

	@Test
	public void testDoServiceLogZip() throws Exception {
		HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
		HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);

		EasyMock.expect(request.getPathInfo()).andReturn("/log");

		response.setHeader("Content-Type", WebUtils.getContentType("file.zip"));
		response.setHeader("Content-Disposition", "attachment; filename=" + new File(Directories.getTempDir(), "logs.zip").getName());

		ByteArrayServletOutputStream output = new ByteArrayServletOutputStream();
		EasyMock.expect(response.getOutputStream()).andReturn(output);

		EasyMock.replay(request, response);

		ScenarioServlet servlet = new ScenarioServlet();

		servlet.init();

		servlet.doService(request, response);

		ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(output.toByteArray()));

		boolean bSysProperties = false, bConfig = false;
		List<String> files = new ArrayList<String>();

		ZipEntry entry = zipStream.getNextEntry();
		while(entry != null) {
			files.add(entry.getName());

			if(entry.getName().equals("easyTravelConfig.log")) {
				bConfig = true;
			} else if (entry.getName().equals("SystemProperties.log")) {
				bSysProperties = true;
			}


			entry = zipStream.getNextEntry();
		}

		assertTrue("Did not find easyTravelConfig.log in files: " + files, bConfig);
		assertTrue("Did not find SystemProperties.log in files: " + files, bSysProperties);

		EasyMock.verify(request, response);

		log.info("Had files: " + files);

		File file = new File(Directories.getLogDir(), "easyTravelConfig.log");
		assertTrue(!file.exists() || file.delete());
		file = new File(Directories.getLogDir(), "SystemProperties.log");
		assertTrue(!file.exists() || file.delete());
	}

	@Test
	public void testStartScenario() throws ParamException, InterruptedException {
		// we need classic here to have the UEM/Standard Scenario available
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);

		try {
			ScenarioServlet.startScenario("some", "notexisting");
			fail("Should throw exception because scenario is not found");
		} catch (ParamException e) {
			TestHelpers.assertContains(e, "some", "notexisting", "Scenario not found");
		}

		ScenarioServlet.startScenario("Production", "Standard");

		// ensure that a scenario is running now, first wait for the thread
		ThreadTestHelper.waitForThreadToFinishSubstring("Scenario Runner");
		assertNotNull("Did not find a scenario running after starting one!", LaunchEngine.getRunningBatch());

		// second time fails
		try {
			ScenarioServlet.startScenario("UEM", "Standard");
			fail("Should throw exception because scenario is already starting");
		} catch (ParamException e) {
			TestHelpers.assertContains(e, "Cannot start, current state: ");
		}

		// first wait for non-STOPPED state
		log.info("Waiting 10 seconds for Batch to be not stopped any more");
		for(int i = 0;i < 100 && LaunchEngine.getRunningBatch().getState() == State.STOPPED;i++)  {
			Thread.sleep(100);
		}
		assertFalse("Expected Batch-State to be different than STOPPED, but still was STOPPED",
				LaunchEngine.getRunningBatch().getState() == State.STOPPED);

		log.info("Stopping all threads");
		LaunchEngine.stop();
		UemLoadScheduler.shutdownNow();
		PluginChangeMonitor.shutdown();
		HostAvailability.INSTANCE.shutdown();

		// make sure all related threads are stopped
		ThreadTestHelper.waitForThreadToFinish("Scenario Runner Standard");
		ThreadTestHelper.waitForThreadToFinishSubstring("Uem-Load");
		//ThreadTestHelper.waitForThreadToFinishSubstring("derby");
		//ThreadTestHelper.waitForThreadToFinishSubstring("DRDAC");
		ThreadTestHelper.waitForThreadToFinishSubstring("Executor");
		ThreadTestHelper.waitForThreadToFinishSubstring("WATCHDOG");
	}


	@Test
	public void testDoServiceVersion() throws Exception {
		HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
		HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);

		EasyMock.expect(request.getPathInfo()).andReturn("/version");

		EasyMock.replay(request, response);

		ScenarioServlet servlet = new ScenarioServlet();

		String version = servlet.doService(request, response);
		assertNotNull(version);
		// Note: If you change the version-format, also change anything that requests the version via REST,
		// e.g. the status dashboards and other integration features
		assertTrue("Had: " + version, version.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"));

		EasyMock.verify(request, response);
	}
}
