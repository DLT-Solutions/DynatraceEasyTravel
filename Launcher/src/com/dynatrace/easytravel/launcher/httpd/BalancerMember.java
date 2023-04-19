package com.dynatrace.easytravel.launcher.httpd;

import static com.dynatrace.easytravel.constants.BaseConstants.COLON;
import static com.dynatrace.easytravel.constants.BaseConstants.EQUAL;
import static com.dynatrace.easytravel.constants.BaseConstants.FSLASH;
import static com.dynatrace.easytravel.constants.BaseConstants.WS;
import static com.dynatrace.easytravel.constants.BaseConstants.Apache.AJP;
import static com.dynatrace.easytravel.constants.BaseConstants.Apache.BALANCER_MEMBER;
import static com.dynatrace.easytravel.constants.BaseConstants.Apache.CONNECTION_TIMEOUT;
import static com.dynatrace.easytravel.constants.BaseConstants.Apache.RETRY;
import static com.dynatrace.easytravel.constants.BaseConstants.Apache.ROUTE;

import java.io.PrintWriter;
import java.io.Serializable;

/**
 * Configuration object representing lines like these
 * 
 * BalancerMember ajp://localhost:8280 route=jvmRoute-8280 connectiontimeout=10 retry=120
 * 
 * within a Proxy Directive
 * 
 * @see {@link ProxyDirective}
 * 
 * @author cwat-rpilz
 *
 */
public final class BalancerMember implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final int UNDEFINED = Integer.MIN_VALUE;

	private final String host;
	private final int port;
	private final int retry;
	private final int connectionTimeout;
	
	public BalancerMember(String host, int port) {
		this(host, port, UNDEFINED, UNDEFINED);
	}
	
	public BalancerMember(String host, int port, int retry, int connectionTimeout) {
		this.host = host;
		this.port = port;
		this.retry = retry;
		this.connectionTimeout = connectionTimeout;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	
	public int getRetry() {
		return retry;
	}
	
	public BalancerMember withHost(String host) {
		return new BalancerMember(host, port, retry, connectionTimeout);
	}
	
	public BalancerMember withPort(int port) {
		return new BalancerMember(host, port, retry, connectionTimeout);
	}
	
	public void write(PrintWriter writer, RouteGenerator routeGenerator) {
		writer.println("    " + toString(routeGenerator));
	}
	
	public String toString(RouteGenerator routeGenerator) {
		StringBuilder sb = new StringBuilder();
		sb.append(BALANCER_MEMBER).append(WS).append(AJP).append(COLON).append(FSLASH).append(FSLASH).append(host).append(COLON).append(port);
		if (routeGenerator != null) {
			sb.append(WS).append(ROUTE).append(EQUAL).append(routeGenerator.genRoute(host, port));
		}
		if (connectionTimeout != UNDEFINED) {
			sb.append(WS).append(CONNECTION_TIMEOUT).append(EQUAL).append(connectionTimeout);
		}
		if (retry != UNDEFINED) {
			sb.append(WS).append(RETRY).append(EQUAL).append(retry);
		}
		return sb.toString();
	}

}
