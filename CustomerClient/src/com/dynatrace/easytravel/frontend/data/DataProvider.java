package com.dynatrace.easytravel.frontend.data;

import java.rmi.RemoteException;
import java.util.Date;

import org.apache.ws.namespaces.axis2.enums.ComDynatraceEasytravelMiscUserType.Enum;

import com.codahale.metrics.Timer.Context;
import com.dynatrace.easytravel.business.client.AuthenticationServiceStub;
import com.dynatrace.easytravel.business.client.BookingServiceStub;
import com.dynatrace.easytravel.business.client.JourneyServiceStub;
import com.dynatrace.easytravel.business.webservice.*;
import com.dynatrace.easytravel.cache.MemoryCache;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.metrics.Metrics;
import com.dynatrace.easytravel.misc.UserType;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.PluginList;
import com.dynatrace.easytravel.util.ServiceStubProvider;

import ch.qos.logback.classic.Logger;

/**
 * This bean encapsulates data provider methods for the frontend,
 * with optional MemoryCache plugin.
 *
 * @author philipp.grasboeck
 */
public class DataProvider implements DataProviderInterface {

	private static final String TYPE_LOCATION_SEARCH_RESULT = "LocationSearchResult";

	private static final String TYPE_JOURNEY_SEARCH_RESULT = "JourneySearchResult";

	private static final String TYPE_JOURNEY = "Journey";

    private static final Logger log = LoggerFactory.make();

	private final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_DATA_PROVIDER);
	private final PluginList<MemoryCache> memoryCachePlugins = new PluginList<MemoryCache>(MemoryCache.class);

    /* (non-Javadoc)
	 * @see com.dynatrace.easytravel.frontend.data.DataProviderInterface#findLocations(java.lang.String, int)
	 */
    @Override
	public LocationDO[] findLocations(String destination, int maxResultSize) throws RemoteException {
    	LocationDO[] locations;
		for (MemoryCache cache : memoryCachePlugins){
			if ((locations = (LocationDO[]) cache.get(TYPE_LOCATION_SEARCH_RESULT, destination)) != null) {
		    	log.info("CACHE hit for: " + destination);
				return locations; // cache hit
			}
		}

    	FindLocationsDocument doc = FindLocationsDocument.Factory.newInstance();
    	doc.setFindLocations(FindLocationsDocument.FindLocations.Factory.newInstance());
    	doc.getFindLocations().setName(destination);
    	doc.getFindLocations().setMaxResultSize(maxResultSize);
    	doc.getFindLocations().setCheckForJourneys(true);
    	JourneyServiceStub journeyService = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);
        final Context context = Metrics.getTimerContext(this, "findLocations");
    	try {
	    	FindLocationsResponseDocument res = journeyService.findLocations(doc);
	    	locations = DOFactory.getWrappers(res.getFindLocationsResponse().getReturnArray());
	    	ServiceStubProvider.returnServiceStub(journeyService);
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(journeyService);
			throw e;
		} finally {
            context.close();
        }

		for (MemoryCache cache : memoryCachePlugins) {
			cache.put(locations, TYPE_LOCATION_SEARCH_RESULT, destination);
		}

    	return locations;
    }

    /* (non-Javadoc)
	 * @see com.dynatrace.easytravel.frontend.data.DataProviderInterface#findJourneys(java.lang.String, java.util.Date, java.util.Date)
	 */
    @Override
	public JourneyDO[] findJourneys(String destination, Date fromDate, Date toDate) throws RemoteException  {
    	JourneyDO[] journeys;

		plugins.execute(PluginConstants.FRONTEND_JOURNEY_SEARCH, destination, fromDate, toDate);

		for (MemoryCache cache : memoryCachePlugins) {
			if ((journeys = (JourneyDO[]) cache.get(TYPE_JOURNEY_SEARCH_RESULT, destination, fromDate, toDate)) != null) {
		    	log.info("CACHE hit for: " + destination + ", " + fromDate + ", " + toDate);
				return journeys; // cache hit
			}
		}

    	FindJourneysDocument doc = FindJourneysDocument.Factory.newInstance();
    	doc.setFindJourneys(FindJourneysDocument.FindJourneys.Factory.newInstance());
    	doc.getFindJourneys().setDestination(destination);
    	doc.getFindJourneys().setFromDate(fromDate != null ? fromDate.getTime() : 0);
    	doc.getFindJourneys().setToDate(toDate != null ? toDate.getTime() : 33333333333333l); // far off in the future (ca. 3000)

    	JourneyServiceStub journeyService = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);
        final Context context = Metrics.getTimerContext(this, "findJourneys");
        try {
	    	FindJourneysResponseDocument res = journeyService.findJourneys(doc);
	    	journeys = DOFactory.getWrappers(res.getFindJourneysResponse().getReturnArray());
	    	ServiceStubProvider.returnServiceStub(journeyService);
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(journeyService);
			throw e;
		} finally {
            context.close();
        }

		for (MemoryCache cache : memoryCachePlugins) {
			cache.put(journeys, TYPE_JOURNEY_SEARCH_RESULT, destination, fromDate, toDate);
		}

		plugins.execute(PluginConstants.FRONTEND_JOURNEY_SEARCH_AFTER, (Object) journeys);

    	return journeys;
    }

    /*@Override
	public boolean checkDestination(String destination) throws RemoteException {
		plugins.execute(PluginConstants.FRONTEND_DESTINATION_CHECK, destination);

    	CheckDestinationDocument doc = CheckDestinationDocument.Factory.newInstance();
    	doc.setCheckDestination(CheckDestinationDocument.CheckDestination.Factory.newInstance());
    	doc.getCheckDestination().setDestination(destination);

    	JourneyServiceStub journeyService = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);
    	try {
	    	CheckDestinationResponseDocument res = journeyService.CheckDestination(doc);
	    	return res.getCheckDestinationResponse().getReturn();
	    	ServiceStubProvider.returnServiceStub(journeyService);
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(journeyService);
			throw e;
		}
	}*/


    /* (non-Javadoc)
	 * @see com.dynatrace.easytravel.frontend.data.DataProviderInterface#checkCreditCard(java.lang.String)
	 */
    @Override
	public boolean checkCreditCard(String creditCard) throws RemoteException {
    	CheckCreditCardDocument doc = CheckCreditCardDocument.Factory.newInstance();
    	doc.setCheckCreditCard(CheckCreditCardDocument.CheckCreditCard.Factory.newInstance());
    	doc.getCheckCreditCard().setCreditCard(creditCard);
    	doc.getCheckCreditCard().setUserType(Enum.forString(UserType.WEB.toString()));

    	BookingServiceStub bookingService = ServiceStubProvider.getServiceStub(BookingServiceStub.class);
        final Context context = Metrics.getTimerContext(this, "checkCreditCard");
    	try {
	    	CheckCreditCardResponseDocument res = bookingService.checkCreditCard(doc);
	    	boolean valid = res.getCheckCreditCardResponse().getReturn();
	    	ServiceStubProvider.returnServiceStub(bookingService);
	    	return valid;
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(bookingService);
			throw e;
		} finally {
            context.close();
        }
    }

    @Override
	public String storeBooking(Integer journeyId, String userName, UserType userType, String creditCard, Double amount) throws RemoteException {    	
    	StoreBookingDocument doc = StoreBookingDocument.Factory.newInstance();
    	doc.setStoreBooking(StoreBookingDocument.StoreBooking.Factory.newInstance());
    	doc.getStoreBooking().setJourneyId(journeyId);
    	doc.getStoreBooking().setUserName(userName);
    	doc.getStoreBooking().setCreditCard(creditCard);
    	doc.getStoreBooking().setAmount(amount);
    	Enum userTypeEnum = Enum.forString(userType.toString());
    	doc.getStoreBooking().setUserType(userTypeEnum);
    	BookingServiceStub bookingService = ServiceStubProvider.getServiceStub(BookingServiceStub.class);
        final Context context = Metrics.getTimerContext(this, "storeBooking");
    	try {
    		StoreBookingResponseDocument res = bookingService.storeBooking(doc);
        	String id = res.getStoreBookingResponse().getReturn();
        	ServiceStubProvider.returnServiceStub(bookingService);
        	return id;
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(bookingService);
			throw e;
		} finally {
            context.close();
        }
    }

    /* (non-Javadoc)
	 * @see com.dynatrace.easytravel.frontend.data.DataProviderInterface#getJourneyById(java.lang.Integer)
	 */
    @Override
	public JourneyDO getJourneyById(Integer id) throws RemoteException {
    	JourneyDO journey;
		for (MemoryCache cache : memoryCachePlugins) {
			if ((journey = (JourneyDO) cache.get(TYPE_JOURNEY, id)) != null) {
		    	log.info("CACHE hit for: " + id);
				return journey; // cache hit
			}
		}

    	GetJourneyByIdDocument doc = GetJourneyByIdDocument.Factory.newInstance();
    	doc.setGetJourneyById(GetJourneyByIdDocument.GetJourneyById.Factory.newInstance());
    	doc.getGetJourneyById().setId(id);

    	JourneyServiceStub journeyService = ServiceStubProvider.getServiceStub(JourneyServiceStub.class);
        final Context context = Metrics.getTimerContext(this, "getJourneyById");
    	try {
	    	GetJourneyByIdResponseDocument res = journeyService.getJourneyById(doc);
	    	journey = DOFactory.getWrapper(res.getGetJourneyByIdResponse().getReturn());
	    	ServiceStubProvider.returnServiceStub(journeyService);
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(journeyService);
			throw e;
		} finally {
            context.close();
        }

		for (MemoryCache cache : memoryCachePlugins) {
			cache.put(journey, TYPE_JOURNEY, id);
		}

    	return journey;
    }

    private final static GetDatabaseStatisticsDocument databaseStatisticsDocument = GetDatabaseStatisticsDocument.Factory.newInstance();
    static {
    	databaseStatisticsDocument.setGetDatabaseStatistics(GetDatabaseStatisticsDocument.GetDatabaseStatistics.Factory.newInstance());
    }
    /* (non-Javadoc)
	 * @see com.dynatrace.easytravel.frontend.data.DataProviderInterface#getDatabaseStatistics()
	 */
    @Override
	public String getDatabaseStatistics() throws RemoteException {
    	BookingServiceStub bookingService = ServiceStubProvider.getServiceStub(BookingServiceStub.class);
    	try {

    		GetDatabaseStatisticsResponseDocument res = bookingService.getDatabaseStatistics(databaseStatisticsDocument);
    		ServiceStubProvider.returnServiceStub(bookingService);
    		return res.getGetDatabaseStatisticsResponse().getReturn();
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(bookingService);
			throw e;
		}
    }

    private final static GetUsersDocument usersDocument = GetUsersDocument.Factory.newInstance();
    static {
    	usersDocument.setGetUsers(GetUsersDocument.GetUsers.Factory.newInstance());
    }
    /* (non-Javadoc)
	 * @see com.dynatrace.easytravel.frontend.data.DataProviderInterface#getUsers()
	 */
    @Override
	public UserDO[] getUsers() throws RemoteException {
        AuthenticationServiceStub authenticationService = ServiceStubProvider.getServiceStub(AuthenticationServiceStub.class);
        final Context context = Metrics.getTimerContext(this, "getUsers");
        try {

        	GetUsersResponseDocument resp = authenticationService.getUsers(usersDocument);
    		ServiceStubProvider.returnServiceStub(authenticationService);
        	return DOFactory.getWrappers(resp.getGetUsersResponse().getReturnArray());
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(authenticationService);
			throw e;
		} finally {
            context.close();
        }
    }

    /* (non-Javadoc)
	 * @see com.dynatrace.easytravel.frontend.data.DataProviderInterface#getUsersWithPrefix(java.lang.String)
	 * @author Michal.Bakula
	 */
    @Override
	public UserDO[] getUsersWithPrefix(String pref) throws RemoteException {

    	GetTwentyUsersDocument doc =GetTwentyUsersDocument.Factory.newInstance();
    	doc.setGetTwentyUsers(GetTwentyUsersDocument.GetTwentyUsers.Factory.newInstance());
    	doc.getGetTwentyUsers().setPref(pref);

        AuthenticationServiceStub authenticationService = ServiceStubProvider.getServiceStub(AuthenticationServiceStub.class);
        final Context context = Metrics.getTimerContext(this, "getUsersWithPrefix");
        try {
        	GetTwentyUsersResponseDocument resp = authenticationService.getTwentyUsers(doc);
    		ServiceStubProvider.returnServiceStub(authenticationService);
        	return DOFactory.getWrappers(resp.getGetTwentyUsersResponse().getReturnArray());
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(authenticationService);
			throw e;
		} finally {
            context.close();;
        }
    }

	@Override
	public BookingDO getBookingById(String bookingId) throws RemoteException {

		GetBookingByIdDocument doc = GetBookingByIdDocument.Factory.newInstance();
		doc.setGetBookingById(GetBookingByIdDocument.GetBookingById.Factory.newInstance());
		doc.getGetBookingById().setBookingId(bookingId);

		BookingServiceStub bookingService = ServiceStubProvider.getServiceStub(BookingServiceStub.class);
		final Context context = Metrics.getTimerContext(this, "getBookingById");
		try {
			GetBookingByIdResponseDocument resp = bookingService.getBookingById(doc);
			ServiceStubProvider.returnServiceStub(bookingService);
			return DOFactory.getWrapper(resp.getGetBookingByIdResponse().getReturn());
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(bookingService);
			throw e;
		} finally {
			context.close();
		}
	}

	@Override
	public BookingDO[] getRecentBookings(int bookingsLimit) throws RemoteException {

		GetRecentBookingsDocument doc = GetRecentBookingsDocument.Factory.newInstance();
		doc.setGetRecentBookings(GetRecentBookingsDocument.GetRecentBookings.Factory.newInstance());
		doc.getGetRecentBookings().setBookingsLimit(bookingsLimit);

		BookingServiceStub bookingService = ServiceStubProvider.getServiceStub(BookingServiceStub.class);
		final Context context = Metrics.getTimerContext(this, "getRecentBookings");
		try {
			GetRecentBookingsResponseDocument resp = bookingService.getRecentBookings(doc);
			ServiceStubProvider.returnServiceStub(bookingService);
			return DOFactory.getWrapper(resp.getGetRecentBookingsResponse().getReturnArray());
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(bookingService);
			throw e;
		} finally {
			context.close();
		}
	}
}
