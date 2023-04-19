package com.dynatrace.diagnostics.uemload.headless;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.process.HeadlessProcessKiller;
import com.dynatrace.easytravel.util.process.HeadlessProcessKillerFactory;

import ch.qos.logback.classic.Logger;

public class ProcessKillerFactoryTest {
	Logger LOGGER = LoggerFactory.make();
	HeadlessVisitTestUtil visitTestUtil;

	@Before
	public void setUp() {
 		visitTestUtil = new HeadlessVisitTestUtil();
 		HeadlessVisitTestUtil.setup(false);
 		HeadlessProcessKiller.resetState();
	}

	@After
	public void tearDown() throws InterruptedException {
		visitTestUtil.tearDown(false);
	}

	@Ignore("not stable")
	@Test
	public void testStopCanBeInterrupted() throws InterruptedException {
		LOGGER.trace("Create Chrome with blocking visits");
		List<HeadlessDummyVisit> visits = createBlockingVisits();

		LOGGER.trace("Start thread that will kill all processes - stop interrupted");
		stopChromeProcessesInThread(false);

		LOGGER.trace("Interrupt stop and wait until it all stop actions are done");
		assertThat("Interrupting stop process was not successfull", HeadlessProcessKillerFactory.interruptStopAndWait(), is(true));
		assertThat("We expected that some Chrome will survive", HeadlessProcessKillerFactory.getChromeAndDriverProcesses(), is(not(empty())));

		LOGGER.trace("Finish all visits");
		visits.forEach(v -> unblockActionAndWait(v));
	}

	@Ignore("not stable")
	@Test
	public void testLauncherShutdown() throws InterruptedException {
		List<HeadlessDummyVisit> visits = createBlockingVisits();

		LOGGER.trace("Start thread that will kill all processes");
		Thread stopThread = stopChromeProcessesInThread(true);

		LOGGER.trace("Waiting for stop thread to finish");
		stopThread.join(5*60*1000);
		LOGGER.trace("Stop thread finished");

		assertThat("Thre should be no Chrome processes", HeadlessProcessKillerFactory.getChromeAndDriverProcesses(), is(empty()));

		LOGGER.trace("Finish all visits");
		visits.forEach(v -> unblockActionAndWait(v));
	}

	private List<HeadlessDummyVisit> createBlockingVisits() {
		LOGGER.trace("Create Chrome with blocking visits");
		List<HeadlessDummyVisit> visits = new ArrayList<>();
		IntStream.range(0, 5).forEach(i -> visits.add(HeadlessVisitTestUtil.createHeadlessBlockingVisit()));
		visits.forEach(v -> visitTestUtil.runHeadlessVisitInThread(v, false));
		visits.forEach(v -> waitForBlockingAction(v));
		return visits;
	}

	private void waitForBlockingAction(HeadlessDummyVisit visit) {
		try {
			visit.waitForBlockingAction(0, 50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void unblockActionAndWait(HeadlessDummyVisit visit) {
		try {
			visit.unblockActionAndWait(0, 50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Thread stopChromeProcessesInThread(boolean launcherShutdown) throws InterruptedException {
		Runnable stopChromeProcesses = () -> HeadlessProcessKillerFactory.stopChromeProcesses(launcherShutdown);
		Thread stopThread = new Thread(stopChromeProcesses);
		stopThread.start();

		Supplier<Boolean> factoryStop = () -> HeadlessProcessKillerFactory.isStopInProgress() ;
    	TestUtil.waitWhileNotTrue(factoryStop, 10);

    	Supplier<Boolean> processKillerStop = () -> HeadlessProcessKiller.isStopInProgress();
    	TestUtil.waitWhileNotTrue(processKillerStop, 10);

    	return stopThread;
	}


}
