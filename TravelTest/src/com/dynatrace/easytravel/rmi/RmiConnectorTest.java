package com.dynatrace.easytravel.rmi;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * 
 * @author stefan.moschinski
 */
@RunWith(MockitoJUnitRunner.class)
public class RmiConnectorTest {

	@Mock private RmiConnectionSocketFactory socketFactory;
	@Mock private RmiConnectionSocket connectionSocket;

	private RmiConnector rmiConnector;

	private List<Integer> expectedHotDealIds = Arrays.asList(1, 2, 3, 4);

	@Before
	public void setUp() throws Exception {
		when(socketFactory.getConnectionSocket()).thenReturn(connectionSocket);
		rmiConnector = new RmiConnector(socketFactory);
	}

	@Test
	public void getHotDealReturnsExpectedDeals() throws RemoteException {
		when(connectionSocket.getHotDealIds()).thenReturn(expectedHotDealIds);
		List<Integer> hotDealIds = rmiConnector.getHotDealIds();
		assertReceivedDealsAreValid(hotDealIds);
	}


	private void assertReceivedDealsAreValid(List<Integer> hotDealIds) {
		assertEquals(expectedHotDealIds.size(), hotDealIds.size());

		int counter = 0;
		for(Integer expectedId : expectedHotDealIds) {
			assertEquals(expectedId, hotDealIds.get(counter++));
		}
	}
}
