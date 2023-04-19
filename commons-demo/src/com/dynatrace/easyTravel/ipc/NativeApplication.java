package com.dynatrace.easytravel.ipc;

import java.io.IOException;

import com.dynatrace.easytravel.misc.UserType;
import com.dynatrace.easytravel.spring.Plugin;

/**
 * An interface to encapsulate how we access the Native Application
 * for credit checks
 *
 * @author dominik.stadler
 */
public interface NativeApplication extends Plugin {

	String VALID = "Valid";

	String INCORRECT = "Incorrect";
	
	String FAILED = "Failed";

	/**
	 * Set the channel that should be used, e.g. for named pipes this is the name of the pipe.
	 *
	 * @param channel
	 */
	void setChannel(String channel);

	/**
	 * Send a credit card number to the native application for processing
	 * and return the received text. It will return a string that starts
	 * with "Valid" if the check was done successfully, otherwise it will
	 * return some other string.
	 *
	 * @param creditCard
	 * @return
	 * @throws IOException Thrown if communication to the native application
	 * 					could not be established.
	 */
	String sendAndReceive(String creditCard) throws IOException;
	
	default String sendAndReceive(UserType userType, String creditCard) throws IOException {
		return sendAndReceive(creditCard);
	}
}
