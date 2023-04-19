package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.dynatrace.diagnostics.uemload.FixedLocation;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.easytravel.config.CustomerTrafficScenarioEnum;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.misc.CommonUser;
import com.google.common.collect.Sets;

/**
 * @author Rafal Psciuk
 * 
 * {@link BasicCostumerSession} test.
 *
 */
public class BasicCustomerSessionTest {
			
	/**
	 * Mock class for testing abstract {@link BasicCostumerSession} class
	 *
	 */
	class MockSession extends BasicCostumerSession {

		public MockSession(String host, Location location,
				boolean taggedWebRequest) {
			super(host, new CommonUser("user", "user"), location, taggedWebRequest);
		}

		@Override
		public void setResponseHtml(String html) {}
		
	}
	
	/**
	 * Test if {@link #getDestination()} method returns different values
	 */
	@Test
	public void testGetRandomDestination() {
		//disable fixed base load
		EasyTravelConfig config = EasyTravelConfig.read();
		config.customerLoadScenario = CustomerTrafficScenarioEnum.EasyTravel;

		try{
			Set<String> res = getDestinations();
			//should be multiple destinations
			assertTrue("There should be only one destinations", res.size()>1);
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}
	
	/**
	 * Test if {@link #getDestination()} method returns always the same value when customerTrafficScenario=customerFixedBaseLoad 
	 * or customerPredictableBaseLoad
	 */
	@Test
	public void testGetFixedScenarioDestination() {
		testGetFixedScenarioDestination(CustomerTrafficScenarioEnum.EasyTravelFixed);
		testGetFixedScenarioDestination(CustomerTrafficScenarioEnum.EasyTravelPredictable);
	}	
	
	/**
	 * Test {@link #getDestination()} for different scenarios. Assume that one destination is returned for multiple sessions.
	 * @param scenario
	 */
	private void testGetFixedScenarioDestination(CustomerTrafficScenarioEnum scenario) {
		//enable fixed base load
		EasyTravelConfig config = EasyTravelConfig.read();
		config.customerLoadScenario = CustomerTrafficScenarioEnum.EasyTravelFixed;

		try {		
			Set<String> res = getDestinations();
			//should be only one destination
			assertTrue("There should be only one destination for traffic scenario " + scenario.toString(), res.size()==1);
		} finally {
			EasyTravelConfig.resetSingleton();
		}				
	}
	
	/**
	 * Get destinations from multiple session objects.
	 * @return
	 */
	private Set<String> getDestinations() {
		//get multiple destinations
		Set<String> res = Sets.newHashSet();
		for(int i=0; i<10; i++){
			BasicCostumerSession session = new MockSession("http://1.1.1.1", new FixedLocation().get(), true);
			res.add(session.getDestination());
		}		
		return res;
	}
}
