package com.dynatrace.easytravel.webservices;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.logging.Logger;

import com.dynatrace.easytravel.business.client.JourneyServiceStub;
import com.dynatrace.easytravel.business.webservice.GetJourneyNamesDocument;
import com.dynatrace.easytravel.business.webservice.GetJourneyNamesResponseDocument;
import com.dynatrace.easytravel.business.webservice.GetJourneysDocument;
import com.dynatrace.easytravel.business.webservice.GetJourneysResponseDocument;
import com.dynatrace.easytravel.jpa.business.xsd.Journey;
import com.dynatrace.easytravel.util.ServiceStubProvider;

public class TestGetJourneyNames {

    private static Logger log = Logger.getLogger(TestGetJourneyNames.class.getName());

	public static void main(String args[]) throws RemoteException {

    	JourneyServiceStub journeyService = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);
    	getNames(journeyService);
    	getJourneys(journeyService);
    }

    public static void getNames(JourneyServiceStub journeyService) throws RemoteException {

    	GetJourneyNamesDocument document = GetJourneyNamesDocument.Factory.newInstance();
		document.setGetJourneyNames(GetJourneyNamesDocument.GetJourneyNames.Factory.newInstance());
    	GetJourneyNamesResponseDocument res = journeyService.getJourneyNames(document);
    	log.fine("Journey names: " + Arrays.toString(res.getGetJourneyNamesResponse().getReturnArray()));
    }

    public static void getJourneys(JourneyServiceStub journeyService) throws RemoteException {

    	GetJourneysDocument document = GetJourneysDocument.Factory.newInstance();
		document.setGetJourneys(GetJourneysDocument.GetJourneys.Factory.newInstance());
    	GetJourneysResponseDocument res = journeyService.getJourneys(document);
    	Journey[] array = res.getGetJourneysResponse().getReturnArray();
    	for (Journey journey : array)
    	{
    		log.fine("Journey: " + journey.getName() + " / " + journey.getId() + " from " + journey.getStart().getName() + " to " + journey.getDestination().getName());
    	}
    }
}
