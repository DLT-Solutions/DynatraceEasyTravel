package com.dynatrace.easytravel.launcher.httpd;

import static com.dynatrace.easytravel.constants.BaseConstants.*;
import static com.dynatrace.easytravel.constants.BaseConstants.Apache.PROXY;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.engine.ProcedureFactory;

/**
 * Configuration object which represents this section within httpd.conf
 *
 * <Proxy balancer://mycluster>
 *    ProxySet stickysession=JSESSIONID
 *    BalancerMember ajp://localhost:8280 route=jvmRoute-8280 connectiontimeout=10 retry=120
 *    BalancerMember ajp://localhost:8281 route=jvmRoute-8281 connectiontimeout=10 retry=120
 *    BalancerMember ajp://localhost:8282 route=jvmRoute-8282 connectiontimeout=10 retry=120
 *    BalancerMember ajp://localhost:8283 route=jvmRoute-8283 connectiontimeout=10 retry=120
 *    BalancerMember ajp://localhost:8284 route=jvmRoute-8284 connectiontimeout=10 retry=120
 *    BalancerMember ajp://localhost:8285 route=jvmRoute-8285 connectiontimeout=10 retry=120
 *    BalancerMember ajp://localhost:8286 route=jvmRoute-8286 connectiontimeout=10 retry=120
 *    BalancerMember ajp://localhost:8287 route=jvmRoute-8287 connectiontimeout=10 retry=120
 *    BalancerMember ajp://localhost:8288 route=jvmRoute-8288 connectiontimeout=10 retry=120
 *    BalancerMember ajp://localhost:8289 route=jvmRoute-8289 connectiontimeout=10 retry=120
 *    BalancerMember ajp://localhost:8290 route=jvmRoute-8290 connectiontimeout=10 retry=120
 * </Proxy>
 *
 *
 * @see {@link BalancerMember}
 * @see {@link RouteGenerator}
 *
 * @author cwat-rpilz
 *
 */
public class ProxyDirective {

	private final String balancerUrl;
	private final String proxySet;
	private Collection<BalancerMember> balancerMembers = new ArrayList<BalancerMember>();

	public ProxyDirective(final String balancerUrl, final String proxySet) {
		this.balancerUrl = balancerUrl;
		this.proxySet = proxySet;
	}

	public void addBalancerMember(BalancerMember balancerMember) {
		balancerMembers.add(balancerMember);
	}

	public void write(PrintWriter writer, RouteGenerator routeGenerator) {
		if (balancerUrl != null) {
			writer.println(LABRA + PROXY + WS + balancerUrl + RABRA);
		} else {
			writer.println(LABRA + PROXY + RABRA);
		}

		if (proxySet != null) {
			writer.println("    " + proxySet);
		}

		for (BalancerMember balancerMember : balancerMembers) {
			if (balancerMember != null) {
				balancerMember.write(writer, routeGenerator);
			}
		}

		writer.println(LABRA + FSLASH + PROXY + RABRA);
	}

	public static class CustomerFrontedProxyDirective extends ProxyDirective {

		public CustomerFrontedProxyDirective() {
			super("balancer://mycluster", "ProxySet stickysession=JSESSIONID");
			final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();

			for (String host : getRemoteHosts()) {
				for (int ajp = EASYTRAVEL_CONFIG.frontendAjpPortRangeStart; ajp <= EASYTRAVEL_CONFIG.frontendAjpPortRangeEnd; ajp++) {
					addBalancerMember(new BalancerMember(host, ajp, 120, 10));
				}
			}
		}
	}

	public static class AngularFrontedProxyDirective extends ProxyDirective {

		public AngularFrontedProxyDirective() {
			super("balancer://angcluster", "ProxySet stickysession=JSESSIONID");
			final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();

			for (String host : getRemoteHosts()) {
				for (int ajp = EASYTRAVEL_CONFIG.angularFrontendAjpPortRangeStart; ajp <= EASYTRAVEL_CONFIG.angularFrontendAjpPortRangeEnd; ajp++) {
					addBalancerMember(new BalancerMember(host, ajp, 120, 10));
				}
			}
		}
	}
	
	public static class BusinessBackendProxyDirective extends ProxyDirective {

		public BusinessBackendProxyDirective() {
			super("balancer://backendcluster", "ProxySet stickysession=JSESSIONID");
			final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();

			for (String host : getRemoteHosts()) {
				for (int ajp = EASYTRAVEL_CONFIG.backendAjpPortRangeStart; ajp <= EASYTRAVEL_CONFIG.backendAjpPortRangeEnd; ajp++) {
					addBalancerMember(new BalancerMember(host, ajp, 120, 10));
				}
			}
		}

	}

	private static ArrayList<String> getRemoteHosts() {
		ArrayList<String> remoteHosts = new ArrayList<String>();
		remoteHosts.addAll(ProcedureFactory.getAllRemoteHosts());
		remoteHosts.add(LOCALHOST);
		return remoteHosts;
	}
}
