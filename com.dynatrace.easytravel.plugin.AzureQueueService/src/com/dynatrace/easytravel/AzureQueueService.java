package com.dynatrace.easytravel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.ipc.NativeApplication;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractPlugin;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

import ch.qos.logback.classic.Logger;

public class AzureQueueService extends AbstractPlugin implements NativeApplication {

    private static final int TIMEOUT_IN_SECONDS = 15;
    private Map<String, ListenableFuture<String>> results = new ConcurrentHashMap<>();
    private EasyTravelConfig CONFIG = EasyTravelConfig.read();
    private static final Logger LOGGER = LoggerFactory.make();

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynatrace.easytravel.ipc.NativeApplication#setChannel(java.lang.String)
     */
    @Override
    public void setChannel(String channel) {
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynatrace.easytravel.ipc.NativeApplication#sendAndReceive(java.lang.
     * String)
     */
    @Override
    public String sendAndReceive(String creditCard) throws IOException {
        Connection connectionRequest = null;
        Session sessionRequest = null;

        Connection connectionResponse = null;
        Session sessionResponse = null;

        MessageConsumer consumer = null;

        String isCardValid = FAILED;

        try {
            QueueConfiguration azureRequestQueueConfig = createQueueConfiguration(CONFIG.azureServiceBusConnectionString, CONFIG.azureRequestQueue);
            QueueConfiguration azureResponseQueueConfig = createQueueConfiguration(CONFIG.azureServiceBusConnectionString, CONFIG.azureResponseQueue);

            connectionRequest = azureRequestQueueConfig.getCf().createConnection(azureRequestQueueConfig.getCsb().getSasKeyName(), azureRequestQueueConfig.getCsb().getSasKey());
            connectionRequest.start();
            sessionRequest = connectionRequest.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            connectionResponse = azureResponseQueueConfig.getCf().createConnection(azureResponseQueueConfig.getCsb().getSasKeyName(), azureResponseQueueConfig.getCsb().getSasKey());
            connectionResponse.start();
            sessionResponse = connectionResponse.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            consumer = sessionResponse.createConsumer(azureResponseQueueConfig.getDestination());
            isCardValid = validateCard(sessionRequest, creditCard, azureRequestQueueConfig, consumer);
            LOGGER.info("Result of card validation (card number: " + creditCard + "): " + isCardValid);
        } catch (NamingException | JMSException e) {
        	LOGGER.error("Problem occurred during receiving result from queue");
            e.printStackTrace();
        } finally {
            closeConsumer(consumer);
            closeQueueConnection(connectionRequest, sessionRequest);
            closeQueueConnection(connectionResponse, sessionResponse);
        }
        
        return isCardValid;
    }

    private void closeConsumer(MessageConsumer consumer) {
        try {
            if (consumer != null) {
                consumer.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void closeQueueConnection(Connection connection, Session session) {
        try {
            if (session != null) {
                session.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {
            if (connection != null) {
                connection.stop();
                connection.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private String validateCard(Session sessionRequest, String cardNumber, QueueConfiguration azureRequestQueueConfig,
                                MessageConsumer consumer) throws JMSException {
        String correlationID = new Date().toString();
        ListenableFuture<String> result = sendRequest(sessionRequest, cardNumber, azureRequestQueueConfig, correlationID, consumer);
        try {
            return result.get(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            return stopWaitingForMessage(correlationID);
        }
    }

    private ListenableFuture<String> sendRequest(Session sessionRequest, String cardNumber, QueueConfiguration queueRequest,
                                                 String correlationID, MessageConsumer consumer) throws JMSException {

        ListenableFuture<String> resultOfCardValidation;
        try (MessageProducer producer = sessionRequest.createProducer(queueRequest.getDestination())) {
            BytesMessage message = sessionRequest.createBytesMessage();
            message.writeBytes(cardNumber.getBytes());
            message.setJMSCorrelationID(correlationID);
            resultOfCardValidation = registerMessageListener(correlationID, consumer);
            producer.send(message);
            LOGGER.trace(String.format("Sent message with correlationID: %s\n", correlationID));
        }
        return resultOfCardValidation;


    }

    private ListenableFuture<String> registerMessageListener(String correlationID, MessageConsumer consumer)
            throws JMSException {
        SettableFuture<String> futureResult = SettableFuture.create();
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    if (results.containsKey(message.getJMSCorrelationID())) {
                        BytesMessage byteMessage = (BytesMessage) message;
                        byte[] byteArr = new byte[(int) byteMessage.getBodyLength()];
                        byteMessage.readBytes(byteArr);
                        String result = "true".equals(new String(byteArr, "UTF-8")) ? VALID : INCORRECT;
                        futureResult.set(result);
                        message.acknowledge();
                        LOGGER.trace("received message with correlation: " + message.getJMSCorrelationID());
                        results.remove(correlationID);
                    }
                } catch (JMSException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        results.put(correlationID, futureResult);
        return futureResult;
    }

    private String stopWaitingForMessage(String correlationID) {
    	LOGGER.trace("Stop waiting for message with ID: " + correlationID);
        results.remove(correlationID);
        return FAILED;
    }

    private QueueConfiguration createQueueConfiguration(String connectionString, String queueName) throws NamingException {
        ConnectionStringBuilder csb = new ConnectionStringBuilder(connectionString, queueName);
        Hashtable<String, String> hashtable = new Hashtable<>();
        hashtable.put("connectionfactory.SBCF", "amqps://" + csb.getEndpoint().getHost() + "?amqp.idleTimeout=120000&amqp.traceFrames=true");
        hashtable.put("queue.QUEUE", queueName);
        hashtable.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        InitialContext context = new InitialContext(hashtable);
        ConnectionFactory cf = (ConnectionFactory) context.lookup("SBCF");
        Destination queue = (Destination) context.lookup("QUEUE");
        return new QueueConfiguration(csb, cf, queue);
    }
}

