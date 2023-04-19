package com.dynatrace.easytravel.config;

import java.io.IOException;

import com.dynatrace.easytravel.ipc.SocketUtils;


public class AngularFrontendReservation extends TomcatResourceReservation {

	public AngularFrontendReservation(int port, int shutdownPort, int ajpPort, String contextRoot, String webappBase) {
		super(port, shutdownPort, ajpPort, contextRoot, webappBase);
	}

	/**
	 *
	 * @return
	 * @throws IOException if some necessary IO resources (ports,...) are not available
	 * @author martin.wurzinger
	 */
	public static AngularFrontendReservation reserveResources() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();

		// use port-range if specified in config
		final int port = SocketUtils.reserveNextFreePort(config.angularFrontendPortRangeStart, config.angularFrontendPortRangeEnd, null);
		// for shutdown-port we use "localhost" as hostname as Tomcat also uses this to bind the socket!
		final int shutdownPort = SocketUtils.reserveNextFreePort(config.angularFrontendShutdownPortRangeStart,
				config.angularFrontendShutdownPortRangeEnd, "localhost");

		// don't reserve an ajpPort if not configure
		final int ajpPort;
		if (config.angularFrontendAjpPortRangeStart == 0 || config.angularFrontendAjpPortRangeEnd == 0) {
			ajpPort = 0;
		} else {
			ajpPort = SocketUtils.reserveNextFreePort(config.angularFrontendAjpPortRangeStart, config.angularFrontendAjpPortRangeEnd, null);
		}
		String webappBase = config.webappBase;
		String contextRoot = config.angularFrontendContextRoot;

		return new AngularFrontendReservation(port, shutdownPort, ajpPort, contextRoot, webappBase);
	}
}
