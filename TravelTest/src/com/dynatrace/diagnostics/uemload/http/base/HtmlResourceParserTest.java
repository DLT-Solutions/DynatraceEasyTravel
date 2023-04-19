package com.dynatrace.diagnostics.uemload.http.base;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class HtmlResourceParserTest {
	
	public static final String TEST_DATA_PATH = "../TravelTest/testdata";
	
	@Test
	public void testProtocolRelativeURL() {
		HtmlResourceParser parser = new HtmlResourceParser();
		
		String html = "<html><head>"
				+ "<link rel=\"stylesheet\" href=\"/someRelativeURL\" />"
				+ "<link rel=\"stylesheet\" href=\"//someProtocolRelativeURL\" />"
				+ "<link rel=\"stylesheet\" href=\"https://someAbsoluteURL\" />"
				+ "</head></html>";
		
		Collection<String> listResourceReferences = parser.listResourceReferences("http://127.0.0.1", html);
		assertTrue(listResourceReferences.contains("http://127.0.0.1/someRelativeURL"));
		assertTrue(listResourceReferences.contains("http://someProtocolRelativeURL"));
		assertTrue(listResourceReferences.contains("https://someAbsoluteURL"));
	
		listResourceReferences = parser.listResourceReferences("https://127.0.0.1", html);
		assertTrue(listResourceReferences.contains("https://127.0.0.1/someRelativeURL"));
		assertTrue(listResourceReferences.contains("https://someProtocolRelativeURL"));
		assertTrue(listResourceReferences.contains("https://someAbsoluteURL"));
	}
	
	@Test
	public void testExamplePage() throws IOException{
		HtmlResourceParser parser = new HtmlResourceParser();
		
		File file = new File(TEST_DATA_PATH, "/Response.txt");
		assertTrue(file.exists());
		String html = FileUtils.readFileToString(file);
		
		Collection<String> listResourceReferences = parser.listResourceReferences("SECRET", html);
		
		assertTrue("Not enough resources found. Possible cause might be changes in HtmlResourceParser.", listResourceReferences.size()==9);
	}
	
	@Test
	public void testResourceExclusion() {
		HtmlResourceParser parser = new HtmlResourceParser();
		
		String html = "<html><head>" +
	            "<link rel=\"stylesheet\" type=\"text/css\" href=\"cg.css\"></link>\n" +
	            "<link rel=\"pingback\" type=\"text/css\" href=\"/pingback.html\"></link>\n" +
	            "<link rel=\"dns-prefetch\" type=\"text/css\" href=\"/dns-prefetch.php\"></link>" +
	            "<title>A Simple HTML Document</title></html>";
		
		Collection<String> listResourceReferences = parser.listResourceReferences("http://127.0.0.1", html);
		assertTrue(listResourceReferences.size() == 1);
		assertTrue(listResourceReferences.contains("http://127.0.0.1/cg.css"));
	}
}
