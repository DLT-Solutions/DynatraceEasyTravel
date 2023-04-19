package com.dynatrace.easytravel;


import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import com.dynatrace.easytravel.frontend.data.DataProviderInterface;
import com.dynatrace.easytravel.frontend.data.JourneyDO;


public class WebServiceDealsTest {

	private WebServiceDeals webServiceDeals;
	private DataProviderInterface dataProvider;
	private List<Integer> journeyIds;
	private JourneyDO journey1;
	private JourneyDO journey2;
	private JourneyDO journey3;
	private JourneyDO journey4;

	@Before
	public void setUp() throws Exception {
		dataProvider = mock(DataProviderInterface.class);
		webServiceDeals = new WebServiceDeals(dataProvider);
		
		journey1 = new JourneyDO(1, "One", null, null, null, null, null, 100, null);
		journey2 = new JourneyDO(2, "Two", null, null, null, null, null, 200, null);
		journey3 = new JourneyDO(3, "Three", null, null, null, null, null, 300, null);
		journey4 = new JourneyDO(4, "Four", null, null, null, null, null, 400, null);

		when(dataProvider.getJourneyById(1)).thenReturn(journey1);
		when(dataProvider.getJourneyById(2)).thenReturn(journey2);
		when(dataProvider.getJourneyById(3)).thenReturn(journey3);
		when(dataProvider.getJourneyById(4)).thenReturn(journey4);
		
		journeyIds = Arrays.asList(1, 2, 3, 4);
	}
	
	
	@Test
	public void returnEmptyArrayWhenNullIsPassed () throws RemoteException {
		assertEquals(webServiceDeals.getDeals(null).length, 0);
		
	}
	
	@Test 
	public void returnsTheExactNumberOfJourneys() throws RemoteException {
		assertEquals(webServiceDeals.getDeals(journeyIds).length, 4);
	}
	

	@Test 
	public void returnsTheRightJourneys() throws RemoteException {
		JourneyDO[] returnedJourney = new JourneyDO[journeyIds.size()];
		returnedJourney = webServiceDeals.getDeals(journeyIds);
		
		assertEquals(returnedJourney[0], journey1);
		assertEquals(returnedJourney[1], journey2);
		assertEquals(returnedJourney[2], journey3);
		assertEquals(returnedJourney[3], journey4); 
	}
	
}
