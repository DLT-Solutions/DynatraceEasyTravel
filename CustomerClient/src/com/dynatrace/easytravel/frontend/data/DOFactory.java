package com.dynatrace.easytravel.frontend.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.jpa.business.xsd.Booking;
import com.dynatrace.easytravel.jpa.business.xsd.Journey;
import com.dynatrace.easytravel.jpa.business.xsd.Location;
import com.dynatrace.easytravel.jpa.business.xsd.User;
import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 *  Display Object (DO) Factory
 *  Takes Axis XSD object that are very memory consuming
 *  and returns corresponding DO objects.
 *
 * @author philipp.grasboeck
 */
class DOFactory {

	private static final Logger log = LoggerFactory.make();

	private static final Map<Integer, byte[]> PICTURE_CACHE = new ConcurrentHashMap<Integer, byte[]>();

	private static byte[] getPicture(Journey delegate) {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		if (!CONFIG.cacheJourneyPictures)
		{
			return delegate.getPicture(); // inefficient - of course
		}

		Integer journeyId = delegate.getId();
		byte[] picture = PICTURE_CACHE.get(journeyId);
		if (picture == null)
		{
			picture = delegate.getPicture();
			if (picture != null) // ConcurrentHashMap doesn't allow null values but HashMap does ...
			{
				PICTURE_CACHE.put(journeyId, picture);
				if (log.isDebugEnabled()) log.debug("Put picture to cache for journeyId=" + journeyId);
			}
		}

		return picture;
	}

	static JourneyDO[] getWrappers(Journey[] journeys)
	{
		JourneyDO[] result = new JourneyDO[journeys.length];
		for (int i = 0; i < journeys.length; i++)
		{
			result[i] = getWrapper(journeys[i]);

		}
		return result;
	}

	static JourneyDO getWrapper(Journey j)
	{
		return j != null && !j.isNil() ? new JourneyDO(j.getId(), j.getName(), j.getFromDate(), j.getToDate(), j.getStart().getName(), j.getDestination().getName(), j.getTenant().getName(), j.getAmount(), getPicture(j)) : null;
	}

	static LocationDO[] getWrappers(Location[] locations)
	{
		LocationDO[] result = new LocationDO[locations.length];

		for (int i = 0; i < locations.length; i++)
		{
			result[i] = getWrapper(locations[i]);
		}
		return result;
	}

	static LocationDO getWrapper(Location l)
	{
		return l != null && !l.isNil() ? new LocationDO(l.getName()) : null;
	}

	static UserDO[] getWrappers(User[] users) {
	    UserDO[] result = new UserDO[users.length];
	    for (int i = 0; i < users.length; i++) {
	        User user = users[i];
	        result[i] = new UserDO(user.getName(), user.getPassword());
	    }
	    return result;
	}
	
	static BookingDO[] getWrapper(Booking[] bookings) {
		BookingDO[] result = new BookingDO[bookings.length];
		for(int i=0;i<bookings.length;i++) {
			result[i]= getWrapper(bookings[i]);
		}
		return result;
	}
	
	static BookingDO getWrapper(Booking booking) {
		return new BookingDO(booking.getId(), getWrapper(booking.getJourney()), booking.getUser().getName(), booking.getBookingDate());
	}
}
