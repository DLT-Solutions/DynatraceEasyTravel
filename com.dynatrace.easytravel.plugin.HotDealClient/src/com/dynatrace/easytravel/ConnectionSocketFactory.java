package com.dynatrace.easytravel;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ConnectionSocketFactory {
	
	Remote getConnectionSocket() throws RemoteException, NotBoundException;
}
