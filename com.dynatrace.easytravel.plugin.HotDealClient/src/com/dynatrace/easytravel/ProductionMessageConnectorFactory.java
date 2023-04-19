package com.dynatrace.easytravel;

import com.dynatrace.easytravel.jms.JmsConnector;
import com.dynatrace.easytravel.rmi.RmiConnectionSocketFactory;
import com.dynatrace.easytravel.rmi.RmiConnector;


public class ProductionMessageConnectorFactory implements MessageConnectorFactory {

	@Override
	public MessageConnector createJms(String host, int port) {
		return new JmsConnector(host, port);
	}

	@Override
	public MessageConnector createRmi(String host, int port) {
		return new RmiConnector(new RmiConnectionSocketFactory(host, port)); 
	}
	
	@Override
	public MessageConnector create(MessagingMode messagingMode, String host,  int port) {
		if (messagingMode.equals(MessagingMode.JMS)) {
			return createJms(host, port);
		} else if (messagingMode.equals(MessagingMode.RMI)) {
			return createRmi(host, port);
		}
		
		throw new IllegalArgumentException("The arguments " + messagingMode + 
				" and " + port + " are invalid.");
	}

}
