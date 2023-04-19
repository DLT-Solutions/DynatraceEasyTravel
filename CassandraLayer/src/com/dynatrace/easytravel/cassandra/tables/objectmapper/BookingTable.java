package com.dynatrace.easytravel.cassandra.tables.objectmapper;

import com.datastax.driver.core.ResultSet;
import com.dynatrace.easytravel.cassandra.base.BookingLocationType;
import com.dynatrace.easytravel.cassandra.base.CassandraModelTable;
import com.dynatrace.easytravel.cassandra.base.EtCluster;
import com.dynatrace.easytravel.cassandra.entities.BookingEntity;
import com.dynatrace.easytravel.cassandra.tables.cql.CountersTable;
import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.util.TextUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
public class BookingTable extends CassandraModelTable<BookingEntity, Booking> {

    public static final String BOOKING_TABLE_NAME = "BookingTable";
    public static final String BOOKING_BYUSER_VIEW = "Booking_ByUser_View";
    public static final int MAX_USER_BOOKINGS = 50;

    private CountersTable countersTable;

    public BookingTable(EtCluster cluster, CountersTable counters) {
        super(cluster, BookingEntity.class);
        countersTable = counters;
    }

    @Override
    public void create() {
        executeQuery("CREATE TABLE IF NOT EXISTS " + BOOKING_TABLE_NAME + "(" +
                "id text, " +
                "journeyName text, " +
                "journeyId int, " +
                "depLocation text, " +
                "destLocation text, " +
                "amount double, " +
                "tenant text, " +
                "userName text, " +
                "bookingDate timestamp, " +
                " PRIMARY KEY (id, userName, tenant));");

        executeQuery("CREATE MATERIALIZED VIEW IF NOT EXISTS " + BOOKING_BYUSER_VIEW + " AS" +
                " SELECT id, journeyName, journeyId, depLocation, destLocation, amount, tenant, bookingDate " +
                "  FROM " + BOOKING_TABLE_NAME +
                " WHERE userName IS NOT NULL AND id IS NOT NULL AND tenant IS NOT NULL" +
                " PRIMARY KEY (userName, id, tenant)");
    }

    @Override
    protected BookingEntity getEntity(Booking model) {
        return new BookingEntity(model);
    }

    @Override
    public void delete() {
        executeQuery(TextUtils.merge("DROP MATERIALIZED VIEW IF EXISTS {0}", BOOKING_BYUSER_VIEW));
        super.delete();
    }

    @Override
    public void addModel(Booking model) {
        if (model.getId() == null) {
            model.setId( String.valueOf(model.hashCode()));
        }
        super.addModel(model);

        countersTable.incrementBookingCountForUser(model.getUser().getName());
        String tenantName = model.getJourney().getTenant().getName();
        countersTable.incrementSalesForTenant(tenantName, model.getJourney().getAmount());
        countersTable.incrementBookingCountForTenant(tenantName);
        countersTable.incrementDestinationCountForTenant(tenantName, model.getJourney().getDestination().getName());
        countersTable.incrementDepatureCountForTenant(tenantName, model.getJourney().getStart().getName());
    }

    @Override
    public void deleteModel(Booking model) {
        super.deleteModel(model);
        String tenantName = model.getJourney().getTenant().getName();
        countersTable.decrementBookingCountForUser(model.getUser().getName());
        countersTable.decrementSalesForTenant(tenantName, model.getJourney().getAmount());
        countersTable.decrementBookingCountForTenant(tenantName);
        countersTable.decrementDestinationCountForTenant(tenantName, model.getJourney().getDestination().getName());
        countersTable.decrementDepatureCountForTenant(tenantName, model.getJourney().getStart().getName());
    }

    public void removeBookingById(String bookingId) {
        Booking booking = getModel(bookingId);
        if( booking != null) {
            deleteModel(booking);
        }
    }
    
    public Booking getBookingById(String bookingId) {
        return getModel(bookingId);
    }

    public Collection<Booking> getBookingsByUserName(String username) {
        String cql = TextUtils.merge("SELECT * FROM {0} where username = $${1}$$ LIMIT {2}", BOOKING_BYUSER_VIEW, username, MAX_USER_BOOKINGS);
        List<BookingEntity> entities = runQueryAndMapResults(cql);
        return entitiesToModels(entities);
    }

    public double getTotalSalesByTenant(String tenantName) {
        return countersTable.getSalesAmountForTenant(tenantName);
    }

    public int getBookingCountForTenant(String tenantName) {
        return countersTable.getBookingCountForTenant(tenantName);
    }

    public Map<Location, Integer> getDestinationsByTenant(String tenantName, int limit) {
        return getLocationCountBytenant(BookingLocationType.DESTINATION_LOCATION, tenantName, limit);
    }

    private Map<Location, Integer> getLocationCountBytenant(BookingLocationType locationType, String tenantName, int limit ) {
        String cql = TextUtils.merge("SELECT {0} FROM {1} WHERE tenant = $${2}$$ ALLOW FILTERING;", locationType.getLocationColumn(), BOOKING_TABLE_NAME, tenantName);
        ResultSet rows = executeQuery(cql);
        Set<String> locations = rows.all()
                .stream()
                .map(row -> row.getString(locationType.getLocationColumn()))
                .collect(toSet());

        Map<Location, Integer> values = locations.stream().collect(
                toMap(
                        location -> new Location(location),
                        location -> locationType.getBookingsForLocations(countersTable, tenantName, location))
        );

        Map<Location, Integer> result = values
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return result;
    }

    public Map<Location, Integer> getDeparturesByTenant(String tenantName, int limit) {
        return getLocationCountBytenant(BookingLocationType.DEPARTURE_LOCATION, tenantName, limit);
    }

    public int getBookingCountForUser(String userName) {
        return countersTable.getBookingCountForUser(userName);
    }

    public Collection<Booking> getBookingsByTenant(String tenantName, int fromIdx, int count) {
//TODO change this one method call
        List<BookingEntity> entities = getBookingsByTenant(tenantName, "LIMIT " + (fromIdx + count));
        List<BookingEntity> limitedEntities = entities.subList(fromIdx, fromIdx + count);
        return entitiesToModels(limitedEntities);
    }

    public List<Booking> getBookingsByTenant(String tenantName) {
        List<BookingEntity> entities = getBookingsByTenant(tenantName, "");
        return entitiesToModels(entities);
    }

    private List<BookingEntity> getBookingsByTenant(String tenantName, String limitString) {
        String cql = TextUtils.merge("SELECT * FROM {0} WHERE tenant = $${1}$$ {2} ALLOW FILTERING;", BOOKING_TABLE_NAME, tenantName, limitString);
        //TODO change this one method call
        return runQueryAndMapResults(cql);
    }

    public Collection<String> getBookingIdsExcludingUser(String userToExclude, int resultLimit) {
        String cql = TextUtils.merge("SELECT username, id from {0};", BOOKING_BYUSER_VIEW);
        ResultSet rows = executeQuery(cql);
        List<String> result = rows.all()
                .stream()
                .filter(row -> !userToExclude.equals(row.getString("username")))
                .limit(resultLimit)
                .map(row -> row.getString("id"))
                .collect(toList());
        return result;
    }

    public int getBookingCountExcludingUser(String userToExclude) {
        return countersTable.getTotalBookingCountUser() - countersTable.getBookingCountForUser(userToExclude);
    }
}
