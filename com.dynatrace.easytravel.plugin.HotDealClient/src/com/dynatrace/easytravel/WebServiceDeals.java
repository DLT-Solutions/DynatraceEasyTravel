package com.dynatrace.easytravel;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.dynatrace.easytravel.frontend.data.DataProviderInterface;
import com.dynatrace.easytravel.frontend.data.JourneyDO;

public class WebServiceDeals {

	private final DataProviderInterface dataProvider;

	public WebServiceDeals(DataProviderInterface dataProvider) {
		this.dataProvider = dataProvider;
	}

	/**
	 * @param journeyIds The ids of the journeys or rather hot deals.
	 * @return An array with the hot deals. If the given list including the journeyIds is empty, an empty array is returned.
	 * @throws RemoteException
	 */
	public JourneyDO[] getDeals(List<Integer> journeyIds) throws RemoteException {
		if (journeyIds == null)
			return new JourneyDO[0];

		List<JourneyDO> deals = new ArrayList<JourneyDO>(5);
		for (Integer id : journeyIds) {
			deals.add(dataProvider.getJourneyById(id));
		}
		
		return deals.toArray(new JourneyDO[deals.size()]);
	}
}
