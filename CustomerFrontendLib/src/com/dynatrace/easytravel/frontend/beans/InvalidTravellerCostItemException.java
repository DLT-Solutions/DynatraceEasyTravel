/**
 *
 */
package com.dynatrace.easytravel.frontend.beans;

/**
 * Unchecked exception to throw a com.dynatrace.easytravel... exception instead of java.lang.ArrayIndexOutOfBoundsException which is
 * not captured by dynaTrace by default.
 *
 * @author tomasz.wieremjewicz
 * @date 9 kwi 2019
 */
public class InvalidTravellerCostItemException extends RuntimeException {
	private static final long serialVersionUID = 7533762721142243235L;

	public InvalidTravellerCostItemException(String str, Throwable cause) {
		super(str, cause);
	}
}