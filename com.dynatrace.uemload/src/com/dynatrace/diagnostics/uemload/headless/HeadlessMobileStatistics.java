/**
 *
 */
package com.dynatrace.diagnostics.uemload.headless;

import com.dynatrace.easytravel.metrics.Metrics;

/**
 * @author tomasz.wieremjewicz
 * @date 25 sty 2019
 *
 */
public class HeadlessMobileStatistics extends HeadlessStatistics {
	@Override
	protected void incMetricStarted() {
		Metrics.incHeadlessMobileStarted();
	}

	@Override
	protected void incMetricCompleted() {
		Metrics.incHeadlessMobileCompleted();
	}

	@Override
	protected void incMetricSkipped() {
		Metrics.incHeadlessMobileSkipped();
	}

	@Override
	protected void incMetricException() {
		Metrics.incHeadlessMobileException();
	}

	@Override
	protected String getPartialName() {
		return "MOBILE";
	}
}
