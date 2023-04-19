package com.dynatrace.easytravel.launcher.engine;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Global starter for threads
 *
 * @author philipp.grasboeck
 */
public class ThreadEngine {
	private static final Logger LOGGER = LoggerFactory.make();

	private static final AtomicInteger nextActionId = new AtomicInteger(); // NOPMD

	/**
	 * Creates a background thread.
	 * Convenience method that takes care of invoking fireBackgroundActionStart()
	 * and fireBackgroundActionEnd().
	 *
	 * @param name
	 * @param target
	 * @return
	 */
	public static Thread createBackgroundThread(String name, Runnable target, Display display) {
		return createBackgroundThread(name, target, false, display);
	}

	/**
	 * Creates a background thread.
	 * Convenience method that takes care of invoking fireBackgroundActionStart()
	 * and fireBackgroundActionEnd().
	 *
	 * @param name
	 * @param target
	 * @param daemon
	 * @return
	 */
	public static Thread createBackgroundThread(String name, Runnable target, boolean daemon, Display display) {
		String actionId = "action-" + nextActionId.getAndIncrement();
		BackgroundRunnable run = new BackgroundRunnable(target, actionId, display);

		Thread thread = new Thread(run, name);
		thread.setDaemon(daemon);
		return run.init(thread);
	}

	/**
	 * Fires the start of a background action.
	 *
	 * @param actionId The unique identifier of the action
	 * @param thread The associated thread, may be null if not started in a separate thread.
	 */
	public static void fireBackgroundActionStart(final String actionId, final Thread thread, final Display display) {
		if (Launcher.isWeblauncher()) {
			runInDisplayThread(new Runnable() {

				@Override
				public void run() {
					Launcher.notifyBackgroundActionStart(actionId, thread);
				}
			}, display);
		}
	}

	/**
	 * Fires the end of a background action.
	 *
	 * @param actionId The unique identifier of the action
	 * @param thread The associated thread, may be null if not started in a separate thread.
	 */
	public static void fireBackgroundActionEnd(final String actionId, final Thread thread, final Display display) {
		if (Launcher.isWeblauncher()) {
			runInDisplayThread(new Runnable() {

				@Override
				public void run() {
					Launcher.notifyBackgroundActionEnd(actionId, thread);
				}
			}, display);
		}
	}

	private static class BackgroundRunnable implements Runnable {

		private final String actionId;
		private final Runnable target;
		private final Display display;
		private Thread thread;

		private BackgroundRunnable(Runnable target, String actionId, Display display) {
			this.actionId = actionId;
			this.target = target;
			this.display = display;
		}

		private Thread init(Thread thread) {
			fireBackgroundActionStart(actionId, thread, display);
			this.thread = thread;
			return this.thread;
		}

		@Override
		public void run() {
			target.run();
			fireBackgroundActionEnd(actionId, thread, display);
		}
	}

	public static void runInDisplayThread(Runnable target, Widget widget) {
		if (widget == null) {
			return;
		}
		
		try {
			Display display = widget.getDisplay();
			runInDisplayThread(target, display);
		} catch (Exception e) {
			LOGGER.error("Cannot run in display thread: " + e);
		}
	}
	
	/**
	 * Executes the given runnable within the display thread.
	 *
	 * @param target the runnable to execute in display thread
	 */
	public static void runInDisplayThread(Runnable target, Display display) {
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(target);
	}
}
