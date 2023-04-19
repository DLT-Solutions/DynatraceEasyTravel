package com.dynatrace.easytravel.launcher.engine;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;

import com.dynatrace.easytravel.util.TextUtils;


abstract class AbstractStopper implements Stopper {

	private static final int CONCURRENT_STOP_TIMEOUT_SEC = 120;
	private static final Logger log = Logger.getLogger(BatchStopper.class.getName());
	private Set<Procedure> stoppables;
	private List<Procedure> procedures;

	AbstractStopper(Collection<? extends Procedure> procedures) {
		this.procedures = new LinkedList<Procedure>(procedures);
	}

	/**
	 * Method tries to stop all running procedures.
	 * @author stefan.moschinski
	 * @return
	 */
	@Override
	public boolean execute() {
		log.info("Stopping " + procedures.size() + " easyTravel procedures...");

		// take the list of procedures and separate them into parallel-stoppable and synchronous stopable
		// depending on the StopMode which the Procedures report
		ListIterator<Procedure> sequentialStoppableIt = filterConcurrentStoppable();

		// stop all given parallel procedures in reverse order
		boolean concurrentStoppedSuccessfully = endConcurrent(stoppables);
		boolean parallelStoppedSuccessfully = true;

		while (sequentialStoppableIt.hasPrevious()) {
			Procedure procedure = sequentialStoppableIt.previous();
			if (!procedure.getStopMode().isStoppable() || !procedure.isEnabled()) {
				continue;
			}

			logStopProcedure(procedure);
			parallelStoppedSuccessfully &= procedure.stop().isOk();
		}
		return concurrentStoppedSuccessfully && parallelStoppedSuccessfully;
	}

	private ListIterator<Procedure> filterConcurrentStoppable() {
		stoppables = new HashSet<Procedure>();
		List<Procedure> procs = new LinkedList<Procedure>(procedures);
		for (ListIterator<Procedure> it = procs.listIterator(); it.hasNext();) {
			Procedure entry = it.next();
			if (StopMode.PARALLEL == entry.getStopMode()) {
				stoppables.add(entry);
				it.remove();
			}
		}
		return procs.listIterator(procs.size());
	}

	private boolean endConcurrent(Collection<Procedure> stoppables) {
		if (stoppables == null || stoppables.isEmpty()) {
			return true;
		}
		final CountDownLatch procCountDown = new CountDownLatch(stoppables.size());
		final AtomicBoolean allProceduresStoppedSuccessfully = new AtomicBoolean(true);
		for (final Procedure proc : stoppables) {
			if (notStoppable(proc)) {
				procCountDown.countDown();
				continue;
			}
	    	ThreadEngine.createBackgroundThread("AbstractStopper", new Runnable() {
				@Override
				public void run() {
					try {
						logStopProcedure(proc);
						allProceduresStoppedSuccessfully.set(allProceduresStoppedSuccessfully.get() && proc.stop().isOk());
						if (!proc.stop().isOk()) {
							log.severe("Stopping was not succesful: " + proc.getName());
						}
					} finally {
						procCountDown.countDown();
					}
				}
			}, Display.getCurrent()).start();
		}

		try {
			procCountDown.await(CONCURRENT_STOP_TIMEOUT_SEC, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.log(Level.WARNING, "Waiting for CountDownLatch was interrupted", e);
		}
		return procCountDown.getCount() == 0 && allProceduresStoppedSuccessfully.get();
	}

	@Override
	public boolean notStoppable(final Procedure proc) {
		return !proc.isStoppable() || !proc.isRunning();
	}

	private static void logStopProcedure(Procedure procedure) {
		log.info(TextUtils.merge("Stopping {0}...", procedure.getName()));
	}
}
