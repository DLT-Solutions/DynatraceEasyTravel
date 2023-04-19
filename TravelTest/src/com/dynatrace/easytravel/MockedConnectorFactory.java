package com.dynatrace.easytravel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import com.dynatrace.easytravel.jms.JmsConnector;
import com.dynatrace.easytravel.rmi.RmiConnector;


public class MockedConnectorFactory implements MessageConnectorFactory {

	private List<Integer> journeyIds = Arrays.asList(1, 2, 3, 4);
	private List<Integer> journeyIds2 = Arrays.asList(5, 6, 7, 8);

	@SuppressWarnings("unchecked")
	@Override
	public MessageConnector createJms(String host, int port) {
		MessageConnector jmsConnector = mock(JmsConnector.class);
		when(jmsConnector.getHotDealIds()).thenReturn(journeyIds, journeyIds2);
		return jmsConnector;
	}

	@SuppressWarnings("unchecked")
	@Override
	public MessageConnector createRmi(String host, int port) {
		MessageConnector rmiConnector = mock(RmiConnector.class);
		when(rmiConnector.getHotDealIds()).thenReturn(journeyIds, journeyIds2);
		return rmiConnector;
	}

	@Override
	public MessageConnector create(MessagingMode messagingMode, String host, int port) {
		if (MessagingMode.JMS.equals(messagingMode)) {
			return createJms(host, port);
		} else if (MessagingMode.RMI.equals(messagingMode)) {
			return createRmi(host, port);
		}

		throw new IllegalArgumentException("The arguments " + messagingMode +
					" and " + port + " are invalid.");
	}

}
