package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;

public class SlowRegionPluginUtilTest {

	//countries with slow dns for worldMapRegionDNSFailsAsiaPlugin
	public static final String[] SLOW_COUNTRIES_ASIA = {"Vietnam", "China", "Japan"};
	//countries with slow dns for worldMapRegionDNSFailsEuropePlugin
	public static final String[] SLOW_COUNTRIES_EUROPE = {"Italy", "France"};
	//countries with slow dns for worldMapRegionDNSFailsUnitedStatesPlugin
	public static final String[] SLOW_COUNTRIES_UNITED_STATES = {"United States"};

	//countries with slow bandwidth for all plugins
	public static final String[] SLOW_COUNTRIES;
	static {
		List<String> countries = new ArrayList<String>();
		countries.addAll(Arrays.asList(SLOW_COUNTRIES_ASIA));
		countries.addAll(Arrays.asList(SLOW_COUNTRIES_EUROPE));
		countries.addAll(Arrays.asList(SLOW_COUNTRIES_UNITED_STATES));

		SLOW_COUNTRIES = countries.toArray(new String[] {} );
	}

	// dns slowdown factor per continent
	public static final Map<String, Integer> DNS_SLOWDOWN_FACTOR_CONTINENT = Maps.newHashMap();
	static {
		DNS_SLOWDOWN_FACTOR_CONTINENT.put("Asia", 1000);
		DNS_SLOWDOWN_FACTOR_CONTINENT.put("Europe", 1000);
	}

	// dns slowdown factor per country
	public static final Map<String, Integer> DNS_SLOWDOWN_FACTOR_COUNTRY = Maps.newHashMap();
	static {
		//Asia countries
		DNS_SLOWDOWN_FACTOR_COUNTRY.put("United States", 1000);
	}

	//all plugin names that affect world map
	private static final String[] ALL_PLUGINS = {"WorldMapDNSFailsAsia", "WorldMapDNSFailsEurope", "WorldMapDNSFailsUnitedStates"};


	/**
	 * Test setting bandwidth for countries
	 */
	@Test
	public void testAddCountriesDNSSlowdownFactor() {
		Map<String, Integer> dnsSlowdownFactorMap = Maps.newHashMap();

		//check if we can pass null as enabled pulgins
		SlowRegionPluginUtil.addDNSSlowdownByCountry(null, dnsSlowdownFactorMap);
		checkCountriesDNSSlowdown(new String[] {}, dnsSlowdownFactorMap, DNS_SLOWDOWN_FACTOR_COUNTRY);

		//test countries for none plugins enabled
		SlowRegionPluginUtil.addDNSSlowdownByCountry(new String[] {}, dnsSlowdownFactorMap);
		checkCountriesDNSSlowdown(new String[] {}, dnsSlowdownFactorMap, DNS_SLOWDOWN_FACTOR_COUNTRY);

		//test countries for all plugins enabled
		dnsSlowdownFactorMap.clear();
		SlowRegionPluginUtil.addDNSSlowdownByCountry(ALL_PLUGINS, dnsSlowdownFactorMap);
		checkCountriesDNSSlowdown(new String[] {}, dnsSlowdownFactorMap, DNS_SLOWDOWN_FACTOR_COUNTRY);

		//test continents for all plugin enabled
		dnsSlowdownFactorMap.clear();
		SlowRegionPluginUtil.addDNSSlowdownByContinent(ALL_PLUGINS, dnsSlowdownFactorMap);
		checkCountriesDNSSlowdown(new String[] {}, dnsSlowdownFactorMap, DNS_SLOWDOWN_FACTOR_CONTINENT);

		//test continents for one plugin enabled
		dnsSlowdownFactorMap.clear();
		SlowRegionPluginUtil.addDNSSlowdownByContinent(new String[] {"WorldMapRegionDNSFailsEurope"}, dnsSlowdownFactorMap);
		checkCountriesDNSSlowdown(new String[] {}, dnsSlowdownFactorMap, DNS_SLOWDOWN_FACTOR_CONTINENT);

	}

	/**
	 * Utility method for checking dns slowdown factor. It is also used in {@link AbstractUEMLoadSessionTest}
	 *
	 * @param countries
	 * @param map
	 */
	public static void checkCountriesDNSSlowdown(String[] countries, Map<String, Integer> mapToTest, Map<String, Integer> comparisonMap) {
		for (String country : countries) {
			assertSame("Wrong dns slowdown factor for country" + country, comparisonMap.get(country), mapToTest.get(country));
		}
	}

}