package com.dynatrace.diagnostics.uemload;

import static java.lang.String.format;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Queues;

/**
 * If a resource takes to long to load, Uem ignores them. Consequently, if a page load or a custom action takes longer than 9
 * seconds we signal that to Uem to avoid ignoring the resource (see JLT-73392).
 *
 * @author cwat-smoschin
 */
public class LoadTimeWatcher {
	private static final Logger log = Logger.getLogger(LoadTimeWatcher.class.getName());

	private static final Object lock = new Object();
	//guarded by lock
	private static final Set<UemAction> actions = Collections.newSetFromMap(new MapMaker().concurrencyLevel(16).initialCapacity(100).<UemAction, Boolean> makeMap());
	//guarded by lock
	private static boolean isScenarioRunning = true;
	private static long maxActionDurationSec = TimeUnit.MINUTES.toSeconds(3);
	private static int maxSize = 10000;
	private static final int TIMEOUT_SECONDS = 9;

	private volatile CustomAction currentAction;
	private volatile PageLoad currentPageLoad;

	static {
		UemLoadScheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				synchronized (lock) {
					//cwpl-rpsciuk: APM-8129 if scenario is not running do not continue any action 
					if (UemLoadScheduler.isShutdown() || !isScenarioRunning) {
						actions.clear();
						return;
					}
				}

				try {
					Queue<UemAction> sortedActions = null;
					synchronized (lock) {
						sortedActions = Queues.newPriorityBlockingQueue(actions);
					}
					for (UemAction action : sortedActions) {
						// only start to run the action after 9 seconds
						if (action.elapsedMillis() < TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS)) {
							// since the actions are sorted in a descending order according to their start time
							// we can stop the method if an action not match the if condition (all following actions won't match either)
							return;
						} else {
							if (!isActionFinished(action)) {
								if (log.isLoggable(Level.FINE))
									log.fine("Page load is running for more than 9 seconds - sending preview" + action +
											" elapsed: " + action.elapsedMillis());
								action.sendActionPreview();

								// reset stopWatch to start the action again after 9 more seconds
								action.resetTimer();
							}
							removeActionIfNecessary(action);
						}
					}
					sortedActions = null;
				} catch (RuntimeException e) {
					log.log(Level.SEVERE,
							TextUtils.merge("An exception happened while running {0}", LoadTimeWatcher.class.getSimpleName()), e);
				}
			}
		}, 0, 1L, TimeUnit.SECONDS);
	}

	private static boolean isActionFinished(UemAction action) {
		return !actions.contains(action);
	}

	private static void removeActionIfNecessary(UemAction action) {
		if (System.currentTimeMillis() > action.getStartTime() + TimeUnit.SECONDS.toMillis(maxActionDurationSec)) {
			removeLongRunningAction(action);
		} else if (actions.size() > maxSize) {
			removeExceedingSizeAction(action);
		}

	}

	private static void removeExceedingSizeAction(UemAction action) {
		if (log.isLoggable(Level.FINE)) {
			log.fine(format(
					"Removing action '%s' because having too many actions (max size is %d)",
					action, maxSize));
		}
		actions.remove(action);
	}

	private static void removeLongRunningAction(UemAction action) {
		if (log.isLoggable(Level.FINE)) {
			log.fine(format(
					"Action '%s' is running longer than %d seconds, there will be no longer previews sent for this action",
					action, maxActionDurationSec));
		}
		actions.remove(action);
	}

	void startPageLoad(PageLoad pageLoad) {
		Preconditions.checkArgument(currentPageLoad == null,
				"There must no page load be active, but currently there is still one: " + currentPageLoad);
		actions.add(pageLoad);
		currentPageLoad = pageLoad;
	}

	void startCustomAction(CustomAction customAction) {
		Preconditions.checkArgument(currentAction == null,
				"There must no custom action be active, but currently there is still one: " + currentAction);
		actions.add(customAction);
		currentAction = customAction;
	}

	void stopPageLoad() {
		if (log.isLoggable(Level.FINE)) {
			log.fine("stopPageLoad " + currentPageLoad);
		}
		
		if (currentPageLoad != null) {
			actions.remove(currentPageLoad);
		}
		currentPageLoad = null;
	}

	void stopCustomAction() {
		if (actions != null && currentAction != null && actions.contains(currentAction)) {
			actions.remove(currentAction);
		}
		currentAction = null;
	}

	static Collection<UemAction> getUnfinishedActions() {
		return Collections.unmodifiableCollection(actions);
	}

	@TestOnly
	static void setMaxActionDuration(long durationSec) {
		maxActionDurationSec = durationSec;
	}

	@TestOnly
	static void setMaxSize(int maxSize) {
		LoadTimeWatcher.maxSize = maxSize;
	}

	/**
	 * Notify @LoadTimeWatcher if batch is running or stopped
	 * If no batch is running list of actions will be cleared next time when thread is run
	 * @param running
	 */
	public static void notifyBatchStateChanged(boolean running) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("batch state chagned: " + running);
		}
			
		synchronized (lock) {
			isScenarioRunning = running; 
		}
		
	}
}
