package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.util.Map;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.SampledRandomLocation;
import com.dynatrace.diagnostics.uemload.dcrum.DCRumDataRecord;
import com.dynatrace.diagnostics.uemload.dcrum.DCRumdDataRecordsHolder;
import com.dynatrace.diagnostics.uemload.http.base.ResponseHeaders;
import com.dynatrace.easytravel.constants.BaseConstants;


public abstract class AbstractUEMLoadSession implements UEMLoadSession {

	private DCRumdDataRecordsHolder dcRum = DCRumdDataRecordsHolder.getInstance();
	private DCRumDataRecord dataRecord;
	
	@Override
	public void registerPageLoad(String url, String ip) {
		if (PluginChangeMonitor.isPluginEnabled(BaseConstants.Plugins.DC_RUM_EMULATOR)) {
			dataRecord = dcRum.addEntry(url, ip, getUser().getName());
		}
	}

	@Override
	public void setResponseHeaders(ResponseHeaders responseHeaders) {
		if (dataRecord != null) {
			dataRecord.filterPurePath(responseHeaders);
		}
	}

	/**
	 * Method adds bandwidth distribution for countries defined in enabled plugins
	 * @param res
	 */
	public static void addCountriesBandwidth(Map<String, RandomSet<Bandwidth>> res) {
		String[] names = PluginChangeMonitor.enabledPluginNames.get();
		SlowRegionPluginUtil.addCountriesBandwidth(names, res);
	}


	/**
	 * Method adds latency for continents defined in enabled plugins
	 * @param latencyMap
	 */
	public static void addLatencyByContinent(Map<String, Integer> latencyMap) {
		String[] names = PluginChangeMonitor.enabledPluginNames.get();
		SlowRegionPluginUtil.addLatencyByContinent(names, latencyMap);

	}


	/**
	 * Method adds latency for countries defined in enabled plugins
	 * @param latencyMap
	 */
	public static void addLatencyByCountry(Map<String, Integer> latencyMap) {
		String[] names = PluginChangeMonitor.enabledPluginNames.get();
		SlowRegionPluginUtil.addLatencyByCountry(names, latencyMap);
	}



	/**
	 * Method sets probability that given location will appear in the generated traffic for countries defined in enabled plugins
	 * @param location
	 */
	public static void setCustomerLocations(SampledRandomLocation location) {
		String[] names = PluginChangeMonitor.enabledPluginNames.get();
		SlowRegionPluginUtil.setCustomerLocations(names, location);
	}


	/**
	 * Adds a DNS slowdown factor to the given map. It should simulate that a certain continent has a slowdown
	 * because the DNS lookup is slow.
	 * @param map
	 */
	public static void addDNSSlowdownByContinent(Map<String, Integer> map) {
		String[] names = PluginChangeMonitor.enabledPluginNames.get();
		SlowRegionPluginUtil.addDNSSlowdownByContinent(names, map);
	}


	/**
	 * Adds a DNS slowdown factor to the given map. It should simulate that a certain country has a slowdown
	 * because the DNS lookup is slow.
	 * @param map
	 */
	public static void addDNSSlowdownByCountry(Map<String, Integer> map) {
		String[] names = PluginChangeMonitor.enabledPluginNames.get();
		SlowRegionPluginUtil.addDNSSlowdownByCountry(names, map);
	}
}
