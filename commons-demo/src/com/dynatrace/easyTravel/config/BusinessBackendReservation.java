package com.dynatrace.easytravel.config;

import java.io.IOException;

import com.dynatrace.easytravel.ipc.SocketUtils;


public class BusinessBackendReservation extends TomcatResourceReservation {
	public BusinessBackendReservation(int port, int shutdownPort, int ajpPort, String contextRoot, String webappBase) {
		super(port, shutdownPort, ajpPort, contextRoot, webappBase);
	}

	/**
	 *
	 * @return
	 * @throws IOException if some necessary IO resources (ports,...) are not available
	 * @author martin.wurzinger
	 */
	public static BusinessBackendReservation reserveResources() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();

		// use port-range if specified in config
		final int port = SocketUtils.reserveNextFreePort(config.backendPortRangeStart, config.backendPortRangeEnd, null);
		// for shutdown-port we use "localhost" as hostname as Tomcat also uses this to bind the socket!
		final int shutdownPort = SocketUtils.reserveNextFreePort(config.backendShutdownPortRangeStart,
				config.backendShutdownPortRangeEnd, "localhost");

		// don't reserve an ajpPort if not configure
		final int ajpPort;
		if (config.backendAjpPortRangeStart == 0 || config.backendAjpPortRangeEnd == 0) {
			ajpPort = 0;
		} else {
			ajpPort = SocketUtils.reserveNextFreePort(config.backendAjpPortRangeStart, config.backendAjpPortRangeEnd, null);
		}

		String webappBase = config.webappBase;
		String contextRoot = config.backendContextRoot;

		 BusinessBackendReservation businessBackendReservation = new BusinessBackendReservation(port, shutdownPort, ajpPort, contextRoot, webappBase);
		 return businessBackendReservation;
	}
}
