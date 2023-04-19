package com.dynatrace.easytravel.launcher.procedures;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.lightcouch.CouchDbClient;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.couchdb.CouchDBCommons;
import com.dynatrace.easytravel.integration.IntegrationTestBase;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.CouchDBProcedure;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.remote.HttpServiceThread;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;
import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Logger;

//==================================================================
// The simulated CouchDB protocol conversation in this test is based on the
// following real trace capture, where we connected to CouchDB to create
// the image database:
//

/*

[4] ---> GET / HTTP/1.1
Accept: application/json
Host: 172.18.150.5:5984
Connection: Keep-Alive
User-Agent: Apache-HttpClient/4.3.5 (java 1.5)
Accept-Encoding: gzip,deflate

[8] <-- HTTP/1.1 200 OK
Server: CouchDB/1.6.1 (Erlang OTP/R16B02)
Date: Fri, 23 Jan 2015 08:52:39 GMT
Content-Type: application/json
Content-Length: 151
Cache-Control: must-revalidate
{"couchdb":"Welcome","uuid":"ab8197b25a70c1f654d7400aecd00551","version":"1.6.1","vendor":{"version":"1.6.1","name":"The Apache Software Foundation"}}

[9] ---> GET /_all_dbs HTTP/1.1
Accept: application/json
Host: 172.18.150.5:5984
Connection: Keep-Alive
User-Agent: Apache-HttpClient/4.3.5 (java 1.5)
Accept-Encoding: gzip,deflate

[12] <--- HTTP/1.1 200 OK
Server: CouchDB/1.6.1 (Erlang OTP/R16B02)
Date: Fri, 23 Jan 2015 08:52:39 GMT
Content-Type: application/json
Content-Length: 48
Cache-Control: must-revalidate
["_replicator","_users","couchdb-test","tasks"]

[20] ---> GET /easy_travel_images HTTP/1.1
Accept: application/json
Host: 172.18.150.5:5984
Connection: Keep-Alive
User-Agent: Apache-HttpClient/4.3.5 (java 1.5)
Accept-Encoding: gzip,deflate

[24] <--- HTTP/1.1 404 Object Not Found
Server: CouchDB/1.6.1 (Erlang OTP/R16B02)
Date: Fri, 23 Jan 2015 08:52:40 GMT
Content-Type: application/json
Content-Length: 44
Cache-Control: must-revalidate
{"error":"not_found","reason":"no_db_file"}

[31] ---> PUT /easy_travel_images HTTP/1.1
Content-Length: 0
Host: 172.18.150.5:5984
Connection: Keep-Alive
User-Agent: Apache-HttpClient/4.3.5 (java 1.5)
Accept-Encoding: gzip,deflate

[35] <--- HTTP/1.1 201 Created
Server: CouchDB/1.6.1 (Erlang OTP/R16B02)
Location: http://172.18.150.5:5984/easy_travel_images
Date: Fri, 23 Jan 2015 08:52:40 GMT
Content-Type: text/plain; charset=utf-8
Content-Length: 12
Cache-Control: must-revalidate
{"ok":true}

[70] ---> PUT /easy_travel_images/result_pic_1.png/result_pic_1.png HTTP/1.1
Transfer-Encoding: chunked
Content-Type: image/png
Host: 172.18.150.5:5984
Connection: Keep-Alive
User-Agent: Apache-HttpClient/4.3.5 (java 1.5)
Accept-Encoding: gzip,deflate
1000
PNG
IHDRasRGBbKGDpHYstIME2=q IDATxTIe&dZ...

[74] <--- HTTP/1.1 201 Created
Server: CouchDB/1.6.1 (Erlang OTP/R16B02)
Location: http://172.18.150.5:5984/easy_travel_images/result_pic_1.png/result_pic_1.png
ETag: "1-60f662112976a8ec6208b322422159bc"
Date: Fri, 23 Jan 2015 08:52:40 GMT
Content-Type: text/plain; charset=utf-8
Content-Length: 79
Cache-Control: must-revalidate
{"ok":true,"id":"result_pic_1.png","rev":"1-60f662112976a8ec6208b322422159bc"}

[75] ---> GET /easy_travel_images/result_pic_1.png/result_pic_1.png HTTP/1.1
Accept: application/json
Host: 172.18.150.5:5984
Connection: Keep-Alive
User-Agent: Apache-HttpClient/4.3.5 (java 1.5)
Accept-Encoding: gzip,deflate

[88] <--- HTTP/1.1 200 OK
Server: CouchDB/1.6.1 (Erlang OTP/R16B02)
ETag: "GTcXQGJhc7C3dXg1o+fbOw=="
Date: Fri, 23 Jan 2015 08:52:40 GMT
Content-Type: image/png
Content-MD5: GTcXQGJhc7C3dXg1o+fbOw==
Content-Length: 32049
Cache-Control: must-revalidate
Accept-Ranges: bytes
PNG
IHDRasRGBbKGDpHYstIME2=q IDATxTIe&dZ...

[127] ---> PUT /easy_travel_images/result_pic_2.png/result_pic_2.png HTTP/1.1
Transfer-Encoding: chunked
Content-Type: image/png
Host: 172.18.150.5:5984
Connection: Keep-Alive
User-Agent: Apache-HttpClient/4.3.5 (java 1.5)
Accept-Encoding: gzip,deflate
1000
PNG
IHDRasRGBbKGDpHYstIME1 IDATxIeKr&ff...

*/

public class CouchDBContentCreationProcedureTest {
    private static final Logger LOGGER = LoggerFactory.make();

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

    private static final EasyTravelConfig CONFIG = EasyTravelConfig.read();

    private HttpServiceThread remoteController;

    private Runnable exitInDisplayThreadRunnable = new Runnable() {
        @Override
        public void run() {
            throw new IllegalStateException("Should not be called in this test!");
        }
    };
    
    private CouchDbClient dbClient; 

    @BeforeClass
    public static void setUpClass() throws IOException {
    	LoggerFactory.initLogging();

    	TestHelpers.waitForPort(CONFIG.launcherHttpPort, 2000);
    }

	@Before
	public void setUp() throws IOException, InterruptedException {
		IntegrationTestBase.checkPort(CONFIG.launcherHttpPort);

        remoteController = new HttpServiceThread(CONFIG.launcherHttpPort, exitInDisplayThreadRunnable);
        remoteController.start();

        // try to sleep a bit to let httpd service thread start up fully
        Thread.sleep(200);       
	}

	@After
	public void tearDown() {
		if(remoteController != null) {
			remoteController.stopService();
		}

		TestHelpers.waitForPort(CONFIG.launcherHttpPort, 2000);
		
        if(dbClient != null) {
        	dbClient.shutdown();        	
        }
	}

	@Test
	public void testCouchDBContentCreation() throws Exception {
	
		// Make following 'final' arrays, so as to circumvent the Java restriction of:
		// "not being able to refer to a non-final local variable in an enclosing scope".
		
		final int[] numberOfImagesCreated = new int[1]; // the number of images we put into the database
		final boolean[] createdDB = new boolean[1]; // the call to the DB creator returned success
		final boolean[] DBdidNotExist = new boolean[1]; // the database did not already exist
		final boolean[] dbCreationRequest = new boolean[1]; // have received a request to create the easy_travel_images database
		final boolean[] dbInitialRequest = new boolean[1]; // have received an initial identification request
		final boolean[] dbListRequest = new boolean[1];	// have received a request to list all databases
		final boolean[] db_easy_travel_images_Request = new boolean[1]; // have received a question about the easy_travel_images database
		
		numberOfImagesCreated[0] = 0;
		createdDB[0] = false;
		DBdidNotExist[0] = false;
		dbCreationRequest[0] = false;
		dbInitialRequest[0] = false;
		dbListRequest[0] = false;
		db_easy_travel_images_Request[0] = false;

		// Stop the real REST server.
		remoteController.stopService();
		
		HTTPResponseRunnable runable = new HTTPResponseRunnable() {
			
			//
			// Note:
			// - To see the complete response as transmitted to the socket, look at NanoHTTPD.sendResponse().
			// - The following might become important in the future: sendResponse() will compose a reply as for HTTP 1.0, while
			//   some future libraries of CouchDB might require higher versions.
			//
		
			//==================================================================
			// Set up mock server responses
			//==================================================================

			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
					Response response = null;
					final String PORT_RANGE_START = "15100";
					
					
				if (uri.contains("/_all_dbs")) {
					
					// The client is asking us for a list of all the databases currently defined.
					// We return a list, but the list does not contain "easy_travel_images".
					dbListRequest[0] = true;
					return new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, "application/json", "[\"_replicator\",\"_users\",\"my_images\",\"tasks\"]");
					
				} else if (uri.contains("/easy_travel_images")) {
					
					if (method.equals("GET")) {
						byte[] pictureContent = 
							{ (byte)0x89, (byte)'P', (byte)'N', (byte)'G', (byte)'\r', (byte)'\n', (byte)0x1A, (byte)'\n', (byte)0x00, (byte)0x00, (byte)0x00, (byte)'\r', (byte)'I', (byte)'H', (byte)'D', (byte)'R',
							(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x01, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x25, (byte)0xDB, (byte)'V',
							(byte)0xCA, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03, (byte)'P', (byte)'L', (byte)'T', (byte)'E', (byte)0xFF, (byte)'M', (byte)0x00, (byte)0x5C, (byte)'5', (byte)'8', (byte)0x7F,
							(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)'t', (byte)'R', (byte)'N', (byte)'S', (byte)0xCC, (byte)0xD2, (byte)'4', (byte)'V', (byte)0xFD, (byte)0x00, (byte)0x00, (byte)0x00,
							(byte)'\n', (byte)'I', (byte)'D', (byte)'A', (byte)'T', (byte)'x', (byte)0x9C, (byte)'c', (byte)'b', (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x00, (byte)0x03, (byte)'6',
							(byte)'7', (byte)0x7C, (byte)0xA8, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)'I', (byte)'E', (byte)'N', (byte)'D', (byte)0xAE, (byte)'B', (byte)0x60, (byte)0x82};
						
							
						if (uri.contains("/easy_travel_images/")) {
							
							// The client is asking for a specific image, or for the presence of the database in general.
							// We cannot supply specific images, except for the two test ones.
							
							if (uri.contains("test_pic_1") || uri.contains("test_pic_2")) {
								response = new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, "image/png", pictureContent);
								response.addHeader("Content-Type", "image/png");
								response.addHeader("Server", "CouchDB/1.6.1 (Erlang OTP/R16B02)");
								response.addHeader("Etag", "\"GTcXQGJhc7C3dXg1o+fbOw==\"");
								response.addHeader("Content-MD5", "GTcXQGJhc7C3dXg1o+fbOw==");
								response.addHeader("Content-Length", "95");
								response.addHeader("Cache-Control" , "must-revalidate");
								response.addHeader("Accept-Ranges" , "bytes");
								db_easy_travel_images_Request[0] = true;
								return response;
							}
							
							// The client is asking for an image we cannot respond with.
							// So we reply negatively as we cannot supply any other images except those two test ones.
							db_easy_travel_images_Request[0] = true;
							return new NanoHTTPD.Response("404 Object Not Found", "application/json", "{\"error\":\"not_found\",\"reason\":\"no_db_file\"}");
						}
					
						// The client is asking for the presence of the database - we should reply that we do not have the database (yet).
						return new NanoHTTPD.Response("404 Object Not Found", "application/json", "{\"error\":\"not_found\",\"reason\":\"no_db_file\"}");
						
					} else if (method.equals("PUT")) {
						
						// The client is asking us to create the database named "easy_travel_images" or to put an image into it.
						// We want to reply with a success message. However, we need to distinguish these two types of requests,
						// as for the latter the response must also contain the name of the image, its id and and revision (we could
						// probably make them up, but the values  given here come from a real CouchDB conversation).
						
						if (uri.contains("test_pic_1.png")) {
							
							numberOfImagesCreated[0]++;
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"test_pic_1.png\",\"rev\":\"1-60f662112976a8ec6208b322422159bc\"}\n");
							
							// For this picture we try to supply all sorts of things, but it seems that it is quite enough to supply
							// just a simple reply without Location, Etag, len or Cache-control.
							response.addHeader("Location", "http://127.0.0.1:" + PORT_RANGE_START + "/easy_travel_images/test_pic_1.png/test_pic_1.png");
							response.addHeader("Etag", "\"1-60f662112976a8ec6208b322422159bc\"");
							response.addHeader("Content-Length", "77");
							response.addHeader("Cache-Control" , "must-revalidate");

						} else if (uri.contains("test_pic_2.png")) {
							
							numberOfImagesCreated[0]++;
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"test_pic_2.png\",\"rev\":\"1-60f662112976a8ec6208b322422159bc\"}\n");
							
							// For this picture we try to supply all sorts of things, but it seems that it is quite enough to supply
							// just a simple reply without Location, Etag, len or Cache-control.
							response.addHeader("Location", "http://127.0.0.1:" + PORT_RANGE_START + "/easy_travel_images/test_pic_2.png/test_pic_2.png");
							response.addHeader("Etag", "\"1-60f662112976a8ec6208b322422159bc\"");
							response.addHeader("Content-Length", "77");
							response.addHeader("Cache-Control" , "must-revalidate");

						// At the moment we do not test for the other images as they are too big for the mock server to either receive or send,
						// so the following code will not be exercised for now, but might be in the future.
						} else if (uri.contains("result_pic_1.png")) {
							numberOfImagesCreated[0]++;
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"result_pic_1.png\",\"rev\":\"1-60f662112976a8ec6208b322422159bc\"}\n");
						} else if (uri.contains("Booking_transaction_picture_page2.png")) {
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"Booking_transaction_picture_page2.png\",\"rev\":\"1-d606a94196b24fd4c222b9ba95a067c5\"}");
							numberOfImagesCreated[0]++;
						} else if (uri.contains("easyTravel_banner.png")) {
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"easyTravel_banner.png\",\"rev\":\"1-cfeaf28d7d3c17d740ef2c807e5588fa\"}");
							numberOfImagesCreated[0]++;
						} else if (uri.contains("easyTravel_bookingtransaction_Header.png")) {
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"easyTravel_bookingtransaction_Header.png\",\"rev\":\"1-5e7197dd60a83738924bea8bfa589683\"}");
							numberOfImagesCreated[0]++;
						} else if (uri.contains("header1.png")) {
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"header1.png\",\"rev\":\"1-5227faa600d2ec40f7bcfa8ee35db97e\"}");
							numberOfImagesCreated[0]++;
						} else if (uri.contains("header2.jpg")) {
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"header2.jpg\",\"rev\":\"1-aa48690b13468f6ddac05690c3d7d076\"}");
							numberOfImagesCreated[0]++;
						} else if (uri.contains("header3.jpg")) {
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"header3.jpg\",\"rev\":\"1-e419961eb88dca278e0079c0bcc7adf7\"}");
							numberOfImagesCreated[0]++;
						} else if (uri.contains("header4.jpg")) {
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"header4.jpg\",\"rev\":\"1-e08e44f014e47d12806491ab427b4746\"}");
							numberOfImagesCreated[0]++;
						} else if (uri.contains("header5.jpg")) {
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"header5.jpg\",\"rev\":\"1-cb5e07ab973bb1ca1ea143a0cf0870cf\"}");
							numberOfImagesCreated[0]++;
						} else if (uri.contains("header6.jpg")) {
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"header6.jpg\",\"rev\":\"1-2ac382556d3a8f214032d6b649a395d2\"}");
							numberOfImagesCreated[0]++;
						} else if (uri.contains("header7.jpg")) {
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"header7.jpg\",\"rev\":\"1-16105ffe88b2d5bfaa605083b8f240a6\"}");
							numberOfImagesCreated[0]++;
						} else if (uri.contains("result_pic_2.png")) {
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true,\"id\":\"result_pic_2.png\",\"rev\":\"1-8a8338dcfd16db6da8c1b0afb20eb097\"}");
							numberOfImagesCreated[0]++;
						} else {
							
							// We know that if the request is not about any of the above pictures, then we are most likely simply creating the database
							// since the test can only ask us about the above pictures and about no other ones.
							dbCreationRequest[0] = true;
							response = new NanoHTTPD.Response("201 Created", "text/plain; charset=utf-8", "{\"ok\":true}");
						}
						
						// This seem necessary for all of the above responses.
						response.addHeader("Server", "CouchDB/1.6.1 (Erlang OTP/R16B02)");
						return response;
						
					} else {
						return null;
					}
					
				} else if (uri.contains("/")) {
					
					// It must be an opening request, at the very start of the conversation.
					// We just introduce ourselves as a CouchDB instance.
					dbInitialRequest[0] = true;
					response = new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, "application/json", "{\"couchdb\":\"Welcome\",\"uuid\":\"ab8197b25a70c1f654d7400aecd00551\",\"version\":\"1.6.1\",\"vendor\":{\"version\":\"1.6.1\",\"name\":\"The Apache Software Foundation\"}");
					response.addHeader("Server", "CouchDB/1.6.1 (Erlang OTP/R16B02)");
					return response;
					
				} else {
					throw new IllegalStateException("Unexpected REST-call: <" + uri + ">");
				}
			}
		};

		// start the dummy REST server
		MockRESTServer server = new MockRESTServer(runable);
		
		//==================================================================
		// Set up configuration for accessing CouchDB
		//==================================================================

		final EasyTravelConfig conf = EasyTravelConfig.read();
		conf.launcherHttpPort = server.getPort();
		conf.couchDBPort = conf.launcherHttpPort;
		conf.couchDBHost = "127.0.0.1";
		
		conf.couchDBName = "easy_travel_images";

		//==================================================================
		// Exercise CouchDBCommons.initCouchDbClient(false)
		// i.e. try to connect to CouchDB and see if "easy_travel_images"
		// database is there.  We will not find it there (as per
		// responses above), so we are expected to get null and the CouchDB
		// client that is created in initCouchDbClient(), will be closed
		// and we should get a null here.
		//==================================================================
		CouchDbClient dbClient = null;

		// try {
			dbClient = CouchDBCommons.initCouchDbClient(false);
			
			if (dbClient == null) {
				DBdidNotExist[0] = true;
			} else {
				DBdidNotExist[0] = false;
			}
				
		// } catch (Exception e) {
		// }

		//==================================================================
		// Exercise CouchDBCommons.initCouchDbClient(true)
		// i.e. try to create a new database.  This should be successful
		// (as per responses prepared above).
		//==================================================================

		try {
			dbClient = CouchDBCommons.initCouchDbClient(true);
			
			if (dbClient == null) {
				createdDB[0] = false;
			} else {
				createdDB[0] = true;
			}
				
		} catch (Exception e) {
		}
		
		//==================================================================
		// Now populate the database with images.
		// Note that createDBContent() will also call CouchDBCommons.initCouchDbClient()
		// of its own, though the above test is a useful introduction, as it it
		// fails, there is no point going further.
		//==================================================================
		
		server.stop();
		
		numberOfImagesCreated[0] = 0;
		
		// Creating a new server for the next part of the test may help
		// the mock server survive all these tests.
		server = new MockRESTServer(runable);
		conf.launcherHttpPort = server.getPort();
		conf.couchDBPort = conf.launcherHttpPort;
	
		// Override the image list with our own test list
		ArrayList<String> testList = new ArrayList<String>();
		testList.add("/img/test_pic_1.png");
		testList.add("/img/test_pic_2.png");
		CouchDBCommons.setImageList(testList);
		
		Feedback myFeedback = CouchDBContentCreationProcedure.createDBContent();
		
		server.stop();

	    remoteController = new HttpServiceThread(CONFIG.launcherHttpPort, exitInDisplayThreadRunnable);
	    remoteController.start();
	    
		// Assert that the action that we expected the content creator to do,
		// have indeed happened.
		assertTrue(DBdidNotExist[0]);
	    assertTrue(!myFeedback.equals(Feedback.Failure));
		assertTrue(createdDB[0]);
		assertTrue(dbInitialRequest[0]);
		assertTrue(dbCreationRequest[0]);
		assertTrue(db_easy_travel_images_Request[0]);
		assertTrue(numberOfImagesCreated[0] == 2); // we know there are two test images in our test list as per 
								//	CouchDBContentCreationProcedure.setUpTestList();
		server.stop();
	}
	
	@Test
	@Ignore("assumes that CouchDB is running")
	public void couchDBIntegrationTest() throws CorruptInstallationException, InterruptedException, ClientProtocolException, IOException {
		CouchDBProcedure couchdb  = runCouchDB();
		String response = executeCouchDBRest("http://localhost:5984");
		assertTrue(response.contains("\"couchdb\":\"Welcome\""));
		response = executeCouchDBRest(String.format("http://localhost:%d/erl/couchDB_ET:test_now", EasyTravelConfig.read().couchDBShutdownPort));
		assertTrue(response.contains("CouchDB controlling process is running"));
		CouchDBContentCreationProcedure.createDBContent();
		checkIfImagesExist();
		couchdb.stop();		
		assertFalse(couchdb.isRunning());
	}
	
	
	@Test(expected = ConnectionClosedException.class)
	@Ignore("assumes that CouchDB is running")
	public void couchDBRemoteStopTest() throws CorruptInstallationException, ClientProtocolException, IOException {
		CouchDBProcedure couchdb  = runCouchDB();
		assertTrue(couchdb.isRunning());
		executeCouchDBRest(String.format("http://localhost:%d/erl/couchDB_ET:stop_now", EasyTravelConfig.read().couchDBShutdownPort));
		assertFalse(couchdb.isRunning());
	}
	
	@Test(expected = SocketException.class)
	@Ignore("assumes that CouchDB is running")
	public void couchDBRemoteCrashTest() throws CorruptInstallationException, ClientProtocolException, IOException {
		CouchDBProcedure couchdb  = runCouchDB();
		assertTrue(couchdb.isRunning());
		executeCouchDBRest(String.format("http://localhost:%d/erl/couchDB_ET:crash_now", EasyTravelConfig.read().couchDBShutdownPort));
		assertFalse(couchdb.isRunning());
	}
	
	private CouchDBProcedure runCouchDB() throws CorruptInstallationException {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping("couchdb");
		CouchDBProcedure couchdb = new CouchDBProcedure(mapping);
		couchdb.run();
		assertTrue(couchdb.isRunning());
		return couchdb;
	}
	
	private String executeCouchDBRest(String serviceUrl) throws ClientProtocolException, IOException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
		    HttpGet httpGet = new HttpGet(serviceUrl);
		    HttpResponse response = httpClient.execute(httpGet);
		    System.out.println(response.getStatusLine());
		    assertEquals(200, response.getStatusLine().getStatusCode());
		    HttpEntity entity = response.getEntity();
		    return EntityUtils.toString(entity);	
		}		
	}
			
	private void checkIfImagesExist() {
		dbClient = CouchDBCommons.initCouchDbClient(true);
		ArrayList<String> imageList = CouchDBCommons.getImageList();
		boolean allImagesExist = true;
		for(String imageUrl: imageList) {
			String imageName = imageUrl.substring(imageUrl.lastIndexOf("/"));
			allImagesExist &= dbClient.find(imageName) != null;
		}
		assertTrue("Not all images exist", allImagesExist);
	}
}

