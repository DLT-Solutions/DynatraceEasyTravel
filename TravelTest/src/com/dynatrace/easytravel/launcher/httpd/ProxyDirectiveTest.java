package com.dynatrace.easytravel.launcher.httpd;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Test;

import com.dynatrace.easytravel.launcher.httpd.ProxyDirective.BusinessBackendProxyDirective;
import com.dynatrace.easytravel.launcher.httpd.ProxyDirective.CustomerFrontedProxyDirective;


public class ProxyDirectiveTest {
	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private PrintWriter writer = new PrintWriter(out);

	@Test
	public void test() throws IOException {
		ProxyDirective dir = new ProxyDirective("http://url1", "set1");
		dir.addBalancerMember(new BalancerMember("host1", 1234));
		dir.write(writer, new RouteGenerator.DefaultRouteGen());


		writer.close();
		out.close();

		assertEquals("<Proxy http://url1>\n" +
			"    set1\n" +
			"    BalancerMember ajp://host1:1234 route=jvmRoute-1234\n" +
			"</Proxy>\n", out.toString().replace("\r", ""));
	}

	@Test
	public void testCoverage() throws IOException {
		// cover some border cases
		ProxyDirective directive = new ProxyDirective(null, null);
		directive.addBalancerMember(null);
		directive.write(writer, null);

		writer.close();
		out.close();

		assertEquals("<Proxy>\n" +
				"</Proxy>\n", out.toString().replace("\r", ""));
	}

	@Test
	public void testCustomerFrontend() throws IOException {
		CustomerFrontedProxyDirective dir = new CustomerFrontedProxyDirective();
		dir.write(writer, null);

		writer.close();
		out.close();

		assertEquals("<Proxy balancer://mycluster>\n" +
				"    ProxySet stickysession=JSESSIONID\n" +
				"    BalancerMember ajp://localhost:8280 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:8281 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:8282 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:8283 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:8284 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:8285 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:8286 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:8287 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:8288 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:8289 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:8290 connectiontimeout=10 retry=120\n" +
				"</Proxy>\n", out.toString().replace("\r", ""));
	}

	@Test
	public void testBusinessBackend() throws IOException {
		BusinessBackendProxyDirective dir = new BusinessBackendProxyDirective();
		dir.write(writer, null);

		writer.close();
		out.close();

		assertEquals("<Proxy balancer://backendcluster>\n" +
				"    ProxySet stickysession=JSESSIONID\n" +
				"    BalancerMember ajp://localhost:28280 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:28281 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:28282 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:28283 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:28284 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:28285 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:28286 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:28287 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:28288 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:28289 connectiontimeout=10 retry=120\n" +
				"    BalancerMember ajp://localhost:28290 connectiontimeout=10 retry=120\n" +
				"</Proxy>\n", out.toString().replace("\r", ""));
	}
}
