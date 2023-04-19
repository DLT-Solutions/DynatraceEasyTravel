package com.dynatrace.diagnostics.uemload.headless;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.Location;

/**
 * @author Rafal.Psciuk
 * @date 2018-11-05
 */
public class HeadlessVisitConfig {
	private final Location location;
	private final String ipAddress;
	private final String userAgent;
	private final String browserWindowSize;
	private final long bandWidthLimit;
	private final ExtendedCommonUser user;

	public HeadlessVisitConfig(ExtendedCommonUser user) {
		this.user = user;
		this.location = user.getLocation();
		this.ipAddress = location.getIp();
		this.userAgent = user.getRandomDesktopBrowser().getUserAgent();
		this.bandWidthLimit = user.getBandwidth().getBandwidthInBytesPerSecond();
		this.browserWindowSize = user.getDesktopBrowserWindowSize().createChromiumArgumentString();
	}

	public Location getLocation() {
		return location;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getBrowserWindowSize() {
		return browserWindowSize;
	}

	public long getBandWidthLimit() {
		return bandWidthLimit;
	}
	
	public boolean isNewVisitor() {
		return user.getVisitorInfo().isNewVisitor();
	}
	
	public String getVisitorId() {
		return user.getVisitorInfo().getVisitorID();
	}

	public ExtendedCommonUser getUser() {
		return user;
	}
}
