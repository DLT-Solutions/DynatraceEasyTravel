package com.dynatrace.easytravel.launcher.baseload;

import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelB2B;
import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;


public class B2BBaseLoad extends BaseLoad implements BatchStateListener{

	private static final Logger LOGGER = Logger.getLogger(B2BBaseLoad.class.getName());

	protected B2BBaseLoad(int value, double ratio, boolean taggedWebRequest) {
		super(new EasyTravelB2B(false), Constants.Procedures.B2B_FRONTEND_ID, value, ratio, taggedWebRequest);
    }

    @Override
    protected void addHost2Scenario(String host) {
        getScenario().getHostsManager().addB2BFrontendHost(host);
    }

    @Override
    protected void removeHostFromScenario(String host) {
    	getScenario().getHostsManager().removeB2BFrontendHost(host);
    }

    @Override
    protected boolean hasHost() {
    	return getScenario().getHostsManager().hasB2BFrontendHost();
    }

    @Override
	public void notifyBatchStateChanged(Scenario scenario, State oldState,
			State newState) {
    	//cwpl-rpsciuk: APM-8129, stop simulation if scenario is stopped. This is needed in case when B2B frontend is on IIS and cannot be stopped.
    	if (State.STOPPED == newState) { //scenario is stooped, must stop simulation
    		LOGGER.warning("Stopping B2B frontend simulation because scenario is stopped.");
    		stopSimulator();
    	} else if (State.OPERATING == newState) {
    		startSimulator();
    	}
	}

}