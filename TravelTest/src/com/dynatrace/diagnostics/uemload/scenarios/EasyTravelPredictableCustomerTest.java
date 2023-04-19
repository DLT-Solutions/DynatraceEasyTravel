package com.dynatrace.diagnostics.uemload.scenarios;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.easytravel.config.CustomerTrafficScenarioEnum;
import com.dynatrace.easytravel.config.EasyTravelConfig;

/**
 * Test for {@link EasyTravelPredictableCustomer}
 * 
 * @author rafal.psciuk
 *
 */
public class EasyTravelPredictableCustomerTest {

	/**
	 * Define ratios for each visit type (bounce, search etc.) and then get some visits and count each type.
	 */
	@Test
	public void testGetVisit() {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.customerLoadScenario = CustomerTrafficScenarioEnum.EasyTravelPredictable;
		config.predictableCustomerLoadBounce = 0.1;
		config.predictableCustomerLoadSearch = 0.2;
		config.predictableCustomerLoadAlmostConvert = 0.3;
		config.predictableCustomerLoadConvert = 0.4;

		//number of visits to test
		int baseLoad = 20;

		EasyTravelPredictableCustomer scenario = new EasyTravelPredictableCustomer("http://1.1.1.1", null, false, baseLoad);
		scenario.init();

		try {
			int bounce = 0;
			int search = 0;
			int almost = 0;
			int convert = 0;

			Location loc = new Location(null, null, null);
			//get some visits and count each type
			for(int i=0; i<baseLoad;i++){
				Visit v = scenario.getRandomVisit(loc);
				if(v instanceof EasyTravel.Bounce) bounce++;
				if(v instanceof EasyTravel.Search) search++;
				if(v instanceof EasyTravel.AlmostConvert) almost++;
				if(v instanceof EasyTravel.Convert) convert++;
			}

			//verfiy number of each visit type
			assertEquals(Math.round(config.predictableCustomerLoadBounce * baseLoad), bounce);
			assertEquals(Math.round(config.predictableCustomerLoadSearch * baseLoad), search);
			assertEquals(Math.round(config.predictableCustomerLoadAlmostConvert * baseLoad), almost);
			assertEquals(Math.round(config.predictableCustomerLoadConvert * baseLoad), convert);
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}
}
