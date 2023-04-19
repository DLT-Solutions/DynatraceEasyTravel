package com.dynatrace.easytravel.webservices;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.logging.Logger;

import com.dynatrace.easytravel.business.client.BookingServiceStub;
import com.dynatrace.easytravel.business.client.JourneyServiceStub;
import com.dynatrace.easytravel.business.webservice.GetJourneyNamesDocument;
import com.dynatrace.easytravel.business.webservice.GetJourneyNamesResponseDocument;
import com.dynatrace.easytravel.business.webservice.StoreBookingDocument;
import com.dynatrace.easytravel.business.webservice.StoreBookingDocument.StoreBooking;
import com.dynatrace.easytravel.util.ServiceStubProvider;

public class TestClientXMLBeans {
    private static Logger log = Logger.getLogger(TestClientXMLBeans.class.getName());

    public static void main(java.lang.String args[]){
        try{
        	JourneyServiceStub journeyService = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);

        	getNames(journeyService);

        	BookingServiceStub bookingService = ServiceStubProvider.getServiceStub(BookingServiceStub.class);
        	storeBooking(bookingService);
        } catch(Exception e){
            e.printStackTrace();
            System.err.println("\n\n\n");
        }
    }

    public static void getNames(JourneyServiceStub stub){
        try{
        	GetJourneyNamesDocument document = GetJourneyNamesDocument.Factory.newInstance();
    		document.setGetJourneyNames(GetJourneyNamesDocument.GetJourneyNames.Factory.newInstance());
        	GetJourneyNamesResponseDocument res =
                stub.getJourneyNames(document);

        	log.fine("Journey names: " + Arrays.toString(res.getGetJourneyNamesResponse().getReturnArray()));
        } catch(Exception e){
            e.printStackTrace();
            System.err.println("\n\n\n");
        }
    }

    private static void storeBooking(BookingServiceStub stub) throws RemoteException {
    	StoreBookingDocument doc = StoreBookingDocument.Factory.newInstance();

    	StoreBooking booking = doc.addNewStoreBooking();
    	booking.setJourneyId(2);
    	booking.setUserName("hainer");
    	booking.setCreditCard("234092322340302");

    	doc.setStoreBooking(booking);

    	stub.storeBooking(doc);

    	log.fine("Stored booking for Journey 2 for user hainer with credit card 234092322340302");
	}
}
