package com.dynatrace.easytravel.utils;

import static org.junit.Assert.fail;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import ch.qos.logback.classic.Logger;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * Helper class to test with many threads.
 *
 * Sample usage is as follows:
 *
 * <code>
    @Test
    public void testMultipleThreads() throws Throwable {
        ThreadTestHelper helper =
            new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

        helper.executeTest(new ThreadTestHelper.TestRunnable() {
            @Override
            public void doEnd(int threadnum) throws Exception {
                // do stuff at the end ...
            }

            @Override
            public void run(int threadnum, int iter) throws Exception {
                // do the actual threaded work ...
            }
        });
    }
  </code>
 */
public class ThreadTestHelper {
	private static final int THREAD_JOIN_TIMEOUT = 60000;

	private static Logger log = LoggerFactory.make();

	private final int threadCount;
	private final int testsPerThread;

	private Queue<Throwable> exception = new ConcurrentLinkedQueue<Throwable>();
	private int executions[] = null;

	/**
	 * Initialize the class with the number of tests that should be executed
	 *
	 * @param threadCount
	 *            The number of threads to start running in parallel.
	 * @param testsPerThread
	 *            The number of single test-executions that are done in each
	 *            thread
	 */
	public ThreadTestHelper(int threadCount, int testsPerThread) {
		this.threadCount = threadCount;
		this.testsPerThread = testsPerThread;

		// Initialize array to allow to summarize afterwards
		executions = new int[threadCount];
	}

	public void executeTest(TestRunnable run) throws Throwable {
		log.debug("Starting thread test");

		List<Thread> threads = new LinkedList<Thread>();

		// start all threads
		for (int i = 0; i < threadCount; i++) {
			Thread t = startThread(i, run);
			threads.add(t);
		}

		// while threads are running, throw exception quickly instead of
		// running until all threads are done and only then failing
		while(threads.get(0).isAlive()) {
			Thread.sleep(1000);
			// report exceptions if there were any
			throwExceptionIfAny();
		}

		// wait for all threads
		for (int i = 0; i < threadCount; i++) {
			threads.get(i).join();
		}

		// report exceptions if there were any
		if(exception != null) {
			throwExceptionIfAny();
		}

		// make sure the resulting number of executions is correct
		for (int i = 0; i < threadCount; i++) {
			// check if enough items were performed
			Assert.assertEquals("Thread " + i
					+ " did not execute all iterations", testsPerThread,
					executions[i]);
		}
	}

	private void throwExceptionIfAny() throws Throwable {
		if(exception.size() > 1) {
			Throwable exc = null;
			StringBuilder exceptions = new StringBuilder();
			while(!exception.isEmpty()) {
				exc = exception.remove();
				exceptions.append(exc.toString()).append("\n");
			}
			throw new IllegalStateException("Had multiple exceptions: \n" + exceptions.toString(), exc);
		} else if (exception.size() > 0) {
			throw exception.remove();
		}
	}

	/**
	 * This method is executed to start one thread. The thread will execute the
	 * provided runnable a number of times.
	 *
	 * @param threadnum
	 *            The number of this thread
	 * @param run
	 *            The Runnable object that is used to perform the actual test
	 *            operation
	 *
	 * @return The thread that was started.
	 *
	 */
	private Thread startThread(final int threadnum, final TestRunnable run) {
		log.debug("Starting thread number: " + threadnum);

		Thread t1 = null;
		t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					for (int iter = 0; iter < testsPerThread; iter++) {
						// log.fine("Executing iteration " + iter +
						// " in thread" +
						// Thread.currentThread().getName());

						// call the actual testcode
						run.run(threadnum, iter);

						executions[threadnum]++;
					}

					// do end-work here, we don't do this in a finally as we log
					// Exception
					// then anyway
					run.doEnd(threadnum);
				} catch (Throwable e) {
					//log.log(Level.SEVERE, "Caught unexpected Throable", e);
					exception.add(e);
				}

			}
		}, "Thread " + threadnum);

		t1.start();

		return t1;
	}

	public interface TestRunnable {
		/**
		 * When an object implementing interface <code>Runnable</code> is used
		 * to create a thread, starting the thread causes the object's
		 * <code>run</code> method to be called in that separately executing
		 * thread.
		 * <p>
		 * The general contract of the method <code>run</code> is that it may
		 * take any action whatsoever.
		 *
		 * @param threadnum
		 *            The number of the thread executing this run()
		 * @param iter
		 *            The count of how many times this thread executed the
		 *            method
		 *
		 * @see java.lang.Thread#run()
		 */
		public abstract void run(int threadnum, int iter) throws Exception;

		/**
		 * Perform any action that should be done at the end.
		 *
		 * This method should throw an Exception if any check fails at this
		 * point.
		 */
		void doEnd(int threadnum) throws Exception;
	}

	public void testDummy() {
		// small empty test to not fail if this class is executed as test case
		// by
		// Hudson/Sonar
	}

	/**
	 * Wait for all threads with the specified name to finish, i.e. to not appear in the
	 * list of running threads any more.
	 *
	 * @param name
	 *
	 * @throws InterruptedException
	 * @author dominik.stadler
	 */
	public static void waitForThreadToFinish(final String name) throws InterruptedException {
		int count = Thread.currentThread().getThreadGroup().activeCount();

		Thread[] threads = new Thread[count];
		Thread.currentThread().getThreadGroup().enumerate(threads);

		for (Thread t : threads) {
			if (t != null && name.equals(t.getName())) {
				waitForThread(t);
			}
		}
	}

	private static void waitForThread(Thread t) throws InterruptedException {
		log.info("Waiting for thread '" + t.getName() + "' to finish");
		t.join(THREAD_JOIN_TIMEOUT);
		if(t.isAlive()) {
			System.out.println(new ThreadDump(true, true).toString());
			fail("Thread " + t.getName() + " did not stop in the timeout of " + THREAD_JOIN_TIMEOUT + "ms");
		}
	}

	/**
	 * Wait for thread whose name contain the specified string to finish, i.e. to not appear in the
	 * list of running threads any more.
	 *
	 * @param name
	 *
	 * @throws InterruptedException
	 * @author dominik.stadler
	 */
	public static void waitForThreadToFinishSubstring(final String name) throws InterruptedException {
		int count = Thread.currentThread().getThreadGroup().activeCount();

		Thread[] threads = new Thread[count];
		Thread.currentThread().getThreadGroup().enumerate(threads);

		for (Thread t : threads) {
			if (t != null && t.getName().contains(name)) {
				waitForThread(t);
			}
		}
	}

	/**
	 * This methods executes the passed {@link Callable}. The number of executions depends
	 * on the given runs number.
	 *
	 * @param testable test {@link TestCallable} to execute
	 * @param runs defines how many times the passed {@link Callable} is executed
	 * @return the results of the the execution of the passed {@link Callable}
	 * @throws Throwable if an exception happened during the execution of the {@link Callable}
	 * @author stefan.moschinski
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static <T> List<T> executeTest(final TestCallable<T> testable, int runs) throws InterruptedException, ExecutionException {
		final CyclicBarrier barrier = new CyclicBarrier(runs);
		ExecutorService executor = Executors.newCachedThreadPool(
				new BasicThreadFactory.Builder()
						.uncaughtExceptionHandler(new UncaughtExceptionHandler() {

							@Override
							public void uncaughtException(Thread t, Throwable e) {
								log.error(TextUtils.merge("An uncaught exception happened in Thread {0}",
												t.getName()),
										e);
							}
						})
						.namingPattern(ThreadTestHelper.class.getSimpleName() + "-Thread-%d")
						.build());
		try {
			List<Callable<T>> tasks = new ArrayList<Callable<T>>(runs);
			for (final AtomicInteger i = new AtomicInteger(0); i.get() < runs; i.incrementAndGet()) {
				tasks.add(new Callable<T>() {

					@Override
					public T call() throws Exception {
						barrier.await(); // causes more contention
						return testable.call(i.get());
					}
				});
			}

			List<Future<T>> futures = executor.invokeAll(tasks);
			List<T> results = new ArrayList<T>(futures.size());
			for (Future<T> future : futures) {
				results.add(future.get());
			}
			return results;
		} finally {
			executor.shutdownNow();
		}
	}

	public interface TestCallable<T> {

		public T call(int threadNo) throws Exception;


	}
}
