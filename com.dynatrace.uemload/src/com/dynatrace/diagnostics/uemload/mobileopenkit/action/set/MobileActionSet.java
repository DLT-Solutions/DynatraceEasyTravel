package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set;

import com.dynatrace.diagnostics.uemload.mobileopenkit.action.EventNameMapper;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.openkit.action.ActionDefinitionSet;
import com.dynatrace.diagnostics.uemload.openkit.action.ActionSet;

import java.util.Random;

public abstract class MobileActionSet extends ActionSet<MobileDevice> {
	protected final EventNameMapper eventNameMapper;
	protected Random randomGenerator = new Random();

	public MobileActionSet(MobileDevice device) {
		super(device);
		eventNameMapper = new EventNameMapper(device.isIOS());
	}

	public void setRandomGenerator(Random randomGenerator) {
		this.randomGenerator = randomGenerator;
	}

	@Override
	protected ActionDefinitionSet build() {
		return device.isIOS() ? buildIOS() : buildAndroid();
	}

	protected abstract ActionDefinitionSet buildAndroid();

	protected abstract ActionDefinitionSet buildIOS();
}
