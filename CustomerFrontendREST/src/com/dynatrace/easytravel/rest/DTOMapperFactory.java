package com.dynatrace.easytravel.rest;

import com.dynatrace.easytravel.frontend.data.BookingDO;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.frontend.data.LocationDO;
import com.dynatrace.easytravel.frontend.data.UserDO;
import com.dynatrace.easytravel.frontend.lib.User;
import com.dynatrace.easytravel.frontend.login.UserContext;
import com.dynatrace.easytravel.frontend.rest.data.*;

/**
 * Data Transfer Object (DTO) Mapper Factory
 * Takes data objects and returns DTO objects.
 * @author tibor.varga
 * @author Michal.Bakula
 */
public class DTOMapperFactory {
	
	private static final String EMPTY_STRING = "";
	
	public static JourneyDTO[] convert(JourneyDO[] journeys) {
		JourneyDTO[] dtoJourneys = new JourneyDTO[journeys.length];
		for (int i = 0; i < journeys.length; i++)
		{
			dtoJourneys[i] = convert(journeys[i]);
		}
		return dtoJourneys;
	}
	
	public static JourneyDTO convert(JourneyDO journey) {
		if(journey == null) return null;
		
		return new JourneyDTO(journey.getId(), journey.getName(), journey.getFromDate(), journey.getToDate(),
				journey.getStart(), journey.getDestination(), journey.getTenant(), journey.getAmount(),
				new JourneyImageDTO(), journey.getAverageTotalOnAngularFrontend());
	}
	
	public static LocationDTO[] convert(LocationDO[] locations) {
		LocationDTO[] dtoLocations = new LocationDTO[locations.length];
		for (int i = 0; i < locations.length; i++)
		{
			dtoLocations[i] = convert(locations[i]);
		}
		return dtoLocations;
	}
	
	public static LocationDTO convert(LocationDO location) {
		return location != null ? new LocationDTO(location.getName()) : null; 
	}
	
	public static UserDTO[] convert(UserDO[] users) {
		UserDTO[] dtoUsers = new UserDTO[users.length];
		for (int i = 0; i < users.length; i++)
		{
			dtoUsers[i] = convert(users[i]);
		}
		return dtoUsers;
	}
	
	public static UserDTO convert(UserDO user) {
		if (user == null)
			return null;
		int space = user.getName().indexOf(' ');
		String firstName = user.getName().substring(0, space);
		String lastName = user.getName().substring(space).trim();
		return new UserDTO(firstName, lastName, EMPTY_STRING, user.getPassword());
	}
	
	public static UserDTO[] convert(User[] users) {
		UserDTO[] dtoUsers = new UserDTO[users.length];
		for (int i = 0; i < users.length; i++)
		{
			dtoUsers[i] = convert(users[i]);
		}
		return dtoUsers;
	}
	
	public static UserDTO convert(User user) {
		if(user == null) return null;
		return new UserDTO(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());
	}
	
	public static UserDTO convert(UserContext uc) {
		String[] names = uc.getFullName().split(" ");
		return new UserDTO(names[0], (names.length > 1) ? names[1] : names[0], uc.getUserName(), uc.getPassword(), 
				"", "", "", "", "", uc.getLoyaltyStatus());
	}
	
	public static BookingDTO[] convert(BookingDO[] bookings) {
		BookingDTO[] bookingDTOs = new BookingDTO[bookings.length];
		for(int i=0;i<bookings.length;i++) {
			bookingDTOs[i] = convert(bookings[i]);
		}
		return bookingDTOs;
	}
	
	public static BookingDTO convert(BookingDO booking) {
		return new BookingDTO(booking.getBookingId(), convert(booking.getJourney()), booking.getUsername(), booking.getDate());		
	}	
}
