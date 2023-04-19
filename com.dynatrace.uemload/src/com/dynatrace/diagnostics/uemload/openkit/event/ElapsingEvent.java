package com.dynatrace.diagnostics.uemload.openkit.event;

import com.dynatrace.diagnostics.uemload.UemLoadScheduler;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * <p>
 * An event that lasts in time running in a separate thread. It's execution can be delayed.
 * Notifies its listeners when it finishes its work. Waits for any registered sub-tasks before returning.
 * </p>
 * <h4>State timeline:</h4>
 * <p>
 * +>   Event thread created {@link #beginAfter(Consumer callback)}
 * |
 * |  Start delay period {@link #withStartDelay(int min, int max)}
 * |
 * +>   Event started, internal {@link #start()} and listeners {@link #started(EventCallback callback)} called
 * |
 * |  Main work period (unspecified on this abstraction level)
 * |
 * +>   Event work finished, listeners called {@link #ended(EventCallback callback)}
 * |
 * |  Sub tasks waiting period {@link #addSubTask(Supplier subTask)} - handled internally
 * |
 * +>   Event thread finished
 * </p>
 * Start delay period is optional and disabled by default - add it with listed chained method
 */
public abstract class ElapsingEvent<E extends ElapsingEvent<E>> extends Event<E> {
	private static final long WAIT_FOR_TASKS_SLEEP_TIME = 200;
	private static final long WAIT_FOR_TASKS_TIMEOUT = 3 * 60 * 1000; //3m;

	private final Set<EventCallback> finishListeners = new HashSet<>();
	protected final Set<Supplier<Future>> subTasks = new HashSet<>();

	private volatile long startDelay;
	protected Future task;

	protected E withStartDelay(int minStartDelay, int maxStartDelay) {
		this.startDelay = UemLoadUtils.randomInt(minStartDelay, maxStartDelay);
		return getThis();
	}

	@Override
	protected void run() {
		callListeners(finishListeners);
		waitForTasksCompletion(subTasks);
	}

	protected final E withParentAction(ActionParent parentAction) {
		parentAction.addSubTask(this::getTask);
		return getThis();
	}

	@Override
	protected void startExecution() {
		task = UemLoadScheduler.schedule(() -> {
			timeService.waitForDuration(startDelay);
			super.startExecution();
		}, 0, TimeUnit.SECONDS);
	}

	protected void waitForTasksCompletion(Set<Supplier<Future>> tasks) {
		long tasksWaitStartTime = System.currentTimeMillis();
		Supplier<Boolean> timeSupplier = () -> (System.currentTimeMillis() - tasksWaitStartTime) < WAIT_FOR_TASKS_TIMEOUT;
		Supplier<Boolean> tasksSupplier = () -> tasks.stream().anyMatch(futureTask -> !taskFinished(futureTask)); 
		while (timeSupplier.get() && tasksSupplier.get()) {
			try {
				Thread.sleep(WAIT_FOR_TASKS_SLEEP_TIME);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "waitForTasksCompletion interrupted while waiting for task completion", e);
				Thread.currentThread().interrupt();
			}
		}
		
		if(!timeSupplier.get()) {
			logger.log(Level.SEVERE, getClass().getName() + " waitForTasksCompletion didn't finished in " + WAIT_FOR_TASKS_TIMEOUT);
		}
	}

	public Future getTask() {
		return task;
	}

	protected long calcRemainingWaitTime(long waitDuration) {
		return waitDuration - (System.currentTimeMillis() - startTime);
	}

	private boolean taskFinished(Supplier<Future> task) {
		return task.get() == null || task.get().isDone();
	}

	public void ended(EventCallback callback) {
		finishListeners.add(callback);
	}

	public void addSubTask(Supplier<Future> task) {
		if (task != null) {
			subTasks.add(task);
		}
	}
}
