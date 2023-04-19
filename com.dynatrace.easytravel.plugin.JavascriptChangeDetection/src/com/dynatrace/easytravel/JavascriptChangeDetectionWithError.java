/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: AbstractJavascriptChangeDetection.java
 * @date: 18.09.2014
 * @author: dieter.ladenhauf
 */
package com.dynatrace.easytravel;



/**
 *
 * @author cwat-dladenha
 */
public class JavascriptChangeDetectionWithError extends JavascriptChangeDetection {

	private final String JS_ERROR_PATTERN = "var x = ;";

	@Override
	protected String getFileContent() {
		return JS_ERROR_PATTERN;
	}

}
