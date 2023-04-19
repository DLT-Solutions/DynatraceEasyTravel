package com.dynatrace.easytravel.launcher.engine;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.rap.rwt.testfixture.TestContext;
import org.eclipse.swt.widgets.Display;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;


public class ThreadEngineTest {
	@ClassRule
	public static TestContext context = new TestContext();
	
	private static Display display;
	
	@BeforeClass
	public static void setup() {
		display = new Display();
		Launcher.addLauncherUI(display);
	}

	@Test
	public void testCreateBackgroundThread() throws InterruptedException {
		final AtomicBoolean exec = new AtomicBoolean(false);
		Thread thread = ThreadEngine.createBackgroundThread("test", new Runnable() {

			@Override
			public void run() {
				exec.set(true);
			}
		}, display);

		thread.start();
		thread.join();

		assertTrue("Runnable needs to be executed, but wasn't", exec.get());
	}

	@Test
	public void testCreateBackgroundThreadDaemon() throws InterruptedException {
		final AtomicBoolean exec = new AtomicBoolean(false);
		Thread thread = ThreadEngine.createBackgroundThread("test", new Runnable() {

			@Override
			public void run() {
				exec.set(true);
			}
		}, true, display);

		thread.start();
		thread.join();

		assertTrue("Runnable needs to be executed, but wasn't", exec.get());
	}

	@Test
	public void testFireBackgroundActionStart() {
		ThreadEngine.fireBackgroundActionStart("action", new Thread("testthread"), null);
	}

	@Test
	public void testFireBackgroundActionEnd() {
		ThreadEngine.fireBackgroundActionEnd("action", new Thread("testthread"), null);
	}

	@Test
	public void testRunInDisplayThreadRunnable() {
		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {

			}
		}, display);
	}

	// helper method to get coverage of the unused constructor
	 @Test
	 public void testPrivateConstructor() throws Exception {
	 	PrivateConstructorCoverage.executePrivateConstructor(ThreadEngine.class);
	 }
}
