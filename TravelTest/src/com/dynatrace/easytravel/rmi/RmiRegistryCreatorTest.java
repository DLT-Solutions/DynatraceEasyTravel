package com.dynatrace.easytravel.rmi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.dynatrace.easytravel.integration.IntegrationTestBase;


/**
 *
 * @author stefan.moschinski
 */
public class RmiRegistryCreatorTest {
	public static int PORT = 13339;
	private RmiRegistryCreator rmiRegistryCreator;

	@Rule public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		// increase port by one for each test to avoid having them interact with each other!
		PORT++;

		IntegrationTestBase.checkPort(PORT);

		rmiRegistryCreator = new RmiRegistryCreator(PORT);
	}

	@Test
	public void createWorksWithoutException() throws RemoteException {
		rmiRegistryCreator.create();
		assertNotNull(rmiRegistryCreator.getClass());
		assertNotNull(rmiRegistryCreator.getRegistry());
	}

	@Test
	public void createThrowsException() throws RemoteException {
		exception.expect(IllegalStateException.class);

		// creating twice fails
		rmiRegistryCreator.create();
		rmiRegistryCreator.create();
		assertNull(rmiRegistryCreator.getRegistry());
	}
}
