package com.dynatrace.easytravel.business.webservice;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.jws.WebMethod;

import org.apache.commons.lang3.time.DateUtils;
import ch.qos.logback.classic.Logger;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import com.codahale.metrics.Timer.Context;
import com.dynatrace.easytravel.business.webservice.transferobj.JourneyPage;
import com.dynatrace.easytravel.business.webservice.transferobj.LocationPage;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.metrics.Metrics;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.util.LocationParser;

public class JourneyService {
	private static final Logger log = LoggerFactory.make();

	private final GenericPluginList plugins = new GenericPluginList(PluginConstants.BACKEND_JOURNEY_SERVICE);
	
	private final Tracer tracer = GlobalOpenTelemetry.getTracer("io.opentelemetry.api.GlobalOpenTelemetr", "0.16.0");
	
	public JourneyService() {
		LocationParser.startCalibration(); // Helper class to cause cpu overhead on demand
	}

    /*
     * Query Key for memory leak. As this class does not implement hashCode nor equals "equal" keys can never be found in a Map.
     */
    protected static class QueryKey {

        private String key;

        protected QueryKey(String key) {
            this.key = key;
        }

        protected String getKey() {
            return key;
        }
    }

	private static final AtomicReference<ConcurrentMap<QueryKey, Collection<? extends Location>>> leakyLocationCache = new AtomicReference<ConcurrentMap<QueryKey, Collection<? extends Location>>>();

    private Configuration configuration;
    private DataAccess databaseAccess;
    private LocationProvider locationCache;

    //@Autowired
    @WebMethod(exclude=true)
    public void setDatabaseAccess(DataAccess bookingService) {
		this.databaseAccess = bookingService;
	}

    @WebMethod(exclude=true)
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

    @WebMethod(exclude=true)
    public void setLocationCache(LocationProvider locationCache) {
        this.locationCache = locationCache;
    }

    public Journey[] getJourneys() {
		Collection<? extends Journey> list = databaseAccess.allJourneys();
		plugins.execute(PluginConstants.BACKEND_JOURNEY_GETALL, list);
		return list.toArray(new Journey[list.size()]);
    }

    public int getJourneyIndexByName(String tenantName, String journeyName) {
    	return databaseAccess.getJourneyIndexByName(tenantName, journeyName);
    }

	public String[] getJourneyNames() {
		List<String> list = new ArrayList<String>();
		for(Journey journey : databaseAccess.allJourneys()) {
			list.add(journey.getName());
		}

		String externalUrl = configuration.getExternalUrl();
		if(externalUrl != null && externalUrl.length() > 0) {
			// also asking some website for some stuff
			try {
				// just to test some external website to see if this is listed in the Transaction Flow already...
				UrlUtils.retrieveData(externalUrl, null, 30000);
				log.info("Retrieved external content...");
			} catch (IOException e) {
				log.warn("Failed to retrieve external website", e);
			}
		}

		plugins.execute(PluginConstants.BACKEND_JOURNEY_GETALLNAMES, list);

		log.info("Return journey names: " + list);

		return list.toArray(new String[list.size()]);
	}

	/*
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
	public int addJourney(String name, String from, String to, String tenantName, Calendar dateFrom, Calendar dateTo, double amount, byte [] picture) {
		log.info("Create journey: " + name);
		Journey j = databaseAccess.createJourney(name, from, to, tenantName, dateFrom.getTime(), dateTo.getTime(), amount, picture);
		plugins.execute(PluginConstants.BACKEND_JOURNEY_ADD, name, from, to, tenantName, dateFrom, dateTo, amount, j );
		return j.getId();
	}

	/**
	 * Find locations by a part of the name, case insensitive.
	 * @param name
	 * @param maxResultSize;
	 * @return
	 */
	public Location[] findLocations(String name, int maxResultSize, boolean checkForJourneys) {
	    LocationProvider locationProvider = getLocationProvider();
		AtomicBoolean memoryLeakEnabled = new AtomicBoolean(false);
		plugins.execute(PluginConstants.BACKEND_LOCATION_SEARCH_BEFORE, memoryLeakEnabled);

		// helper method for CPU usage, plugins, ...
		checkDestination(name);

		log.debug("Looking for Locations: " + name);
		Collection<? extends Location> locations;
		ConcurrentMap<QueryKey, Collection<? extends Location>> cache = leakyLocationCache.get();
		if (configuration.isMemoryLeakEnabled() || memoryLeakEnabled.get()) {
		    if (cache == null) {
				leakyLocationCache.compareAndSet(null, new ConcurrentHashMap<QueryKey, Collection<? extends Location>>());
		        cache = leakyLocationCache.get();
		    }
		    QueryKey key = new QueryKey(name);
		    locations = cache.get(key);
			if (locations == null) {

				locations = getLocations(name, checkForJourneys, locationProvider);
		        cache.put(key, locations);
		        if (log.isInfoEnabled()) {
		            log.info("leaked: " + cache.size());
		        }
		    }
		} else {
		    if (cache != null) {
		        leakyLocationCache.set(null);
		    }

			locations = getLocations(name, checkForJourneys, locationProvider);

		}

		plugins.execute(PluginConstants.BACKEND_LOCATION_SEARCH, name, locations, checkForJourneys, locationProvider );
		if (locations.size() > maxResultSize) {
			locations = new ArrayList<Location>(locations).subList(0, maxResultSize);
		}

		return locations.toArray(new Location[locations.size()]);

	}

	/**
	 * @param locationName
	 * @param checkForJourneys
	 * @param locationProvider
	 * @return
	 */
	private Collection<? extends Location> getLocations(String locationName, boolean checkForJourneys,
			LocationProvider locationProvider) {
		if (!checkForJourneys) {
			return locationProvider.getMatchingLocations(locationName);
		}

		if (configuration.isDBSpammingEnabled()) {
			Collection<Location> matchingLocations = getMatchingJourneyDestinations(locationName);
			List<Location> locationsX = new ArrayList<Location>(matchingLocations.size());

			for (Location location : matchingLocations) {
				if (databaseAccess.isLocationUsedByJourney(location.getName())) {
					locationsX.add(location);
				}
			}
			return locationsX;
		}

		return getMatchingJourneyDestinations(locationName);
	}

	//
	// Helper function to condition getMatchingJourneyDestination on DB Slowdown plugin.
	//
	private Collection<Location> getMatchingJourneyDestinations(String name) {
		
		// Check if the DBSlowdown plugin is ON and if it is, attempt to invoke the query with the delay.
		AtomicBoolean dbSlowdownEnabled = new AtomicBoolean(false);
		plugins.execute(PluginConstants.BACKEND_LOCATION_MATCHING, dbSlowdownEnabled);
		
		return databaseAccess.getMatchingJourneyDestinations(name, dbSlowdownEnabled.get());
	}

	/**
	 * Find Journeys
	 * @param destination The destination location
	 * @param fromDate The earliest from-date (optional)
	 * @param toDate The latest to-date (optional)
	 * @return
	 */
	
	public Journey[] findJourneys(String destination, long fromDate, long toDate) {
		String dest = destination.replace(BaseConstants.PLUS, BaseConstants.WS);
		Collection<? extends Journey> journeys = null;
		
		Span span = tracer.spanBuilder("Get a journey by destination and dates").startSpan();

		// helper method for CPU usage, plugins, ...
		span.addEvent("Check destination");
		checkDestination(dest);
		span.addEvent("Destination checked");
		
		try (Scope scope = span.makeCurrent()) {
			span.addEvent("Looking for journeys");
			span.setAttribute("destination", destination);
			span.setAttribute("fromDate", fromDate);
			span.setAttribute("toDate", toDate);
			
			// Check if the DBSlowdown plugin is ON and if it is, attempt to invoke the query with the delay.
			AtomicBoolean dbSlowdownEnabled = new AtomicBoolean(false);
			plugins.execute(PluginConstants.BACKEND_LOCATION_MATCHING, dbSlowdownEnabled);
			journeys = databaseAccess.findJourneys(
						dest,
						new Date(fromDate),
						DateUtils.addDays(new Date(toDate), 1),
						dbSlowdownEnabled.get());
			
			plugins.execute(PluginConstants.BACKEND_JOURNEY_SEARCH, dest, fromDate, toDate, journeys);
	    } finally {
	      span.end();
	    }

		
		return journeys.toArray(new Journey[journeys.size()]);
	}
	
	public Journey getJourneyById(Integer id) {
		Journey journey = null;
		Span span = tracer.spanBuilder("Get a journey by ID").startSpan();
		
		try (Scope scope = span.makeCurrent()) {
			span.setAttribute("journeyID", id);
			journey = databaseAccess.getJourneyById(id);
			plugins.execute(PluginConstants.BACKEND_JOURNEY_SEARCH, id, journey);
	    } finally {
	      span.end();
	    }
		
		return journey;
	}


	public Location[] getLocations() {
		Collection<? extends Location> locations = getLocationProvider().getAll();
		plugins.execute(PluginConstants.BACKEND_LOCATION_ALL, locations);
		return locations.toArray(new Location[locations.size()]);
	}

	/**
	 * Method for arbitrary actions whenever a destination is used, currently mainly used
	 * to trigger CPU load per action.
	 *
	 * Note: this is currently called from other service-methods in here and
	 * not from the Frontend, it is exported for other usages nevertheless!
	 *
	 * @param name
	 * @return
	 * @author cwat-dstadler
	 */
	public boolean checkDestination(String destination) {
		// record some metrics for these to be able to analyse if we see enough CPU slowdown
		Context time = Metrics.registry().timer("journeyservice.checkdestination").time();
		try {
			
	    // Allow plugins to do more stuff here
		plugins.execute(PluginConstants.BACKEND_DESTINATION_CHECK, destination);

		LocationParser.parseSection(EasyTravelConfig.read().backendCPUCycleTime);
		
		// add an extra delay if the plugin is turned on
		log.debug("ValidateName...");
		plugins.execute(PluginConstants.BACKEND_JOURNEY_VALIDATENAME);
		
		} finally {
			time.stop();
		}

		return true;
	}

	/**
	 *
	 *
	 * @param fromIdx inclusive, -1 means last page
	 * @param count
	 * @return
	 * @author peter.kaiser
	 *
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
	public LocationPage getLocationPage(int fromIdx, int count) {
		LocationProvider locationProvider = getLocationProvider();
		int cnt = count;
		int total = locationProvider.getCount();
		if (fromIdx == -1 || fromIdx + count > total) {
			cnt = total % count;
			fromIdx = total - cnt;
		}
		Collection<? extends Location> locations = locationProvider.getLocations(fromIdx, cnt);
        plugins.execute(PluginConstants.BACKEND_LOCATION_ALL_PAGE,  fromIdx, count, locations );
		return new LocationPage(locations.toArray(new Location[locations.size()]), fromIdx, count, total);
    }


	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
	public boolean addLocation(String name) {
		plugins.execute(PluginConstants.BACKEND_LOCATION_ADD, name);
        return databaseAccess.addLocation(name);
    }


	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
	public boolean deleteLocation(String name) {
		plugins.execute(PluginConstants.BACKEND_LOCATION_DELETE, name);
	    return databaseAccess.deleteLocation(name);
	}


	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
	public boolean deleteJourney(int id) {
		plugins.execute(PluginConstants.BACKEND_JOURNEY_DELETE, id);
		databaseAccess.deleteJourney(id);
	    return true;
	}


	public Journey[] getJourneysByTenant(String tenantName) {
		Collection<? extends Journey> journeys = databaseAccess.getJourneysByTenant(tenantName);
		plugins.execute(PluginConstants.BACKEND_JOURNEY_BY_TENANT,  tenantName, journeys );
		return journeys.toArray(new Journey[journeys.size()]);
	}

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 *
	 * @param fromIdx inclusive, -1 means last page
	 */
	public JourneyPage getJourneyPageByTenant(String tenantName, int fromIdx, int count) {
		int cnt = count;
		int total = databaseAccess.getJourneyCountByTenant(tenantName);
		if (fromIdx == -1 || fromIdx + count > total) {
			cnt = total % count;
			fromIdx = total - cnt;
		}
		Collection<? extends Journey> journeys = databaseAccess.getJourneysByTenant(tenantName, fromIdx, cnt);
		plugins.execute(PluginConstants.BACKEND_JOURNEY_BY_TENANT_PAGE, tenantName, fromIdx, count, journeys );
		return new JourneyPage(journeys.toArray(new Journey[journeys.size()]), fromIdx, count, total);
	}


	private LocationProvider getLocationProvider() {
	    AtomicBoolean useCache = new AtomicBoolean(false);
	    plugins.execute(PluginConstants.BACKEND_LOCATION_ACTION_BEFORE, useCache);

		return useCache.get() ? locationCache : databaseAccess.getLocationProvider();
	}
}
