package com.dynatrace.easytravel.cassandra.tables.objectmapper;

import com.dynatrace.easytravel.cassandra.base.CassandraModelTable;
import com.dynatrace.easytravel.cassandra.base.EtCluster;
import com.dynatrace.easytravel.cassandra.entities.LocationSearchEntity;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.util.TextUtils;

import java.util.Collection;
import java.util.List;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
public class LocationSearchTable extends CassandraModelTable<LocationSearchEntity, Location> {
    public static final int MIN_CHARS_TO_FIND_LOCATION = 2;
    public static final String LOCATION_SEARCH_TABLE = "LocationSearch";

    public LocationSearchTable(EtCluster cluster) {
        super(cluster, LocationSearchEntity.class);
    }

    @Override
    protected LocationSearchEntity getEntity(Location location) {
        return new LocationSearchEntity(location);
    }

    @Override
    public void create() {
        executeQuery("CREATE TABLE IF NOT EXISTS " + LOCATION_SEARCH_TABLE + " (searchidx text, name text, PRIMARY KEY(searchidx, name));");
    }

    @Override
    public void addModel(Location model) {
        StringBuilder builder = new StringBuilder(model.getName().toLowerCase());

        do {
            LocationSearchEntity entity = new LocationSearchEntity(model.getName(), builder.toString());
            addEntity(entity);
            builder.deleteCharAt(builder.length() - 1);
        } while (builder.length() >= MIN_CHARS_TO_FIND_LOCATION);
    }
    
    public Collection<Location> getMatchingLocations(String locationNamePart) {
        String cql = TextUtils.merge("SELECT * FROM " + LOCATION_SEARCH_TABLE + " WHERE searchIdx = $${0}$$;", locationNamePart.toLowerCase());
        List<LocationSearchEntity> entitities = runQueryAndMapResults(cql);
        return entitiesToModels(entitities);
    }
}
