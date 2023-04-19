package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class DbmsProcedureTest {
	private static final Logger log = LoggerFactory.make();

	@Test
	public void testDbmsProcedure() {
        DbmsProcedure proc = new DbmsProcedure(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID));

        assertFalse(proc.agentFound());
        assertFalse(proc.hasLogfile());
        assertNull(proc.getLogfile());
        assertNotNull(proc.getDetails());
        proc.clearStopListeners();
        proc.removeStopListener(null);
        proc.addStopListener(null);
        assertEquals(StopMode.SEQUENTIAL, proc.getStopMode());
        proc.setConsoleWriter(new PrintWriter(new ByteArrayOutputStream()));
        proc.setConsoleWriter(null);

        // can stop when nothing is running yet
		assertEquals(Feedback.Neutral, proc.stop());

		assertFalse(proc.isRunning());
		assertFalse(proc.isOperating());
		assertTrue(proc.isOperatingCheckSupported());
		assertTrue(proc.isStoppable());
		assertFalse(proc.isSynchronous());
		assertNotNull(proc.getName());
		assertTrue("Database Procedure should be enabled, but wasn't, switched to external Database in config file?", proc.isEnabled());

		// can stop even when not running
		assertEquals(Feedback.Neutral, proc.stop());

		assertFalse(proc.isRunning());
		assertFalse(proc.isOperating());
		assertTrue(proc.isOperatingCheckSupported());
		assertTrue(proc.isStoppable());
		assertFalse(proc.isSynchronous());
		assertNotNull(proc.getName());
		assertTrue(proc.isEnabled());

		// can start
		assertEquals(Feedback.Success, proc.run());

		assertTrue(proc.isRunning());
		assertTrue(proc.isOperating());
		assertTrue(proc.isOperatingCheckSupported());
		assertTrue(proc.isStoppable());
		assertFalse(proc.isSynchronous());
		assertNotNull(proc.getName());
		assertTrue(proc.isEnabled());

		// can stop
		assertEquals(Feedback.Neutral, proc.stop());

		assertFalse(proc.isRunning());
		assertFalse(proc.isOperating());
		assertTrue(proc.isOperatingCheckSupported());
		assertTrue(proc.isStoppable());
		assertFalse(proc.isSynchronous());
		assertNotNull(proc.getName());
		assertTrue(proc.isEnabled());

		// can stop even when not running
		assertEquals(Feedback.Neutral, proc.stop());

		assertFalse(proc.isRunning());
		assertFalse(proc.isOperating());
		assertTrue(proc.isOperatingCheckSupported());
		assertTrue(proc.isStoppable());
		assertFalse(proc.isSynchronous());
		assertNotNull(proc.getName());
		assertTrue(proc.isEnabled());


		// now with system properties
	}

	@Test
	public void testWithSettings() {
        DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);
        mapping.addSetting(new DefaultProcedureSetting("derby", "derby.test.key", "testvalue"));
		DbmsProcedure proc = new DbmsProcedure(mapping);

		assertNull(System.getProperty("derby.test.key"));
		// can start
		assertEquals(Feedback.Success, proc.run());

		assertEquals("testvalue", System.getProperty("derby.test.key"));

		assertEquals(Feedback.Neutral, proc.stop());

		assertNull(System.getProperty("derby.test.key"));
	}

	@Ignore("This test does not work yet, the ping() in DbmsProcedure returns successfully even for the MockSocketServer and hangs endlessly for MockRESTServer!")
	@Test
	public void testPortBlocked() throws IOException {
		MockSocketServer server = new MockSocketServer();
		//MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response12345243524\n\nasdfasdfas\n\n");
		EasyTravelConfig.read().internalDatabasePort = server.getPort();
		EasyTravelConfig.read().syncProcessTimeoutMs = 1000;
		try {
			DbmsProcedure proc = new DbmsProcedure(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID));

			assertEquals("Expect to fail during startup because the port is blocked",
					Feedback.Failure, proc.run());
		} finally {
			// reset config to not skew other tests
			EasyTravelConfig.resetSingleton();

			server.stop();
		}
	}


	private static class MockSocketServer {
		// The range of ports that we try to use for the listening.
		private static final int PORT_RANGE_START = 15100;
		private static final int PORT_RANGE_END = 15110;

		int port;

		private final ServerSocket myServerSocket;
		private Thread myThread;

		private final ThreadPoolExecutor sessionThreadPool;

		public MockSocketServer() throws IOException {
			// first try to get the next free port
			port = getNextFreePort();

			myServerSocket = new ServerSocket( port );
			myThread = new Thread( new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							while( true )
								new HTTPSession( myServerSocket.accept());
						}
						catch ( Exception ioe ) // NOPMD - imported code
						{
							// ignore expected closed-exception
							if(!(ioe instanceof SocketException && (ioe.getMessage().equals("socket closed") || ioe.getMessage().equals("Socket is closed")))) {
								ioe.printStackTrace();
							}
						}
					}
				}, "NanoHTTPD Acceptor Thread-" + port);
			myThread.setDaemon( true );
			myThread.start();

			sessionThreadPool = new ThreadPoolExecutor(0, 5, 10, TimeUnit.SECONDS,
					new ArrayBlockingQueue<Runnable>(5, true),
					new ThreadFactoryBuilder()
							.setDaemon(true)
							.setNameFormat("NanoHTTPD-" + port + " thread %d")
							.build());
			sessionThreadPool.setKeepAliveTime(10L, TimeUnit.SECONDS);
			sessionThreadPool.allowCoreThreadTimeOut(true);
		}

		private static final int getNextFreePort() throws IOException {
			for (int port = PORT_RANGE_START; port < PORT_RANGE_END; port++) {
				ServerSocket sock;
				try {
					sock = new ServerSocket(port);
					sock.close();
					//
					return port;
				} catch (IOException e) {
					// seems to be taken, try next one
					log.warn("Port " + port + " seems to be used already, trying next one...");
				}
			}

			throw new IOException("No free port found in the range of [" + PORT_RANGE_START + " - " + PORT_RANGE_END + "]");
		}

		public int getPort() {
			return port;
		}

		public void stop()
		{
			try
			{
				myServerSocket.close();
				myThread.join();
			}
			catch ( IOException ioe ) {} // NOPMD - imported code
			catch ( InterruptedException e ) {} // NOPMD - imported code

			sessionThreadPool.shutdown();
			try {
				if(!sessionThreadPool.awaitTermination(10, TimeUnit.SECONDS)) {
					sessionThreadPool.shutdownNow();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}


		/**
		 * Handles one session, i.e. parses the HTTP request
		 * and returns the response.
		 */
		private class HTTPSession implements Runnable
		{
			public HTTPSession( Socket s )
			{
				mySocket = s;
				sessionThreadPool.execute(this);
			}

			@Override
			public void run()
			{
				try
				{
					/*InputStream is = mySocket.getInputStream();
					if ( is == null) return;

					OutputStream os = mySocket.getOutputStream();

					while(is.read() != -1) {
						os.write(1);
					}*/

					mySocket.close();
				}
				catch ( IOException ioe )
				{
					ioe.printStackTrace();
					try
					{
						sendError( NanoHTTPD.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
					}
					catch ( Throwable t ) {} // NOPMD - imported code
				}
				catch ( Throwable ioe )
				{
					ioe.printStackTrace();
					try
					{
						sendError( NanoHTTPD.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Throwable: " + ioe.getMessage());
					}
					catch ( Throwable t ) {} // NOPMD - imported code
				}
			}

			private void sendError( String status, String msg ) throws InterruptedException
			{
				throw new InterruptedException(msg);
			}

			private Socket mySocket;
		}

	}

}
