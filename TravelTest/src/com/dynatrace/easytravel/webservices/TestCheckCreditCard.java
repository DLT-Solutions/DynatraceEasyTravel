package com.dynatrace.easytravel.webservices;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import com.dynatrace.easytravel.business.client.BookingServiceStub;
import com.dynatrace.easytravel.business.webservice.CheckCreditCardDocument;
import com.dynatrace.easytravel.business.webservice.CheckCreditCardResponseDocument;
import com.dynatrace.easytravel.util.ServiceStubProvider;

public class TestCheckCreditCard
{
    private static Logger log = Logger.getLogger(TestCheckCreditCard.class.getName());

	public static void main(String[] args) throws RemoteException {
		BookingServiceStub bookingSercice = ServiceStubProvider.getServiceStub(BookingServiceStub.class);

		String creditCard = (args.length > 0) ? args[0] : "123";
		log.info("checking: " + creditCard);
		boolean valid = checkCreditCard(bookingSercice, creditCard);
		log.info("check result: " + valid);
	}

	static boolean checkCreditCard(BookingServiceStub stub, String creditCard) throws RemoteException
	{
		CheckCreditCardDocument doc = CheckCreditCardDocument.Factory.newInstance();
		doc.setCheckCreditCard(CheckCreditCardDocument.CheckCreditCard.Factory.newInstance());
		doc.getCheckCreditCard().setCreditCard(creditCard);

		CheckCreditCardResponseDocument res = stub.checkCreditCard(doc);
		boolean valid = res.getCheckCreditCardResponse().getReturn();
		return valid;
	}

}
