package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.*;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitParams;
import com.dynatrace.diagnostics.uemload.openkit.visit.OpenKitVisit;
import com.dynatrace.diagnostics.uemload.openkit.visit.Visits;

public abstract class OpenKitScenario<VISITS extends Visits, VISIT extends OpenKitVisit, P extends OpenKitParams> extends UEMLoadScenario implements EasyTravelLauncherScenario {
	protected final EasyTravelHostManager hostManager = new EasyTravelHostManager();

	protected RandomSet<VISITS> visits;

	protected abstract RandomSet<VISITS> createVisitSet();

	public abstract VISIT getRandomVisit(String host, P params, ExtendedCommonUser user);

	@Override
	public EasyTravelHostManager getHostsManager() {
		return hostManager;
	}

	@Override
	protected IterableSet<Visit> createVisits() {
		return new SimpleIterableSet<>();
	}

	@Override
	public void init(boolean taggedWebRequest) {
		super.init();
		visits = createVisitSet();
	}

	@Override
	public void init() {
		init(false);
	}
}
