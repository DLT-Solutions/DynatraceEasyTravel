package com.dynatrace.easytravel.cassandra.base;

import com.dynatrace.easytravel.cassandra.tables.cql.CountersTable;

public enum BookingLocationType {
    DEPARTURE_LOCATION("depLocation"),
    DESTINATION_LOCATION("destLocation");

    private final String columnName;

    BookingLocationType(String columnName) {
        this.columnName = columnName;
    }

    public int getBookingsForLocations(CountersTable counters, String tenant, String locationName) {
        switch (this) {
            case DEPARTURE_LOCATION: return counters.getDepatureCountForTenant(tenant, locationName);
            case DESTINATION_LOCATION: return counters.getDestinationCountForTenant(tenant, locationName);
            default: return 0;
        }
    }

    public String getLocationColumn() {
        return columnName;
    }
}