package com.dynatrace.easytravel.plugins;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dynatrace.easytravel.jquery.JQueryEffectsCloudflare;

public class JQueryEffectsCloudflareTest {


	@Test
	public void testJQueryEffectsCloudflare() throws Exception {
		JQueryEffectsCloudflare cloudFlare = new JQueryEffectsCloudflare();

		// You do not need to call addJQueryPaths() first, because getJQueryPaths()
		// internally calls addJQueryPaths() after initializing jQueryPathBuilder.
		// In fact, if you do call addJQueryPaths() first, you will get a null pointer
		// exception, as then the jQueryPathBuilder will still be null.
		String s=cloudFlare.WgetJQueryPaths();
		// System.out.println("Got path <" + s + ">");

		// Verify that the correct paths have been returned
		assertTrue(s.equals("<script type='text/javascript' src='//cdnjs.cloudflare.com/ajax/libs/jquery/1.8.1/jquery.js' ></script><script type='text/javascript' src='//cdnjs.cloudflare.com/ajax/libs/jquery/1.8.1/jquery.min.js' ></script>"));

	}
}

