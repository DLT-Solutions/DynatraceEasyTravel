package com.dynatrace.easytravel;


public interface MessageConnectorFactory {
	
	MessageConnector createJms(String host, int port);
	
	MessageConnector createRmi(String host, int port); 
	
	MessageConnector create(MessagingMode messagingMode, String host, int port);

}
