package com.dynatrace.easytravel.launcher.httpd;

import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.easytravel.constants.BaseConstants.UrlType;

/**
 * Configuration object for algorithms within {@link HttpdConfSetup}
 * @author cwat-rpilz
 *
 */
public final class ApacheConf implements RouteGenerator {

	private boolean isStartPhp = false;
	private boolean isApacheSlowDown = false;
	private int[] extraListenPorts = null;
	private UrlType[] relevantUrlTypes = new UrlType[] { UrlType.APACHE_JAVA_FRONTEND, UrlType.APACHE_B2B_FRONTEND, UrlType.APACHE_PROXY, UrlType.APACHE_ANGULAR_FRONTEND };
	private RouteGenerator routeGenerator = new DefaultRouteGen();

	public void setRouteGen(RouteGenerator routeGenerator) {
		this.routeGenerator = routeGenerator;
	}

	@Override
	public String genRoute(String host, int ajpPort) {
		return routeGenerator.genRoute(host, ajpPort);
	}

	public void setStartPhp(boolean isStartPhp) {
		this.isStartPhp = isStartPhp;
	}

	public boolean isStartPhp() {
		return isStartPhp;
	}

	public boolean isApacheSlowDown() {
		return isApacheSlowDown;
	}

	public void setApacheSlowDown(boolean apacheSlowDown) {
		isApacheSlowDown = apacheSlowDown;
	}

	public void setExtraListenPorts(int[] extraListenPorts) {
		this.extraListenPorts = ArrayUtils.clone(extraListenPorts);
	}

	public int[] getExtraListenPorts() {
		return extraListenPorts;
	}

	public UrlType[] getRelevantUrlTypes() {
		return relevantUrlTypes;
	}

	public void setRelevantUrlTypes(UrlType[] relevantUrlTypes) {
		this.relevantUrlTypes = ArrayUtils.clone(relevantUrlTypes);
	}

}
