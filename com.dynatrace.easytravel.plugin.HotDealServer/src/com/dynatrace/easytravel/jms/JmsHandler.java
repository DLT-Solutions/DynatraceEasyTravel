package com.dynatrace.easytravel.jms;

import java.lang.management.ManagementFactory;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.management.ObjectName;

import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;

import com.dynatrace.easytravel.MessageHandler;
import com.dynatrace.easytravel.MessageServer;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;

import ch.qos.logback.classic.Logger;

public class JmsHandler implements MessageHandler {

	private final class JmsConnectionExceptionListener implements
			ExceptionListener {
		@Override
		public void onException(JMSException e) {
			LOGGER.error("Exception in jms Connection", e);
			//restart provider
			hotDealProvider.restart();
			//register exception listener
			registerConnectionExceptionListener();
		}
	}

	private MessageServer messageServer;
	private JmsHotDealProvider hotDealProvider;
	private JmsConnectionExceptionListener exceptionListener;
	private static final Logger LOGGER = LoggerFactory.make();

    private EasyTravelConfig config = EasyTravelConfig.read();

	@Override
	public void execute(int port, DataAccess dataAccess) {
		messageServer = JmsServer.getJmsServerSingletonPerPort(port, config.backendHost);
		messageServer.start();

		LOGGER.info("HOT DEAL DIAG ---------->>> plugin.HotDealServer / JmsHandler class / execute(): call JmsHotDealProvider()");
		hotDealProvider = new JmsHotDealProvider(dataAccess, config.backendHost, port);
		hotDealProvider.start();

		exceptionListener = new JmsConnectionExceptionListener();
		registerConnectionExceptionListener();
	}

	private void registerConnectionExceptionListener() {
		try {
			hotDealProvider.setJMSConnectionRequestListener(exceptionListener);
		} catch (JMSException e) {
			LOGGER.error("Cannot set ExceptionListener for hotDealProvider", e);
		}
	}

	@Override
	public void close() {
		if(hotDealProvider != null) {
			hotDealProvider.close();
		}

		if(messageServer != null) {
			messageServer.close();
		}
		exceptionListener = null;
	}

	public int getRequestQueueConsumers(){
		int customerCount = 0;

		try {
			//get number of consumers for queue using JMX beans
			Queue requestQueue = ActiveMQJMSClient.createQueue(JmsHotDealProvider.REQUEST_QUEUE);
			ObjectName on = ObjectNameBuilder.DEFAULT.getQueueObjectName(SimpleString.toSimpleString(requestQueue.getQueueName()), SimpleString.toSimpleString(requestQueue.getQueueName()), RoutingType.ANYCAST);
			customerCount = ((Integer)ManagementFactory.getPlatformMBeanServer().getAttribute(on, "ConsumerCount")).intValue();
			LOGGER.info("Number of customers: " + customerCount);
		}
		catch (Exception e) {
			LOGGER.warn("getRequestQueueConsumers failed - can't get the number of customers");
			LOGGER.trace("Error in getRequestQueueConsumers: ", e);
		}

		return customerCount;
	}

	/**
	 * for tests only
	 * @return
	 */
	public JmsHotDealProvider getJmsHotDealProvider() {
		return hotDealProvider;
	}

	@Override
	public void checkIfRunning() {
		try {
			if (getRequestQueueConsumers() <1) {
				LOGGER.warn("JmsHandler is not running");
				//restart provider
				hotDealProvider.restart();
				//register exception listener
				registerConnectionExceptionListener();
				LOGGER.info("JmsHandler restarted");
			}
		} catch (Exception e) {
			LOGGER.warn("Error when checking if JmsHandler is running", e);
		}

	}
}
