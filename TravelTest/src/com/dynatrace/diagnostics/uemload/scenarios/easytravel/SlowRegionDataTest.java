package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.SampledRandomLocation;
import com.google.common.collect.Maps;

/**
 * Test for {@link SlowRegionData}
 * @author rafal.psciuk
 *
 */
public class SlowRegionDataTest {
	
	/**
	 * Test addCountriesBandwidth method.
	 */
	@Test
	public void testAddCountriesBandwidth() {
		RandomSet<Bandwidth> bandwidth = new RandomSet<Bandwidth>();		
		SlowRegionData data = new SlowRegionData.Builder().withBandwidthByCountry("testCountry", bandwidth).build();
		
		Map<String, RandomSet<Bandwidth>> result = Maps.newHashMap();
		//update bandwidth by country
		data.addCountriesBandwidth(result);
		
		assertSame(bandwidth, result.get("testCountry"));
	}
	
	/**
	 * Test addLatencyByContinent method
	 */
	@Test
	public void testAddLatencyByContinent() {
		SlowRegionData data = new SlowRegionData.Builder().withLatencyByContinent("someContinent", 8).build();
		
		Map<String, Integer> latency = Maps.newHashMap();
		data.addLatencyByContinent(latency);
		
		assertEquals("Wrong latency for continent", 8, latency.get("someContinent").intValue());
	}
	
	/**
	 * Test addLatencyByCountry method 
	 */
	@Test
	public void testAddLatencyByCountry() {
		SlowRegionData data = new SlowRegionData.Builder().withLatencyByCountry("someCountry", 32).build();
		
		Map<String, Integer> latency = Maps.newHashMap();
		data.addLatencyByCountry(latency);
		
		assertEquals("Wrong latency for continent", 32, latency.get("someCountry").intValue());
	}
	
	/**
	 * Test setCustomerLocations method
	 */
	@Test
	public void testSetCustomerLocations() {
		SlowRegionData data = new SlowRegionData.Builder().withCountryWeight("someCountry", 16).build();
		
		SampledRandomLocation locationMock = EasyMock.createNiceMock(SampledRandomLocation.class);
		locationMock.setCountryPercent("someCountry", 16);
		replay(locationMock);
		
		data.setCustomerLocations(locationMock);
		
		verify(locationMock);
	}
}
