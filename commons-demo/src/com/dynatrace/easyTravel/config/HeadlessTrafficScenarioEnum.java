package com.dynatrace.easytravel.config;

public enum HeadlessTrafficScenarioEnum {
	/**
	 * Standard random load. Default value.
	 * uses HeadlessCustomerScenario class to generate visits to run
	 */
	StandardHeadlessTraffic,

	/**
	 * Traffic designed to simulate overload prevention
	 * uses HeadlessOverloadPreventionScenario class to generate visits to run
	 */
	OverloadDetectionHeadlessTraffic,
}
