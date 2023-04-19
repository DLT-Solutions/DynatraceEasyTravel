package com.dynatrace.easytravel.webservices;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.business.webservice.transferobj.JourneyPage;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;

@RunWith(MockitoJUnitRunner.class)
public class JourneyServiceTest extends WSTestBase {


    private static final String DESTINATION_NAME = "destination name";

    private static final String LOCATION_NAME = "location name";

    private static final Integer JOURNEY_COUNT = 230;

	@Mock
	LocationProvider locationProviderMock;

	@Before
	public void setUp() {
		when(dbAccess.getLocationProvider()).thenReturn(locationProviderMock);
	}

    @Test
    public void testAddJourney() {
        Journey journey = new Journey(JOURNEY_NAME, new Location(JOURNEY_FROM), new Location(JOURNEY_TO), new Tenant(TENANT_NAME, TENANT_PASSWORD, ""), FROM_DATE, TO_DATE, JOURNEY_AMOUNT, JOURNEY_PICTURE);
//        when(dbAccess.verifyLocation(JOURNEY_FROM)).thenReturn(true);
//        when(dbAccess.verifyLocation(JOURNEY_TO)).thenReturn(true);
		when(
				dbAccess.createJourney(JOURNEY_NAME, JOURNEY_FROM, JOURNEY_TO, TENANT_NAME, FROM_DATE, TO_DATE, JOURNEY_AMOUNT,
						JOURNEY_PICTURE)).thenReturn(journey);
        Calendar calFrom = new GregorianCalendar();
        calFrom.setTime(FROM_DATE);
        Calendar calTo = new GregorianCalendar();
        calTo.setTime(TO_DATE);

        journeyService.addJourney(JOURNEY_NAME, JOURNEY_FROM, JOURNEY_TO, TENANT_NAME, calFrom, calTo, JOURNEY_AMOUNT, JOURNEY_PICTURE);

    }

    @Test
    public void testFindLocation() {
    	journeyService.findLocations("a", 100, true);
    }

    @Test
    public void testFindLocationWithMemCache() {
		when(config.isMemoryLeakEnabled()).thenReturn(true);

		journeyService.findLocations("a", 100, true);
		journeyService.findLocations("a", 100, true);

		when(config.isMemoryLeakEnabled()).thenReturn(true);

		journeyService.findLocations("a", 100, true);
		journeyService.findLocations("a", 100, true);
    }
    
    @Test
    public void testAddLocation() {
// 		when(locationProviderMock.add(any(Location.class))).thenReturn(new Location());
		when(dbAccess.addLocation(LOCATION_NAME)).thenReturn(true);

        assertTrue(journeyService.addLocation(LOCATION_NAME));

    }

    @Test
    public void testDeleteJourney() {
        dbAccess.deleteJourney(JOURNEY_ID);


        assertTrue(journeyService.deleteJourney(JOURNEY_ID));

    }

    @Test
    public void testDeleteLocation() {
		//when(locationProviderMock.deleteLocation(LOCATION_NAME)).thenReturn(true);
		when(dbAccess.deleteLocation(LOCATION_NAME)).thenReturn(true);

        assertTrue(journeyService.deleteLocation(LOCATION_NAME));

    }

    @Test
    public void testDeleteLocationNotFound() {
		when(dbAccess.deleteLocation(LOCATION_NAME)).thenReturn(false);


        assertFalse(journeyService.deleteLocation(LOCATION_NAME));

    }

    @Test
    public void testFindJourneys() {
        List<Journey> journeys = createJourneyList();
		when(dbAccess.findJourneys(DESTINATION_NAME, FROM_DATE, new Date(TO_DATE.getTime() + 24 * 3600000), false)).thenReturn(journeys);

        Journey[] jArr = journeyService.findJourneys(DESTINATION_NAME, FROM_DATE.getTime(), TO_DATE.getTime());
        for (int i = 0; i < jArr.length; i++) {
            assertEquals(journeys.get(i), jArr[i]);
        }

    }


    @Test
    public void testGetJourneyById(){
        Journey journey = new Journey(JOURNEY_NAME, new Location(JOURNEY_FROM), new Location(JOURNEY_TO), new Tenant(TENANT_NAME, TENANT_PASSWORD, ""), FROM_DATE, TO_DATE, JOURNEY_AMOUNT, JOURNEY_PICTURE);
		when(dbAccess.getJourneyById(JOURNEY_ID)).thenReturn(journey);

        assertEquals(journey, journeyService.getJourneyById(JOURNEY_ID));

    }


    @Test
    public void testGetJourneyNames() {
        List<Journey> journeys = createJourneyList();
		when(dbAccess.allJourneys()).thenReturn(journeys);
		when(config.getExternalUrl()).thenReturn(null);

        String[] names = journeyService.getJourneyNames();
        for (int i = 0; i < names.length; i++) {
            assertEquals(journeys.get(i).getName(), names[i]);
        }

    }

    @Test
    public void testGetJourneyNamesWithExternalURL() throws IOException {
    	MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "ok");
    	try {
	        List<Journey> journeys = createJourneyList();
			when(dbAccess.allJourneys()).thenReturn(journeys);
			when(config.getExternalUrl()).thenReturn("http://localhost:" + server.getPort());

	        String[] names = journeyService.getJourneyNames();
	        for (int i = 0; i < names.length; i++) {
	            assertEquals(journeys.get(i).getName(), names[i]);
	        }
    	} finally {
    		server.stop();
    	}
    }

    @Test
    public void testGetJourneyNamesWithExternalURLIOException() throws IOException {
    	MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "ok");
    	int port = server.getPort();
    	server.stop();

        List<Journey> journeys = createJourneyList();
		when(dbAccess.allJourneys()).thenReturn(journeys);
		when(config.getExternalUrl()).thenReturn("http://localhost:" + port);

        String[] names = journeyService.getJourneyNames();
        for (int i = 0; i < names.length; i++) {
            assertEquals(journeys.get(i).getName(), names[i]);
        }
    }

    @Test
    public void testGetJourneyPageByTenant() {
        List<Journey> journeys = createJourneyList();
		when(dbAccess.getJourneyCountByTenant(TENANT_NAME)).thenReturn(JOURNEY_COUNT);
		when(dbAccess.getJourneysByTenant(TENANT_NAME, PAGE_FROM, PAGE_COUNT)).thenReturn(journeys);

        JourneyPage jp = journeyService.getJourneyPageByTenant(TENANT_NAME, PAGE_FROM, PAGE_COUNT);
        for (int i = 0; i < journeys.size(); i++) {
            assertEquals(journeys.get(i), jp.getJourneys()[i]);
        }

    }


    @Test
    public void testGetJourneys() {
        List<Journey> journeys = createJourneyList();
		when(dbAccess.allJourneys()).thenReturn(journeys);

        Journey[] jArr = journeyService.getJourneys();
        for (int i = 0; i < jArr.length; i++) {
            assertEquals(journeys.get(i), jArr[i]);
        }

    }

    @Test
    public void testGetJourneyIndexByName() {
    	when(dbAccess.getJourneyIndexByName("", "")).thenReturn(12);

    	assertEquals(12, journeyService.getJourneyIndexByName("", ""));
    }

    @Test
    public void testGetJourneysByTenant() {
        List<Journey> journeys = createJourneyList();
		when(dbAccess.getJourneysByTenant(TENANT_NAME)).thenReturn(journeys);

        Journey[] jArr = journeyService.getJourneysByTenant(TENANT_NAME);
        for (int i = 0; i < jArr.length; i++) {
            assertEquals(journeys.get(i), jArr[i]);
        }

    }


    @Test
    public void testGetLocationPage() {
        List<Location> locations = createLocationList();
		when(dbAccess.getLocationCount()).thenReturn(JOURNEY_COUNT);
		when(dbAccess.getLocations(PAGE_FROM, PAGE_COUNT)).thenReturn(locations);

        Location[] lArr = journeyService.getLocationPage(PAGE_FROM, PAGE_COUNT).getLocations();
        for (int i = 0; i < lArr.length; i++) {
            assertEquals(locations.get(i), lArr[i]);
        }

    }


    @Test
    public void testGetLocations() {
        List<Location> locations = createLocationList();
		when(dbAccess.allLocations()).thenReturn(locations);

        Location[] lArr = journeyService.getLocations();
        for (int i = 0; i < lArr.length; i++) {
            assertEquals(locations.get(i), lArr[i]);
        }

    }


    private List<Journey> createJourneyList() {
        List<Journey> list = new ArrayList<Journey>();
        list.add(new Journey(JOURNEY_NAME, new Location(JOURNEY_FROM), new Location(JOURNEY_TO), new Tenant(TENANT_NAME, TENANT_PASSWORD, ""), FROM_DATE, TO_DATE, JOURNEY_AMOUNT, JOURNEY_PICTURE));
        Journey j = new Journey();
        j.setName("asdf");
        list.add(j);
        return list;
    }


    private List<Location> createLocationList() {
        List<Location> list = new ArrayList<Location>();
        list.add(new Location(JOURNEY_FROM));
        list.add(new Location(JOURNEY_TO));
        list.add(new Location(LOCATION_NAME));
        list.add(new Location(DESTINATION_NAME));
        return list;
    }

}