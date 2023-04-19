package com.dynatrace.easytravel.config;

import com.dynatrace.easytravel.ipc.SocketUtils;


public abstract class TomcatResourceReservation extends ResourceReservation implements Comparable<TomcatResourceReservation> {
	private final int shutdownPort;
	private final int ajpPort;
	private final String contextRoot;
	private final String webappBase;
	private final int port;
	private String routePrefix;

	public TomcatResourceReservation(int port, int shutdownPort, int ajpPort, String contextRoot, String webappBase) {
		this.port = port;
		this.shutdownPort = shutdownPort;
		this.ajpPort = ajpPort;
		this.contextRoot = contextRoot;
		this.webappBase = webappBase;
	}

	public void setRoutePrefix(String routePrefix) {
		this.routePrefix = routePrefix;
	}

	public String getRoutePrefix() {
		return routePrefix;
	}

	public int getPort() {
		return port;
	}

	public int getShutdownPort() {
		return shutdownPort;
	}

	public String getWebappBase() {
		return webappBase;
	}

	@Override
	public void release() {
		if (isReleased()) {
			return;
		}

		SocketUtils.freePort(port);
		SocketUtils.freePort(shutdownPort);
		if (ajpPort != 0) {
			SocketUtils.freePort(ajpPort);
		}

		setReleased();
	}

	@Override
	public String toString() {
		return "[port=" + port + ", shutdownport=" + shutdownPort + ", base=" + webappBase + "]";
	}

	public int getAjpPort() {
		return ajpPort;
	}

	public String getContextRoot() {
		return contextRoot;
	}

	/*
	 * compares the used port to the port used by the given CustomerFromentReservation
	 * instance
	 */
	@Override
	public int compareTo(TomcatResourceReservation o) {
		if(o == null) {
			return -1;
		}

		return (getPort() < o.getPort() ? -1 : (getPort() == o.getPort() ? 0 : 1));
	}
}
