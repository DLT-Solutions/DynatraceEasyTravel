package com.dynatrace.easytravel.cassandra.entities;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.LocationNameTable;
import com.dynatrace.easytravel.jpa.business.Location;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
//TODO set read and write policy (everywhere)
@Table(name = LocationNameTable.LOCATION_NAME_TABLE)
public class LocationNameEntity implements CassandraEntity<Location>{
    private  String name;

    public LocationNameEntity(){}

    public LocationNameEntity(Location location) {
        this.name = location.getName();
    }

    public LocationNameEntity(String name, String searchIdx) {
        this.name = name;
    }

    @PartitionKey
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Location createModel() {
        Location location = new Location();
        location.setName(name);
        return location;
    }
}
