package com.dynatrace.easytravel.jms;


import static org.junit.Assert.*;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 *
 * @author stefan.moschinski
 */
public class JmsServerTest {
	@Rule public ExpectedException exception = ExpectedException.none();


	public static final int JMS_PORT = 5446;
	public static final String JMS_HOST = "localhost";

	@Test
	public void getJmsServerSingletonReturnsSingleton() {
		JmsServer server = iniateJmsServer();
		JmsServer server2 = iniateJmsServer();
		assertEquals(server, server2);
	}

	@Test
	public void startCreatesEmbeddedJmsServer() {
		JmsServer server = iniateJmsServer();
		server.start();
		assertNotNull(server.getEmbeddedJMSServer());
	}

	@Test
	public void startReturnsTheSameEmbeddedJmsServer() {
		JmsServer server = iniateJmsServer();
		server.start();
		EmbeddedActiveMQ embeddedJMS_old = server.getEmbeddedJMSServer();
		server.start();
		EmbeddedActiveMQ embeddedJMS_new = server.getEmbeddedJMSServer();
		assertEquals(embeddedJMS_old, embeddedJMS_new);
	}

	@Test
	public void close() {
		JmsServer server = iniateJmsServer();
		server.start();
		server.close();
		assertNull(server.getEmbeddedJMSServer());
	}

	@Test
	public void doubleCallingCloseThrowsIllegalStateException() {
		JmsServer server = iniateJmsServer();
		server.start();
		server.close();
		exception.expect(IllegalStateException.class);
		server.close();
	}

	@Test
	public void queueMessagingTest() {
		JmsServer server = null;
		Connection connection = null;

		try {
			server = iniateJmsServer();
			server.start();

			Queue exampleQueue1 = ActiveMQJMSClient.createQueue("exampleQueue1");
			Queue exampleQueue2 = ActiveMQJMSClient.createQueue("exampleQueue2");

			String url = String.format("tcp://%s:%s", JMS_HOST, JMS_PORT);
			@SuppressWarnings("resource")
			ConnectionFactory cf = new ActiveMQConnectionFactory(url);

	        connection = cf.createConnection();
	        connection.start();

	        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

	        MessageProducer producer1 = session.createProducer(exampleQueue1);
	        MessageProducer producer2 = session.createProducer(exampleQueue2);
	        MessageConsumer messageConsumer1 = session.createConsumer(exampleQueue1);
	        MessageConsumer messageConsumer2 = session.createConsumer(exampleQueue2);
	        TextMessage message = null, messageReceived1 = null, messageReceived2 = null;

	        messageReceived1 = (TextMessage) messageConsumer1.receive(1000);
	        messageReceived2 = (TextMessage) messageConsumer2.receive(1000);
	        assertNull(messageReceived1);
	        assertNull(messageReceived2);

	        message = session.createTextMessage("This is a text message 1");
	        producer1.send(message);
	        messageReceived1 = (TextMessage) messageConsumer1.receive(1000);
	        messageReceived2 = (TextMessage) messageConsumer2.receive(1000);
	        assertEquals("This is a text message 1", messageReceived1.getText());
	        assertNull(messageReceived2);

	        message = session.createTextMessage("This is a text message 2");
	        producer2.send(message);
	        messageReceived1 = (TextMessage) messageConsumer1.receive(1000);
	        messageReceived2 = (TextMessage) messageConsumer2.receive(1000);
	        assertNull(messageReceived1);
	        assertEquals("This is a text message 2", messageReceived2.getText());
		}
		catch (JMSException e) {
			fail("There was an unexpected exception: " + e.getMessage());
		}
		finally {
			if (connection != null) {
				try {
					connection.stop();
				} catch (JMSException e) {
					fail("Couldn't close JMSConnection");
					e.printStackTrace();
				}
			}

			if (server != null) {
				server.close();
			}
		}
	}

	private JmsServer iniateJmsServer() {
		return JmsServer.getJmsServerSingletonPerPort(JMS_PORT, JMS_HOST);
	}
}
