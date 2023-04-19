package com.dynatrace.easytravel.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;


public class TextUtilsTest {

	private final static int TEST_COUNT = 10000;

	private static final String TEXT = "Browse trips to many exciting destinations and book your vacation with the easyTravel demo web application. There are always some interesting offers available, so don''t hesitate to step on board and fly one of those.\nBehind the scenes it uses various technologies to demonstrate how dynaTrace helps your business and your customers. It includes four application tiers across Browser, Java Web Application Server, Database and a native application.\n\n";
	private static final String PATTERN = TEXT +
			"Customer Frontend (Java): {0} Business2Business Frontend (.NET): {1}";
	private static final String EXPECTED = TEXT.replace("''", "'") +
			"Customer Frontend (Java): N/A Business2Business Frontend (.NET): N/A";

	@Test
	public void testMerge() {
		assertEquals("", TextUtils.merge(""));
		assertEquals("somestring", TextUtils.merge("somestring"));
		assertEquals("some 1", TextUtils.merge("some {0}", 1));
		assertTrue(TextUtils.merge("some {0}", 1101).equals("some 1.101") ||
				TextUtils.merge("some {0}", 1101).equals("some 1,101"));
		assertEquals("some 1101", TextUtils.merge("some {0,number,#}", 1101));
		assertTrue(TextUtils.merge("some {0} and some {1}", 1101, "text").equals("some 1.101 and some text") ||
				TextUtils.merge("some {0} and some {1}", 1101, "text").equals("some 1,101 and some text"));
		assertEquals("the replacement value is missing here",
				"<unknown>", TextUtils.merge("some {0}"));
	}

	@Test
	public void testProblem() {
		MessageFormat messageFormat = new MessageFormat(PATTERN);

		assertEquals("Expecting 2 replacement-places", 2, messageFormat.getFormats().length);

		assertEquals(EXPECTED, TextUtils.merge(PATTERN, "N/A", "N/A"));

		assertEquals("<unknown>", TextUtils.merge("{0}", "bla", "foo"));
	}

	@Test
	public void testRandomChar() {
		for (int i = 0; i < TEST_COUNT; i++) {
			char c = TextUtils.randomChar();
			assertTrue("Expected to get A or greater, but had: " + c, c >= 'A');
			assertTrue("Expected to get Z or lower, but had: " + c, c <= 'Z');
		}
	}

	@Test
	public void testInvalidPattern() {
		assertEquals("<unknown>", TextUtils.merge("Customer Frontend (Java): {0} Business2Business Frontend (.NET): {1}", "N/A"));
		assertEquals("<unknown>", TextUtils.merge("Customer Frontend (Java): {0} Business2Business Frontend (.NET): {1}", "N/A", null));
		assertEquals("<unknown>", TextUtils.merge("Customer Frontend (Java): {0} Business2Business Frontend (.NET): {1}", "N/A", new Object() {
			@Override
			public String toString() {
				throw new IllegalArgumentException();
			}
		}));
	}

	@Test
	public void testGetEndEllipsis() {
		assertEquals("123", TextUtils.getEndEllipsis("123", 10));
		assertEquals("...", TextUtils.getEndEllipsis("1234567890", 3));
		assertEquals("1...", TextUtils.getEndEllipsis("1234567890", 4));
		assertEquals("12...", TextUtils.getEndEllipsis("1234567890", 5));
		assertEquals("123...", TextUtils.getEndEllipsis("1234567890", 6));
		assertEquals("1234...", TextUtils.getEndEllipsis("1234567890", 7));
		assertNull(TextUtils.getEndEllipsis(null, 3));
		assertEquals("", TextUtils.getEndEllipsis("", 3));
		assertEquals("..", TextUtils.getEndEllipsis("1234567890", 2));
		assertEquals(".", TextUtils.getEndEllipsis("1234567890", 1));
		assertEquals("", TextUtils.getEndEllipsis("1234567890", 0));
		assertEquals("1234567890", TextUtils.getEndEllipsis("1234567890", -1));
		assertEquals("1234567890", TextUtils.getEndEllipsis("1234567890", -100));
		assertEquals("1234567890", TextUtils.getEndEllipsis("1234567890", -10000000));
	}

	@Test
	public void testAppendTrailingSlash() {
		// invalid
		assertNull(TextUtils.appendTrailingSlash(null));

		// w/o slash
		assertEquals("/", TextUtils.appendTrailingSlash(""));
		assertEquals("somestr/", TextUtils.appendTrailingSlash("somestr"));
		assertEquals("somestr/some/", TextUtils.appendTrailingSlash("somestr/some"));

		// with slash
		assertEquals("/", TextUtils.appendTrailingSlash("/"));
		assertEquals("somestr/", TextUtils.appendTrailingSlash("somestr/"));
		assertEquals("somestr/some/", TextUtils.appendTrailingSlash("somestr/some/"));
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(TextUtils.class);
	}

	@Test
	public void testReadTail() throws IOException {
		for (int i = 0; i < 15; i++) {
			testReadTailHelper(10, i, 0);
		}
		for (int i = 0; i < 15; i++) {
			testReadTailHelper(10, i, 100);
		}
		for (int i = 0; i < 15; i++) {
			testReadTailHelper(10000, i, 0);
		}
		for (int i = 0; i < 15; i++) {
			testReadTailHelper(10000, i, 100);
		}
		testReadTailHelper(100, 0, 0);
		testReadTailHelper(100, 0, 500);
	}

	private void testReadTailHelper(int produceLines, int maxLines, int maxChars) throws IOException {
		String actual = TextUtils.readTail(new BufferedReader(new StringReader(makeLines(produceLines))), maxLines, maxChars);
//		System.out.println(actual);
		int actualLines = countLines(actual);
		int actualChars = actual.length();
		if (maxLines == 0 && maxChars == 0) {
			assertTrue(String.format("Line count: %d|%d|%d|%d|%d", produceLines, maxLines, maxChars, actualLines, actualChars), actualLines == produceLines);
		}
		if (maxLines > 0) {
			assertTrue(String.format("Line count: %d|%d|%d|%d|%d", produceLines, maxLines, maxChars, actualLines, actualChars), actualLines <= maxLines);
		}
		if (maxChars > 0) {
			assertTrue(String.format("Char count: %d|%d|%d|%d|%d", produceLines, maxLines, maxChars, actualLines, actualChars), actualChars <= maxChars);
		}
	}

	private static String makeLines(int n) {
		final String LF = System.getProperty("line.separator");
		StringBuilder buf = new StringBuilder();

		for (int i = 0; i < n; i++) {
			buf.append(String.format("Line %d of %d - %s", i, n, RandomStringUtils.randomAlphanumeric(10))).append(LF);
		}

		return buf.toString();
	}

	private static int countLines(String text) throws IOException {
		BufferedReader reader = new BufferedReader(new StringReader(text));
		int lines = 0;
		while (reader.readLine() != null) {
			lines++;
		}
		return lines;
	}

	@Test
	public void testReadTailBufInstance() throws IOException {
		StringBuilder buf = new StringBuilder(100);
		StringBuilder bufRef = buf;
		buf = TextUtils.readTail(new BufferedReader(new StringReader("input1")), buf, 1, 0);
		buf = TextUtils.readTail(new BufferedReader(new StringReader("superfluent\ninput2")), buf, 1, 0);
		buf = TextUtils.readTail(new BufferedReader(new StringReader("input3")), buf, 1, 0);
		final String LF = System.getProperty("line.separator");
		String expected = "input1" + LF + "input2" + LF + "input3" + LF;
		String actual = buf.toString();
		assertEquals("Content text", expected, actual);
		assertTrue("Buf reference", buf == bufRef);
	}

	@Test
	public void testReadTailBufNull() throws IOException {
		StringBuilder buf = null;
		buf = TextUtils.readTail(new BufferedReader(new StringReader("input1")), buf, 1, 0);
		assertNotNull("Buf reference", buf);
		StringBuilder bufRef = buf;
		buf = TextUtils.readTail(new BufferedReader(new StringReader("superfluent\ninput2")), buf, 1, 0);
		buf = TextUtils.readTail(new BufferedReader(new StringReader("input3")), buf, 1, 0);
		final String LF = System.getProperty("line.separator");
		String expected = "input1" + LF + "input2" + LF + "input3" + LF;
		String actual = buf.toString();
		assertEquals("Content text", expected, actual);
		assertTrue("Buf reference", buf == bufRef);
	}
}
