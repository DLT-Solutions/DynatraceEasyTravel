package com.dynatrace.easytravel;

import java.io.IOException;

import com.dynatrace.easytravel.cache.PaymentService;
import com.dynatrace.easytravel.spring.AbstractPlugin;

public class DummyPaymentService extends AbstractPlugin implements PaymentService {
	public static String response = PAYMENT_ACCEPTED;
	public static IOException exception = null;

    @Override
    public String callPaymentService(String bookingId, String creditCard, String user, double amount, String location,
    		String tenant) throws IOException {
    	if(exception != null) {
    		throw exception;
    	}
    	return response;
    }
}
