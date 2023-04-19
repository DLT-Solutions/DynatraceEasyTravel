package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.net.UrlUtils.Availability;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Level;

public class TomcatShutdownCommandTest {
	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
	}

	@Test
	public void testTomcatShutdownCommandInetAddressIntFails() throws Exception {
		int port = SocketUtils.reserveNextFreePort(10000, 20000, "localhost");
		try {
			TomcatShutdownCommand cmd = new TomcatShutdownCommand(InetAddress.getLocalHost(), port);
			assertEquals("Does not succeed, noone listens on the port", Feedback.Failure, cmd.execute());
		} finally {
			SocketUtils.freePort(port);
		}
	}

	@Test
	public void testTomcatShutdownCommandInetAddressIntStringFails() throws Exception {
		int port = SocketUtils.reserveNextFreePort(10000, 20000, "localhost");
		try {
			TomcatShutdownCommand cmd = new TomcatShutdownCommand(InetAddress.getLocalHost(), port, "somemessage");
			assertEquals("Does not succeed, noone listens on the port", Feedback.Failure, cmd.execute());
		} finally {
			SocketUtils.freePort(port);
		}
	}

	@Test
	public void testTomcatShutdownCommandInetAddressIntStringSucceeds() throws Exception {
		// use a REST Server to simulate the shutdown listening Tomcat
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK");

		checkThatHostIsAvailable(server);

		try {
			TomcatShutdownCommand cmd = new TomcatShutdownCommand(InetAddress.getLocalHost(), server.getPort(), "somemessage");
			assertEquals("Now it should not fail", Feedback.Neutral, cmd.execute());
		} finally {
			server.stop();
		}
	}

	private void checkThatHostIsAvailable(MockRESTServer server) throws UnknownHostException {
		assertTrue(UrlUtils.checkServiceAvailability("localhost", server.getPort()));
		assertEquals(Availability.READ_OK, UrlUtils.checkRead("http://localhost:" + server.getPort()));

		String hostName = InetAddress.getLocalHost().getCanonicalHostName();
		assertTrue("Failed for hostname: " + hostName,
				UrlUtils.checkServiceAvailability(hostName, server.getPort()));
		assertEquals("Failed for hostname: " + hostName,
				Availability.READ_OK, UrlUtils.checkRead("http://" + hostName + ":" + server.getPort()));

		assertTrue("Failed for hostname: " + InetAddress.getLocalHost().getHostAddress(),
				UrlUtils.checkServiceAvailability(InetAddress.getLocalHost().getHostAddress(), server.getPort()));
		assertEquals("Failed for hostname: " + InetAddress.getLocalHost().getHostAddress(),
				Availability.READ_OK, UrlUtils.checkRead("http://" + InetAddress.getLocalHost().getHostAddress() + ":" + server.getPort()));
	}

	@Test
	public void testInvalidInput() throws Exception {
		try {
			new TomcatShutdownCommand(null, 0);
			fail("Should fail because of invalid address");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "No address defined");
		}

		try {
			new TomcatShutdownCommand(InetAddress.getLocalHost(), 0, "");
			fail("Should fail because of invalid message");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "No shutdown message defined");
		}

		try {
			new TomcatShutdownCommand(InetAddress.getLocalHost(), 0, null);
			fail("Should fail because of invalid message");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "No shutdown message defined");
		}

		int port = SocketUtils.reserveNextFreePort(10000, 20000, "localhost");
		try {
			TomcatShutdownCommand cmd = new TomcatShutdownCommand(InetAddress.getByName("192.168.1.123"), port);
			assertEquals("Does not succeed, invalid host", Feedback.Failure, cmd.execute());

			cmd = new TomcatShutdownCommand(InetAddress.getByAddress("invalidhost", new byte[] {127,0,0,1}), port);
			assertEquals("Does not succeed, invalid host", Feedback.Failure, cmd.execute());
		} finally {
			SocketUtils.freePort(port);
		}
	}

	@Test
	public void testWithDifferentLoglevel() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {
			@Override
			public void run() {
				try {
					testInvalidInput();
					testTomcatShutdownCommandInetAddressIntFails();
					testTomcatShutdownCommandInetAddressIntStringFails();
					testTomcatShutdownCommandInetAddressIntStringSucceeds();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, TomcatShutdownCommand.class.getName(), Level.DEBUG);
	}
}
