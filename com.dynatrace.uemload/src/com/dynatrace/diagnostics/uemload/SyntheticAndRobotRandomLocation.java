package com.dynatrace.diagnostics.uemload;

import java.util.Random;

import com.dynatrace.diagnostics.uemload.Location.LocationType;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.util.DtVersionDetector;

/**
 * 
 * @author Michal.Bakula
 *
 */

public class SyntheticAndRobotRandomLocation implements RandomLocation {
	public final static SyntheticAndRobotRandomLocation SINGLETON = new SyntheticAndRobotRandomLocation();
	private static final Random r = new Random();
	
	private static final String SYNTHETIC_DUMMY_IP_ADDRESS = "1.1.1.1";
	
	private double dynatraceSyntheticLoad;
	private double syntheticLoad;
	private double robotLoad;
	
	@Override
	public Location get() {
		refreshProperties();
		double scaledDraw = Math.random() * ((DtVersionDetector.isAPM()) ? dynatraceSyntheticLoad + syntheticLoad + robotLoad
				: syntheticLoad + robotLoad);
		
		if(scaledDraw < syntheticLoad + robotLoad) {
			Location loc = new FullyRandomLocation().get();
			return (scaledDraw < syntheticLoad) ? new Location(loc, LocationType.Synthetic) : new Location(loc, LocationType.Robot);
		} else {
			return new Location(null, null, SYNTHETIC_DUMMY_IP_ADDRESS, r.nextInt(24)-11, LocationType.DynatraceSynthetic);
		}
	}

	public boolean isNextLocationRobotOrSynthetic() {
		refreshProperties();
		double draw = Math.random();
		if(DtVersionDetector.isAPM()) {
			return (draw < (dynatraceSyntheticLoad + syntheticLoad + robotLoad));			
		} else {
			return (draw < (syntheticLoad + robotLoad));
		}
	}
	
	private void refreshProperties() {
		EasyTravelConfig config = EasyTravelConfig.read();
		dynatraceSyntheticLoad = config.baseDynatraceSyntheticLoad / 100.0;
		syntheticLoad = config.baseSyntheticLoad / 100.0;
		robotLoad = config.baseRobotLoad / 100.0;
	}
}
