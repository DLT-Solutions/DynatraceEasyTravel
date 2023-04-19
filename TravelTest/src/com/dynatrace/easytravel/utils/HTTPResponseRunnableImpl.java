package com.dynatrace.easytravel.utils;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static org.junit.Assert.*;

import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;

/**
 * @author cwpl-rpsciuk
 * Implementation of {@link HTTPResponseRunnable} for {@link MockRESTServer} that is able to create responses from given list of responses and validate
 * requests
 * Parameters: 
 * responses - list of responses; subsequent calls to the run method will return Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, responses[i])
 * where i is request number; if there is more requests than responses then null will be returned
 * requests - used to validate is subsequent request are expected. It can be null or empty - validation will not fail. Also null values in 
 * the table are allowed - validation will be skipped in such case. 
 * request[i][0] - uri to be validated
 * request[i][1], request[i][2] ... - parameters to be validated  
 */
public class HTTPResponseRunnableImpl implements HTTPResponseRunnable 
{
	
	private static Logger LOGGER = Logger.getLogger(HTTPResponseRunnableImpl.class.getName());
	
	private final String[] responses;
	private final String[][] requests;
	private final AtomicInteger count = new AtomicInteger(0);
	private final AtomicReference<String> failureString = new AtomicReference<String>("");
	//false - responds only for exact number of requests. Number of responses equals size of the responses table. Next request generates RuntimeException
	//true - if there is more requests than responses then last defined response is returned    
	private AtomicBoolean runContinuosly = new AtomicBoolean(false);
	
	/**
	 * Constructor with responses only (no URI validation)
	 * @param responses
	 */
	public HTTPResponseRunnableImpl(String[] responses) {
		this.responses = responses;
		this.requests = null;
		
		assertNotNull("responses should not be null", responses);
	}
	
	/**
	 * Constructor with URI validation
	 * @param requests
	 * @param responses
	 */
	public HTTPResponseRunnableImpl(String[][] requests, String[] responses) {
		this.responses = responses;
		this.requests = requests;
		
		assertNotNull("responses should not be null", responses);
		if (requests != null) {
			assertEquals("Requests and responses should have the same size", requests.length, responses.length);
		}
	}
	
	/**
	 * Define if we should respond for any number of requests or only for exact number (equal to size of responses table)
	 * @param runAlways
	 */
	public void setRunContinuosly(boolean runAlways) {
		runContinuosly.set(runAlways);
	}
	
	
	/**
	 * @return failure string; empty string means that everything is ok
	 */
	public String getFailure() {
		return failureString.get();
	}
	
	/**
	 * @return number of run method executions
	 */
	public int getNumberOfRequests() {
		return count.get();
	}
	
	/**
	 * Helper method for creating response containing list of enabled plugins 
	 * @param pluginNames - list of enabled plugins
	 * @return
	 */
	public static String createPluginNameResponse(String[] pluginNames) {
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		for(String name : pluginNames) {
			sb.append("<ns:return>" + name + "</ns:return>");
		}
		sb.append("</xml>");
		return sb.toString();
	}
	
	/**
	 * Activate assertion if the failure String is not empty
	 */
	public void checkFailure() {
		assertTrue("Should not have failed in the HTTPResponseRunnable, but did:" + getFailure(), getFailure().isEmpty());
	}
	
	@Override
	public Response run(String uri, String method, Properties header, Properties params) {
		count.incrementAndGet();
		LOGGER.info("count: " + count + " uri: " + uri + " params: " + params);

		try {
			validateRequest(uri, params, count.get()-1);
			return createResponse(uri, params, count.get()-1); 
		} catch (Throwable e) {
			addFailure(e.toString());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create response for given parameters. 
	 * @param uri
	 * @param params
	 * @param i - id of the respnse
	 * @return response number 'i'. When 'i' is bigger then responses table then:
	 *  - null is returned when runContinuosly=false
	 *  - last response is returned when runContinuosly=true  
	 */
	private Response createResponse(String uri, Properties params, int i) {
		if (i>=responses.length) {
			if (runContinuosly.get()) { //return  last response
				return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, responses[responses.length-1]);
			} else {
				addFailure(i + "-Unexpected uri: " + uri);				
				return null;
			}
		}
		return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, responses[i]);
	}
	
	/**
	 * Remember failure string
	 * @param error
	 */
	private void addFailure(String error) {
		failureString.set(failureString.get() + "\n" + error);
	}

	/**
	 * Validate request
	 * @param uri
	 * @param params
	 * @param i
	 */
	private void validateRequest(String uri, Properties params, int i) {
		if (requests == null || i>= requests.length) {
			return;
		}		
		String[] expected = requests[i];
		if (expected == null) {
			return;
		}
				
		LOGGER.info("Count: " + i + " had: " + uri + " and params " + params + ", expecting " + Arrays.toString(expected) );
		assertTrue("Expected string '" + expected[0] + "' not found in uri: " + uri,
				uri.contains(expected[0]));
		
		for (int j=1;j<expected.length;j++) {
			String param = expected[j];
			assertTrue("Expected params '" + param + "' not found in params: " + params.toString(), params.toString().contains(param));
		}
	}	
}
