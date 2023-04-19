package com.dynatrace.easytravel.config;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.NanoHTTPD;

public abstract class AbstractReservationTester {
	private static final Logger log = LoggerFactory.make();

	protected static final int NUMBER_OF_PRE_TESTS = 10;
	protected static final int NUMBER_OF_TESTS = 100;

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
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

	protected abstract TomcatResourceReservation reserveResources() throws IOException;

	@Test
	public void testReserve() throws IOException {
		TomcatResourceReservation res = reserveResources();
		assertNotNull(res);

		TomcatResourceReservation res2 = reserveResources();
		assertNotNull(res2);

		assertFalse("Port-Had: " + res + " and " + res2, res.getPort() == res2.getPort());
		assertFalse("ShutdownPort-Had: " + res + " and " + res2, res.getShutdownPort() == res2.getShutdownPort());
		assertFalse("AjpPort-Had: " + res + " and " + res2, res.getAjpPort() == res2.getAjpPort());

		res.release();
		res2.release();
	}

	@Test
	public void testReserveNoAjp() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.frontendAjpPortRangeStart = 0;
		config.frontendAjpPortRangeEnd = 0;
		config.backendAjpPortRangeStart = 0;
		config.backendAjpPortRangeEnd = 0;

		try {
			TomcatResourceReservation res = reserveResources();
			assertNotNull(res);

			TomcatResourceReservation res2 = reserveResources();
			assertNotNull(res2);

			assertFalse("Port-Had: " + res + " and " + res2, res.getPort() == res2.getPort());
			assertFalse("ShutdownPort-Had: " + res + " and " + res2, res.getShutdownPort() == res2.getShutdownPort());
			assertEquals("AjpPort-Had: " + res + " and " + res2, 0, res.getAjpPort());
			assertEquals("AjpPort-Had: " + res + " and " + res2, 0, res2.getAjpPort());

			res.release();
			res2.release();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}


	@Test
	public void testManyReservations() throws Throwable {
		// Queue for keeping the reservations and to free them up later on
		final Queue<TomcatResourceReservation> resQueue =
				new ArrayDeque<TomcatResourceReservation>();

		// Set to ensure that no port is used twice at the same time
		final Map<Integer, NanoHTTPD> ports = new ConcurrentHashMap<Integer, NanoHTTPD>();

		EasyTravelConfig config = EasyTravelConfig.read();
		config.frontendPortRangeStart = 30000;
		config.frontendPortRangeEnd = 30999;
		config.frontendShutdownPortRangeStart = 31000;
		config.frontendShutdownPortRangeEnd = 31999;
		config.frontendAjpPortRangeStart = 32000;
		config.frontendAjpPortRangeEnd = 32999;
		config.backendPortRangeStart = 30000;
		config.backendPortRangeEnd = 30999;
		config.backendShutdownPortRangeStart = 31000;
		config.backendShutdownPortRangeEnd = 31999;
		config.backendAjpPortRangeStart = 32000;
		config.backendAjpPortRangeEnd = 32999;

		try {
			for(int iter = 0;iter < NUMBER_OF_TESTS;iter++) {
				TomcatResourceReservation res = reserveResources();
				assertNotNull(res);

				print(ports, iter, res);

				// create socket-connection
				NanoHTTPD httpd = usePort(ports, iter, res);

				assertTrue(resQueue.offer(res));
				assertNull("Already had port: " + res.getPort(), ports.put(res.getPort(), httpd));

				// start freeing only after some iterations to keep a few ports blocked always
				if (iter > NUMBER_OF_PRE_TESTS) {
					freeOnePort(resQueue, ports, iter);
				}
			}

			// release the remaining ones
			while (!resQueue.isEmpty()) {
				TomcatResourceReservation poll = resQueue.poll();
				poll.release();
				assertNotNull("Did not have port in list of reserved ports: " + poll.getPort() + "\nPorts: " + ports +
						"\nQueue: " + resQueue,
						ports.remove(poll.getPort()));
			}

			assertTrue("Now we should not have any ports listed any more", ports.isEmpty());
		} finally {
			// restore properties
			EasyTravelConfig.resetSingleton();
		}
	}

	protected NanoHTTPD usePort(final Map<Integer, NanoHTTPD> ports, int iter, TomcatResourceReservation res) throws IOException {
		NanoHTTPD httpd;
		try {
			httpd = new NanoHTTPD(res.getPort());
		} catch (SocketException e) {
			log.warn("While adding port: " + res.getPort() + ", iter: " + iter + ", having: " + ports.size() +
				" ports: " + new TreeSet<Integer>(ports.keySet()) + ": " + e);
			throw new IOException("While adding port: " + res.getPort() + ", iter: " + iter + ", having: " + ports.size() +
				" ports: " + new TreeSet<Integer>(ports.keySet()), e);
		}

		return httpd;
	}

	protected void freeOnePort(final Queue<TomcatResourceReservation> resQueue, final Map<Integer, NanoHTTPD> ports, int iter) {
		TomcatResourceReservation poll = resQueue.poll();
		if (poll != null) {
			NanoHTTPD httpd = ports.remove(poll.getPort());
			assertNotNull("Did not have port in list of reserved ports: " + poll.getPort() + "\nPorts: " +
					ports + "\nQueue: " + resQueue,
					httpd);
			httpd.stop();
			poll.release();

			log.info(iter + ": Freed port: " + poll.getPort());
		}
	}

	protected void print(final Map<Integer, NanoHTTPD> ports, int iter, TomcatResourceReservation res) {
		StringBuilder set = new StringBuilder(StringUtils.repeat('.', 1000));
		for (Integer port : ports.keySet()) {
			set.setCharAt(port - 30000, 'x');
		}
		while (set.length() > 0 && set.charAt(set.length() - 1) == '.') {
			set.setLength(set.length() - 1);
		}
		log.info(iter + ": Found port: " + res.getPort() + ": " + set);
	}
}
