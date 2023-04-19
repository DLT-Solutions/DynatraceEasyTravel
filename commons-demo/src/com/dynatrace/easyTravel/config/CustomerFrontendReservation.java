package com.dynatrace.easytravel.config;

import java.io.IOException;

import com.dynatrace.easytravel.ipc.SocketUtils;


public class CustomerFrontendReservation extends TomcatResourceReservation {

	public CustomerFrontendReservation(int port, int shutdownPort, int ajpPort, String contextRoot, String webappBase) {
		super(port, shutdownPort, ajpPort, contextRoot, webappBase);
	}

	/**
	 *
	 * @return
	 * @throws IOException if some necessary IO resources (ports,...) are not available
	 * @author martin.wurzinger
	 */
	public static CustomerFrontendReservation reserveResources() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();

		// use port-range if specified in config
		final int port = SocketUtils.reserveNextFreePort(config.frontendPortRangeStart, config.frontendPortRangeEnd, null);
		// for shutdown-port we use "localhost" as hostname as Tomcat also uses this to bind the socket!
		final int shutdownPort = SocketUtils.reserveNextFreePort(config.frontendShutdownPortRangeStart,
				config.frontendShutdownPortRangeEnd, "localhost");

		// don't reserve an ajpPort if not configure
		final int ajpPort;
		if (config.frontendAjpPortRangeStart == 0 || config.frontendAjpPortRangeEnd == 0) {
			ajpPort = 0;
		} else {
			ajpPort = SocketUtils.reserveNextFreePort(config.frontendAjpPortRangeStart, config.frontendAjpPortRangeEnd, null);
		}
		String webappBase = config.webappBase;
		String contextRoot = config.frontendContextRoot;

		return new CustomerFrontendReservation(port, shutdownPort, ajpPort, contextRoot, webappBase);
	}
}
