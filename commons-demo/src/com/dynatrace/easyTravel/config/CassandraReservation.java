/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CassandraReservation.java
 * @date: 06.08.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.config;

import java.io.IOException;

import com.dynatrace.easytravel.constants.BaseConstants.Persistence;
import com.dynatrace.easytravel.ipc.SocketUtils;


/**
 * Each instance of this class represents a reservation of a port for Cassandra.
 * 
 * @author stefan.moschinski
 */
public class CassandraReservation extends ResourceReservation {

	public static final String DEFAULT_HOST = "127.0.0.1";
	private String hostIp;

	/**
	 * 
	 * @param port the port that should be reserved
	 * @author stefan.moschinski
	 */
	public CassandraReservation(String host) {
		this.hostIp = host;
	}

	public boolean isDefaultHostIp() {
		return DEFAULT_HOST.equals(hostIp);
	}

	public String getLocalHostIp() {
		return hostIp;
	}

	@Override
	public void release() {
		if (isReleased()) {
			return;
		}
		SocketUtils.freeLocalHostIp(hostIp);
		setReleased();
	}

	/**
	 * 
	 * @return the a port reservation that can be used for the Cassandra process
	 * @throws IOException
	 * @author stefan.moschinski
	 */
	public static CassandraReservation reserveLocalHost() throws IOException {
		// use port-range if specified in config
		final String host = SocketUtils.getNextFreeLocalHostIp(Persistence.Cassandra.DEFAULT_RPC_PORT);
		return new CassandraReservation(host);
	}
}
