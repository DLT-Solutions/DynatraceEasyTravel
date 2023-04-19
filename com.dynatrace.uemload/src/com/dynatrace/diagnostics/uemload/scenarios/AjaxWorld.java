package com.dynatrace.diagnostics.uemload.scenarios;

import com.dynatrace.diagnostics.uemload.BrowserScenario;
import com.dynatrace.diagnostics.uemload.DefaultVisit;
import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.Pageview;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.Visit;


public class AjaxWorld extends BrowserScenario {

	private static final String[] RESOURCES = new String[] {
		"/tests/resources/1px.jpg",
		"/tests/resources/1px.png",
		"/tests/resources/1px.gif"
	};

	private final String host;

	public AjaxWorld(String host) {
		this.host = host;
	}

	@Override
	protected IterableSet<Visit> createVisits() {
		RandomSet<Visit> res = new RandomSet<Visit>();
		Visit bounce = new DefaultVisit(
			createIndex(host)
		);
		Visit visit = new DefaultVisit(
			createIndex(host),
			createP1(host),
			createP2(host)
		);
		Visit convert = new DefaultVisit(
			createIndex(host),
			createP1(host),
			createP2(host),
			createP3(host)
		);
		res.add(bounce, 35);
		res.add(visit, 60);
		res.add(convert, 5);
		return res;
	}

	private static Pageview createIndex(String host) {
		return new Pageview(
			host + "/tests/index.htm",
			"End User Monitoring Test Cases",
			getResources(host, RESOURCES, 5),
			2000
		);
	}

	private static Pageview createP1(String host) {
		return new Pageview(
			host + "/tests/simple/p1.htm",
			"Test Page 1",
			getResources(host, RESOURCES, 3),
			600
		);
	}

	private static Pageview createP2(String host) {
		return new Pageview(
			host + "/tests/simple/p2.htm",
			"Test Page 2",
			getResources(host, RESOURCES, 4),
			1000
		);
	}

	private static Pageview createP3(String host) {
		return new Pageview(
			host + "/tests/simple/p3.htm",
			"Test Page 3",
			getResources(host, RESOURCES, 8),
			4600
		);
	}

}
