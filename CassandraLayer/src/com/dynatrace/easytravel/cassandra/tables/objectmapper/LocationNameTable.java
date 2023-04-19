package com.dynatrace.easytravel.cassandra.tables.objectmapper;

import com.dynatrace.easytravel.cassandra.base.CassandraModelTable;
import com.dynatrace.easytravel.cassandra.base.EtCluster;
import com.dynatrace.easytravel.cassandra.entities.LocationNameEntity;
import com.dynatrace.easytravel.jpa.business.Location;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
public class LocationNameTable extends CassandraModelTable<LocationNameEntity, Location> {
    public static final String LOCATION_NAME_TABLE = "LocationNameTable";

    public LocationNameTable(EtCluster cluster) {
        super(cluster, LocationNameEntity.class);
    }

    @Override
    protected LocationNameEntity getEntity(Location model) {
        return new LocationNameEntity(model);
    }

    @Override
    public void create() {
        executeQuery("CREATE TABLE IF NOT EXISTS " + LOCATION_NAME_TABLE + "(name text PRIMARY KEY);");
    }

    public Collection<Location> getLocations(int fromIdx, int count) {
        List<Location> allLocations = getAllModels();
        allLocations.sort(new Comparator<Location>() {
            @Override
            public int compare(Location o1, Location o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
        return allLocations.subList(fromIdx, Math.min(allLocations.size(), fromIdx + count));
    }
    
    public Location getLocationByName(String name) {
    	return getModel(name);
    }
    
    public boolean deleteLocationByName(String name) {
        Location booking = getModel(name);
        if( booking != null) {
            deleteModel(booking);
            return true;
        }
        return false;
    }
}
