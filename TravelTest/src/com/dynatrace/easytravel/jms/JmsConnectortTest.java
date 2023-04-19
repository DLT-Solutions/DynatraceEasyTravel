package com.dynatrace.easytravel.jms;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;


/**
 * 
 * @author stefan.moschinski
 */
public class JmsConnectortTest {

	private static int PORT = 5446;
	private JmsConnector jmsConnector; 
	private List<Integer> expectedHotDealIds = Arrays.asList(1, 2, 3, 4);
	
	@Before
	public void setUp() throws Exception {
		jmsConnector = new JmsConnector(null, PORT);
	}

	@Test
	public void testGetHotDealIds() throws JMSException {
		ConnectionFactory mockedFactory = getAndPrepareMockedFactory();
		
		jmsConnector.setConnectionFactory(mockedFactory);
		List<Integer> hotDealIds = jmsConnector.getHotDealIds();
		
		assertReceivedDealsAreValid(hotDealIds);
	}
	

	private void assertReceivedDealsAreValid(List<Integer> hotDealIds) {
		assertEquals(expectedHotDealIds.size(), hotDealIds.size());
		
		int counter = 0; 
		for(Integer expectedId : expectedHotDealIds) {
			assertEquals(expectedId, hotDealIds.get(counter++));
		}
	}

	private ConnectionFactory getAndPrepareMockedFactory() throws JMSException {
		ConnectionFactory mockedFactory = mock(ConnectionFactory.class);
		Connection mockedConnection = mock(Connection.class);
		Session mockedSession = mock(Session.class);
		MessageConsumer mockedConsumer = mock(MessageConsumer.class);
		MessageProducer mockedProducer = mock(MessageProducer.class);
		ObjectMessage mockedMessage = mock(ObjectMessage.class);
		
		when(mockedFactory.createConnection()).thenReturn(mockedConnection);
		when(mockedConnection.createSession(anyBoolean(), anyInt())).thenReturn(mockedSession);
		
		
		when(mockedSession.createConsumer(argThat(getDestinationMatcher()))).thenReturn(mockedConsumer);
		when(mockedSession.createProducer(argThat(getDestinationMatcher()))).thenReturn(mockedProducer);
		
		when(mockedConsumer.receive(anyLong())).thenReturn(mockedMessage);
		when(mockedMessage.getObject()).thenReturn((Serializable) expectedHotDealIds);
		
		return mockedFactory;
	}

	private Matcher<Destination> getDestinationMatcher() {
		return new ArgumentMatcher<Destination>() {
			@Override
			public boolean matches(Object item) {
				return item instanceof Destination;
			}
		};
	}
}
