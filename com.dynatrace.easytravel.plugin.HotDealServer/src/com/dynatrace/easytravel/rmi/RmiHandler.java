package com.dynatrace.easytravel.rmi;

import java.rmi.RemoteException;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.MessageHandler;
import com.dynatrace.easytravel.MessageServer;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;

public class RmiHandler implements MessageHandler {

	private static final Logger log = LoggerFactory.make();

	private RmiRegistryCreator rmiRegistryCreator;
	private MessageServer rmiServer;

	@Override
	public void execute(int port, DataAccess dataAccess) {

		try {
			
			if (rmiRegistryCreator == null) {
				rmiRegistryCreator = new RmiRegistryCreator(port);				
				rmiRegistryCreator.create();
			}

			if (rmiServer == null) {
				rmiServer = new RmiServer(port, rmiRegistryCreator.getRegistry(), dataAccess);
			}
			rmiServer.start();			
		} catch (IllegalStateException e) {
			log.error("RMI Server could not start .", e);
		} catch (RemoteException e) {
			log.error("RMI Registry could not start. Possibly, it is already running or another program such as dynaTrace server uses the port.", e);
		}
	}	
	
	@Override
	public void close() {
		if (rmiServer != null) {
			rmiServer.close();
		}
	}

	@Override
	public void checkIfRunning() {
		// nothing to do
	}
}
