package com.dynatrace.easytravel.rmi;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RmiConnectionSocketFactory {

	private final int port;
	private final String host;
	private Registry registry;
	public static final String SERVICE_NAME = "hot_deal_service";
	
	public RmiConnectionSocketFactory(String host, int port) {
		this.port = port;
		this.host = host;
	}
	
	public Remote getConnectionSocket() throws RemoteException, NotBoundException, AccessException {
		makeSureRegistryIsSet();
		return registry
				.lookup(SERVICE_NAME);
	}

	private void makeSureRegistryIsSet() {
		if(registry == null)
			try {				
				setRegistry(LocateRegistry.getRegistry(host, port));
			} catch (RemoteException ex) {
				new RemoteException("Could not get RMI registry on port " + port + ". Verify whether RMI registry has been started.", ex);
			}
	}
	
	protected void setRegistry(Registry registry) {
		this.registry = registry;
	}
	


	
	
}
