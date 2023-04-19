/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: LoadTimeWatcher.java
 * @date: 25.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.diagnostics.uemload;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;
import org.junit.rules.ExpectedException;
import ch.qos.logback.classic.Logger;

import com.dynatrace.diagnostics.uemload.JavaScriptAgent.NullJavaScriptAgent;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.ThreadTestHelper;
import com.dynatrace.easytravel.utils.ThreadTestHelper.TestCallable;


/**
 *
 * @author stefan.moschinski
 */
public class LoadTimeWatcherTest {
	private static final Logger log = LoggerFactory.make();

	@BeforeClass
	public static void setUpClass() {
		System.setProperty("com.dynatrace.diagnostics.uemload.uemSchedulerDaemonThreads", "false");

		// TODO: reduce timeout to make unit test run much faster
		//LoadTimeWatcher.setTimeoutSeconds(2);
	}

	@Before
	public void setUp() {
		assertEquals("Had: "+ LoadTimeWatcher.getUnfinishedActions(),
				0, LoadTimeWatcher.getUnfinishedActions().size());
	}

	@After
	public void tearDown() throws InterruptedException {
		long start = System.currentTimeMillis();

		// set zero to ensure all actions are removed in the loop after some time
		LoadTimeWatcher.setMaxSize(0);

		while(!LoadTimeWatcher.getUnfinishedActions().isEmpty()) {
			Thread.sleep(100);
		}

		assertEquals("Had: "+ LoadTimeWatcher.getUnfinishedActions(),
				0, LoadTimeWatcher.getUnfinishedActions().size());

		// reset max size
		LoadTimeWatcher.setMaxSize(10000);

		log.info("TearDown took: " + (System.currentTimeMillis() - start) + "ms");
	}

	@Test
	public void testLoadIsSent() throws Exception {
		LoadTimeWatcher loadTimeWatcher1 = new LoadTimeWatcher();
		TestablePageLoad action1 = new TestablePageLoad();
		loadTimeWatcher1.startPageLoad(action1);

		TimeUnit.SECONDS.sleep(6);
		assertThat(action1.getCallCounter(), is(0));

		LoadTimeWatcher loadTimeWatcher2 = new LoadTimeWatcher();
		TestablePageLoad action2 = new TestablePageLoad();
		loadTimeWatcher2.startPageLoad(action2);

		TimeUnit.SECONDS.sleep(6);
		assertThat(action1.getCallCounter(), is(1));
		assertThat(action2.getCallCounter(), is(0));

		LoadTimeWatcher loadTimeWatcher3 = new LoadTimeWatcher();
		TestablePageLoad action3 = new TestablePageLoad();
		loadTimeWatcher3.startPageLoad(action3);

		TimeUnit.SECONDS.sleep(6);
		assertThat(action1.getCallCounter(), is(1));
		assertThat(action2.getCallCounter(), is(1));
		assertThat(action3.getCallCounter(), is(0));

		TimeUnit.SECONDS.sleep(6);
		assertThat(action1.getCallCounter(), is(2));
		assertThat(action2.getCallCounter(), is(1));
		assertThat(action3.getCallCounter(), is(1));

		assertThat(action1, isIn(LoadTimeWatcher.getUnfinishedActions()));
		assertThat(action2, isIn(LoadTimeWatcher.getUnfinishedActions()));
		assertThat(action3, isIn(LoadTimeWatcher.getUnfinishedActions()));
	}


	@Test
	public void testAddingAndRemovingWorks() throws Exception {
		LoadTimeWatcher loadTimeWatcher1 = new LoadTimeWatcher();
		TestablePageLoad action1 = new TestablePageLoad();
		loadTimeWatcher1.startPageLoad(action1);

		LoadTimeWatcher loadTimeWatcher2 = new LoadTimeWatcher();
		TestablePageLoad action2 = new TestablePageLoad();
		loadTimeWatcher2.startPageLoad(action2);

		LoadTimeWatcher loadTimeWatcher3 = new LoadTimeWatcher();
		TestablePageLoad action3 = new TestablePageLoad();
		loadTimeWatcher3.startPageLoad(action3);

		assertThat(action1, isIn(LoadTimeWatcher.getUnfinishedActions()));
		assertThat(action2, isIn(LoadTimeWatcher.getUnfinishedActions()));
		assertThat(action3, isIn(LoadTimeWatcher.getUnfinishedActions()));

		loadTimeWatcher1.stopPageLoad();
		assertThat(action1, not(isIn(LoadTimeWatcher.getUnfinishedActions())));

		loadTimeWatcher2.stopPageLoad();
		assertThat(action2, not(isIn(LoadTimeWatcher.getUnfinishedActions())));

		loadTimeWatcher3.stopPageLoad();
		assertThat(action3, not(isIn(LoadTimeWatcher.getUnfinishedActions())));
	}


	@Test
	public void testClearingOfLongRunningActionsWorks() throws Exception {
		LoadTimeWatcher.setMaxActionDuration(10);

		LoadTimeWatcher loadTimeWatcher1 = new LoadTimeWatcher();
		TestablePageLoad action1 = new TestablePageLoad();
		loadTimeWatcher1.startPageLoad(action1);

		TimeUnit.SECONDS.sleep(6);

		LoadTimeWatcher loadTimeWatcher2 = new LoadTimeWatcher();
		TestablePageLoad action2 = new TestablePageLoad();
		loadTimeWatcher2.startPageLoad(action2);

		assertThat(action1, isIn(LoadTimeWatcher.getUnfinishedActions()));
		assertThat(action2, isIn(LoadTimeWatcher.getUnfinishedActions()));

		TimeUnit.SECONDS.sleep(6);

		assertThat(action1, isIn(LoadTimeWatcher.getUnfinishedActions()));
		assertThat(action2, isIn(LoadTimeWatcher.getUnfinishedActions()));

		TimeUnit.SECONDS.sleep(8);

		assertThat(action1, not(isIn(LoadTimeWatcher.getUnfinishedActions())));
		assertThat(action2, isIn(LoadTimeWatcher.getUnfinishedActions()));

		// reset max action duration!
		LoadTimeWatcher.setMaxActionDuration(TimeUnit.MINUTES.toSeconds(3));
	}

	@Test
	public void testClearingOfExeedingActionSize() throws Exception {
		LoadTimeWatcher.setMaxSize(1);

		LoadTimeWatcher loadTimeWatcher1 = new LoadTimeWatcher();
		TestablePageLoad action1 = new TestablePageLoad();
		loadTimeWatcher1.startPageLoad(action1);

		TimeUnit.SECONDS.sleep(6);

		LoadTimeWatcher loadTimeWatcher2 = new LoadTimeWatcher();
		TestablePageLoad action2 = new TestablePageLoad();
		loadTimeWatcher2.startPageLoad(action2);

		assertThat(action1, isIn(LoadTimeWatcher.getUnfinishedActions()));
		assertThat(action2, isIn(LoadTimeWatcher.getUnfinishedActions()));

		TimeUnit.SECONDS.sleep(6);

		assertThat(action1, not(isIn(LoadTimeWatcher.getUnfinishedActions())));
		assertThat(action2, isIn(LoadTimeWatcher.getUnfinishedActions()));

		// reset max action duration!
		LoadTimeWatcher.setMaxSize(10000);
	}

	@Rule
	public ExpectedException expException = ExpectedException.none();

	@Test
	public void testAddingASecondPage() throws Exception {
		LoadTimeWatcher loadTimeWatcher = new LoadTimeWatcher();
		TestablePageLoad action1 = new TestablePageLoad();
		loadTimeWatcher.startPageLoad(action1);

		loadTimeWatcher.stopPageLoad();

		TestablePageLoad action2 = new TestablePageLoad();
		loadTimeWatcher.startPageLoad(action2);

		TestablePageLoad action3 = new TestablePageLoad();
		expException.expect(IllegalArgumentException.class);
		loadTimeWatcher.startPageLoad(action3);
	}

	@Test
	public void testConcurrentStartingAndStoppingPageLoads() throws Throwable {
		final Random random = new Random();

		ThreadTestHelper.executeTest(new TestCallable<Void>() {

			@Override
			public Void call(int threadNo) throws Exception {
				TimeUnit.SECONDS.sleep(random.nextInt(15));
				LoadTimeWatcher loadTimeWatcher = new LoadTimeWatcher();
				TestablePageLoad action1 = new TestablePageLoad();
				loadTimeWatcher.startPageLoad(action1);
				TimeUnit.SECONDS.sleep(random.nextInt(5));
				loadTimeWatcher.stopPageLoad();
				return null;
			}

		}, 500);
	}

	private class TestablePageLoad extends PageLoad {

		private AtomicInteger callCounter = new AtomicInteger(0);

		TestablePageLoad() {
			super(new NullJavaScriptAgent(null, null, null, null, null));
		}

		@Override
		protected void sendActionPreviewInternal() throws IOException {
			callCounter.incrementAndGet();
		}

		int getCallCounter() {
			return callCounter.get();
		}

		@Override
		public String toString() {
			return super.toString() + "-" + hashCode();
		}
	}
}
