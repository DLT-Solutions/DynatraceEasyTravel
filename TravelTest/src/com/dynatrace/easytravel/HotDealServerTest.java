package com.dynatrace.easytravel;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.jms.JmsHandler;
import com.dynatrace.easytravel.jms.JmsHotDealProvider;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.rmi.RmiHandler;
import com.dynatrace.easytravel.spring.PluginConstants;

/**
 * @author rafal.psciuk
 *
 */
public class HotDealServerTest {

	private static final Logger LOGGER = Logger.getLogger(HotDealServerTest.class.getName());

	private HotDealServer server = null;
	private int port;
	private DataAccess dataAccess = Mockito.mock(DataAccess.class);


	@Before
	public void setup() throws IOException {
		port = SocketUtils.reserveNextFreePort(13000, 14000, null);
		LOGGER.info("Reserved port " + port);
		server = new HotDealServer();
		server.setPort(port);
		server.setDatabaseAccess(dataAccess);
	}

	@After
	public void teardown() {
		SocketUtils.freePort(port);
		assertTrue(SocketUtils.isPortAvailable(port, "localhost"));
	}

	@Ignore("Since we are not able to destroy RMI registry, this should be used as an integration test. It reserves a new port and creates a new registry for each test method")
	@Test
	public void testRMIInitialization() {
		server.setMode(MessagingMode.RMI.toString());

		//start server
		server.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, new Object[]{});
		MessageHandler messageHandler = server.getMessageHandler();
		assertNotNull(messageHandler);
		assertEquals("Invalid message handler class", RmiHandler.class, messageHandler.getClass());

		//stop server
		server.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, new Object[]{});
	}

	@Ignore("Since we are not able to destroy RMI registry, this should be used as an integration test. It reserves a new port and creates a new registry for each test method")
	@Test
	public void testRMIEnableDisable() {
		server.setMode(MessagingMode.RMI.toString());

		//start server
		server.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, new Object[]{});

		MessageHandler messageHandler = server.getMessageHandler();

		//stop server
		server.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, new Object[]{});
		//start server
		server.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, new Object[]{});

		assertSame(messageHandler, server.getMessageHandler());

		//stop server
		server.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, new Object[]{});
	}

	@Test
	public void testJMS() throws Exception {
		server.setMode(MessagingMode.JMS.toString());

		//start server
		server.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, new Object[]{});

		MessageHandler messageHandler = server.getMessageHandler();
		assertNotNull(messageHandler);
		assertEquals("Invalid message handler class", JmsHandler.class, messageHandler.getClass());

		JmsHandler jmsHandler = (JmsHandler)messageHandler;
		JmsHotDealProvider hotDealProvider = jmsHandler.getJmsHotDealProvider();
		assertNotNull(hotDealProvider);

		int consumerCount = jmsHandler.getRequestQueueConsumers();
		assertTrue("number of registered consumers should be >0 " + consumerCount, consumerCount > 0);

		//stop provider and check that there are no consumers registered
		hotDealProvider.close();
		consumerCount = jmsHandler.getRequestQueueConsumers();
		assertFalse("number of registered consumers should not be >0 " + consumerCount, consumerCount > 0);

		//restart run a health check, JmsHandler will be restarted
		server.setHealthCheckInterval(0);

		server.doExecute(PluginConstants.PERIODIC_EXECUTE, new Object[]{});
		//check if there are any consumers registered
		consumerCount = jmsHandler.getRequestQueueConsumers();
		assertTrue("number of registered consumers should be >0 "  + consumerCount, consumerCount > 0);

		server.setHealthCheckInterval(HotDealServer.DEFAULT_HEALTH_CHECK_INTERVAL);

		//stop server
		server.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, new Object[]{});
	}
}
