package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.cassandra.CassandraUtils;
import com.dynatrace.easytravel.config.CassandraReservation;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.Persistence;
import com.dynatrace.easytravel.constants.BaseConstants.Persistence.Cassandra;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.NetstatUtil;

import ch.qos.logback.classic.Logger;

public class CassandraProcedureTest {
    private static final Logger LOGGER = LoggerFactory.make();

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
	}

	@Before
	public void setUp() throws IOException {
		// ensure before running that no process is listening on the Cassandra port 9160
		ensurePortsAvailable(Persistence.Cassandra.DEFAULT_RPC_PORT);
		ensurePortsAvailable(7199);
	}

	@After
	public void tearDown() throws IOException {
		// ensure that we cleaned up everything correctly in the tests
		ensurePortsAvailable(Persistence.Cassandra.DEFAULT_RPC_PORT);
		ensurePortsAvailable(7199);
	}

	private static void ensurePortsAvailable(int port) throws IOException {
		NetstatUtil util = new NetstatUtil(Runtime.getRuntime());
		String processId = util.findProcessIdForPort(port);
		String process = util.findProcessForPort(port);

		assertNull("Expected to have no process listening on the cassandra port at " + port + ", but found " + processId + "/" + process,
				processId);
		assertNull("Expected to have no process listening on the cassandra port at " + port + ", but found " + processId + "/" + process,
				process);

		// then ensure that we get the first localhost ip address for the test to work smoothly
		CassandraReservation reservation = CassandraReservation.reserveLocalHost();
		String localhost = reservation.getLocalHostIp();
		assertEquals("127.0.0.1", localhost);
		reservation.release();
	}

	@Ignore
	@Test
	public void test() throws InterruptedException, CorruptInstallationException, TTransportException {
		Assume.assumeTrue(SocketUtils.isPortAvailable(7199, null));
		CassandraProcedure.setStartupTimeout((int) TimeUnit.SECONDS.toMillis(20));

		CassandraProcedure proc = null;

		try {
			proc = new CassandraProcedure(new DefaultProcedureMapping(Constants.Procedures.CASSANDRA_ID));

			Feedback run = proc.run();

			// try to get an exception if connecting did not work
			TTransport transport = new TSocket("localhost", Cassandra.DEFAULT_RPC_PORT);
			transport.open();
			transport.close();
			transport = new TSocket("127.0.0.1", Cassandra.DEFAULT_RPC_PORT);
			transport.open();
			transport.close();

			// also ensure that the CassandraUtils "sees" the Cassandra process
			assertTrue(CassandraUtils.isNodeAvailable("localhost", Cassandra.DEFAULT_RPC_PORT));
			assertTrue(CassandraUtils.isNodeAvailable("127.0.0.1", Cassandra.DEFAULT_RPC_PORT));

			assertEquals(Feedback.Success, run);

			assertTrue(proc.isOperatingCheckSupported());

			while(!proc.isOperating() && proc.isRunning()) {
				Thread.sleep(500);
			}

			assertTrue(proc.isRunning());
			assertTrue(proc.isOperating());

			assertTrue(proc.hasLogfile());
			assertNotNull(proc.getLogfile());
			assertEquals(0, proc.getPort());
			assertEquals("port", proc.getPortPropertyName());
			assertEquals(StopMode.PARALLEL, proc.getStopMode());
		} finally {
			if(proc != null) {
				assertEquals(Feedback.Neutral, proc.stop());
			}
		}
	}
}
