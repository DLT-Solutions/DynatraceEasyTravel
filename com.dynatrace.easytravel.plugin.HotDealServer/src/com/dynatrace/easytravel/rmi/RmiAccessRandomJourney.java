package com.dynatrace.easytravel.rmi;

import java.rmi.RemoteException;
import java.util.List;

import com.dynatrace.easytravel.RandomJourneyProvider;

public class RmiAccessRandomJourney implements RmiConnectionSocket {	
	
	private RandomJourneyProvider randomJourneyProvider;

	/**
	 * 
	 * @param randomJourneyProvider
	 * @author stefan.moschinski
	 */
	public RmiAccessRandomJourney(RandomJourneyProvider randomJourneyProvider) {
		this.randomJourneyProvider = randomJourneyProvider;
	}

	@Override
	public List<Integer> getHotDealIds() throws RemoteException {
		return randomJourneyProvider.getJourneyIds();
	}

}
