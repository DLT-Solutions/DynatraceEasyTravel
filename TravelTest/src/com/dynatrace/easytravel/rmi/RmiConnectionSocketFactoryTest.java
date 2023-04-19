package com.dynatrace.easytravel.rmi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * 
 * @author stefan.moschinski
 */
@RunWith(MockitoJUnitRunner.class)
public class RmiConnectionSocketFactoryTest {

	@Rule public ExpectedException exception = ExpectedException.none(); 
	
	@Mock private Registry mockedRegistry;
	@Mock private RmiConnectionSocket expectedRemote;

	private final int port = 1111;
	private RmiConnectionSocketFactory rmiFactory; 
	
	@Before
	public void setUp() throws Exception {
		rmiFactory = new RmiConnectionSocketFactory(null, port);
		rmiFactory.setRegistry(mockedRegistry);
	}

	@Test
	public void getConnectionSocketRuns() throws AccessException, RemoteException, NotBoundException {
		when(mockedRegistry.lookup(RmiConnectionSocketFactory.SERVICE_NAME)).thenReturn(expectedRemote);
		assertEquals(expectedRemote, rmiFactory.getConnectionSocket());
	}

	@Test
	public void getConnectionSocketThrowsNotBoundException() throws AccessException, RemoteException, NotBoundException {
		when(mockedRegistry.lookup(RmiConnectionSocketFactory.SERVICE_NAME)).thenThrow(new NotBoundException());
		exception.expect(NotBoundException.class);
		rmiFactory.getConnectionSocket();
	}
}
