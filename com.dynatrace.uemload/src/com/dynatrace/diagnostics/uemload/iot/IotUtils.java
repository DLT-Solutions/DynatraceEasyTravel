package com.dynatrace.diagnostics.uemload.iot;

import java.security.SecureRandom;

/**
 * @author Michal.Bakula
 */
public class IotUtils {
	private static final SecureRandom RANDOM = new SecureRandom();

	private static final String CHARS = "ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx";

	private IotUtils() {
		//empty constructor to hide default one
	}

	public static String getRandomString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
		}
		return sb.toString();
	}

}
