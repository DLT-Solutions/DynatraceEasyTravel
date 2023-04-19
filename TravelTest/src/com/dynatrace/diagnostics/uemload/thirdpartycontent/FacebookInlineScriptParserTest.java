/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: FacebookInlineScriptParserTest.java
 * @date: 11.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.thirdpartycontent;

import org.junit.Assert;

import org.junit.Test;

import com.dynatrace.diagnostics.uemload.thirdpartycontent.FacebookInlineScriptParser;


/**
 *
 * @author peter.lang
 */
public class FacebookInlineScriptParserTest {

	/**
	 * Test method for {@link com.dynatrace.diagnostics.uemload.thirdpartycontent.FacebookInlineScriptParser#parseInlineScript(java.lang.String)}.
	 */
	@Test
	public void testParseInlineScript() {
		String inlineScript = "<script>(function(d, s, id) {                               " +
				"var js, fjs = d.getElementsByTagName(s)[0];               " +
				"if (d.getElementById(id)) {return;}                       " +
				"js = d.createElement(s); js.id = id;                      " +
				"js.src = \"//connect.facebook.net/en_US/all.js#xfbml=1\";   " +
				"fjs.parentNode.insertBefore(js, fjs);                   }(document, 'script', 'facebook-jssdk'));</script>";
		String facebookJsUrl = FacebookInlineScriptParser.parseInlineScript(inlineScript);
		Assert.assertEquals("http://connect.facebook.net/en_US/all.js", facebookJsUrl);
	}

	/**
	 * Test method for {@link com.dynatrace.diagnostics.uemload.thirdpartycontent.FacebookInlineScriptParser#parseInlineScript(java.lang.String)}.
	 */
	@Test
	public void testScriptNotFound() {
		String inlineScript = "<script>( fjs.parentNode.insertBefore(js, fjs);                   }(document, 'script', 'facebook-jssdk'));</script>";
		String facebookJsUrl = FacebookInlineScriptParser.parseInlineScript(inlineScript);
		Assert.assertNull(facebookJsUrl);
	}

}
