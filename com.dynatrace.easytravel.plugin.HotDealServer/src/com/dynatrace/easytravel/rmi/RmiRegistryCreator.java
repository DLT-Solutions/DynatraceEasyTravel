package com.dynatrace.easytravel.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class RmiRegistryCreator {

	private final static Logger log = LoggerFactory.make();
	
	private int port;

	private Registry registry;

	public RmiRegistryCreator(int port) {
		this.port = port;
	}

	public void create() throws RemoteException{
		try {
			registry = LocateRegistry.createRegistry(port);
			log.info("RMI Registry started successfully.");
		} catch (ExportException e) {
			throw new IllegalStateException(e);
		}
	}

	protected Registry getRegistry() {
		return registry;
	}
}
