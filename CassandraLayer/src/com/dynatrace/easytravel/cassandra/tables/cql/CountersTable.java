package com.dynatrace.easytravel.cassandra.tables.cql;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.dynatrace.easytravel.cassandra.base.CassandraCQLTable;
import com.dynatrace.easytravel.cassandra.base.EtCluster;
import com.dynatrace.easytravel.cassandra.base.Operation;
import com.dynatrace.easytravel.util.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
//TODO change it to object mapper
public class CountersTable extends CassandraCQLTable {

    private static final String DEPATURE_PART = "_dp_";
    private static final String DESTINATION_PART = "_dt_";

    private static final String COUNTER_CF_NAME = "Counters";
    private static final String NO_LOGINS = "NoLogins";
    private static final String NO_BOOKINGS_TENANT = "NoBookingsTenant";
    private static final String NO_BOOKINGS_USER = "NoBookingsUser";
    private static final String NO_LOCATIONS = "NoLocations";

    private static final String VALUE_SALES = "ValueSales";

    public CountersTable(EtCluster cluster) {
        super(cluster, "Counters");
    }

    @Override
    public void create() {
        executeQuery("CREATE TABLE IF NOT EXISTS Counters(counter_id text, column1 text, value counter, PRIMARY KEY(counter_id, column1));");
    }

    public int getLoginCount(String userName) {
        return getCount(userName, NO_LOGINS);
    }

    public int getDestinationCountForTenant(String tenantName, String locationName) {
        return getCount(tenantName + DESTINATION_PART + locationName, NO_LOCATIONS);
    }

    public int getDepatureCountForTenant(String tenantName, String depatureName) {
        return getCount(tenantName + DEPATURE_PART + depatureName, NO_LOCATIONS);
    }

    public int getTotalBookingCountUser() {
        return getTotalCount(NO_BOOKINGS_USER);
    }

    public int getBookingCountForUser(String userName) {
        return getCount(userName, NO_BOOKINGS_USER);
    }

    public int getBookingCountForTenant(String tenantName) {
        return getCount(tenantName, NO_BOOKINGS_TENANT);
    }

    public double getSalesAmountForTenant(String tenantName) {
        return BigDecimal.valueOf(getCount(tenantName, VALUE_SALES), 2)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public int getCount(String columnKey, String rowKey) {
        String query = TextUtils.merge("SELECT value FROM {1} WHERE counter_id = $${2}$$ AND column1 = $${0}$$", columnKey, COUNTER_CF_NAME, rowKey);

        ResultSet rs = executeQuery(query);
        if(rs.isExhausted()){
            return 0;
        }

        long cnt = rs.one().getLong("value");
        return Long.valueOf(cnt).intValue();
    }

    private int getTotalCount(String key) {
        String query = TextUtils.merge("SELECT * FROM {0} WHERE counter_id = $${1}$$",
                COUNTER_CF_NAME, key);

        ResultSet rs = executeQuery(query);
        if (rs.isExhausted()) {
            return 0;
        }

        long count = 0;
        for (Row row : rs) {
            count += row.getLong("value");
        }

        return Long.valueOf(count).intValue();
    }

    public void incrementLoginCountForUser(String userName) {
        incrementCounter(userName, 1, NO_LOGINS);
    }

    public void incrementBookingCountForUser(String userName) {
        incrementCounter(userName, 1, NO_BOOKINGS_USER);
    }

    public void incrementBookingCountForTenant(String tenantName) {
        incrementCounter(tenantName, 1, NO_BOOKINGS_TENANT);
    }

    public void decrementLoginCountForUser(String userName) {
        decrementCounter(userName, 1, NO_LOGINS);
    }

    public void decrementBookingCountForUser(String userName) {
        decrementCounter(userName, 1, NO_BOOKINGS_USER);
    }

    public void decrementBookingCountForTenant(String tenantName) {
        decrementCounter(tenantName, 1, NO_BOOKINGS_TENANT);
    }

    public void incrementDestinationCountForTenant(String tenantName, String destinationName) {
        incrementCounter(tenantName + DESTINATION_PART + destinationName, 1, NO_LOCATIONS);
    }

    public void incrementDepatureCountForTenant(String tenantName, String depatureName) {
        incrementCounter(tenantName + DEPATURE_PART + depatureName, 1, NO_LOCATIONS);
    }

    public void decrementDestinationCountForTenant(String tenantName, String destinationName) {
        decrementCounter(tenantName + DESTINATION_PART + destinationName, 1, NO_LOCATIONS);
    }

    public void decrementDepatureCountForTenant(String tenantName, String depatureName) {
        decrementCounter(tenantName + DEPATURE_PART + depatureName, 1, NO_LOCATIONS);
    }

    public void incrementSalesForTenant(String tenantName, double amount) {
        incrementCounter(tenantName, decimalToInt(amount), VALUE_SALES);
    }

    public void decrementSalesForTenant(String tenantName, double amount) {
        changeValue(tenantName, decimalToInt(amount), Operation.MINUS, VALUE_SALES);
    }


    private int decimalToInt(double amount) {
        return BigDecimal.valueOf(amount).movePointRight(2).intValue();
    }

    public void incrementByOne(String columnKey, String rowKey) {
        incrementCounter(columnKey, 1, rowKey);
    }

    public void decrementByOne(String columnKey, String rowKey) {
        decrementCounter(columnKey, 1, rowKey);
    }


    private void incrementCounter(String userName, int increment, String key) {
        changeValue(userName, increment, Operation.PLUS, key);
    }

    private void decrementCounter(String name, int increment, String key) {
        changeValue(name, increment, Operation.MINUS, key);
    }

    private void changeValue(String userName, int value, Operation op, String key) {
        String user = replaceSingleQuote(userName);
        String expression = op.getExpression("value", String.valueOf(value));
        String query = TextUtils.merge("UPDATE {0} SET value = {1} WHERE counter_id = $${2}$$ AND column1 = $${3}$$",
                COUNTER_CF_NAME, expression, key, user);
        executeQuery(query);
    }

    /**
     * Change single quote with two single guotes for proper updating names containing single quote for instance 'S-Hertogenbosch
     * @param userName
     * @return
     */
    private String replaceSingleQuote(String userName) {
        return userName.contains("'") ? userName.replace("'", "''") : userName; //TODO change it
    }

}
