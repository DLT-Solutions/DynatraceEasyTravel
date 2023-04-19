package com.dynatrace.easytravel.cache;

import java.io.IOException;

import com.dynatrace.easytravel.spring.Plugin;

/**
 * A payment service.
 *
 * @author philipp.grasboeck
 */
public interface PaymentService extends Plugin {

	// possible outcomes of callPaymentService

	String PAYMENT_ACCEPTED = "PAYMENT_ACCEPTED";
	String ALREADY_PAID = "ALREADY_PAID";
	String CC_INVALID = "CC_INVALID";
	String CC_WRONG_USER = "CC_WRONG_USER";
	String CC_EXPIRED = "CC_EXPIRED";
	String OTHER_ERROR = "OTHER_ERROR";
	String UPDATE_ERROR = "UPDATE_ERROR";

	String callPaymentService(String bookingId, String creditCard, String user, double amount,
			String location, String tenant) throws IOException;

}
