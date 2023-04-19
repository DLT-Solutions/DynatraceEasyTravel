package com.dynatrace.easytravel.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;


public class WebUtilsTest {

	@Test
	public void testContentType() {
		assertTrue(WebUtils.getContentType("myplugin.dtp").startsWith("application/octet-stream"));
		assertTrue(WebUtils.getContentType("mypage.html").startsWith("text/html"));
		assertTrue(WebUtils.getContentType("doc.txt").startsWith("text/plain"));
		assertTrue(WebUtils.getContentType("style.css").startsWith("text/css"));
		assertTrue(WebUtils.getContentType("foo.js").startsWith("application/javascript"));
		assertTrue(WebUtils.getContentType("screenshot.png").startsWith("image/png"));
		assertTrue(WebUtils.getContentType("graph.gif").startsWith("image/gif"));
		assertTrue(WebUtils.getContentType("photo.jpg").startsWith("image/jpeg"));
		assertTrue(WebUtils.getContentType("photo.jpeg").startsWith("image/jpeg"));
		assertTrue(WebUtils.getContentType("archive.zip").startsWith("application/zip"));
		assertTrue(WebUtils.getContentType("no_ending").startsWith("application/octet-stream"));
	}

	@Test
	public void testHtmlPageTitleSimple() {
		String htmlContent = "<html><head><title>mytitle</title></head>...";
		assertEquals("mytitle", getHtmlPageTitle(htmlContent));
	}

	@Test
	public void testHtmlPageTitleInvalid1() {
		String htmlContent = "<html><head><title>my<>title</title></head>...";
		 // strictly '<' is invalid (must be escaped), but we want to see it anyway
		assertEquals("my<>title", getHtmlPageTitle(htmlContent));
	}

	@Test
	public void testHtmlPageTitleInvalid2() {
		String htmlContent = "<html><head><title>mytitle</title></title></head>...";
		assertEquals("mytitle", getHtmlPageTitle(htmlContent));
	}

	@Test
	public void testHtmlPageTitleSpaces() {
		String htmlContent = "<html><head><title>     mytitle     </title></head>...";
		assertEquals("mytitle", getHtmlPageTitle(htmlContent));
	}

	@Test
	public void testHtmlPageTitleMultiline() {
		String htmlContent = "<html><head><title>     my\ntitle     </title></head>...";
		assertEquals("my\ntitle", getHtmlPageTitle(htmlContent));
	}

	@Test
	public void testHtmlPageTitleMultilineWithDocType() {
		String htmlContent = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
				"    <html>   \n" +
				" <head>  \n" +
				"\n" +
				"<title>     mytitle     </title></head>...";
		assertEquals("mytitle", getHtmlPageTitle(htmlContent));
	}

	@Test
	public void testHtmlPageTitleMultilineWithDocTypeAndOtherTags() {
		String htmlContent = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
				"    <html>   \n" +
				" <head>  \n" +
				" <meta x='y'>\n" +
				" <meta y='z'> \n" +
				"\n" +
				"<title>     mytitle     </title></head>...";
		assertEquals("mytitle", getHtmlPageTitle(htmlContent));
	}

	@Test
	public void testHtmlPageTitleMultilineWithDocTypeAndOtherInvalidTags() {
		String htmlContent = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
				"    <html>   \n" +
				" <head>  \n" +
				" <foo manchu\n" +
				"\n" +
				"<title attr>     mytitle     </title></head>...";
		assertEquals("mytitle", getHtmlPageTitle(htmlContent));
	}

	@Test
	public void testHtmlPageTitleWithAttributes() {
		String htmlContent = "<html xmlns=\"foo\"><head xml:lang='en'><title xml:lang=\"en\">mytitle</title></head>...";
		assertEquals("mytitle", getHtmlPageTitle(htmlContent));
	}

	@Test
	public void testHtmlPageTitleWithAttributesAndSpaces() {
		String htmlContent = "<html xmlns=\"foo\"><head xml:lang='en'><title xml:lang=\"en\">\n\n\n  \r   mytitle  \r\n \n\r   </title></head>...";
		assertEquals("mytitle", getHtmlPageTitle(htmlContent));
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(WebUtils.class);
	}

	private static String getHtmlPageTitle(String htmlContent) {
//		return WebUtils.getHtmlElementText(htmlContent, new String[] { "title" });
//		return WebUtils.getHtmlElementText(htmlContent, new String[] { "html", "title" });
//		return WebUtils.getHtmlElementText(htmlContent, new String[] { "head", "title" });
		return WebUtils.getHtmlElementText(htmlContent, new String[] { "html", "head", "title" });
	}

	@Test
	public void testEnglishOrGerman() {
		String elements = "html.head.title"; // cf. inputs.properties
		String expected = ".*?(Services|Dienste)"; // cf. inputs.properties
		String htmlContent = "<html xmlns=\"foo\"><head xml:lang='en'><title xml:lang=\"en\">pgrasboe Services</title></head>...";
		String actual = WebUtils.getHtmlElementText(htmlContent, elements.split("\\."), expected);
		assertNotNull(actual);
		htmlContent = "<html xmlns=\"foo\"><head xml:lang='en'><title xml:lang=\"en\">pgrasboe Dienste</title></head>...";
		actual = WebUtils.getHtmlElementText(htmlContent, elements.split("\\."), expected);
		assertNotNull(actual);
		htmlContent = "<html xmlns=\"foo\"><head xml:lang='en'><title xml:lang=\"en\">pgrasboe blablabla</title></head>...";
		actual = WebUtils.getHtmlElementText(htmlContent, elements.split("\\."), expected);
		assertNull(actual);
		htmlContent = "<html xmlns=\"foo\"><head xml:lang='en'><title xml:lang=\"en\">pgrasboe blablabla</title></head>...";
		actual = WebUtils.getHtmlElementText(htmlContent, elements.split("\\."));
		assertNotNull(actual);
		assertEquals("pgrasboe blablabla", actual); // check if wrong title can be found anyway.
	}
}
