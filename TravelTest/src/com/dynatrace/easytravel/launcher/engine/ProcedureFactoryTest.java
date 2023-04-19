package com.dynatrace.easytravel.launcher.engine;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import com.dynatrace.easytravel.constants.BaseConstants;
import org.junit.Test;

import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.procedures.RemoteProcedure;
import com.dynatrace.easytravel.launcher.remote.RESTProcedureClient;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.utils.TestHelpers;


public class ProcedureFactoryTest {

    static {
        System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");
    }

	@Test
	public void testCreate() {
		ProcedureFactory factory = new ProcedureFactory();

		assertNull("Handles null-mapping", factory.create(null));
		assertNull("Handles null-mapping-id", factory.create(new DefaultProcedureMapping(null)));
		assertNull("Handles empty-mapping-id", factory.create(new DefaultProcedureMapping("")));

		assertNull("Handles invalid-mapping-id", factory.create(new DefaultProcedureMapping("invalid")));

		// no remote host
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.DATABASE_CONTENT_CREATOR_ID)), false);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID)), false);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.PAYMENT_BACKEND_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.B2B_FRONTEND_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.ANT_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_PHP_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.WEBSERVER_AGENT_RESTART_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.HOST_AGENT_RESTART_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.MONGO_DB_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.PREPARE_VMWARE_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.VMOTION_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.HBASE_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.MYSQL_CONTENT_CREATOR_ID)), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.NGINX_WEBSERVER_ID)), true);

		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.DATABASE_CONTENT_CREATOR_ID), false), false);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID), false), false);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.PAYMENT_BACKEND_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.B2B_FRONTEND_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.ANT_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_PHP_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.WEBSERVER_AGENT_RESTART_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.HOST_AGENT_RESTART_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.MONGO_DB_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.PREPARE_VMWARE_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.VMOTION_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.HBASE_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.MYSQL_CONTENT_CREATOR_ID), false), true);
		verifyNotRemote(factory.create(new DefaultProcedureMapping(Constants.Procedures.NGINX_WEBSERVER_ID), false), true);

		// now with remote host
		verifyRemoteHost(factory, Constants.Procedures.BUSINESS_BACKEND_ID, "com.dynatrace.easytravel.host.business_backend");
		verifyRemoteHost(factory, Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID, "com.dynatrace.easytravel.host.credit_card_authorization");
		verifyRemoteHost(factory, Constants.Procedures.CUSTOMER_FRONTEND_ID, "com.dynatrace.easytravel.host.customer_frontend");
		verifyRemoteHost(factory, Constants.Procedures.DATABASE_CONTENT_CREATOR_ID, "com.dynatrace.easytravel.host.database_content_creator");
		verifyRemoteHost(factory, Constants.Procedures.INPROCESS_DBMS_ID, "com.dynatrace.easytravel.host.inprocess_dbms");
		verifyRemoteHost(factory, Constants.Procedures.PAYMENT_BACKEND_ID, "com.dynatrace.easytravel.host.payment_backend");
		verifyRemoteHost(factory, Constants.Procedures.B2B_FRONTEND_ID, "com.dynatrace.easytravel.host.b2b_frontend");
		verifyRemoteHost(factory, Constants.Procedures.ANT_ID, "com.dynatrace.easytravel.host.ant");
		verifyRemoteHost(factory, Constants.Procedures.APACHE_HTTPD_ID, "com.dynatrace.easytravel.host.apache_httpd");
		verifyRemoteHost(factory, Constants.Procedures.APACHE_HTTPD_PHP_ID, "com.dynatrace.easytravel.host.apache_httpd_php");
		verifyRemoteHost(factory, Constants.Procedures.MONGO_DB_ID, "com.dynatrace.easytravel.host.mongodb");
		verifyRemoteHost(factory, Constants.Procedures.HBASE_ID, "com.dynatrace.easytravel.host.hbase");
		verifyRemoteHost(factory, Constants.Procedures.MYSQL_CONTENT_CREATOR_ID, "com.dynatrace.easytravel.host.mysql_content_creator");
		verifyRemoteHost(factory, Constants.Procedures.NGINX_WEBSERVER_ID, "com.dynatrace.easytravel.host.nginx");

		verifyNotRemote(factory.create(new DefaultProcedureMapping(null), true), true);
	}

	@Test
	public void testVerifyLocalHost() throws UnknownHostException {
		ProcedureFactory factory = new ProcedureFactory();

		verifyLocalHost(factory, Constants.Procedures.BUSINESS_BACKEND_ID, "com.dynatrace.easytravel.host.business_backend");
		verifyLocalHost(factory, Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID, "com.dynatrace.easytravel.host.credit_card_authorization");
		verifyLocalHost(factory, Constants.Procedures.CUSTOMER_FRONTEND_ID, "com.dynatrace.easytravel.host.customer_frontend");
		verifyLocalHost(factory, Constants.Procedures.DATABASE_CONTENT_CREATOR_ID, "com.dynatrace.easytravel.host.database_content_creator");
		verifyLocalHost(factory, Constants.Procedures.INPROCESS_DBMS_ID, "com.dynatrace.easytravel.host.inprocess_dbms");
		verifyLocalHost(factory, Constants.Procedures.PAYMENT_BACKEND_ID, "com.dynatrace.easytravel.host.payment_backend");
		verifyLocalHost(factory, Constants.Procedures.B2B_FRONTEND_ID, "com.dynatrace.easytravel.host.b2b_frontend");
		verifyLocalHost(factory, Constants.Procedures.ANT_ID, "com.dynatrace.easytravel.host.ant");
		verifyLocalHost(factory, Constants.Procedures.APACHE_HTTPD_ID, "com.dynatrace.easytravel.host.apache_httpd");
		verifyLocalHost(factory, Constants.Procedures.APACHE_HTTPD_PHP_ID, "com.dynatrace.easytravel.host.apache_httpd_php");
		verifyLocalHost(factory, Constants.Procedures.MONGO_DB_ID, "com.dynatrace.easytravel.host.mongodb");
		verifyLocalHost(factory, Constants.Procedures.HBASE_ID, "com.dynatrace.easytravel.host.hbase");
		verifyLocalHost(factory, Constants.Procedures.MYSQL_CONTENT_CREATOR_ID, "com.dynatrace.easytravel.host.mysql_content_creator");
		verifyLocalHost(factory, Constants.Procedures.NGINX_WEBSERVER_ID, "com.dynatrace.easytravel.host.nginx");
	}


	@Test
	public void testNameOfProcedure() {
		assertEquals(MessageConstants.UNKNOWN, ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping("unknown")));

		assertEquals(MessageConstants.MODULE_MYSQL_DATABASE_MANAGEMENT_SYSTEM, ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_MYSQL_ID)));
		assertEquals(MessageConstants.MOUDLE_MYSQL_CONTENT_CREATOR, ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(Constants.Procedures.MYSQL_CONTENT_CREATOR_ID)));
		assertEquals(MessageConstants.MODULE_CASSANDRA, ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(Constants.Procedures.CASSANDRA_ID)));
		assertEquals(MessageConstants.MODULE_THIRDPARTY_SERVER, ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(Constants.Procedures.THIRDPARTY_SERVER_ID)));
		assertEquals(MessageConstants.MODULE_BROWSER, ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(Constants.Procedures.BROWSER_ID)));
		assertEquals(MessageConstants.MODULE_PREPARE_VMWARE, ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(Constants.Procedures.PREPARE_VMWARE_ID)));
		assertEquals(MessageConstants.MODULE_VMOTION, ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(Constants.Procedures.VMOTION_ID)));
		assertEquals(MessageConstants.MODULE_NGINX, ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(Constants.Procedures.NGINX_WEBSERVER_ID)));
	}

	@Test
	public void testGetAllRemoteHosts() {
		Collection<String> remoteHosts = ProcedureFactory.getAllRemoteHosts();
		assertNotNull(remoteHosts);

		assertEquals("Did not expect remote hosts, but had " + remoteHosts.size() + ": " + remoteHosts,
				0, remoteHosts.size());

		System.setProperty("com.dynatrace.easytravel.host.business_backend", "somehost");
		try {
			remoteHosts = ProcedureFactory.getAllRemoteHosts();
			assertNotNull(remoteHosts);

			assertEquals("Did expect one remote hosts now, but had " + remoteHosts.size() + ": " + remoteHosts,
					1, remoteHosts.size());

			// check correct splitting
			System.setProperty("com.dynatrace.easytravel.host.additional", "host1,host2,host3");

			remoteHosts = ProcedureFactory.getAllRemoteHosts();
			assertNotNull(remoteHosts);

			assertEquals("Did expect four remote hosts now, somehost, host1, host2, host3, but had " + remoteHosts.size() + ": " + remoteHosts,
					4, remoteHosts.size());

			System.setProperty("com.dynatrace.easytravel.host.additional", "");

			remoteHosts = ProcedureFactory.getAllRemoteHosts();
			assertNotNull(remoteHosts);

			assertEquals("Did expect one remote hosts now, somehost, but had " + remoteHosts.size() + ": " + remoteHosts,
					1, remoteHosts.size());
		} finally {
			System.clearProperty("com.dynatrace.easytravel.host.business_backend");
		}
	}

	@Test
	public void testGetHostOrLocal() {
		assertEquals("localhost", ProcedureFactory.getHostOrLocal(Constants.Procedures.BUSINESS_BACKEND_ID));

		try {
			ProcedureFactory.getHostOrLocal(null);
			fail("Should throw Exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "mapping must not be null");
		}

		System.setProperty("com.dynatrace.easytravel.host.business_backend", "somehost");
		try {
			assertEquals("somehost", ProcedureFactory.getHostOrLocal(Constants.Procedures.BUSINESS_BACKEND_ID));
		} finally {
			System.clearProperty("com.dynatrace.easytravel.host.business_backend");
		}

		assertFalse(ProcedureFactory.isRemote((String) null));
		assertFalse(ProcedureFactory.isRemote((ProcedureMapping) null));
	}

	@Test
	public void textExplicitRemoteHost() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		ProcedureFactory factory = new ProcedureFactory();
		DefaultProcedureMapping procedureMapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		procedureMapping.setHost("www.myhost.com");

		Procedure procedure = factory.create(procedureMapping, false);
		assertFalse("Still not remote with false for allowRemote", procedure instanceof RemoteProcedure);

		procedure = factory.create(procedureMapping, true);
		assertTrue("Should be a remote procedure", procedure instanceof RemoteProcedure);
		RemoteProcedure remoteProcedure = (RemoteProcedure) procedure;
		Field clientField = RemoteProcedure.class.getDeclaredField("client");
		clientField.setAccessible(true);
		RESTProcedureClient client = (RESTProcedureClient) clientField.get(remoteProcedure);
		assertEquals("www.myhost.com", client.getHost());
	}

	@Test
	public void textExplicitRemoteHostIsLocalhost() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		ProcedureFactory factory = new ProcedureFactory();
		DefaultProcedureMapping procedureMapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		procedureMapping.setHost("localhost");

		Procedure procedure = factory.create(procedureMapping, false);
		assertFalse("Still not remote with false for allowRemote", procedure instanceof RemoteProcedure);

		procedure = factory.create(procedureMapping, true);
		assertFalse("Should not be a remote procedure", procedure instanceof RemoteProcedure);
	}

	private void verifyRemoteHost(ProcedureFactory factory, String procedure, String property) {
		try {
			System.setProperty(property, "somehost");
			Procedure proc = factory.create(new DefaultProcedureMapping(procedure));

			assertNotNull("Creates valid procedure", proc);
			assertTrue("Should now be a remote procedure", proc instanceof RemoteProcedure);
			assertEquals("somehost", ProcedureFactory.getHostOrLocal(procedure));

			System.setProperty(property, "localhost");
			proc = factory.create(new DefaultProcedureMapping(procedure));

			// if we can create the local proc, then it should not be a remote proc with "localhost"
			if(proc != null) {
				assertFalse("Should now be a remote procedure", proc instanceof RemoteProcedure);
			}
			assertEquals("localhost", ProcedureFactory.getHostOrLocal(procedure));

		} finally {
			System.clearProperty(property);
		}
	}

	private void verifyNotRemote(Procedure proc, final boolean mayFail) {
		if(!mayFail) {
			assertNotNull("Creates valid procedure", proc);
		}
		assertFalse("Should not be a remote procedure", proc instanceof RemoteProcedure);
	}

	private void verifyLocalHost(ProcedureFactory factory, String procedure, String property) throws UnknownHostException {

		InetAddress localHost = InetAddress.getLocalHost();

		assertNotNull("Creates local host", localHost);

		try {
			Procedure proc = factory.create(new DefaultProcedureMapping(procedure));

			System.setProperty(property, "localhost");
			proc = factory.create(new DefaultProcedureMapping(procedure));

			verifyNotRemote(proc, true);

			// if we can create the local proc, then it should not be a remote proc with "localhost"
			assertFalse("Should not be a remote procedure", proc instanceof RemoteProcedure);
			assertEquals("localhost", ProcedureFactory.getHostOrLocal(procedure));

			// if we can create the local proc, then it should not be a remote proc with "127.0.0.1"
			System.setProperty(property, localHost.getHostAddress());
			proc = factory.create(new DefaultProcedureMapping(procedure));
			assertFalse("Should not be a remote procedure", proc instanceof RemoteProcedure);
			assertEquals(localHost.getHostAddress(), ProcedureFactory.getHostOrLocal(procedure));

			// if we can create the local proc, then it should not be a remote proc with "hostname"
			System.setProperty(property, localHost.getHostName());
			proc = factory.create(new DefaultProcedureMapping(procedure));
			assertFalse("Should not be a remote procedure", proc instanceof RemoteProcedure);
            assertThat(localHost.getHostName(), is(equalToIgnoringCase(ProcedureFactory.getHostOrLocal(procedure))));

			// if we can create the local proc, then it should not be a remote proc with "hostname.local.domain.name"
			System.setProperty(property, localHost.getCanonicalHostName());
			proc = factory.create(new DefaultProcedureMapping(procedure));
			assertFalse("Should not be a remote procedure", proc instanceof RemoteProcedure);
            assertThat(localHost.getCanonicalHostName(), is(equalToIgnoringCase(ProcedureFactory.getHostOrLocal(procedure))));

		} finally {
			System.clearProperty(property);
		}
	}

	@Test
	public void testGetSystemProperty() {
		assertNotNull("Should return some value",
				ProcedureFactory.getSystemProperty("somemappingid"));

		assertTrue("Should replace blanks",
				ProcedureFactory.getSystemProperty("somema pp in gid").contains("somema_pp_in_gid"));

		assertTrue("Should make the string lowercase",
				ProcedureFactory.getSystemProperty("SomeMappingId").contains("somemappingid"));
	}
}
