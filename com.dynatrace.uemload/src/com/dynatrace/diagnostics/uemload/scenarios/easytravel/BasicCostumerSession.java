/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: BasicCostumerSession.java
 * @date: 20.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravel;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelFixedCustomer;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelPredictableCustomer;
import com.dynatrace.easytravel.config.CustomerTrafficScenarioEnum;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.misc.CommonUser;


/**
 *
 * @author peter.lang
 */
public abstract class BasicCostumerSession extends EasyTravelSession {

	private String destination;

	public BasicCostumerSession(String host, CommonUser user, Location location, boolean taggedWebRequest) {
		super(host, location, user, taggedWebRequest);
	}
	
	@Override
	public String getDestination() {
		if(destination == null) {
			if(useFixedDestination()) {
				destination = "Paris";
			} else {
				destination = PseudoRandomJourneyDestination.get();
			}
		}
		return destination;
	}
	
	/**
	 * @return true if fixed destination should be used. Used for example in the {@link EasyTravelFixedCustomer} 
	 * and {@link EasyTravelPredictableCustomer} load scenarios.  
	 */
	private boolean useFixedDestination(){
		CustomerTrafficScenarioEnum scenario = EasyTravelConfig.read().getCustomerTrafficScenario();
		return EasyTravel.isUemCorrelationTestingMode 
				||  scenario == CustomerTrafficScenarioEnum.EasyTravelFixed
				|| scenario == CustomerTrafficScenarioEnum.EasyTravelPredictable;
		
	}

	public String getNewDestination() {
		destination = null;
		return getDestination();
	}

}
