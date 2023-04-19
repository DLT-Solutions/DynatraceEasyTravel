package com.dynatrace.easytravel.cassandra.entities;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.BookingTable;
import com.dynatrace.easytravel.jpa.business.*;

import java.util.Date;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
@Table(name = BookingTable.BOOKING_TABLE_NAME)
public class BookingEntity implements CassandraEntity<Booking> {
    private String id;
    private String journeyName;
    private int journeyId;
    private String depLocation;
    private String destLocation;
    private double amount;
    private String tenant;
    private String userName;
    private Date bookingDate;
    //TODO chekc if we should add more fields from the Journey/Tenant/User

    public BookingEntity() {}

    public BookingEntity(Booking booking) {
        this.id = booking.getId();
        this.journeyId = booking.getJourney().getId();
        this.journeyName = booking.getJourney().getName();
        this.depLocation = booking.getJourney().getStart().getName();
        this.destLocation = booking.getJourney().getDestination().getName();
        this.amount = booking.getJourney().getAmount();
        this.tenant = booking.getJourney().getTenant().getName();
        this.userName = booking.getUser().getName();
        this.bookingDate = booking.getBookingDate();
    }

    @Override
    public Booking createModel() {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setJourney(createJourney());
        booking.setUser(new User(userName));
        booking.setBookingDate(bookingDate);
        return booking;
    }

    private Journey createJourney() {
        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setName(journeyName);
        journey.setStart( new Location(depLocation));
        journey.setDestination( new Location(destLocation));
        journey.setAmount(amount);
        journey.setTenant(new Tenant(tenant, null, null));
        return journey;
    }

    @PartitionKey(0)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJourneyName() {
        return journeyName;
    }

    public void setJourneyName(String journeyName) {
        this.journeyName = journeyName;
    }

    public int getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(int journeyId) {
        this.journeyId = journeyId;
    }

    public String getDepLocation() {
        return depLocation;
    }

    public void setDepLocation(String depLocation) {
        this.depLocation = depLocation;
    }

    public String getDestLocation() {
        return destLocation;
    }

    public void setDestLocation(String destLocation) {
        this.destLocation = destLocation;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }
}
