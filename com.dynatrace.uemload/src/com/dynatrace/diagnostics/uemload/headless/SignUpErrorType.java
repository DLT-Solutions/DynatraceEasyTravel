package com.dynatrace.diagnostics.uemload.headless;

/***
 * 
 * @author tomasz.wieremjewicz
 *
 */
public enum SignUpErrorType {
	MISSING_FIRSTNAME(0),
	MISSING_LASTNAME(1),
	MISSING_EMAIL(2),
	WRONG_CONFIRM_EMAIL(3),
	MISSING_PASSWORD(4),
	WRONG_CONFIRM_PASSWORD(5);
	
	public final int number;
	
	private SignUpErrorType(int number) {
		this.number = number;
	}
	
	public static SignUpErrorType valueOfNumber(int number) {
	    for (SignUpErrorType e : values()) {
	        if (e.number == number) {
	            return e;
	        }
	    }
	    return null;
	}
}
