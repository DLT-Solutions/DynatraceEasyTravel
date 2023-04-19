/**
 *
 */
package com.dynatrace.easytravel.launcher.engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.dynatrace.easytravel.launcher.engine.DatabaseEnvArgs.ChannelType;
import com.dynatrace.easytravel.launcher.engine.DatabaseEnvArgs.Vendor;

/**
 * @author tomasz.wieremjewicz
 * @date 25 sty 2018
 *
 */
public class DatabaseEnvArgsTest {
	private final String NAME = "easyTravelBusiness";
	private final String ENDPOINT = "dbserv\\:50000\\";
	private final String ENDPOINT_EXPECTED = "dbserv:50000";

	private void doAsserts(DatabaseEnvArgs env, Vendor vendor) {
		assertEquals(NAME, env.name);
		assertEquals(vendor.getPreciseName(), env.vendor);
		assertEquals(ChannelType.TCP_IP.name(), env.channelType);
		assertEquals(ENDPOINT_EXPECTED, env.channelEndpoint);
	}

	@Test
	public void testConstructorWithNullParameter() {
		DatabaseEnvArgs env = new DatabaseEnvArgs(null);
		assertEquals(Boolean.TRUE, env.isEmptyOrNull());
	}

	@Test
	public void testConstructorWithEmptyParameter() {
		DatabaseEnvArgs env = new DatabaseEnvArgs("");
		assertEquals(Boolean.TRUE, env.isEmptyOrNull());
	}

	@Test
	public void testMongo() {
		DatabaseEnvArgs env = new DatabaseEnvArgs("jdbc:mongo://" + ENDPOINT + "/" + NAME);
		assertEquals(Boolean.TRUE, env.isEmptyOrNull());
	}

	@Test
	public void testDB2() {
		String connectonString = "jdbc:db2://" + ENDPOINT + "/" + NAME;
		DatabaseEnvArgs env = new DatabaseEnvArgs(connectonString);
		doAsserts(env, Vendor.DB2);
	}

	@Test
	public void testOracle() {
		String connectonString = "jdbc:oracle:thin:$user_name/$user_pass@" + ENDPOINT + ":" + NAME;
		DatabaseEnvArgs env = new DatabaseEnvArgs(connectonString);
		doAsserts(env, Vendor.ORACLE);

		connectonString = "jdbc:oracle:thin:@//" + ENDPOINT + "/" + NAME;
		env = new DatabaseEnvArgs(connectonString);
		doAsserts(env, Vendor.ORACLE);
	}

	@Test
	public void testSQLServer() {
		String connectonString = "jdbc:jtds:sqlserver://" + ENDPOINT + "/" + NAME + ";instance=dynasqlserver";
		DatabaseEnvArgs env = new DatabaseEnvArgs(connectonString);

		doAsserts(env, Vendor.SQLSERVER);

		connectonString = "jdbc:jtds:sqlserver://" + ENDPOINT + "/" + NAME;
		env = new DatabaseEnvArgs(connectonString);
		doAsserts(env, Vendor.SQLSERVER);

		connectonString = "jdbc:sqlserver://" + ENDPOINT + ";databaseName=" + NAME;
		env = new DatabaseEnvArgs(connectonString);
		doAsserts(env, Vendor.SQLSERVER);

		connectonString = "jdbc:sqlserver://" + ENDPOINT + ";database=" + NAME + ";encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
		env = new DatabaseEnvArgs(connectonString);
		doAsserts(env, Vendor.SQLSERVER);
	}

	@Test
	public void testMySQL() {
		String connectonString = "jdbc:mysql://" + ENDPOINT + "/" + NAME;
		DatabaseEnvArgs env = new DatabaseEnvArgs(connectonString);
		doAsserts(env, Vendor.MYSQL);
	}

	@Test
	public void testDerby() {
		String connectonString = "jdbc:derby://" + ENDPOINT + "/" + NAME + ";create=true";
		DatabaseEnvArgs env = new DatabaseEnvArgs(connectonString);
		doAsserts(env, Vendor.DERBY_CLIENT);
	}

	@Test
	public void testPostgreSQL() {
		String connectonString = "jdbc:postgresql://" + ENDPOINT + "/" + NAME;
		DatabaseEnvArgs env = new DatabaseEnvArgs(connectonString);
		doAsserts(env, Vendor.POSTGRESQL);
	}
}
