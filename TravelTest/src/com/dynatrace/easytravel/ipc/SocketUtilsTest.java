package com.dynatrace.easytravel.ipc;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.dynatrace.easytravel.utils.ThreadTestHelper;


public class SocketUtilsTest {
	private static final Logger log = Logger.getLogger(SocketUtils.class.getName());

	private static final int PORT_RANGE_START = 28000;
	private static final int PORT_RANGE_END = 29000;

	private static final int NUMBER_OF_SOCKETS = 10;

	private static int portLimitStart = 29110;
	private static int portLimitEnd = portLimitStart + NUMBER_OF_SOCKETS - 1;

	private static final int TEST_COUNT = 70000;	// i.e. try it more than 65535 times to make sure we do not run out of ports somehow

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();

		// look for free ports where we expect them to be free
		while(true) {
			// test all ports in the current range and exit if we did find one
			boolean failed = false;
			for(int i = 0; i < NUMBER_OF_SOCKETS;i++) {
				// first test if this port is still available
				ServerSocket sock;
				try {
					sock = new ServerSocket();
					//sock.setReuseAddress(true);
					sock.bind(new InetSocketAddress(portLimitStart + i));
					sock.close();
				} catch (IOException e) {
					// try next one
					failed = true;
					break;
				}
			}

			if(!failed) {
				// found range, use it
				log.info("Found port range: " + portLimitStart + " - " + portLimitEnd);
				break;
			}
			if(portLimitStart >= Short.MAX_VALUE || portLimitEnd >= Short.MAX_VALUE) {
				throw new IllegalStateException("Could not find a free range of " + NUMBER_OF_SOCKETS + " ports, cannot run tests!");
			}

			portLimitStart ++;
			portLimitEnd ++;
		}
	}

	@Before
	public void setUp() {
		// delete all previous port-lock-files to avoid log-entries about outdated lock-files
		FileFilter filter = new PrefixFileFilter("port-");
		File[] list = Directories.getExistingTempDir().listFiles(filter);
		for (File file : list) {
			assertTrue("Failed to delete file: " + file,
					file.delete());
		}
	}

	@Test
	public void testBasicConnect() throws Exception {
		int port = SocketUtils.getNextFreePort(PORT_RANGE_START, PORT_RANGE_END, "localhost");
		ServerSocket sock = new ServerSocket();

		sock.bind(new InetSocketAddress("localhost", port));
		try {
			ServerSocket sock2 = new ServerSocket();

			try {
				sock2.bind(new InetSocketAddress("localhost", port));
				fail("Should catch Exception here");
			} catch (BindException e) {
				TestHelpers.assertContains(e, "Address already in use");
			} finally {
				sock2.close();
			}
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			sock.close();
		}
	}

	@Test
	public void testBasicConnectWithReuse() throws Exception {
		int port = SocketUtils.getNextFreePort(PORT_RANGE_START, PORT_RANGE_END, "localhost");
		ServerSocket sock = new ServerSocket();
		sock.setReuseAddress(true);

		sock.bind(new InetSocketAddress("localhost", port));
		try {
			ServerSocket sock2 = new ServerSocket();

			try {
				sock2.bind(new InetSocketAddress("localhost", port));
				fail("Should catch Exception here");
			} catch (BindException e) {
				TestHelpers.assertContains(e, "Address already in use");
			} finally {
				sock2.close();
			}
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			sock.close();
		}
	}

	@Test
	public void testBasicConnectWithMany() throws Exception {
		ServerSocket[] sock = new ServerSocket[NUMBER_OF_SOCKETS];
		for(int i = 0;i < NUMBER_OF_SOCKETS;i++) {
			int port = SocketUtils.getNextFreePort(portLimitStart, portLimitEnd, "localhost");
			sock[i] = new ServerSocket();
			sock[i].bind(new InetSocketAddress("localhost", port));

			log.info("Using port: " + port);
		}

		try {
			// now retrieving another port should fail
			SocketUtils.getNextFreePort(portLimitStart, portLimitEnd, "localhost");
			fail("Should throw Exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, Integer.toString(portLimitStart));
			TestHelpers.assertContains(e, Integer.toString(portLimitEnd));
			TestHelpers.assertContains(e, "No free socket port");
		} finally {
			// free up sockets again
			for(ServerSocket socket : sock) {
				socket.close();
			}
		}
	}

	@Test
	public void testBasicConnectWithManyAndReuse() throws Exception {
		ServerSocket[] sock = new ServerSocket[NUMBER_OF_SOCKETS];
		for(int i = 0;i < NUMBER_OF_SOCKETS;i++) {
			int port = SocketUtils.getNextFreePort(portLimitStart, portLimitEnd, "localhost");
			sock[i] = new ServerSocket();
			sock[i].setReuseAddress(true);
			sock[i].bind(new InetSocketAddress("localhost", port));

			log.info("Using port: " + port);
		}

		try {
			// now retrieving another port should fail
			SocketUtils.getNextFreePort(portLimitStart, portLimitEnd, "localhost");
			fail("Should throw Exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, Integer.toString(portLimitStart));
			TestHelpers.assertContains(e, Integer.toString(portLimitEnd));
			TestHelpers.assertContains(e, "No free socket port");
		} finally {
			// free up sockets again
			for(ServerSocket socket : sock) {
				socket.close();
			}
		}
	}

	@Test
	public void testConnectSameAddress1() throws Exception {
		int port = SocketUtils.getNextFreePort(PORT_RANGE_START, PORT_RANGE_END, "localhost");
		ServerSocket sock = new ServerSocket();

		sock.bind(new InetSocketAddress("localhost", port));
		try {
			ServerSocket sock2 = new ServerSocket();

			try {
				sock2.bind(new InetSocketAddress("localhost", port));
				fail("Should catch Exception here");
			} catch (BindException e) {
				TestHelpers.assertContains(e, "Address already in use");
			} finally {
				sock2.close();
			}
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			sock.close();
		}
	}


	@Test
	public void testConnectSameAddress2() throws Exception {
		int port = SocketUtils.getNextFreePort(PORT_RANGE_START, PORT_RANGE_END, null);
		ServerSocket sock = new ServerSocket();

		sock.bind(new InetSocketAddress((InetAddress)null, port));
		try {
			ServerSocket sock2 = new ServerSocket();

			try {
				sock2.bind(new InetSocketAddress((InetAddress)null, port));
				fail("Should catch Exception here");
			} catch (BindException e) {
				TestHelpers.assertContains(e, "Address already in use");
			} finally {
				sock2.close();
			}
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			sock.close();
		}
	}

	@Test
	public void testConnectSameAddress3() throws Exception {
		int port = SocketUtils.getNextFreePort(PORT_RANGE_START, PORT_RANGE_END, null);
		ServerSocket sock = new ServerSocket();

		sock.bind(new InetSocketAddress(port));
		try {
			ServerSocket sock2 = new ServerSocket();

			try {
				sock2.bind(new InetSocketAddress(port));
				fail("Should catch Exception here");
			} catch (BindException e) {
				TestHelpers.assertContains(e, "Address already in use");
			} finally {
				sock2.close();
			}
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			sock.close();
		}
	}


	@Test
	public void testConnectDifferentAddress1() throws Exception {
		int port = SocketUtils.getNextFreePort(PORT_RANGE_START, PORT_RANGE_END, "localhost");
		ServerSocket sock = new ServerSocket();

		sock.bind(new InetSocketAddress("localhost", port));
		try {
			ServerSocket sock2 = new ServerSocket();

			try {
				// Linux seems to handle this differently...
				if(SystemUtils.IS_OS_WINDOWS) {
					// this works because we mix "localhost" and null-address
					sock2.bind(new InetSocketAddress(port));
				}
				/*fail("Should catch Exception here");
			} catch (BindException e) {
				TestHelpers.assertContains(e, "Address already in use");*/
			} finally {
				sock2.close();
			}
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			sock.close();
		}
	}

	@Test
	public void testConnectDifferentAddress2() throws Exception {
		int port = SocketUtils.getNextFreePort(PORT_RANGE_START, PORT_RANGE_END, null);
		ServerSocket sock = new ServerSocket();

		sock.bind(new InetSocketAddress(port));
		try {
			ServerSocket sock2 = new ServerSocket();

			try {
				// Linux seems to handle this differently...
				if(SystemUtils.IS_OS_WINDOWS) {
					// this works because we mix "localhost" and null-address
					sock2.bind(new InetSocketAddress("localhost", port));
				}
				/*fail("Should catch Exception here");
			} catch (BindException e) {
				TestHelpers.assertContains(e, "Address already in use");*/
			} finally {
				sock2.close();
			}
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			sock.close();
		}
	}

	@Test
	public void testConnectDifferentAddress3() throws Exception {
		int port = SocketUtils.getNextFreePort(PORT_RANGE_START, PORT_RANGE_END, null);
		ServerSocket sock = new ServerSocket();

		sock.bind(new InetSocketAddress((InetAddress)null, port));
		try {
			ServerSocket sock2 = new ServerSocket();

			try {
				// Linux seems to handle this differently...
				if(SystemUtils.IS_OS_WINDOWS) {
					// this works because we mix "localhost" and null-address
					sock2.bind(new InetSocketAddress("localhost", port));
				}
				/*fail("Should catch Exception here");
			} catch (BindException e) {
				TestHelpers.assertContains(e, "Address already in use");*/
			} finally {
				sock2.close();
			}
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			sock.close();
		}
	}

	@Test
	public void testConnectDifferentAddress4() throws Exception {
		int port = SocketUtils.getNextFreePort(PORT_RANGE_START, PORT_RANGE_END, "localhost");
		ServerSocket sock = new ServerSocket();

		sock.bind(new InetSocketAddress("localhost", port));
		try {
			ServerSocket sock2 = new ServerSocket();

			try {
				// Linux seems to handle this differently...
				if(SystemUtils.IS_OS_WINDOWS) {
					// this works because we mix "localhost" and null-address
					sock2.bind(new InetSocketAddress((InetAddress)null, port));
				}
				/*fail("Should catch Exception here");
			} catch (BindException e) {
				TestHelpers.assertContains(e, "Address already in use");*/
			} finally {
				sock2.close();
			}
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			sock.close();
		}
	}

	@Test
	public void testConnectDifferentAddress5() throws Exception {
		int port = SocketUtils.getNextFreePort(PORT_RANGE_START, PORT_RANGE_END, null);
		ServerSocket sock = new ServerSocket();

		sock.bind(new InetSocketAddress(port));
		try {
			ServerSocket sock2 = new ServerSocket();

			try {
				sock2.bind(new InetSocketAddress((InetAddress)null, port));
				fail("Should catch Exception here");
			} catch (BindException e) {
				TestHelpers.assertContains(e, "Address already in use");
			} finally {
				sock2.close();
			}
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			sock.close();
		}
	}

	@Test
	public void testConnectDifferentAddress6() throws Exception {
		int port = SocketUtils.getNextFreePort(PORT_RANGE_START, PORT_RANGE_END, null);
		ServerSocket sock = new ServerSocket();

		sock.bind(new InetSocketAddress((InetAddress)null, port));
		try {
			ServerSocket sock2 = new ServerSocket();

			try {
				sock2.bind(new InetSocketAddress(port));
				fail("Should catch Exception here");
			} catch (BindException e) {
				TestHelpers.assertContains(e, "Address already in use");
			} finally {
				sock2.close();
			}
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			sock.close();
		}
	}

	@Test
	public void testReserveNextFreePort() throws Exception {
		// I can get a free port
		int port = SocketUtils.reserveNextFreePort(portLimitStart, portLimitEnd, null);
		assertTrue("Port is out of range", port >= portLimitStart && port <= portLimitEnd);
		SocketUtils.freePort(port);
		log.info("Connected and freed port: " + port);

		// When I do not use sockets and free the ports, I can do this many more times than the available range
		for(int i = 0;i < 3*NUMBER_OF_SOCKETS;i++) {
			port = SocketUtils.reserveNextFreePort(portLimitStart, portLimitEnd, null);
			assertTrue("Port is out of range", port >= portLimitStart && port <= portLimitEnd);
			SocketUtils.freePort(port);
			log.info("Connected and freed port: " + port);
		}

		// test limits, here we reserve the ports and actually open the socket as well
		ServerSocket[] sock = new ServerSocket[NUMBER_OF_SOCKETS];
		for(int i = 0;i < NUMBER_OF_SOCKETS;i++) {
			port = SocketUtils.reserveNextFreePort(portLimitStart, portLimitEnd, null);
			log.info("Connecting port: " + port);
			sock[i] = openSocket(port, null);
			log.info("Reserved port: " + port);
		}

		// now reserving another port should fail
		try {
			expectReserveToFail();
		} finally {
			// free up sockets again
			for(ServerSocket socket : sock) {
				SocketUtils.freePort(socket.getLocalPort());
				socket.close();
			}
		}
	}

	@Test
	public void testGetNextFreePort() throws Exception {
		// I can get a free port
		int port = SocketUtils.getNextFreePort(PORT_RANGE_START, PORT_RANGE_END, "localhost");
		assertTrue("Port is out of range", port >= PORT_RANGE_START && port <= PORT_RANGE_END);

		// I can do this many times as I do not really "reserve" it
		for(int i = 0;i < 3*NUMBER_OF_SOCKETS;i++) {
			port = SocketUtils.getNextFreePort(portLimitStart, portLimitEnd, "localhost");
			assertTrue("Port is out of range", port >= portLimitStart && port <= portLimitEnd);
		}

		ServerSocket[] sock = new ServerSocket[NUMBER_OF_SOCKETS];
		for(int i = 0;i < NUMBER_OF_SOCKETS;i++) {
			port = SocketUtils.getNextFreePort(portLimitStart, portLimitEnd, null);
			sock[i] = openSocket(port, null);
			log.info("Using port: " + port);
		}

		try {
			// now retrieving another port should fail
			SocketUtils.getNextFreePort(portLimitStart, portLimitEnd, null);
			fail("Should throw Exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, Integer.toString(portLimitStart));
			TestHelpers.assertContains(e, Integer.toString(portLimitEnd));
			TestHelpers.assertContains(e, "No free socket port");
		} finally {
			// free up sockets again
			for(ServerSocket socket : sock) {
				socket.close();
			}
		}
	}


	@Test
	public void testIsPortAvailable() throws Exception {
		// first get a free port
		int port = SocketUtils.reserveNextFreePort(portLimitStart, portLimitEnd, null);
		try {
			assertTrue("Port is out of range", port >= portLimitStart && port <= portLimitEnd);

			assertTrue(SocketUtils.isPortAvailable(port, null));
			assertTrue(SocketUtils.isPortAvailable(port, ""));
			assertTrue(SocketUtils.isPortAvailable(port, "localhost"));
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			SocketUtils.freePort(port);
		}
	}

	@Test
	public void testIsPortAvailableFreePort() throws Exception {
		// first get a free port
		int port = SocketUtils.reserveNextFreePort(portLimitStart, portLimitEnd, null);
		try {
			assertTrue("Port is out of range", port >= portLimitStart && port <= portLimitEnd);

			for(int i = 0;i < TEST_COUNT;i++) {
				assertTrue("Port: " + port + " is not available, iteration-NULL: " + i, SocketUtils.isPortAvailable(port, null));
				assertTrue("Port: " + port + " is not available, iteration-\"\": " + i, SocketUtils.isPortAvailable(port, ""));
				assertTrue("Port: " + port + " is not available, iteration-localhost: " + i, SocketUtils.isPortAvailable(port, "localhost"));

				if(i % 1000 == 0) {
					log.info("Free Port - Iteration " + i + " of " + TEST_COUNT);
				}
			}
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			SocketUtils.freePort(port);
		}

	}

	@Test
	@Ignore("Fails sometimes without any apparent cause, some socket handling on Windows seems to be flaky...")
	public void testIsPortAvailablePortBlocked() throws Exception {
		// first get a free port
		int port = SocketUtils.reserveNextFreePort(portLimitStart, portLimitEnd, null);
		try {
			assertTrue("Port is out of range", port >= portLimitStart && port <= portLimitEnd);

			ServerSocket sock1 = new ServerSocket();
			ServerSocket sock2 = new ServerSocket();

			try {
				sock1.bind(new InetSocketAddress(port));
				sock2.bind(new InetSocketAddress("", port));

				for(int i = 0;i < TEST_COUNT;i++) {
					assertFalse("Iteration-NULL: " + i, SocketUtils.isPortAvailable(port, null));
					assertFalse("Iteration-\"\": " + i, SocketUtils.isPortAvailable(port, ""));
					assertFalse("Iteration-localhost: " + i, SocketUtils.isPortAvailable(port, "localhost"));

					if(i % 1000 == 0) {
						log.info("Blocked Port - Iteration " + i);
					}
				}
			} finally {
				sock1.close();
				sock2.close();
			}
		} catch (Exception e) {
			throw new Exception("While using port: " + port, e);
		} finally {
			SocketUtils.freePort(port);
		}

	}

	@Test
	public void testReserveReuseAfterTimeout() throws Exception {
		// reserve all ports
		ServerSocket[] sock = new ServerSocket[NUMBER_OF_SOCKETS];
		for(int i = 0;i < NUMBER_OF_SOCKETS;i++) {
			int port = SocketUtils.reserveNextFreePort(portLimitStart, portLimitEnd, null);
			sock[i] = openSocket(port, null);
			log.info("Reserved port: " + port);
		}

		try {
			// now reserve should fail
			expectReserveToFail();

			// wait the time it takes for the files to timeout
			Thread.sleep((SocketUtils.PORT_LOCK_FILE_TIMEOUT+1)*1000);

			// Now I can get a free port again
			int port = SocketUtils.reserveNextFreePort(PORT_RANGE_START, PORT_RANGE_END, null);
			assertTrue("Port is out of range", port >= PORT_RANGE_START && port <= PORT_RANGE_END);

			// until I free this one, I should again not get a free one
			try {
				expectReserveToFail();
			} finally {
				SocketUtils.freePort(port);
			}
		} finally {
			// free up sockets again
			for(ServerSocket socket : sock) {
				SocketUtils.freePort(socket.getLocalPort());
				socket.close();
			}
		}
	}

	private void expectReserveToFail() {
		try {
			SocketUtils.reserveNextFreePort(portLimitStart, portLimitEnd, null);
			fail("Should throw Exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, Integer.toString(portLimitStart));
			TestHelpers.assertContains(e, Integer.toString(portLimitEnd));
			TestHelpers.assertContains(e, "No free socket port");
		}
	}

	private ServerSocket openSocket(int port, String hostname) throws IOException, SocketException {
		ServerSocket sock = new ServerSocket();
		if(hostname == null) {
			sock.bind(new InetSocketAddress(port));
		} else {
			sock.bind(new InetSocketAddress(hostname, port));
		}

		return sock;
	}

	private static final int NUMBER_OF_THREADS = 10;
	private static final int NUMBER_OF_TESTS = 100;

	@Ignore("Do not run this test normally, just used for verifying some NanoHTTPD/Windows-Socket behavior")
	@Test
	public void testManyHttpd() throws IOException {
		List<NanoHTTPD> httpds = new ArrayList<NanoHTTPD>();

		// we have at max
		int startPort = 30000;
		for (int i = 0; i < NUMBER_OF_TESTS * NUMBER_OF_THREADS; i++, startPort++) {
			httpds.add(new NanoHTTPD(startPort));
		}

		for (NanoHTTPD httpd : httpds) {
			httpd.stop();
		}
	}

	@Ignore("I could not yet get this to work multi-threaded")
	@Test
	public void testMultipleThreads() throws Throwable {
		ThreadTestHelper helper =
				new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

		// Set to ensure that no port is used twice at the same time
		final Map<Integer, NanoHTTPD> ports = new ConcurrentHashMap<Integer, NanoHTTPD>();

		helper.executeTest(new ThreadTestHelper.TestRunnable() {

			@Override
			public void doEnd(int threadnum) throws Exception {
				// do stuff at the end ...
			}

			@Override
			public void run(int threadnum, int iter) throws Exception {
				{
					final int port = SocketUtils.reserveNextFreePort(30000, 30999, null);

					print(ports, threadnum, iter, port);

					// create socket-connection
					NanoHTTPD httpd;
					try {
						synchronized (SocketUtils.class)  {
							// I have no clue why this helps here! I tried doing this elsewhere, but it only
							// worked when done here, otherwise NanoHTTPD would sometimes fail to acquire the port
							Thread.sleep(30);

							httpd = new NanoHTTPD(port);
						}
					} catch (SocketException e) {
						log.warning("While adding port: " + port + ", iter: " + iter + ", having: " + ports.size() +
							" ports: " + new TreeSet<Integer>(ports.keySet()) + ": " + e);
						throw new IOException("While adding port: " + port + ", iter: " + iter + ", having: " + ports.size() +
							" ports: " + new TreeSet<Integer>(ports.keySet()), e);
					}

					assertNull("Already had port: " + port, ports.put(port, httpd));
				}
			}

			protected void print(final Map<Integer, NanoHTTPD> ports, int threadnum, int iter, int lport) {
				StringBuilder set = new StringBuilder(StringUtils.repeat('.', 1000));
				for (Integer port : ports.keySet()) {
					set.setCharAt(port - 30000, 'x');
				}
				while (set.length() > 0 && set.charAt(set.length() - 1) == '.') {
					set.setLength(set.length() - 1);
				}
				log.info(threadnum + "/" + iter + ": Found port: " + lport + ": " + set);
			}
		});

		// release the remaining ones
		for(NanoHTTPD httpd : ports.values()) {
			httpd.stop();
		}
	}
}
