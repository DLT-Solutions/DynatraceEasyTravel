package com.dynatrace.easytravel.jms;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.jms.*;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import com.dynatrace.easytravel.HotDealProvider;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;

import ch.qos.logback.classic.Logger;

public class JmsHotDealProvider extends HotDealProvider {
	public static final String ANSWER_QUEUE = "answer_queue";
	public static final String REQUEST_QUEUE = "request_queue";
	private final ConnectionFactory connectionFactory;
	private final Queue requestQueue;
	private final Queue answerQueue;
	private volatile Connection connection = null;
	private Session session;
	private MessageProducer messageProducer;

	private static final Logger log = LoggerFactory.make();

	public JmsHotDealProvider(DataAccess dataAccess, String host, int port) {
		super(dataAccess);
		// Directly instantiate the JMS Queue object.
		requestQueue = ActiveMQJMSClient.createQueue(REQUEST_QUEUE);
		answerQueue = ActiveMQJMSClient.createQueue(ANSWER_QUEUE);

		String url = String.format("tcp://%s:%s", host, port);
		connectionFactory = new ActiveMQConnectionFactory(url);
	}

	@Override
	public void start() {
		try {

			if (connection == null) {
				connection = connectionFactory.createConnection();
			}

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			connection.start();

			createRequestListener();

		} catch (JMSException jmsException) {
			log.warn("Could not start HotDealProvider");
			jmsException.printStackTrace();
		} catch (Exception exception) {
			log.warn("Could not start HotDealProvider");
			exception.printStackTrace();
		}
	}

	void setJMSConnectionRequestListener( ExceptionListener listener ) throws JMSException {
		if (connection != null) {
			connection.setExceptionListener(listener);
		} else {
			log.error("JMSConnection is null, ExceptionListener will not be set.");
		}
	}

	synchronized void restart() {
		close();
		connection = null;
		session = null;
		messageProducer = null;

		start();
	}

	private void createRequestListener() throws JMSException, RemoteException {
		MessageConsumer messageConsumer = session.createConsumer(requestQueue);
		messageConsumer.setMessageListener(new DealRequestListener());
		log.info("Hot deal provider started");
	}

	private boolean isRequestMessage(Message message) throws JMSException {
		return message != null && message instanceof TextMessage
				&& ((TextMessage) message).getText().equals("get_deals");
	}

	@Override
	public void close() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (JMSException e) {
			log.warn("Unable to close the HotDeals provider", e);
		}
	}

	private synchronized void sendDeals(Session session) throws JMSException,
			RemoteException {

		if (messageProducer == null) {
			messageProducer = session.createProducer(answerQueue);
		}

		ObjectMessage objectMessage = session
				.createObjectMessage((Serializable) getRandomJourneyProvider().getJourneyIds());
		messageProducer.send(objectMessage);
		log.debug("HOT DEAL DIAG ---------->>> plugin.HotDealServer / JmsHotDealProvider class / sendDeals(): call getRandomJourneyProvider()");
	}

	private class DealRequestListener implements MessageListener {

		@Override
		public void onMessage(Message message) {
			try {
				if (isRequestMessage(message)) {
					sendDeals(session);
				}
			} catch (JMSException e) {
				log.warn("Unable to send the ids of the deals", e);
			} catch (RemoteException e) {
				log.warn("Unable to send the ids of the deals", e);
			} catch (Exception e) {
				log.warn("Unable to send the ids of the deals", e);
			}

		}
	}

}
