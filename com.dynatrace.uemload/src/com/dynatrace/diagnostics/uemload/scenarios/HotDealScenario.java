package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.HotDealClientSimulator;
import com.dynatrace.diagnostics.uemload.SimpleIterableSet;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.UEMLoadScenario;
import com.dynatrace.diagnostics.uemload.Visit;

public class HotDealScenario extends UEMLoadScenario implements EasyTravelLauncherScenario {
	private final EasyTravelHostManager hostManager = new EasyTravelHostManager();
	private boolean jms;
	private boolean rmi;

	public HotDealScenario() {
	}

	@Override
	public EasyTravelHostManager getHostsManager() {
		return hostManager;
	}

	@Override
	public boolean hasHosts() {
		return hostManager.hasBackendHost();
	}

	@Override
	public Simulator createSimulator() {
		return new HotDealClientSimulator(this);
	}

	@Override
	public void init(boolean taggedWebRequest) {
		//ignore
	}

	@Override
	public void setLoad(int value) {
		// ignore
	}

	@Override
	protected IterableSet<Visit> createVisits() {
		return new SimpleIterableSet<Visit>();
	}
	
	@Override
	protected String getName(){
		return "Hot Deals";
	}

	public void setTechnologies(boolean jms, boolean rmi) {
		this.jms = jms;
		this.rmi = rmi;
	}

	public boolean useJms() {
		return jms;
	}

	public boolean useRmi() {
		return rmi;
	}
}
