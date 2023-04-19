package com.dynatrace.easytravel.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.MessageConnector;

public class RmiConnector implements MessageConnector, RmiConnectionSocket {

	private RmiConnectionSocket rmiConnectionSocket;
	private final RmiConnectionSocketFactory socketFactory;
	private static final Logger logger = Logger.getLogger(RmiConnector.class
			.getName());


	public RmiConnector(RmiConnectionSocketFactory socketFactory) {
		this.socketFactory = socketFactory;
		init();
	}
	
	private synchronized void init() {
		try {
			if (rmiConnectionSocket == null) {
				rmiConnectionSocket = (RmiConnectionSocket) socketFactory.getConnectionSocket();
			}			
		} catch (RemoteException e) {
			logException("Could not establish a connection to the RMI service provider", e);
		} catch (NotBoundException e) {
			logException("Could not find the RMI service " + RmiConnectionSocketFactory.SERVICE_NAME + ".", e);
		}
	}

	@Override
	public List<Integer> getHotDealIds() {
		try {
			//call init method every time when we downloading hot deals. In case it was not initialized correctly in the constructor (server was not 
			//available) it have a chance to do it now.  
			init();
			
			if (rmiConnectionSocket != null) {
				return rmiConnectionSocket.getHotDealIds();
			}
			logger.warning("Not connected to the RMI service provider. Please check your configuration.");			
		} catch (RemoteException e) {
			logException("Unable to receive the hot deals via RMI", e);
		}
		return Collections.emptyList();
	}

	private void logException(String exceptionText, Exception ex) {
		logger.log(Level.WARNING, exceptionText, ex);
	}

}
