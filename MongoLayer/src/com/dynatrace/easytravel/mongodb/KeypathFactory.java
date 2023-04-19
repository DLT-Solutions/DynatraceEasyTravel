/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: KeyNameBuilder.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.google.common.base.Joiner;


/**
 *
 * @author stefan.moschinski
 */
public class KeypathFactory {

	public static String createKeypath(String keypath, String... subkeypath) {
		return createCompositeKey(keypath, subkeypath);
	}
	
	private static String createCompositeKey(String keypath, String... subpath) {
		return Joiner.on(BaseConstants.DOT).skipNulls().join(
				keypath,
				/* placeholder to use this method */null,
				(Object[]) subpath);
	}
}
