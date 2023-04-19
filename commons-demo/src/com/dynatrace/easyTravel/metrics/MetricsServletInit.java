package com.dynatrace.easytravel.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import com.codahale.metrics.servlets.MetricsServlet.ContextListener;

/**
 * Simple initializer which provides the Metrics registry to the {@link MetricsServlet}
 *
 * @author cwat-dstadler
 */
public class MetricsServletInit extends ContextListener {
    @Override
	protected MetricRegistry getMetricRegistry() {
    	return Metrics.registry();
    }
}
