package com.dynatrace.easytravel.rmi;


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.ipc.SocketUtils;


@RunWith(MockitoJUnitRunner.class)
public class RmiServerTest {

	private static final String JAVA_RMI_SERVER_HOSTNAME = "java.rmi.server.hostname";
	
	@Mock
	private Registry mockedRegistry;

	private RmiServer rmiServer;
	@Rule public ExpectedException exception = ExpectedException.none();

	private int port;

	@Before
	public void setUp() throws Exception {
		port = SocketUtils.reserveNextFreePort(13000, 14000, null);

		rmiServer = new RmiServer(port, mockedRegistry, null);
	}

	@After
	public void tearDown() {
		SocketUtils.freePort(port);
	}

	@Test
	public void startWithoutException() throws AccessException, RemoteException {
		rmiServer.start();
		verify(mockedRegistry, times(1)).rebind(eq(RmiServer.SERVICE_NAME), argThat(getRemoteMatcher()));
	}

	@Test
	public void startThrowsIllegalStateException() throws AccessException, RemoteException {
		doThrow(new RemoteException()).when(mockedRegistry).rebind(eq(RmiServer.SERVICE_NAME), argThat(getRemoteMatcher()));
		exception.expect(IllegalStateException.class);
		rmiServer.start();
	}

	
	//check this method
	@Test
	@Ignore
	public void doubleCallStartAndOneTimeStopWorks() throws AccessException, RemoteException, NotBoundException {
		rmiServer.start();
		rmiServer.close();
		rmiServer.start();
		verify(mockedRegistry, times(2)).rebind(eq(RmiServer.SERVICE_NAME), argThat(getRemoteMatcher()));
		verify(mockedRegistry, times(1)).unbind(RmiServer.SERVICE_NAME);
	}

	@Test
	public void closeDestroysRegistry() throws AccessException, RemoteException, NotBoundException {
		rmiServer.close();
		verify(mockedRegistry, times(1)).unbind(RmiServer.SERVICE_NAME);
	}
	
	/**
	 * property java.rmi.server.hostname should be set to config.backendHost 
	 */
	@Test
	public void testSettingServerHost() {
		String orgRMIHost = System.getProperty(JAVA_RMI_SERVER_HOSTNAME);
 
		try {
			EasyTravelConfig config = EasyTravelConfig.read();
			config.backendHost = "somehost";			
			rmiServer.start();			
			assertEquals("java.rmi.server.hostname is not set correctly","somehost",System.getProperty(JAVA_RMI_SERVER_HOSTNAME));
			
			//do it once again and check if this will be not overwritten
			config.backendHost = "otherHost";			
			rmiServer.start();			
			assertEquals("java.rmi.server.hostname is not set correctly","somehost",System.getProperty(JAVA_RMI_SERVER_HOSTNAME));			

			//clear current setting
			setRMIHostname(null);
			
			//test null
			config.backendHost = null;
			rmiServer.start();			
			assertEquals("java.rmi.server.hostname is not set correctly",null,System.getProperty(JAVA_RMI_SERVER_HOSTNAME));
			
		} finally {
			EasyTravelConfig.resetSingleton();
			setRMIHostname(orgRMIHost);
		}

	}
	
	
	private void setRMIHostname(String host) {
		if (host == null) {
			System.clearProperty(JAVA_RMI_SERVER_HOSTNAME);
		} else {
			System.setProperty(JAVA_RMI_SERVER_HOSTNAME, host);
		}
	}

	private Matcher<Remote> getRemoteMatcher() {
		return new ArgumentMatcher<Remote>() {
			@Override
			public boolean matches(Object item) {
				return item instanceof Remote;
			}
		};
	}
}
