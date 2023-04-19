package com.dynatrace.diagnostics.uemload.scenarios;

import java.util.HashMap;
import java.util.Map;

import com.dynatrace.diagnostics.uemload.BrowserScenario;
import com.dynatrace.diagnostics.uemload.DefaultVisit;
import com.dynatrace.diagnostics.uemload.Pageview;
import com.dynatrace.diagnostics.uemload.RandomLocation;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.SampledRandomLocation;
import com.dynatrace.diagnostics.uemload.Visit;

public class SinglePage extends BrowserScenario {

	private final String url;
	private final int pageLoadTime;

	public SinglePage(String url, int pageLoadTime) {
		this.url = url;
		this.pageLoadTime = pageLoadTime;
	}

	@Override
	protected RandomSet<Visit> createVisits() {
		RandomSet<Visit> res = new RandomSet<Visit>();
		res.add(new DefaultVisit(new Pageview(url, url, null, pageLoadTime)), 100);
		return res;
	}

	@Override
	protected RandomLocation createRandomLocations() {
		return new SampledRandomLocation();
	}

	@Override
	protected Map<String, Double> createHardwareSpeedByContinent() {
		Map<String, Double> res = new HashMap<String, Double>();
		res.put(ASIA, 0.5);
		res.put(AFRICA, 0.3);
		return res;
	}

	@Override
	protected Map<String, Double> createHardwareSpeedByCountry() {
		Map<String, Double> res = new HashMap<String, Double>();
		res.put("China", 0.4);
		return res;
	}

}
