package com.dynatrace.diagnostics.uemload.http.callback;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.dynatrace.diagnostics.uemload.http.exception.PageNotAvailableException;
import com.dynatrace.easytravel.utils.TestHelpers;


public class ErrorHandlingHttpResonseCallbackAdapterTest {

	@Test
	public void test() throws IOException {
		// just cover the methods here
		ErrorHandlingHttpResonseCallbackAdapter adapter = new ErrorHandlingHttpResonseCallbackAdapter();
		adapter.readDone(null);

		try {
			adapter.handleRequestError(null);
			fail();
		} catch (IOException e) {
			TestHelpers.assertContains(e, "Passing on unhandled exception");
		}

		try {
			adapter.handleRequestError(new PageNotAvailableException("http://url1/", 404));
			fail();
		} catch (IOException e) {
			TestHelpers.assertContains(e, "Passing on unhandled exception");
			TestHelpers.assertContains(e.getCause(), "url1");
		}

		try {
			adapter.handleRequestError(new PageNotAvailableException("http://url1/", 404));
			fail();
		} catch (IOException e) {
			TestHelpers.assertContains(e, "Passing on unhandled exception");
			TestHelpers.assertContains(e.getCause(), PageNotAvailableException.NO_WARNING);
		}
	}

	@Test
	public void testGetMessage() {
		// should not fail if getMessage() is called multiple times
		PageNotAvailableException exception = new PageNotAvailableException("http://url2/", 404);
		assertEquals(exception.getMessage(), exception.getMessage());
		assertEquals(exception.getMessage(), exception.getMessage());
		assertNotEquals(PageNotAvailableException.NO_WARNING, exception.getMessage());

		assertEquals(404, exception.getHttpResponseCode());
	}

	@Test
	public void testInvalidURL() {
		try {
			new PageNotAvailableException("invalidurl", 404);
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "invalidurl");
		}

	}
}
