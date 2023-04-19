/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: VerificationService.java
 * @date: 05.03.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.business.webservice;


/**
 * Not much logic here, the main purpose of this class is to make the JLT-60533 possible
 * 
 * @author stefan.moschinski
 */
public class VerificationService {


	public boolean isUserBlacklisted(String userName, String finalEndpoint) {
		if (userName == null || userName.isEmpty()) {
			return true;
		}

		return false;
	}
}
