/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ActionRunner.java
 * @date: 23.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload;

import java.io.Closeable;

import com.dynatrace.diagnostics.uemload.http.base.UemLoadHttpClient;

/**
 * Simple abstract base class to generalize Browsers or MobileDevices for
 * executing any actions with applied bandwidth to slow down httpClient.
 *
 * @author peter.lang
 */
public abstract class ActionExecutor implements Closeable {

	protected final UemLoadHttpClient http;
	protected final int latency;
	protected final Bandwidth bandwidth;
	protected final Location location;
	protected final VisitorId visitorId;

	public ActionExecutor(Location location, int latency, Bandwidth bandwidth, BrowserType browserType, String userAgent) {
		this.latency = latency;
		this.bandwidth = bandwidth;
		this.http = new UemLoadHttpClient(bandwidth, browserType);
		this.http.setUserAgent(userAgent);
		if (location.getIp() != null) {
			this.http.setClientIP(location.getIp());
		}
		this.location = location;
		this.visitorId = null;
	}
	
	public ActionExecutor(Location location, int latency, Bandwidth bandwidth, BrowserType browserType, String userAgent, VisitorId visitorId) {
		this.latency = latency;
		this.bandwidth = bandwidth;
		this.http = new UemLoadHttpClient(bandwidth, browserType, visitorId);
		this.http.setUserAgent(userAgent);
		if (location.getIp() != null) {
			this.http.setClientIP(location.getIp());
		}
		this.location = location;
		this.visitorId = visitorId;
	}

	public Location getLocation() {
		return location;
	}

	public int getLatency() {
		return latency;
	}

	public Bandwidth getBandwidth() {
		return bandwidth;
	}
	
	public VisitorId getVisitorId() {
		return visitorId;
	}
	
	public UemLoadHttpClient getUemLoadHttpClient() {
		return http;
	}

	@Override
	public void close() {
		http.close();
	}
}
