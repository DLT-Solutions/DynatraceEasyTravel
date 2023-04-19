package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.UEMLoadScenario;
import com.dynatrace.diagnostics.uemload.Visit;

public abstract class HeadlessScenario extends UEMLoadScenario implements EasyTravelLauncherScenario {
	private final EasyTravelHostManager hostManager = new EasyTravelHostManager();
	private int load;
	public HeadlessScenario() { /* nothing to do here */ }

	@Override
	public EasyTravelHostManager getHostsManager() { return hostManager; }

	@Override
	public void init(boolean taggedWebRequest) { super.init(); }

	@Override
	public void setLoad(int value) {
		load = value;
	}
	@Override
	public int getLoad( ) {
		return load;
	}
	
	@Override
	protected abstract IterableSet<Visit> createRushHourVisits();
	
	@Override
	protected abstract IterableSet<Visit> createAnonymousVisits();

	@Override
	protected abstract String getName();

}
