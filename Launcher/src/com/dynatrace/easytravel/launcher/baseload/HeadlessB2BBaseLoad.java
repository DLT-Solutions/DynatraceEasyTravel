package com.dynatrace.easytravel.launcher.baseload;

import java.util.Collection;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.scenarios.HeadlessB2BScenario;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.utils.TechnologyActivatorListener;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.State;

/**
 * 
 * @author krzysztof.sajko
 * @Date 2021.10.11
 */
public class HeadlessB2BBaseLoad extends BaseLoad implements BatchStateListener, TechnologyActivatorListener{
	
	private static final Logger LOGGER = Logger.getLogger(HeadlessB2BBaseLoad.class.getName());

	protected HeadlessB2BBaseLoad(int value, double ratio, boolean taggedWebRequest) {
		super(new HeadlessB2BScenario(), Constants.Procedures.B2B_FRONTEND_ID, value, ratio, taggedWebRequest);
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
	
	/**
	 * Allows BaseLoad to be added as a listener and therefore controll the simulator from UI elements.
	 */
	@Override
	public void notifyTechnologyStateChanged(Technology technology, boolean enabled, Collection<String> plugins,
			Collection<String> substitutes) {
		if(technology != Technology.DOTNET_20) {
			return;
		}
		if(enabled) {
			LOGGER.info("Starting simulator because b2b frontend has been turned on.");
			startSimulator();
		} else {
			LOGGER.info("Stopping simulator because b2b frontend has been turned off.");
			stopSimulator();
		}
		
	}

}
