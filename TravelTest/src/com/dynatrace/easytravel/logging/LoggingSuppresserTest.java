package com.dynatrace.easytravel.logging;

import static com.dynatrace.easytravel.constants.BaseConstants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.catalina.loader.WebappClassLoader;
import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.TextUtils;


public class LoggingSuppresserTest {

	private static final String MEMORY_LEAK_LOG_REGEX = Pattern.quote("The web application [/] appears to have started a thread named [") +
			"(" +
			UEM_LOAD_THREAD + PIPE + UEM_LOAD_HOST_PLUGIN_ENABLEMENT_WATCHER_THREAD + PIPE +
			UEM_LOAD_HOST_AVAILABILITY_THREAD + ").*";

	@Test
	public void testSuppressMemoryLeakLoggingForUemLoad() {
		Logger logger = Logger.getLogger(WebappClassLoader.class.getName());
		logger.setFilter(null); // make sure not filtering is set

		LoggingVerifier verifier = new LoggingVerifier();
		logger.addHandler(verifier);

		assertThat(verifier.getLogCount(), is(0));

		new LoggingSuppresser(WebappClassLoader.class.getName()).addLogPatternToSuppress(MEMORY_LEAK_LOG_REGEX).suppressLogging();
		logger.severe("Anything could happen");
		assertThat(verifier.getLogCount(), is(1));
		logger.severe("The web application [/] appears to have started a thread named [HostAvailabilitIES-Thread-0] but has failed to stop it. This is very likely to create a memory leak.");
		assertThat(verifier.getLogCount(), is(2));

		// what happens for unusual logs
		logger.severe("");
		logger.severe((String)null);
		assertThat(verifier.getLogCount(), is(4));

		// not logged
		logger.severe(TextUtils.merge(
				"The web application [/] appears to have started a thread named [{0}-0] but has failed to stop it. This is very likely to create a memory leak.",
				BaseConstants.UEM_LOAD_HOST_PLUGIN_ENABLEMENT_WATCHER_THREAD));
		logger.severe(TextUtils.merge(
				"The web application [/] appears to have started a thread named [{0}-1] but has failed to stop it. This is very likely to create a memory leak.",
				BaseConstants.UEM_LOAD_HOST_AVAILABILITY_THREAD));
		logger.severe(TextUtils.merge(
				"The web application [/] appears to have started a thread named [{0}-2] but has failed to stop it. This is very likely to create a memory leak.",
				BaseConstants.UEM_LOAD_THREAD));
		assertThat(verifier.getLogCount(), is(4));
	}

	@Test
	public void testMemoryLeakLoggingForUemLoad() {
		Logger logger = Logger.getLogger(WebappClassLoader.class.getName());
		logger.setFilter(null); // make sure not filtering is set

		LoggingVerifier verifier = new LoggingVerifier();
		logger.addHandler(verifier);

		assertThat(verifier.getLogCount(), is(0));

		new LoggingSuppresser(WebappClassLoader.class.getName()).addLogPatternToSuppress("emptying DBPortPool to").suppressLogging();
		logger.info("emptying DBPortPool to localhost/127.0.0.1:27017 b/c of error: com.mongodb.DBPortPool gotError");
		assertThat(verifier.getLogCount(), is(0));
	}

	@Test
	public void testReset() {
		Logger logger = Logger.getLogger(WebappClassLoader.class.getName());
		logger.setFilter(null); // make sure not filtering is set

		LoggingVerifier verifier = new LoggingVerifier();
		logger.addHandler(verifier);

		assertThat(verifier.getLogCount(), is(0));

		LoggingSuppresser loggingSuppresser = new LoggingSuppresser(WebappClassLoader.class.getName());
		loggingSuppresser.addLogPatternToSuppress("emptying DBPortPool to").suppressLogging();
		logger.info("emptying DBPortPool to localhost/127.0.0.1:27017 b/c of error: com.mongodb.DBPortPool gotError");
		assertThat(verifier.getLogCount(), is(0));

		loggingSuppresser.endSuppressLogging();
		logger.info("emptying DBPortPool to localhost/127.0.0.1:27017 b/c of error: com.mongodb.DBPortPool gotError");
		assertThat(verifier.getLogCount(), is(1));


		Logger logger2 = Logger.getLogger("anyLogger");
		Filter originalFilter = new Filter() {

			@Override
			public boolean isLoggable(LogRecord record) {
				return false;
			}
		};

		logger2.setFilter(originalFilter); // make sure not filtering is set

		LoggingSuppresser logSuppresser2 = new LoggingSuppresser("anyLogger");
		logSuppresser2.addLogPatternToSuppress("emptying DBPortPool to").suppressLogging();
		assertThat(logger2.getFilter(), is(not(originalFilter)));
		logSuppresser2.endSuppressLogging();

		assertThat(logger2.getFilter(), is(originalFilter));
	}

	private static class LoggingVerifier extends Handler {
		private ConcurrentLinkedQueue<String> msgLog = new ConcurrentLinkedQueue<String>();

		@Override
		public void publish(LogRecord record) {
			msgLog.add("Message: " + String.valueOf(record.getMessage()) + "; Throwable: " + String.valueOf(record.getThrown()));
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() throws SecurityException {
		}

		int getLogCount() {
			return msgLog.size();
		}
	}

	@Test
	public void testTwoFilters() {
		Logger logger2 = Logger.getLogger("anyLogger");
		Filter originalFilter = new Filter() {

			@Override
			public boolean isLoggable(LogRecord record) {
				return false;
			}
		};

		logger2.setFilter(originalFilter); // make sure not filtering is set

		LoggingSuppresser logSuppresser = new LoggingSuppresser("anyLogger");
		logSuppresser.suppressLogging();

		LoggingSuppresser logSuppresser2 = new LoggingSuppresser("anyLogger");
		logSuppresser2.suppressLogging();

		logger2.info("some test message");

		logSuppresser.addLogPatternToSuppress("some other message");
		logger2.info("some other message");
	}
}
