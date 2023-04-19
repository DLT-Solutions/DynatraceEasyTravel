package com.dynatrace.easytravel.launcher.remote;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Test;

import com.dynatrace.easytravel.utils.HTTPResponseRunnableImpl;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;

/**
 * Test for class {@link HTTPResponseRunnableImpl}
 * @author cwpl-rpsciuk
 *
 */
public class HTTPResponseRunnbaleImplTest {

	private static Logger LOGGER = Logger.getLogger(HTTPResponseRunnbaleImplTest.class.getName());
	
	/**
	 * Test 3 request, everything is fine
	 * @throws IOException
	 */
	@Test
	public void testResponsesOk() throws IOException {
		String[] responses = {"res1", "res2", "res3"};
		String[][] requests = {
				{"req1", "p1=v1", "p2=v2"},
				null,
				{"req3"}
		};
		
		HTTPResponseRunnableImpl httpResponse = new HTTPResponseRunnableImpl(requests, responses);
		//resp 1
		Response result = httpResponse.run("http://host/req1", "GET", new Properties(), createProperties(requests[0]));
		String res = readInputStream(result.data);
		assertTrue("Request #1 should respond: res1, but responded: " + res, "res1".equals(res));
		assertTrue("Request #1: There should be no errors " + httpResponse.getFailure(), httpResponse.getFailure().isEmpty());
		//resp 2
		result = httpResponse.run("http://host/req2", "GET", new Properties(), new Properties());
		res = readInputStream(result.data);
		assertTrue("Request #2 should respond: res2", "res2".equals(res));
		assertTrue("Request #2: There should be no errors " + httpResponse.getFailure(), httpResponse.getFailure().isEmpty());
		//resp 2
		result = httpResponse.run("http://host/req3", "GET", new Properties(), createProperties(requests[2]));
		res = readInputStream(result.data);
		assertTrue("Request #3 should respond: res3", "res3".equals(res));
		assertTrue("Request #3: There should be no errors " + httpResponse.getFailure(), httpResponse.getFailure().isEmpty());
		
		assertEquals("Incorrect number of requests", 3, httpResponse.getNumberOfRequests());
	}
	
	/**
	 * Test request that is not defined in valid requests - error expected
	 */
	@Test
	public void testInvalidRequest() {
		String[] responses = {"res1"};
		String[][] requests = {{"req1"}};
		
		HTTPResponseRunnableImpl httpResponse = new HTTPResponseRunnableImpl(requests, responses);
		try {
			httpResponse.run("http://host/req", "GET", new Properties(), createProperties(requests[0]));			
		} catch (Throwable e) {
			LOGGER.info(e.toString());
		}
		assertFalse("Request #1: Should end with validation error", httpResponse.getFailure().isEmpty());
		assertEquals("Incorrect number of requests", 1, httpResponse.getNumberOfRequests());
	}	
	
	/**
	 * Create more requests than defined - error expected
	 */
	@Test
	public void testTooManyRequests() {
		String[] responses = {"res1"};
		
		HTTPResponseRunnableImpl httpResponse = new HTTPResponseRunnableImpl(responses);
		httpResponse.run("http://host/req", "GET", new Properties(), new Properties());
		assertTrue("Request #1: Thre should be no errors " + httpResponse.getFailure(), httpResponse.getFailure().isEmpty());		
		try {
			httpResponse.run("http://host/req", "GET", new Properties(), new Properties());			
		} catch (Throwable e) { //expected exception
			LOGGER.info(e.toString());
		}
		assertFalse("Request #2: Should end with validation error", httpResponse.getFailure().isEmpty());
		assertEquals("Incorrect number of requests", 2, httpResponse.getNumberOfRequests());
	}	 
	
	/**
	 * Test checkFailure method
	 */
	@Test
	public void testCheckFailure() {
		String[] responses = {"res1"};
		
		HTTPResponseRunnableImpl httpResponse = new HTTPResponseRunnableImpl(responses);
		httpResponse.run("http://host/req", "GET", new Properties(), new Properties());
		httpResponse.checkFailure(); //shoud be ok		
		try {
			httpResponse.run("http://host/req", "GET", new Properties(), new Properties());			
		} catch (Throwable e) { //expected exception
			LOGGER.info(e.toString());
		}
		try {
			httpResponse.checkFailure();
		} catch (AssertionError err) {
			LOGGER.info("assertion fired - expected result");
		}
	}
		
	/**
	 * Test responses when parameter 'runContinuosly' is set. In this case last defined response should be served for all 'unexpected' requests
	 */
	@Test
	public void testRunContinuosly() {
		String[] responses = {"res1"};

		HTTPResponseRunnableImpl httpResponse = new HTTPResponseRunnableImpl(responses);
		httpResponse.setRunContinuosly(true);
		for (int i=0; i<10; i++) {
			httpResponse.run("http://host/req", "GET", new Properties(), new Properties());
			assertTrue("Request #"+ (i+1) + ": Thre should be no errors " + httpResponse.getFailure(), httpResponse.getFailure().isEmpty());
		}
		assertEquals("Incorrect number of requests", 10, httpResponse.getNumberOfRequests());
	}
	
	/**
	 * Test method for creting response string containing list of plugin names
	 */
	@Test
	public void testCreatePluginResponse() {
		String expected = "<xml><ns:return>somePlugin1</ns:return><ns:return>somePlugin2</ns:return><ns:return>somePlugin3</ns:return></xml>";
		assertEquals("result should be the same", expected, HTTPResponseRunnableImpl.createPluginNameResponse(new String[]{"somePlugin1","somePlugin2","somePlugin3"}));
		
		assertEquals("result should be the same", "<xml></xml>", HTTPResponseRunnableImpl.createPluginNameResponse(new String[0]));
	}	
	
	private Properties createProperties(String[] request) {
		Properties props = new Properties();
		//skip first element - this is uri
		for(int i=1; i<request.length; i++) {
			String prop = request[i];
			String[] tab = prop.split("=");
			props.put(tab[0], tab[1]);
		}
		return props;
	}
	
	private String readInputStream(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		StringBuilder sb = new StringBuilder();	
		String s = null;
		while((s=reader.readLine()) != null) {
			sb.append(s);
		}
		return sb.toString();
	}
}