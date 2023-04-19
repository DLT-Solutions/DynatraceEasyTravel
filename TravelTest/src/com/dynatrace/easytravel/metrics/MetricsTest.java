package com.dynatrace.easytravel.metrics;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Timer.Context;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.TestHelpers;


public class MetricsTest {
	private static final Logger LOGGER = LoggerFactory.make();

	@Test
	public void testMetrics() throws Exception {
		assertNotNull(Metrics.instance());
		assertNotNull(Metrics.registry());

		Metrics.incScheduleAtFixedRateCount(122);
		Metrics.incScheduleCount(123);
		Metrics.incScheduleSleepCount(124);
		Metrics.incVisitCount();

		File dir = File.createTempFile("MetricsTest", ".dir");
		assertTrue(dir.delete());
		assertTrue(dir.mkdirs());
		try {
			CsvReporter reporter = CsvReporter.forRegistry(Metrics.registry()).build(dir);
			reporter.report();

			assertTrue(dir.exists());

			File[] files = dir.listFiles();
			assertTrue("Had: " + Arrays.toString(files), files.length > 1);

			/*String contents = FileUtils.readFileToString(files[0]);
			TestHelpers.assertContains(contents, "visits", "FixedRate");*/
		} finally {
			if(dir.exists()) {
				FileUtils.forceDelete(dir);
			}
		}
	}

	@Test
	public void testMetricsTimer() throws Exception {
		new Runnable() {

			@Override
			public void run() {
				final Context context = Metrics.getTimerContext(this, "forecast-link");
				context.stop();
			}
		}.run();

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			try (PrintStream str = new PrintStream(out)) {
				ConsoleReporter reporter = ConsoleReporter.forRegistry(Metrics.registry()).outputTo(str).build();
				reporter.report();
			}

			out.flush();

			String str = new String(out.toByteArray());

			TestHelpers.assertContains(str, "forecast-link");
			LOGGER.debug("Having: " + str);
		}

	}
}
