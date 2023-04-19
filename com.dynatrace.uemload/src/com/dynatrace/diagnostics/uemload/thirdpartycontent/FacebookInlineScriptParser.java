/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: FacebookInlineScriptParser.java
 * @date: 11.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.thirdpartycontent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Parses html to extract url of facebook inline javascript for embedding a like button.
 *
 * @author peter.lang
 */
public class FacebookInlineScriptParser {

	/**
	 * Parse for url of facebook script.
	 *
	 * @param inlineScript script embedded in document containing facebook code snippet.
	 * @return the url of the facebook script to load or null if not found
	 * @author peter.lang
	 */
	public static String parseInlineScript(String inlineScript) {
		Pattern p = Pattern.compile("<script>.*js.src = \"\\/\\/(.*.js).*\".*");
		Matcher m = p.matcher(inlineScript);
		if (m.matches()) {
			return "http://" + m.group(1);
		}
		return null;
	}

}
