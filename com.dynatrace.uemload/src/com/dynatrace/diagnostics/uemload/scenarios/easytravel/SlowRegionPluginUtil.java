package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.util.Map;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.SampledRandomLocation;
import com.dynatrace.diagnostics.uemload.BandwidthDistribution.BandwidthDistributionBuilder;
import com.google.common.collect.Maps;


/**
 * Class containing traffic parameters for 'SlowRegion' plugins.
 * @author rafal.psciuk
 *
 */
public class SlowRegionPluginUtil {

	private static final Map<String, SlowRegionData> pluginData = Maps.newHashMap();
	private static final Map<String, Map<String, Integer>> dnsSlowdownDataForContinents = Maps.newHashMap();
	private static final Map<String, Map<String, Integer>> dnsSlowdownDataForCountries = Maps.newHashMap();

	/**
	 * Default bandwidth distribution for slow countries
	 */
	public static final RandomSet<Bandwidth> SLOW_BANDWIDTH = new BandwidthDistributionBuilder()
		.use(Bandwidth.DSL_LOW, 25)
		.use(Bandwidth.DSL_MED, 3)
		.use(Bandwidth.DSL_HIGH, 1)
		.build();

	static {

		Map<String, Integer> germanyMap = Maps.newHashMap();
		Map<String, Integer> asiaMap = Maps.newHashMap();
		Map<String, Integer> europeMap = Maps.newHashMap();

		germanyMap.put("United States", 1000);
		asiaMap.put("Asia", 1000);
		europeMap.put("Europe", 1000);

		dnsSlowdownDataForCountries.put("WorldMapDNSFailsUnitedStates", germanyMap);
		dnsSlowdownDataForContinents.put("WorldMapDNSFailsAsia", asiaMap);
		dnsSlowdownDataForContinents.put("WorldMapDNSFailsEurope", europeMap);
	}

	/**
	 * Method adds bandwidth distribution for countries defined in enabled plugins
	 * @param enabledPlugins
	 * @param res
	 */
	public static void addCountriesBandwidth(String[] enabledPlugins, Map<String, RandomSet<Bandwidth>> res) {
		if(enabledPlugins != null) {
			for(String plugin : enabledPlugins){
				SlowRegionData data = pluginData.get(plugin);
				if(data != null){
					data.addCountriesBandwidth(res);
				}
			}
		}
	}

	/**
	 * Method adds latency for continents defined in enabled plugins
	 * @param enabledPlugins
	 * @param latencyMap
	 */
	public static void addLatencyByContinent(String[] enabledPlugins, Map<String, Integer> latencyMap) {
		if(enabledPlugins != null) {
			for(String plugin : enabledPlugins){
				SlowRegionData data = pluginData.get(plugin);
				if(data != null){
					data.addLatencyByContinent(latencyMap);
				}
			}
		}
	}

	/**
	 * Method adds latency for countries defined in enabled plugins
	 * @param enabledPlugins
	 * @param latencyMap
	 */
	public static void addLatencyByCountry(String[] enabledPlugins, Map<String, Integer> latencyMap) {
		if(enabledPlugins != null) {
			for(String plugin : enabledPlugins){
				SlowRegionData data = pluginData.get(plugin);
				if(data != null){
					data.addLatencyByCountry(latencyMap);
				}
			}
		}
	}

	/**
	 * Method sets probability that given location will appear in the generated traffic for countries defined in enabled plugins
	 * @param enabledPlugins
	 * @param location
	 */
	public static void setCustomerLocations(String[] enabledPlugins, SampledRandomLocation location) {
		if(enabledPlugins != null) {
			for(String plugin : enabledPlugins){
				SlowRegionData data = pluginData.get(plugin);
				if(data != null){
					data.setCustomerLocations(location);
				}
			}
		}
	}

	/**
	 * This methods adds dns slowdown factor for continents defined in enabled plugins
	 * @param enabledPlugins
	 * @param map
	 */
	public static void addDNSSlowdownByContinent(String[] enabledPlugins, Map<String, Integer> map) {
		addDNSSlowdownByRegion(enabledPlugins, map, dnsSlowdownDataForContinents);
	}

	/**
	 * This method adds dns slowdown factor for countries defined in enabled plugins
	 * @param enabledPlugins
	 * @param map
	 */
	public static void addDNSSlowdownByCountry(String[] enabledPlugins, Map<String, Integer> map) {
		addDNSSlowdownByRegion(enabledPlugins, map, dnsSlowdownDataForCountries);
	}

	private static void addDNSSlowdownByRegion(String[] enabledPlugins, Map<String, Integer> map, Map<String, Map<String, Integer>> dnsSlowdownDataForRegion) {
		if(enabledPlugins != null) {
			for(String plugin : enabledPlugins){
				Map<String, Integer> data = dnsSlowdownDataForRegion.get(plugin);
				if(data != null){
					map.putAll(data);
				}
			}
		}
	}

	/**
	 * Empty constructor. This class should not be instantiated.
	 */
	private SlowRegionPluginUtil() {}
}
