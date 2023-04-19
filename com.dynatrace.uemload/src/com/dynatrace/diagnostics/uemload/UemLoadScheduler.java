package com.dynatrace.diagnostics.uemload;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codahale.metrics.Gauge;
import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.metrics.Metrics;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;


public class UemLoadScheduler {
	private static final Logger logger = Logger.getLogger(UemLoadScheduler.class.getName());

	private static final ScheduledThreadPoolExecutor scheduler;

	/**
	 * The initial number of threads, can be overridden by a call to setMaxThreads()
	 */
	private static final int MAX_THREADS = 50;

	/**
	 * The factor of how many tasks we allow to queue based on the number of threads. So it will be computed as MAX_THREADS*MAX_CONCURRENT_TASKS_FACTOR,
	 * if the number of threads is adjusted via setMaxThreads(), the resulting number of tasks adjusts automatically to this.
	 */
	private static int MAX_CONCURRENT_TASKS_FACTOR = 3;

	private static final Set<ScheduledFuture<?>> futures = Collections.newSetFromMap(new ConcurrentHashMap<ScheduledFuture<?>, Boolean>());

	private static final boolean DAEMON_THREADS = Boolean.parseBoolean(System.getProperty(
			"com.dynatrace.diagnostics.uemload.uemSchedulerDaemonThreads",
			Boolean.TRUE.toString()));

	static {
		scheduler = new ScheduledThreadPoolExecutor(MAX_THREADS,
				new ThreadFactoryBuilder()
						.setDaemon(DAEMON_THREADS)
						.setNameFormat(BaseConstants.UEM_LOAD_THREAD + "-%d")
						.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
							@Override
							public void uncaughtException(Thread t, Throwable e) {
								logger.log(Level.SEVERE,
										TextUtils.merge("An uncaught exception happened in the thread ''{0}''", t.getName()), e);
							}
						})
						.build());
		scheduler.setKeepAliveTime(60L, TimeUnit.SECONDS);
		scheduler.allowCoreThreadTimeOut(true);

		// register some gauges to provide metrics about the Scheduler
		registerMetrics();
	}

	private static long lastOnlyFreeWarning = 0;
	public static ScheduledFuture<?> scheduleOnlyIfFree(Runnable command, long delay, TimeUnit unit) {
		if(scheduler.getQueue().size() >= scheduler.getCorePoolSize()) {
			lastOnlyFreeWarning = printWarning("Not scheduling any more tasks because limit of " + scheduler.getCorePoolSize() + " is reached", lastOnlyFreeWarning);			
			return null;
		}
		return schedule(command, delay, unit);
	}
	
	private static long lastWarning = 0;
	public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		if (scheduler.isShutdown() || Thread.currentThread().isInterrupted()) {
			throw new IllegalStateException("Cannot schedule when scheduler is shut down or thread is interrupted: " + scheduler.isShutdown() + "/" + Thread.currentThread().isInterrupted());
		}

		int maxTasks = scheduler.getCorePoolSize()*MAX_CONCURRENT_TASKS_FACTOR;
		if(scheduler.getQueue().size() > maxTasks) {
			lastWarning = printWarning("Not scheduling any more tasks because limit of " + maxTasks + " is reached", lastWarning);			
			return null;
		}

		Metrics.incScheduleCount(getActiveCount());
		return scheduler.schedule(command, delay, unit);
	}
	
	/**
	 * Print warning no often than once per 5 minutes 
	 * @param warning
	 * @param lastTime
	 * @return time when last warning was printed
	 */
	private static long printWarning(String warning, long lastTime) {
		// only log this every 5 minutes
		if(System.currentTimeMillis() > (lastTime + (5*60*1000))) {
			logger.warning(warning);
			return System.currentTimeMillis();
		}
		return lastTime;
	}

	public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		if (scheduler.isShutdown() || Thread.currentThread().isInterrupted()) {
			throw new IllegalStateException("Cannot schedule when scheduler is shut down or thread is interrupted: " + scheduler.isShutdown() + "/" + Thread.currentThread().isInterrupted());
		}
		ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
		futures.add(scheduledFuture);

		Metrics.incScheduleAtFixedRateCount(getActiveCount());
		return scheduledFuture;
	}

	public static <V> void sleep(int sleep, Callable<V> continuation) {
		if (!scheduler.isShutdown() && !Thread.currentThread().isInterrupted()) {
			Metrics.incScheduleSleepCount(getActiveCount());
			scheduler.schedule(continuation, sleep, TimeUnit.MILLISECONDS);
		}
	}

	public static void shutdown(long timeout, TimeUnit unit) {
		scheduler.shutdown();
		try {
			scheduler.awaitTermination(timeout, unit);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public static void shutdownNow() {
		scheduler.shutdownNow();
	}

	public static boolean isShutdown() {
		return scheduler.isShutdown();
	}

	public static int getQueueSize() {
		return scheduler.getQueue().size();
	}

	public static int getActiveCount() {
		return scheduler.getActiveCount();
	}

	@TestOnly
	public static BlockingQueue<Runnable> getQueue() {
		return scheduler.getQueue();
	}

	public static void setMaxThreads(int maxThreads) {
		// TODO: why does setMaximumPoolSize not work here...
		scheduler.setCorePoolSize(maxThreads);
	}

	/**
	 * Cancel and remove existing scheduled tasks with fixed-rate execution.
	 *
	 * Normal scheduled tasks for one-time execution are not changed!
	 */
	public static void cleanup() {
		for(ScheduledFuture<?> scheduledFuture : futures) {
			scheduledFuture.cancel(false);
		}
		scheduler.purge();
		futures.clear();
	}

	/**
	 * Register some Metrics for the various values that the scheduler provides.
	 */
	private static void registerMetrics() {
		Metrics.registry().register("load.active", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return scheduler.getActiveCount();
            }
        });
		Metrics.registry().register("load.queue", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return getQueueSize();
            }
        });
		Metrics.registry().register("load.completed", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return scheduler.getCompletedTaskCount();
            }
        });
		Metrics.registry().register("load.pool.size.core", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return scheduler.getCorePoolSize();
            }
        });
		Metrics.registry().register("load.pool.size.max", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return scheduler.getMaximumPoolSize();
            }
        });
		Metrics.registry().register("load.pool.size.largest", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return scheduler.getLargestPoolSize();
            }
        });
	}
}
