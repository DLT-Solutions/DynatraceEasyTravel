package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.util.concurrent.atomic.AtomicInteger;

import com.dynatrace.diagnostics.uemload.BrowserCache;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.dtheader.DynaTraceHeader;
import com.dynatrace.diagnostics.uemload.dtheader.DynaTraceHeaderFactory;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;
import com.dynatrace.easytravel.misc.CommonUser;

public abstract class EasyTravelSession extends AbstractUEMLoadSession {

	private static AtomicInteger TOTAL_ID_COUNTER = new AtomicInteger();
	private final int id;
	private CommonUser user;
	private String destination;
	private final String host;
	private Location location;

	private BrowserCache browserCache = new BrowserCache();
	private DynaTraceHeader header;

	public EasyTravelSession(String host, Location location, CommonUser user, boolean taggedWebRequest) {
		this.id = TOTAL_ID_COUNTER.incrementAndGet();
		this.header = DynaTraceHeaderFactory.newInstance(this.id, taggedWebRequest)
				.setGeographicRegion(location.getCountryAndContinent());
		this.user = user;
		this.host = UemLoadUrlUtils.getExtendedHostUrlTrailingSlash(host);
		this.location = location;
	}

	public String getDestination() {
		if(destination == null) {
			destination = PseudoRandomJourneyDestination.get();
		}

		return destination;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public void addResource(String url, long maxAgeInSeconds) {
		browserCache.addResource(url, maxAgeInSeconds);
	}

	@Override
	public boolean isLoadOfResourceNecessary(String url) {
		return browserCache.isLoadOfResourceNecessary(url);
	}

	protected int getId() {
		return id;
	}

	@Override
	public DynaTraceHeader getHeader() {
		return header;
	}

	@Override
	public CommonUser getUser() {
		return this.user;
	}

	public Location getLocation() {
		return location;
	}

}
