/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CassandraTokenTest.java
 * @date: 23.11.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.launcher.procedures;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import com.dynatrace.easytravel.launcher.procedures.CassandraProcedure.CassandraToken;


/**
 *
 * @author stefan.moschinski
 */
public class CassandraTokenTest {
	@Test
	public void testGetForHost() {
		CassandraToken token = new CassandraToken("host1");
		assertThat(token.getTokenForHost("host1"), is("0"));

		CassandraToken token2 = new CassandraToken("host1", "host2");
		assertThat(token2.getTokenForHost("host1"), is("0"));
		assertThat(token2.getTokenForHost("host2"), is("85070591730234615865843651857942052864"));
	}

	@Test
	public void testGetForHostEmptyAndNull() {
		String[] hosts = new String[] {};
		CassandraToken token = new CassandraToken(hosts);
		assertThat(token.getTokenForHost("host1"), is(""));

		CassandraToken token2 = new CassandraToken((String[])null);
		assertThat(token2.getTokenForHost("host1"), is(""));
	}

	@Test
	public void testUnknownHostReturnsEmptyString() {
		CassandraToken token2 = new CassandraToken("host1", "host2");
		assertThat(token2.getTokenForHost("host1"), is("0"));
		assertThat(token2.getTokenForHost("host2"), is("85070591730234615865843651857942052864"));
		assertThat(token2.getTokenForHost("host3"), is(""));
	}

	@Test
	public void testGetHostForTooManyHostsReturnsEmptyString() {
		String[] hosts = { "host1", "host2", "host3", "host4", "host5", "host6", "host7" };
		CassandraToken token = new CassandraToken(hosts);
		for (String host : hosts) {
			assertThat(token.getTokenForHost(host), is(""));
		}
	}
}
