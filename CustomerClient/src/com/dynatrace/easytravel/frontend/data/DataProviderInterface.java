package com.dynatrace.easytravel.frontend.data;

import java.rmi.RemoteException;
import java.util.Date;

import com.dynatrace.easytravel.misc.UserType;

/**
 * The class serves especially as an interface for mocking.
 * @author stefan.moschinski
 */
public interface DataProviderInterface {

	LocationDO[] findLocations(String destination, int maxResultSize) throws RemoteException;

	JourneyDO[] findJourneys(String destination, Date fromDate, Date toDate) throws RemoteException;

	boolean checkCreditCard(String creditCard) throws RemoteException;

	String storeBooking(Integer journeyId, String userName, UserType userType, String creditCard, Double amount) throws RemoteException;

	JourneyDO getJourneyById(Integer id) throws RemoteException;

	String getDatabaseStatistics() throws RemoteException;

	UserDO[] getUsers() throws RemoteException;

	/**
	 * @author Michal.Bakula
	 */
	UserDO[] getUsersWithPrefix(String pref) throws RemoteException;

	BookingDO getBookingById(String bookingId) throws RemoteException;

	BookingDO[] getRecentBookings(int bookingsLimit) throws RemoteException;
}