package com.dynatrace.diagnostics.uemload.headless;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HeadlessVisitRunnableTest2 {
	Logger logger = LoggerFactory.make();
	private static final int RETRIES = 5;
	HeadlessVisitTestUtil visitTestUtil;

	@Before
	public void setUp() {
 		visitTestUtil = new HeadlessVisitTestUtil();
 		HeadlessVisitTestUtil.setup(false);
	}

	@After
	public void tearDown() throws InterruptedException {
		visitTestUtil.tearDown(false);
	}
	

	@Ignore("Integration test")
	@Test
	public void testMouseMovesAndScrolls() {
		HeadlessVisitTestUtil.runHeadlessDummyMoveVisit("SECRET", false);
	}

	@Ignore("Integration test")
	@Test
	public void testBlogVisit() {
		HeadlessBlogVisit visit = new HeadlessBlogVisit("http://localhost:9080");
		HeadlessVisitTestUtil.runHeadlessVisit(visit, ExtendedDemoUser.DEMOUSER2, false);
	}

	@Ignore("Integration test")	
	@Test
	public void testMouseMove() {
		HeadlessVisit visit = new HeadlessMouseMoveVisit("http://localhost:9080");
		HeadlessVisitTestUtil.runHeadlessVisit(visit, ExtendedDemoUser.MONICA_USER, false);
	}

	@Ignore("Integration test")
    @Test
    public void angularSearchTest() throws IOException {
    	int visitsCount = 1;
    	HeadlessVisit visit = new HeadlessAngularSearchVisit("http://localhost:9080");

    	logger.warn("Demouser: " + ExtendedDemoUser.DEMOUSER.getLocation().getIp());
    	IntStream.range(0, visitsCount).forEach( i -> {
    		HeadlessVisitTestUtil.runHeadlessVisit(visit, ExtendedDemoUser.DEMOUSER, false);
    	});

    	logger.warn("Demouser2: " + ExtendedDemoUser.DEMOUSER2.getLocation().getIp());
    	IntStream.range(0, visitsCount).forEach( i -> {
    		HeadlessVisitTestUtil.runHeadlessVisit(visit, ExtendedDemoUser.DEMOUSER2, false);
    	});


    	DriverEntryPoolSingleton.getInstance().getPool().stopAll();
    }

    @Test
    public void testStopDriverPool() throws InterruptedException, ExecutionException {
    	HeadlessBlockingAction visit1BlockingAction = new HeadlessBlockingAction("action1");
    	HeadlessDummyAction vist1NotExectutedAction = new HeadlessDummyAction("action2");
    	Visit visit1 = new HeadlessDummyVisit(new HeadlessDummyAction[] {visit1BlockingAction, vist1NotExectutedAction});

    	Future<?> visit1Future = visitTestUtil.runHeadlessVisitInThread(visit1, false);
    	visit1BlockingAction.waitForBlockingAction();

    	stopPoolInThread();

    	visit1BlockingAction.unblockActionAndWait();
    	assertThat(visit1Future.get(), equalTo(null));

    	HeadlessDummyVisit abandonedVisit = HeadlessVisitTestUtil.runHeadlessVisit(false);

    	assertThat(visit1BlockingAction.getCallsNumber(), equalTo(1));
    	assertThat(vist1NotExectutedAction.getCallsNumber(), equalTo(0));
    	assertThat(abandonedVisit.getCallsNumber(), equalTo(0));

    	DriverEntryPoolSingleton.getInstance().getPool().start();

    	assertThat(HeadlessVisitTestUtil.runHeadlessVisit(false).getCallsNumber(), equalTo(1));
    	assertThat(DriverEntryPoolSingleton.getInstance().getPool().getNumActive(), equalTo(0));

    	HeadlessTestUtil.stopPoolAndCheckLeftDrivers(false);
    }

    @Test
    public void testStopHeadlessVisitRunnable() throws InterruptedException, ExecutionException {
    	HeadlessBlockingAction visit1BlockingAction = new HeadlessBlockingAction("action1");
    	HeadlessDummyAction vist1NotExectutedAction = new HeadlessDummyAction("action2");
    	Visit visit1 = new HeadlessDummyVisit(new HeadlessDummyAction[] {visit1BlockingAction, vist1NotExectutedAction});

    	Future<?> visit1Future = visitTestUtil.runHeadlessVisitInThread(visit1, false);
    	visit1BlockingAction.waitForBlockingAction();

    	HeadlessAngularRunnable.stopAll();

    	visit1BlockingAction.unblockActionAndWait();
    	assertThat(visit1Future.get(), equalTo(null));

    	HeadlessDummyVisit abandonedVisit = HeadlessVisitTestUtil.runHeadlessVisit(false);

    	assertThat(visit1BlockingAction.getCallsNumber(), equalTo(1));
    	assertThat(vist1NotExectutedAction.getCallsNumber(), equalTo(0));
    	assertThat(abandonedVisit.getCallsNumber(), equalTo(0));

    	HeadlessAngularRunnable.startAll();

    	assertThat(HeadlessVisitTestUtil.runHeadlessVisit(false).getCallsNumber(), equalTo(1));

    	HeadlessTestUtil.stopPoolAndCheckLeftDrivers(false);
    }

    private void stopPoolInThread() throws InterruptedException {
    	Runnable stopTask = () -> { DriverEntryPoolSingleton.getInstance().getPool().stopAll(); };
    	new Thread(stopTask).start();

    	waitForDriverEntryPoolStop();
    	assertFalse(DriverEntryPoolSingleton.getInstance().getPool().isVisitGenerationEnabled());
    }

    private void waitForDriverEntryPoolStop() throws InterruptedException {
    	Supplier<Boolean> s = () -> DriverEntryPoolSingleton.getInstance().getPool().isVisitGenerationEnabled();
    	TestUtil.waitWhileTrue(s, RETRIES);
    }
}
