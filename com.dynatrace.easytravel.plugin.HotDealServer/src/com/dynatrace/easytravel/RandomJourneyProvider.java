package com.dynatrace.easytravel;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.model.DataAccess;

public class RandomJourneyProvider {

	private int numberJourneys = 4;
	private DataAccess dataAccess;

	/**
	 * 
	 * @author stefan.moschinski
	 */
	public RandomJourneyProvider(DataAccess dataAccess) {
		this.dataAccess = dataAccess;
	}


	public List<Integer> getJourneyIds() throws RemoteException {
		List<Journey> journeys = new ArrayList<Journey>(dataAccess.getJourneys(numberJourneys * 5)); // multiply 5 to allow
// some randomness
		Collections.shuffle(journeys);
		return getJourneyIds(journeys, numberJourneys);
	}


	private List<Integer> getJourneyIds(List<Journey> journeys, int numberJourneys) {
		List<Integer> ids = new ArrayList<Integer>(numberJourneys);
		for (int i = 0; i < numberJourneys; i++) {
			ids.add(journeys.get(i).getId());
		}
		return ids;
	}
}
