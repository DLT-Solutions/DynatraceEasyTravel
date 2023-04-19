package com.dynatrace.easytravel.html;

import static com.dynatrace.easytravel.html.helpers.HtmlUnitHelpers.createWebClient;
import static com.dynatrace.easytravel.html.helpers.HtmlUnitHelpers.loginB2B;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.BeforeClass;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.ExecuteInMultipleThreads;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class HtmlUnitB2BThreadedTest {
	private final static Logger logger = LoggerFactory.make();

	// how long to run the load test
	private static final int TEST_DURATION = 10 * 60 * 60 * 1000;	// 10 minutes

	private static final int THREAD_COUNT = 10;
	// private static final int TEST_COUNT = 20;

	private final Thread[] threads = new Thread[THREAD_COUNT];

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
	}

	@Test
	public void testSearchByLocationThreaded() throws Exception {
		logger.info("Starting " + THREAD_COUNT + " Threads for executing searches in parallel");

		// start the configured number of threads
		for (int i = 0; i < THREAD_COUNT; i++) {
			logger.debug("Starting thread number: " + i);
			threads[i] = new Thread(
					new HtmlUnitExecutorThread(THREAD_COUNT, i, System.currentTimeMillis()), "ExecuteInMultipleThreads-" + i);

			threads[i].start();
		}

		// Wait the time that is configured to allow some reports to be created
		try {
			ExecuteInMultipleThreads.waitForThreads(TEST_DURATION, threads);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}

		// tell the threads to stop
		for (int i = 0; i < THREAD_COUNT; i++) {
			logger.info("Stopping thread number: " + i);
			ExecuteInMultipleThreads.stop();
		}

		// wait for threads to finish
		for (int i = 0; i < THREAD_COUNT; i++) {
			logger.info("Joining thread number: " + i);
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}

		ConcurrentLinkedQueue<Throwable> exceptions = ExecuteInMultipleThreads.getExceptions();

		logger.info("Had " + exceptions.size() + " exceptions reported.");
		// now check if we had any exceptions
		if (exceptions.size() > 0) {
			StringBuilder builder = new StringBuilder();
			for (Throwable t : exceptions) {
				builder.append("Exception: ").append(t.getClass()).append(": ").append(t.getMessage()).append("\n");
			}
			throw new IllegalStateException(builder.toString());
		}

		AtomicInteger goodReports = ExecuteInMultipleThreads.getGoodReports();

		logger.info("Executed " + goodReports.get() + " sets in " + TEST_DURATION / 1000 + " seconds.");
	}

	private final class HtmlUnitExecutorThread extends ExecuteInMultipleThreads {
		WebClient webClient;
		HtmlPage page;
		int count = 0;

		HtmlUnitExecutorThread(int threadCount, int nr, long reportStart) {
			super(threadCount, nr, reportStart);

			logger.info("Starting to run some searches for thread number " + nr);
		}

		private void initialize() {
			// shut down previous client if any
			if(webClient != null) {
				webClient.closeAllWindows();
			}

	        webClient = createWebClient();

	        try {
		        page = webClient.getPage(TestUtil.getB2BFrontendUrl() + "/Account/LogOn?ReturnUrl=%2fJourney");

		        page = loginB2B(webClient, page, "Personal Travel Inc.", "pti");
	        } catch (IOException e) {
	        	throw new RuntimeException(e);
	        }
		}

		@Override
		protected boolean runSomeWork() throws Exception {
			// re-initialize every 10 requests to see if that reduces memory leaks in htmlunit itself
			// Note: this also initializes on first call because "0 % 10 == 0"
			//if(count % 10 == 0) {

			if(count == 0) {
				initialize();
			}
			count ++;

	        page = webClient.getPage(TestUtil.getB2BFrontendUrl() + "/Account/LogOn?ReturnUrl=%2fJourney");
	        page = loginB2B(webClient, page, "Personal Travel Inc.", "pti");

			// reload
	        //page = (HtmlPage) page.refresh();

	        String xml = page.asXml();
	        if(logger.isDebugEnabled()) {
	        	logger.debug("Xml: \n" + xml);
	        }
	        assertTrue("Had: " + xml,
	        		xml.contains("showing entries 1 to "));

			return true;
		}
	}
}
