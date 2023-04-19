package com.dynatrace.easytravel.webservices;

import static org.junit.Assert.assertFalse;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;

import com.dynatrace.easytravel.business.client.JourneyServiceStub;
import com.dynatrace.easytravel.business.webservice.AddJourneyDocument;
import com.dynatrace.easytravel.business.webservice.AddJourneyDocument.AddJourney;
import com.dynatrace.easytravel.business.webservice.AddLocationDocument;
import com.dynatrace.easytravel.business.webservice.AddLocationDocument.AddLocation;
import com.dynatrace.easytravel.business.webservice.FindJourneysDocument;
import com.dynatrace.easytravel.business.webservice.FindJourneysDocument.FindJourneys;
import com.dynatrace.easytravel.business.webservice.FindJourneysResponseDocument;
import com.dynatrace.easytravel.jpa.business.xsd.Journey;
import com.dynatrace.easytravel.util.ServiceStubProvider;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author cwpl-rpsciuk
 *
 */
//@Ignore ("integration test")
public class TestAddJourneys { 
	private static final Logger log = Logger.getLogger(TestAddJourneys.class.getName());
	
	private static final String JOURNEY_START = "Berlin";
	private static final String JOURNEY_DEST = "Berlin";
	private static final String JOURNEY_NAME = "testJourney";

	@Test
	public void addJourney() throws RemoteException{
		JourneyServiceStub journeyService = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);
		String name = JOURNEY_NAME + System.currentTimeMillis();
		createJourney(journeyService, name);
		findJourney(journeyService, name);
	}
	
	@Test
	public void addLocation() throws RemoteException{
		JourneyServiceStub journeyService = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);
		createLocation(journeyService);
	}
	

	private String createJourney(JourneyServiceStub journeyService, String name) throws RemoteException {
		AddJourney addJourney = AddJourney.Factory.newInstance();
		
		addJourney.setName(name);
		addJourney.setTenantName("Personal Travel Inc.");
		GregorianCalendar cal = new GregorianCalendar(2015, 05, 01);
		addJourney.setDateFrom(cal);
		addJourney.setDateTo(cal);
		addJourney.setFrom(JOURNEY_START);
		addJourney.setTo(JOURNEY_DEST);
		addJourney.setAmount(111);
		addJourney.setPicture(new byte[] {1});

		AddJourneyDocument doc = AddJourneyDocument.Factory.newInstance();
		doc.setAddJourney(addJourney);

		journeyService.addJourney(doc);
		return name;
	}
	
	private void findJourney(JourneyServiceStub journeyService, final String name) throws RemoteException {
		FindJourneys findJourneys = FindJourneys.Factory.newInstance();
		findJourneys.setDestination(JOURNEY_DEST);
		findJourneys.setFromDate(0);
		findJourneys.setToDate(33333333333333l);
		
		FindJourneysDocument doc = FindJourneysDocument.Factory.newInstance();
		doc.setFindJourneys(findJourneys);
		
		FindJourneysResponseDocument findJourneysResp = journeyService.findJourneys(doc);
		List<Journey> returnedJourneys = new ArrayList<Journey>(Arrays.asList(findJourneysResp.getFindJourneysResponse().getReturnArray()));
		log.info("Number of returned journeys: " + returnedJourneys.size() );
		assertFalse("No journey found", returnedJourneys.isEmpty());
						
		List<Journey> filtered = Lists.newArrayList(Iterables.filter(returnedJourneys,new Predicate<Journey>() {
			@Override
			public boolean apply(Journey input) {
				log.info("found journey " + input.getName());
				return name.equals(input.getName());
			}
			
			@Override
			public boolean test(Journey input) {
				return apply(input);
			}
		}
		));
				
		log.info("Number of filtered journeys: " + filtered.size() );
		assertFalse("Journey not found", filtered.isEmpty());		
	}
	
	private void createLocation(JourneyServiceStub journeyService) throws RemoteException {
		AddLocation addLocation = AddLocation.Factory.newInstance();
		addLocation.setName("testLocation" + System.currentTimeMillis());
		
		AddLocationDocument doc = AddLocationDocument.Factory.newInstance();
		doc.setAddLocation(addLocation);
		
		journeyService.addLocation(doc);
	}
}
