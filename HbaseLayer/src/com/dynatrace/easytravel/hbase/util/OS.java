/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: OS.java
 * @date: 05.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.util;


/**
 * Importing Apache commons exec would be way over the top
 * 
 * @author stefan.moschinski
 */
public final class OS {

	private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

	public static boolean isWinOs() {
		return OS_NAME.startsWith("windows");
	}

	private OS() {
		// only for static access
	};
}
