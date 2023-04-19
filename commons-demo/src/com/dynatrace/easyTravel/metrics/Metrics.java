package com.dynatrace.easytravel.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;

/**
 * Simple class to collect some statistics to enable us to
 * provide them via some means, e.g. output in the UI or retrieval
 * via REST
 *
 * @author cwat-dstadler
 */
public class Metrics {
	private static Metrics instance;

	private final MetricRegistry registry;
    private CsvReporter reporter;
    private final boolean enableReporter = EasyTravelConfig.read().enableMetricsReporter;

	public Metrics() {
		registry = new MetricRegistry();

        if (enableReporter) {
            // create reporter to store measurements in a tmp directory
            reporter = CsvReporter.forRegistry(registry)
                    .formatFor(Locale.UK)
                    .convertRatesTo(TimeUnit.MINUTES)
                    .convertDurationsTo(TimeUnit.SECONDS)
                    .build(Directories.getTempDir());

            // record measurements every 15min
            reporter.start(15, TimeUnit.MINUTES);
        }

		// also do a simple measure of the uptime of the whole application by comparing start-time with current time
		// in a Gauge
		final long start = System.currentTimeMillis();
		registry.register("uptime", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return System.currentTimeMillis() - start;
            }
        });

		// add some Metrics provided by the library
		registry.registerAll(new GarbageCollectorMetricSet());
		registry.register("filedescriptors", new FileDescriptorRatioGauge());
		registry.registerAll(new MemoryUsageGaugeSet());
		registry.registerAll(new ThreadStatesGaugeSet());
		// requires MBeanServer and Java 7: registry.registerAll(new BufferPoolMetricSet());
	}

	public static MetricRegistry registry() {
		return instance().registry;
	}

	public static Metrics instance() {
		if(instance == null) {
			instance = new Metrics();
		}
		return instance;
	}

	public static void incVisitCount() {
		instance().registry.meter("visits").mark();
	}

	public static void incHeadlessStarted() {
		instance().registry.meter("visits.headless.started").mark();
	}

	public static void incHeadlessCompleted() {
		instance().registry.meter("visits.headless.completed").mark();
	}

	public static void incHeadlessSkipped() {
		instance().registry.meter("visits.headless.skipped").mark();
	}

	public static void incHeadlessException() {
		instance().registry.meter("visits.headless.exception").mark();
	}

	public static void incHeadlessMobileStarted() {
		instance().registry.meter("visits.headless.mobile.started").mark();
	}

	public static void incHeadlessMobileCompleted() {
		instance().registry.meter("visits.headless.mobile.completed").mark();
	}

	public static void incHeadlessMobileSkipped() {
		instance().registry.meter("visits.headless.mobile.skipped").mark();
	}

	public static void incHeadlessMobileException() {
		instance().registry.meter("visits.headless.mobile.exception").mark();
	}

	public static void incScheduleCount(int activeCount) {
		instance().registry.meter("scheduler.schedule").mark();
		instance().registry.histogram("scheduler.active").update(activeCount);
	}

	public static void incScheduleAtFixedRateCount(int activeCount) {
		instance().registry.meter("scheduler.scheduleAtFixdRate").mark();
		instance().registry.histogram("scheduler.active").update(activeCount);
	}

	public static void incScheduleSleepCount(int activeCount) {
		instance().registry.meter("scheduler.sleep").mark();
		instance().registry.histogram("scheduler.active").update(activeCount);
	}

    public static Timer.Context getTimerContext(Object object, String methodName) {
        return instance().registry.timer(name(object.getClass(), methodName)).time();
    }

    public static Timer.Context getTimerContext(Class<?> c, String methodName) {
    	return instance().registry.timer(name(c, methodName)).time();
    }

}
