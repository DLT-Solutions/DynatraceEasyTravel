package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.util.Collections;
import java.util.Map;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.SampledRandomLocation;
import com.google.common.collect.Maps;

/**
 * This class contains traffic parameters for one of the "SlowRegion" plugins. See WorldMapRegionFailsPlugin.
 * @author rafal.psciuk
 *
 */
public class SlowRegionData {
	private final String continent;
	private final Map<String,Integer> countryWeights; 
	private final int continentLatency;
	private final Map<String,Integer> latencyByCountry;
	private final Map<String, RandomSet<Bandwidth>> bandwidthByCountry;
		
	/**
	 * Add bandwidth distribution
	 * @param res
	 */
	public void addCountriesBandwidth(Map<String, RandomSet<Bandwidth>> res) {
		for(Map.Entry<String, RandomSet<Bandwidth>> entry: bandwidthByCountry.entrySet()) {
			res.put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Add latency for this continent
	 * @param latencyMap
	 */
	public void addLatencyByContinent(Map<String, Integer> latencyMap) {
		if(continent != null) {
			latencyMap.put(continent, continentLatency);
		}		
	}
	
	/**
	 * Add latency for countries
	 * @param latencyMap
	 */
	public void addLatencyByCountry(Map<String, Integer> latencyMap) {
		for(Map.Entry<String, Integer> entry : latencyByCountry.entrySet()) {
			latencyMap.put(entry.getKey(), entry.getValue());
		}		
	}
	
	/**
	 * Sets customer locations probability
	 * @param location
	 */
	public void setCustomerLocations(SampledRandomLocation location) {		
		for(Map.Entry<String,Integer> entry : countryWeights.entrySet()) {
			location.setCountryPercent(entry.getKey(), entry.getValue());
		}
	}
		
	public static class Builder {
		private String continent;
		private Map<String,Integer> countryWeights = Maps.newHashMap(); 
		private int latencyByContinent;
		private Map<String,Integer> latencyByCountry = Maps.newHashMap();
		private Map<String, RandomSet<Bandwidth>> bandwidthByCountry = Maps.newHashMap();;
 				
		public Builder withCountryWeight(String country, int weight) {
			countryWeights.put(country, weight);
			return this;
		}
		
		public Builder withLatencyByContinent(String continent, int latency) {
			this.continent = continent;
			this.latencyByContinent = latency;
			return this;
		}
		
		public Builder withLatencyByCountry(String country, int latency) {
			latencyByCountry.put(country, latency);
			return this;
		}
		
		public Builder withBandwidthByCountry(String country, RandomSet<Bandwidth> bandwidth) {
			bandwidthByCountry.put(country, bandwidth);
			return this;
		}
		
		public SlowRegionData build() { 
			return new SlowRegionData(this);
		}		
	}
	
	private SlowRegionData(Builder builder) {
		this.continent = builder.continent;
		this.countryWeights = Collections.unmodifiableMap(builder.countryWeights);
		this.continentLatency = builder.latencyByContinent;
		this.latencyByCountry = Collections.unmodifiableMap(builder.latencyByCountry);
		this.bandwidthByCountry = Collections.unmodifiableMap(builder.bandwidthByCountry);
	}

}
