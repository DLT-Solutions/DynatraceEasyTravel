package com.dynatrace.easytravel.jms;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import com.dynatrace.easytravel.MessageConnector;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class JmsConnector implements MessageConnector {

	private ConnectionFactory connectionFactory;
	private Queue requestQueue;
	private Queue answerQueue;
	private static String exceptionMessage = "Could not receive hot deals.";
	private static final Logger LOGGER = LoggerFactory.make();

	private final int port;
    private final String host;

	//host is not used currently
	public JmsConnector(String host, int port) {
		this.port = port;
        this.host = host;
		initiate();
	}

	private void initiate() {
		requestQueue = ActiveMQJMSClient.createQueue("request_queue");
		answerQueue = ActiveMQJMSClient.createQueue("answer_queue");

		if (connectionFactory == null) {
			String url = String.format("tcp://%s:%s", host, port);
			connectionFactory = new ActiveMQConnectionFactory(url);
		}
	}

	@Override
	public List<Integer> getHotDealIds() {

		List<Integer> deals = Collections.emptyList();
		if (connectionFactory != null) {
			Connection connection = null;
			try {

				connection = connectionFactory.createConnection();
				Session session = connection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);

				sendMessage(session);

				connection.start();

				LOGGER.debug("HOT DEAL DIAG ---------->>> plugin.HotDealClient / JmsConnector class / getHotDealIds(): call receiveDeals()");
				deals = receiveDeals(session);

			} catch (Exception exception) {
				LOGGER.warn(exceptionMessage, exception);
			} finally {
				if (connection != null) {
					try {
						connection.close();
						connection = null;
					} catch (JMSException e) {
						LOGGER.warn("Canot close connection", e);
					}
				}
			}
		}
		return deals;
	}

	@SuppressWarnings("unchecked")
	private List<Integer> receiveDeals(Session session) throws JMSException, InterruptedException {
		try (MessageConsumer consumer = session.createConsumer(answerQueue);) {
			ObjectMessage receivedDeals = null;

			for (int attempts = 10; attempts > 0 && receivedDeals == null; attempts--) {
				receivedDeals = (ObjectMessage) consumer.receive(1000);
			}

			Serializable receivedObject = receivedDeals.getObject();

			if (returnTypeCheck(receivedObject)) {
				return (List<Integer>) receivedObject;
			} else {
				throw new JMSException("Invalid return value");
			}
		}
	}

	private boolean returnTypeCheck(Serializable receivedObject) {
		return receivedObject instanceof List<?>
				&& ((List<?>) receivedObject).size() > 0
				&& ((List<?>) receivedObject).get(0) instanceof Integer;
	}

	private void sendMessage(Session session) throws JMSException {
		try (MessageProducer producer = session.createProducer(requestQueue);) {
			TextMessage dealRequest = session.createTextMessage("get_deals");
			producer.send(dealRequest);
		}
	}

	protected void setConnectionFactory(ConnectionFactory connectionFactory) {
		if(connectionFactory != null) {
			this.connectionFactory = connectionFactory;
		}
	}

}
