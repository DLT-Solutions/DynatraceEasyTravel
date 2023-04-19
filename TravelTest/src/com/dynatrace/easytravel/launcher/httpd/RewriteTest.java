package com.dynatrace.easytravel.launcher.httpd;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestHelpers;


public class RewriteTest {
	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private PrintWriter writer = new PrintWriter(out);

	@After
	public void tearDown() {
		// we modify the config for some tests, ensure that we reset it at the end of the test
		EasyTravelConfig.resetSingleton();
	}

	@Test
	public void testWriteModule() throws IOException {
		Rewrite.writeModule(writer);
		writer.close();
		out.close();

		String result = out.toString();
		assertEquals("", result);
	}

	@Test
	public void testWriteModuleWithCustomContext() throws IOException {
		EasyTravelConfig.read().frontendContextRoot = "/blabla";

		Rewrite.writeModule(writer);
		writer.close();
		out.close();

		String result = out.toString();
		TestHelpers.assertContains(result, "LoadModule", "rewrite_module", "modules/mod_rewrite");
	}

	@Test
	public void testWriteRedirectToCustomContext() throws IOException {
		Rewrite.writeRedirectToCustomContext(writer);
		writer.close();
		out.close();

		String result = out.toString();
		assertEquals("", result);
	}

	@Test
	public void testWriteRedirectToCustomContextWithCustomContext() throws IOException {
		EasyTravelConfig.read().frontendContextRoot = "/blabla";

		Rewrite.writeRedirectToCustomContext(writer);
		writer.close();
		out.close();

		String result = out.toString();
		TestHelpers.assertContains(result, "RewriteEngine On", "RewriteRule");
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(Rewrite.class);
	}
}
