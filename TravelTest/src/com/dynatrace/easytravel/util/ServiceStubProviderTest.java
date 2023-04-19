package com.dynatrace.easytravel.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.business.client.JourneyServiceStub;
import com.dynatrace.easytravel.business.webservice.FindLocationsDocument;
import com.dynatrace.easytravel.business.webservice.FindLocationsResponseDocument;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;


public class ServiceStubProviderTest {
	//private static final Logger log = LoggerFactory.make();

	private static final int TEST_COUNT = 1000;

	@After
	public void tearDown() {
		EasyTravelConfig.resetSingleton();
	}

	@Test
	public void testGetServiceStub() {
		Set<JourneyServiceStub> set = new HashSet<JourneyServiceStub>();

		// ensure that we can do many invocations of this without running into trouble
		for (int i = 0; i < TEST_COUNT; i++) {
			JourneyServiceStub serviceStub = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);
			assertNotNull(serviceStub);

			assertNotNull("ServiceClient not null", serviceStub._getServiceClient());
			assertNotNull("Options not null", serviceStub._getServiceClient().getOptions());
			assertNotNull("To not null", serviceStub._getServiceClient().getOptions().getTo());
			assertEquals("http://localhost:8091/services/JourneyService/",
					serviceStub._getServiceClient().getOptions().getTo().getAddress());

			assertFalse("Should have this in the list after the first iteration, but had it at " + i, set.add(serviceStub) && i != 0);

			ServiceStubProvider.returnServiceStub(serviceStub);
		}

		ServiceStubProvider.clear();
	}

	@Test
	public void testInvalidateServiceStub() {
		Set<JourneyServiceStub> set = new HashSet<JourneyServiceStub>();

		// ensure that we can do many invocations of this without running into trouble
		// use /10 as this runs much slower
		for (int i = 0; i < TEST_COUNT/10; i++) {
			JourneyServiceStub serviceStub = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);
			assertNotNull(serviceStub);

			assertNotNull("ServiceClient not null", serviceStub._getServiceClient());
			assertNotNull("Options not null", serviceStub._getServiceClient().getOptions());
			assertNotNull("To not null", serviceStub._getServiceClient().getOptions().getTo());
			assertEquals("http://localhost:8091/services/JourneyService/",
					serviceStub._getServiceClient().getOptions().getTo().getAddress());

			assertTrue("Should not have this in the list at iteration " + i + " because we always expect new ones on invalidate", set.add(serviceStub));

			ServiceStubProvider.invalidateServiceStub(serviceStub);
		}

		ServiceStubProvider.clear();
	}

	@Test
	public void testGetServiceStubRemoteBackend() {
		EasyTravelConfig CONFIG = EasyTravelConfig.read();

		CONFIG.backendPort = 8092;
		CONFIG.webServiceBaseDir = "http://localhost:8093/someroot/service/myserver/";

		// clear now to not have older entries in the pool
		ServiceStubProvider.clear();

		// ensure that we can do many invocations of this without running into trouble
		JourneyServiceStub serviceStub = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);
		assertNotNull(serviceStub);

		assertNotNull("ServiceClient not null", serviceStub._getServiceClient());
		assertNotNull("Options not null", serviceStub._getServiceClient().getOptions());
		assertNotNull("To not null", serviceStub._getServiceClient().getOptions().getTo());
		assertEquals("http://localhost:8093/someroot/service/myserver/JourneyService/",
				serviceStub._getServiceClient().getOptions().getTo().getAddress());

		ServiceStubProvider.returnServiceStub(serviceStub);

		ServiceStubProvider.clear();

		CallbackRunnable.CLEANUP.run();
	}

	/* TODO: does not work because of static STRATEGY which is not re-initialized
	@Test
	public void testWithAllStrategies() {
		EasyTravelConfig CONFIG = EasyTravelConfig.read();

		for(ServiceStubStrategy strategy : ServiceStubStrategy.values()) {
			log.info("Using strategy: " + strategy);

			CONFIG.serviceStubStrategy = strategy;

			JourneyServiceStub serviceStub = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);
			assertNotNull(serviceStub);

			assertNotNull("ServiceClient not null", serviceStub._getServiceClient());
			assertNotNull("Options not null", serviceStub._getServiceClient().getOptions());
			assertNotNull("To not null", serviceStub._getServiceClient().getOptions().getTo());
			assertEquals("http://localhost:" + CONFIG.backendPort + "/services/JourneyService/",
					serviceStub._getServiceClient().getOptions().getTo().getAddress());

			ServiceStubProvider.returnServiceStub(serviceStub);
		}
	}*/


	@Ignore("Not finished")
	@Test
	public void testGetServiceStubFails() throws IOException {
		MockRESTServer server = new MockRESTServer(new HTTPRunnable() {

			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
				throw new IllegalStateException("test exception");
			}
		}, NanoHTTPD.HTTP_INTERNALERROR, NanoHTTPD.MIME_PLAINTEXT, "failed");

		EasyTravelConfig CONFIG = EasyTravelConfig.read();
		CONFIG.backendPort = server.getPort();
		CONFIG.webServiceBaseDir = "http://localhost:" + server.getPort() + "/services/";

		// clear to make config-changes active
		ServiceStubProvider.clear();

		try {
			// ensure that we can do many invocations of this without running into trouble
			for (int i = 0; i < TEST_COUNT; i++) {
				JourneyServiceStub serviceStub = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);
				assertNotNull(serviceStub);

				assertNotNull("ServiceClient not null", serviceStub._getServiceClient());
				assertNotNull("Options not null", serviceStub._getServiceClient().getOptions());
				assertNotNull("To not null", serviceStub._getServiceClient().getOptions().getTo());
				assertEquals("http://localhost:" + server.getPort() + "/services/JourneyService/",
						serviceStub._getServiceClient().getOptions().getTo().getAddress());

				FindLocationsDocument doc = FindLocationsDocument.Factory.newInstance();
		    	doc.setFindLocations(FindLocationsDocument.FindLocations.Factory.newInstance());
		    	doc.getFindLocations().setName("Paris");
		    	doc.getFindLocations().setMaxResultSize(10);
		    	doc.getFindLocations().setCheckForJourneys(true);
		    	JourneyServiceStub journeyService = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);

		    	FindLocationsResponseDocument res = journeyService.findLocations(doc);
		    	assertTrue(res.getFindLocationsResponse().getReturnArray().length > 0);

				ServiceStubProvider.returnServiceStub(serviceStub);
			}
		} finally {
			server.stop();
		}

		ServiceStubProvider.clear();
	}
}
