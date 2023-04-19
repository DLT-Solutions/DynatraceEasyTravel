/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BrowserScenario.java
 * @date: 20.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.mobile;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.BandwidthDistribution.BandwidthDistributionBuilder;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.UEMLoadScenario;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.Visit;


/**
 *
 * @author peter.lang
 */
public abstract class MobileAppScenario extends UEMLoadScenario {

	public MobileAppScenario() {
		super();
	}

	@Override
	final public Simulator createSimulator() {
		return new MobileNativeSimulator(this);
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.diagnostics.uemload.Scenario#createVisits()
	 */
	@Override
	protected RandomSet<Visit> createVisits() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.diagnostics.uemload.Scenario#createBandwidth()
	 */
	@Override
	protected RandomSet<Bandwidth> createBandwidth() {
		return createMobileBandwidthDistribution();
	}

	public static RandomSet<Bandwidth> createMobileBandwidthDistribution() {
		BandwidthDistributionBuilder b = new BandwidthDistributionBuilder();
		b.use(Bandwidth.DIALUP, 3).
			use(Bandwidth.DSL_LOW, 8);
		return b.build();
	}
}
