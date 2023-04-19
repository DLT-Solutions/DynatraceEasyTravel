package com.dynatrace.diagnostics.uemload.scenarios;


import java.util.HashMap;
import java.util.Map;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.BrowserScenario;
import com.dynatrace.diagnostics.uemload.DefaultVisit;
import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.Pageview;
import com.dynatrace.diagnostics.uemload.RandomLocation;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.SampledRandomLocation;
import com.dynatrace.diagnostics.uemload.Visit;


public class AjaxWorldPageviews extends BrowserScenario {

	private static final String[] URLS = {
		"/tests/index.htm",
		"/tests/simple/p1.htm",
		"/tests/simple/p2.htm",
		"/tests/simple/p3.htm"
	};

	private static final String[] TITLES = {
		"End User Monitoring Test Cases",
		"Test Page 1",
		"Test Page 2",
		"Test Page 3"
	};

	private Action[] actions;

	public AjaxWorldPageviews(String host, int numPageViews, int numResourcesPerPage, int avgPageLoadTime) {
		actions = new Action[numPageViews];
		for(int i = 0; i < numPageViews; i++) {
			int page = i % URLS.length;
			actions[i] = createPage(host, URLS[page], TITLES[page], numResourcesPerPage, avgPageLoadTime + 100 * i);
		}
	}

	@Override
	protected IterableSet<Visit> createVisits() {
		RandomSet<Visit> res = new RandomSet<Visit>();
		res.add(new DefaultVisit(actions), 100);
		return res;
	}

	@Override
	protected RandomLocation createRandomLocations() {
		SampledRandomLocation res = new SampledRandomLocation();
//		res.setCountryPercent("Germany", 70);
//		res.setCountryPercent("Austria", 10);
//		res.setCountryPercent("Switzerland", 5);
		return res;
	}

	@Override
	protected Map<String, Double> createHardwareSpeedByContinent() {
		Map<String, Double> res = new HashMap<String, Double>();
		res.put(ASIA, 0.5);
		res.put(AFRICA, 0.25);
		return res;
	}

	@Override
	protected Map<String, Double> createHardwareSpeedByCountry() {
		Map<String, Double> res = new HashMap<String, Double>();
		res.put("China", 0.3);
		return res;
	}

	private static Pageview createPage(String host, String url, String title, int numResourcesPerPage, int avgPageLoadTime) {
		return new Pageview(
			host + url,
			title,
			getResources(host, numResourcesPerPage),
			avgPageLoadTime
		);
	}

	private static String[] getResources(String host, int count) {
		String[] res = new String[count];
		for(int i = 0; i < count; i++) {
			if(i % 3 == 0) {
				res[i] = host + "/tests/resources/1px.jpg";
			} else if(i % 3 == 1) {
				res[i] = host + "/tests/resources/1px.png";
			} else {
				res[i] = host + "/tests/resources/1px.gif";
			}
		}
		return res;
	}

}
