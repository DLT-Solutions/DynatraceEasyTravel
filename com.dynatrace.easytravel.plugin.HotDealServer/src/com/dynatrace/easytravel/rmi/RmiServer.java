package com.dynatrace.easytravel.rmi;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.dynatrace.easytravel.HotDealProvider;
import com.dynatrace.easytravel.MessageServer;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;

import ch.qos.logback.classic.Logger;

public class RmiServer extends HotDealProvider implements MessageServer {

	public static final String SERVICE_NAME = "hot_deal_service";
	private static final String JAVA_RMI_SERVER_HOSTNAME = "java.rmi.server.hostname";

	private static final Logger log = LoggerFactory.make();

	private Remote rmiAccessRandomJourney;
	private Registry registry;

	private final int port;

	public RmiServer(int port, Registry registry, DataAccess dataAccess) {
		super(dataAccess);
		this.port = port;
		this.registry = registry;
	}

	@Override
	public void start() {
		
		//set server host
		setRMIServerHost();			
		
		try {
			provideJourneysViaRmi();
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	private void provideJourneysViaRmi() throws RemoteException, AccessException {
		RmiConnectionSocket journeyRmiProvider = getJourneyRmiProvider();
		bindToRmiRegistry(journeyRmiProvider);
	}


	private RmiConnectionSocket getJourneyRmiProvider() throws RemoteException {
		rmiAccessRandomJourney = new RmiAccessRandomJourney(getRandomJourneyProvider());
		RmiConnectionSocket journeyRmiProvider =
			(RmiConnectionSocket) UnicastRemoteObject
				.exportObject(rmiAccessRandomJourney, port);
		return journeyRmiProvider;
	}

	private void bindToRmiRegistry(RmiConnectionSocket jorneyRmiProvider) throws RemoteException, AccessException {
		registry.rebind(SERVICE_NAME, jorneyRmiProvider);
	}

	@Override
	public void close() {
		try {
			registry.unbind(SERVICE_NAME);
			UnicastRemoteObject.unexportObject(rmiAccessRandomJourney, true);
		} catch (AccessException e) {
			unbindException(e);
		} catch (RemoteException e) {
			unbindException(e);
		} catch (NotBoundException e) {
			unbindException(e);
		}
		rmiAccessRandomJourney = null;
	}

	private void unbindException(Exception e){
		log.warn("Could not unbind the hot deal service from RMI registry", e);
	}

	/**
	 * This method is setting java.rmi.server.hostname to the value of config.backendHost.
	 * This is needed to correctly publish the rmi registry that will be available remotely 
	 */
	private void setRMIServerHost() {
		String host = System.getProperty(JAVA_RMI_SERVER_HOSTNAME);
		
		if (host == null || host.trim().isEmpty()) {
			String backendHost = EasyTravelConfig.read().backendHost;
			if (backendHost != null) {
				log.debug("Setting java.rmi.server.hostname to: " +  backendHost);
				System.setProperty(JAVA_RMI_SERVER_HOSTNAME, backendHost);
			}
		} else {
			log.debug("java.rmi.server.hostname is set to: " +  host);
		}
	}

}
