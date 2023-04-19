package com.dynatrace.easytravel.business.webservice;

import java.io.IOException;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jws.WebMethod;

import org.apache.axis2.AxisFault;
import ch.qos.logback.classic.Logger;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import com.codahale.metrics.Timer.Context;
import com.dynatrace.easytravel.business.webservice.transferobj.BookingPage;
import com.dynatrace.easytravel.business.webservice.transferobj.BookingSummary;
import com.dynatrace.easytravel.business.webservice.transferobj.BookingTO;
import com.dynatrace.easytravel.cache.PaymentService;
import com.dynatrace.easytravel.ipc.NativeApplication;
import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.metrics.Metrics;
import com.dynatrace.easytravel.misc.UserType;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.PluginList;

public class BookingService {
    private static final Logger log = LoggerFactory.make();

    private DataAccess databaseAccess;
    private Configuration configuration;

    private final PluginList<NativeApplication> nativeApplicationPlugins = new PluginList<NativeApplication>(NativeApplication.class);
    private final PluginList<PaymentService> paymentServicePlugins = new PluginList<PaymentService>(PaymentService.class);
	private final GenericPluginList plugins = new GenericPluginList(PluginConstants.BACKEND_BOOKING_SERVICE);

	private final Tracer tracer = GlobalTracer.get();
	
    @WebMethod(exclude=true)
    public void setDatabaseAccess(DataAccess bookingService) {
		this.databaseAccess = bookingService;
	}

    @WebMethod(exclude=true)
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
    
    public Booking getBookingById(String bookingId) {
    	return databaseAccess.getBookingById(bookingId);
    }
    
    public Booking[] getRecentBookings(int bookingsLimit) {
    	Collection<Booking> bookings = databaseAccess.getRecentBookings(bookingsLimit);

		if (bookings == null) {
			return new Booking[] {};
		}

		return bookings.toArray(new Booking[bookings.size()]);
    }

	public String[] getBookingIds(String username) {
		log.info("Return all Booking-IDs for user: " + username);
		Collection<Booking> bookings = databaseAccess.findBookings(username);

		if (bookings == null) {
			return new String[] {};
		}

		List<String> bookingIds = new ArrayList<String>();
		for (Booking booking : bookings) {
			bookingIds.add(booking.getId());
		}

		return bookingIds.toArray(new String[bookingIds.size()]);
	}

	public Booking[] getBooking(String username) {
		log.info("Return all Bookings for user: " + username);
		Collection<Booking> bookings = databaseAccess.findBookings(username);

		if (bookings == null) {
			return new Booking[] {};
		}

		return bookings.toArray(new Booking[bookings.size()]);
	}

	/**
	 * Tries to store a booking
	 *
	 * @param journeyId
	 * @param userName
	 * @param creditCard
	 * @param amount the amount (total cost) of the booked journey. this may be null and defaults to the journey amount.
	 * @return the UUID of the booking, if successful, null otherwise
	 * @throws AxisFault
	 * @author philipp.grasboeck
	 */
	public String storeBooking(Integer journeyId, String userName, UserType userType, String creditCard, Double amount) throws AxisFault {
		Context time = Metrics.registry().timer("bookingservice.storebooking").time();
		
		Span storeSpan = tracer.buildSpan("store-booking")
				.withTag("journeyID", journeyId)
				.withTag("value", amount)
				.start();
		
		try (Scope scope = tracer.activateSpan(storeSpan)) {		
			User user = databaseAccess.getUser(userName);
			if (user == null) {
				throw new IllegalArgumentException("Could not find user '" + userName + "'");
			}
			
			boolean normalize = false;	
			
			// Allow plugins to enable DB Spamming as well
			AtomicBoolean dbSpammingEnabled = new AtomicBoolean(false);
			AtomicBoolean dbSlowdown = new AtomicBoolean(false);
			plugins.execute(PluginConstants.BACKEND_BOOKING_STORE_BEFORE, dbSpammingEnabled, dbSlowdown);
			
			if(dbSlowdown.get()){
				normalize = true;
			}
			
			Journey journey = null;
			if (configuration.isDBSpammingEnabled() || dbSpammingEnabled.get()) {
				for (Integer jId : databaseAccess.allJourneyIds()) {
			        Journey jTmp2 = databaseAccess.getJourneyByIdNormalize(jId, normalize);
			        if (jTmp2 != null && journeyId != null && jTmp2.getId() == journeyId.intValue()) {
			            journey = jTmp2;
			        }
			    }
			} else {
				journey = databaseAccess.getJourneyByIdNormalize(journeyId, normalize);
			}
			
	
			if (journey == null) {
				throw new IllegalArgumentException("Could not find journey with id '" + journeyId + "'");
			}
	
			if (creditCard == null || creditCard.isEmpty()) {
				throw new IllegalArgumentException("Cannot create Booking without CreditCard details.");
			}
	
			doCheckCreditCard(userType, creditCard, true);
	
			String id = UUID.randomUUID().toString();
			boolean success = false;
			try {
		        // Destination is passed here as well in order to allow dynaTrace BTs to group by Destination
			    String paymentResult = callPaymentService(id, creditCard, user.getName(),
			    		amount != null ? amount : journey.getAmount(), journey.getDestination().getName(),
			    		journey.getTenant().getName());
	
			    checkLoyaltyStatus(user.getName(), user.getLoyaltyStatus());
	
			    if (PaymentService.PAYMENT_ACCEPTED.equals(paymentResult)) {
			        success = true;
	            } else if (PaymentService.ALREADY_PAID.equals(paymentResult)) {
	                throw new IllegalArgumentException("This booking has already been paid!");
	            } else if (PaymentService.CC_INVALID.equals(paymentResult)) {
	                throw new IllegalArgumentException("The Payment-Service reported that the provided credit card number " + creditCard + " is invalid!");
	            } else if (PaymentService.CC_WRONG_USER.equals(paymentResult)) {
	                throw new IllegalArgumentException("This credit card has already been registered for another user!");
	            } else if (PaymentService.CC_EXPIRED.equals(paymentResult)) {
	                throw new IllegalArgumentException("Credit card has expired!");
	            } else if (PaymentService.UPDATE_ERROR.equals(paymentResult)) {
	                throw new IllegalArgumentException("PaymentService returned 'UPDATE_ERROR', there seems to have been a problem with the database, check logFile of dotNet Service.");
	            } else if (PaymentService.OTHER_ERROR.equals(paymentResult)) {
	                log.error("PaymentService returned 'OTHER_ERROR'");
	                throw new IllegalArgumentException("PaymentService returned 'OTHER_ERROR', check logFile of dotNet Service.");
	            } else {
	                log.error("Unknown exception calling PaymentService");
	                throw new IllegalArgumentException("Unknown exception calling PaymentService, check logFile of dotNet Service, Web Service reported: " + paymentResult);
	            }
			} catch (IOException e) {
				log.warn("Error invoking PaymentService: " + e.getMessage(), e);
			    throw new IllegalArgumentException("Error invoking PaymentService: " + e.getMessage());
			} finally {
				time.stop();
			}
	
	        // success - store booking
	
			AtomicBoolean measureExplosionByBookingIdEnabled = new AtomicBoolean(false);
	
			if (success) {
				databaseAccess.storeBooking(new Booking(id, journey, user, new Date()));
				plugins.execute(PluginConstants.BACKEND_BOOKING_STORE, journeyId, userName, creditCard, measureExplosionByBookingIdEnabled);
			}
	
			if (measureExplosionByBookingIdEnabled.get()) {
				setBookingIdDummy(id);
			}
		
			storeSpan.finish();
			
			return success ? id : null;
		}
	}

	private String setBookingIdDummy(String id) {
		return id;
	}

    @WebMethod(exclude=true)
	// verify credit card
	// TODO: move this to .NET/Finance application and only call the respective webservice from here
    private boolean doCheckCreditCard(UserType userType, String creditCard, boolean shouldThrow) {
    	try {
			return tryCheckCreditCard(userType, creditCard, shouldThrow);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error while checking credit card", e);
		}
    }

    @WebMethod(exclude=true)
    private boolean tryCheckCreditCard(UserType userType, String creditCard, boolean shouldThrow) throws IOException {

    	for (NativeApplication app : nativeApplicationPlugins) {
    		String result = app.sendAndReceive(userType, creditCard);
    		if (result != null) {
    			boolean valid = result.startsWith(NativeApplication.VALID);
	    		if (!valid && shouldThrow) {
	    			boolean oneAgentSdkFailure = result.startsWith(NativeApplication.FAILED);
	    			if(oneAgentSdkFailure)
	    				throw new IllegalArgumentException("OneAgent SDK externalOutgoingRemoteCall() failed. Result=" + result);
	    			else
	    				throw new IllegalArgumentException("Provided CreditCard number was not valid: " + creditCard + ", result=" + result);
	    		}
	    		return valid;
    		}
    	}

		throw new IllegalStateException("No NativeApplication (CheckCreditCard) found or all NativeApplication plugins returned null. Check if application has fully started.");
    }

    public boolean checkCreditCard(UserType userType, String creditCard) {
		log.info("Check Credit card: " + creditCard);
		return doCheckCreditCard(userType, creditCard, false);
    }

	/**
	 * Invoke the enabled PaymentService plugins.
	 * This returns of all enabled PaymentService plugins the first result that is non-null.
	 * Result is guaranteed to be non-null if the method returns.
	 * If no PaymentService plugin is found or they all return null, an IllegalStateException is thrown.
	 *
	 * @param bookingId
	 * @param creditCard
	 * @param user
	 * @param amount
	 * @param location
	 * @param tenant
	 * @return
	 * @throws IOException
	 * @author philipp.grasboeck
	 *
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
    @WebMethod(exclude=true)
    private String callPaymentService(String bookingId, String creditCard, String user, double amount,
			String location, String tenant) throws IOException {

    	for (PaymentService service : paymentServicePlugins) {
			String result = service.callPaymentService(bookingId, creditCard, user, amount, location, tenant);
			if (result != null) {
				return result;
			}
		}

		throw new IllegalStateException("No PaymentService found or all PaymentService plugins returned null. Check if application has fully started.");
	}

    /*
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
    @WebMethod(exclude=true)
    private void checkLoyaltyStatus(String user, String loyaltyStatus) throws IOException {
    	// currently we do nothing with the loyaltyStatus here as we just want to be able to capture it in dynaTrace
    }

    /**
	 *
	 *
	 * @param tenantName
	 * @param locationCount
	 * @return an array containing all bookings for the given tenant
	 */
	public BookingSummary getBookingSummaryByTenant(String tenantName, int locationCount) {
	    int nBookings = databaseAccess.getBookingCountByTenant(tenantName);
	    double totalSales = databaseAccess.getTotalSalesByTenant(tenantName);
		Map<Location, Integer> departures = databaseAccess.getDeparturesByTenant(tenantName, locationCount);
		Map<Location, Integer> destinations = databaseAccess.getDestinationsByTenant(tenantName, locationCount);
		return new BookingSummary(nBookings, totalSales, departures, destinations);
	}

	/**
	 *
	 *
	 * @param tenantName
	 * @param fromIdx the startIndex for the page, -1 means last page.
	 * @param count
	 * @return
	 * @author peter.kaiser
	 */
	public BookingPage getBookingPageByTenant(String tenantName, int fromIdx, int count) {
		int cnt = count;
		int total = databaseAccess.getBookingCountByTenant(tenantName);
		if (fromIdx == -1 || fromIdx + count > total) {
			cnt = total % count;
			fromIdx = total - cnt;
		}
		Collection<Booking> bookings = databaseAccess.getBookingsByTenant(tenantName, fromIdx, cnt);

		plugins.execute(PluginConstants.BACKEND_BOOKING_BY_TENANT_PAGE, tenantName, fromIdx, count, bookings);
	    return new BookingPage(bookingListToArray(bookings), fromIdx, count, total);
	}

    @WebMethod(exclude=true)
	private BookingTO[] bookingListToArray(Collection<Booking> bookings) {
	    BookingTO[] ret = new BookingTO[bookings.size()];

		int i = 0;
		for (Booking bkg : bookings) {

            Journey j = bkg.getJourney();
            Calendar cal;
            if (bkg.getBookingDate() != null) {
                cal = Calendar.getInstance();
                cal.setTime(bkg.getBookingDate());
            } else {
                cal = null;
            }

			ret[i++] = new BookingTO(bkg.getId(), cal, bkg.getUser().getName(), j.getId(), j.getName(), j.getStart().getName(),
					j.getDestination().getName());
        }
        return ret;
	}


    /**
     * Returns a statistic of the hibernate cache.
     * Only returns sensible results if generate_statistics in persistence.xml
     * is on:
     *
     * <property name="hibernate.generate_statistics" value="true" />
     *
     * @return
     * @author philipp.grasboeck
     */
    public String getDatabaseStatistics() {
    	return databaseAccess.getStatistics();
    }
}
