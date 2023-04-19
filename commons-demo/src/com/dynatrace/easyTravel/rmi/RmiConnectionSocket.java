package com.dynatrace.easytravel.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface RmiConnectionSocket extends Remote {
	List<Integer> getHotDealIds() throws RemoteException;
}
