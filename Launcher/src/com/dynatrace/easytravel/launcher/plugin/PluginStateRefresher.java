package com.dynatrace.easytravel.launcher.plugin;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.widgets.Display;

import com.dynatrace.easytravel.launcher.config.UIChangeListener;
import com.dynatrace.easytravel.launcher.config.UIProperties;
import com.dynatrace.easytravel.launcher.engine.ThreadEngine;

/**
 * Starts a timer which periodically refreshes plugin state so that
 * newly added plugins are visible in the Launcher.
 *
 * This is mainly necessary to get the plugins added by the Customer Frontend
 * listed correctly.
 *
 * @author dominik.stadler
 */
public class PluginStateRefresher implements UIChangeListener {
	// system properties that can overwrite the timings of the Timer
	public static final String PROPERTY_DELAY = "com.dynatrace.easytravel.plugins.tasktimerdelay";
	public static final String PROPERTY_PERIOD = "com.dynatrace.easytravel.plugins.tasktimerperiod";

	// 10 seconds each by default
	public static final String DEFAULT_DELAY = "10000";
	public static final String DEFAULT_PERIOD = "10000";

	private Timer timer = null;
	private final PluginStateListener pluginStateListener = new PluginStateListener();

	public void startTimer() {
		// start timer for action center
		Timer toStart = new Timer("PluginStateRefresherTask", true);
		timer = toStart;

		// read the properties as late as possible to allow tests to override it for easier testing
		final int ACTION_CENTER_TIMER_DELAY = Integer.parseInt(System.getProperty(
				PROPERTY_DELAY, DEFAULT_DELAY)); // ms, start after 10 sec to show warnings early after startup
		final int ACTION_CENTER_TIMER_PERIOD = Integer.parseInt(System.getProperty(
				PROPERTY_PERIOD, DEFAULT_PERIOD)); // ms, every 5 min

		toStart.schedule(new PluginStateTimerTask(), ACTION_CENTER_TIMER_DELAY, ACTION_CENTER_TIMER_PERIOD);
		ThreadEngine.fireBackgroundActionStart("PluginStateRefresher", null, Display.getCurrent());
	}

	public void stopTimer() {
		if (timer != null) {
			Timer toStop = timer;
			timer = null;
			toStop.cancel();
			ThreadEngine.fireBackgroundActionEnd("PluginStateRefresher", null, Display.getCurrent());
		}
	}

    /**
     * notifies registered observers
     *
     * @param elem  - UI element changed
     */
    @Override
    public void notifyUIChanged(UIProperties elem) {
        pluginStateListener.refreshPluginMenuPage();
    }

    /**
	 * The task simply calls the {@link PluginStateListener} to update the list of plugins if necessary.
	 *
	 * @author dominik.stadler
	 */
	private class PluginStateTimerTask extends TimerTask {
		@Override
		public void run() {
			pluginStateListener.refreshPluginMenuPage();
		}
	}

	public PluginStateListener getPluginStateListener() {
		return pluginStateListener;
	}

	public void disposePluginStateRefresher() {
		this.pluginStateListener.disposePluginStateListener();
	}
}
