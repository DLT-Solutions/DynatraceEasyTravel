package com.dynatrace.easytravel.cassandra.entities;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.LocationSearchTable;
import com.dynatrace.easytravel.jpa.business.Location;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
//TODO set read and write policy (everywhere)
@Table(name = LocationSearchTable.LOCATION_SEARCH_TABLE)
public class LocationSearchEntity implements CassandraEntity<Location>{
    private  String name;

    private String searchIdx;

    public LocationSearchEntity(){}

    public LocationSearchEntity(Location location) {
        this.name = location.getName();
        this.searchIdx = location.getName();
    }

    public LocationSearchEntity(String name, String searchIdx) {
        this.name = name;
        this.searchIdx = searchIdx;
    }

    @PartitionKey(1)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @PartitionKey(0)
    public String getSearchIdx() {
        return searchIdx;
    }

    public void setSearchIdx(String searchIdx) {
        this.searchIdx = searchIdx;
    }

    @Override
    public Location createModel() {
        Location location = new Location();
        location.setName(name);
        return location;
    }
}
