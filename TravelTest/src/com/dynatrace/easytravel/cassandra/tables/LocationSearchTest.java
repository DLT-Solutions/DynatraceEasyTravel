package com.dynatrace.easytravel.cassandra.tables;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.cassandra.tables.objectmapper.LocationSearchTable;
import com.dynatrace.easytravel.jpa.business.Location;

@Ignore
public class LocationSearchTest extends CassandraTableTestUtil{

    LocationSearchTable locationSearchTable = new LocationSearchTable(getCluster());

    public LocationSearchTest() {
        setCassandraObjects(locationSearchTable);
    }

    @Test
    public void test() {
        assertThat(locationSearchTable.getCount(), is(0));
        Location location1 = new Location();
        location1.setName("new location");
        locationSearchTable.addModel(location1);
        int expectedCount = location1.getName().length() - LocationSearchTable.MIN_CHARS_TO_FIND_LOCATION + 1;
        assertThat(locationSearchTable.getCount(), is(expectedCount));
        List<Location> locations = locationSearchTable.getAllModels();
        assertThat(locations.size(), is(expectedCount));

        Location location2 = new Location();
        location2.setName("New York");
        locationSearchTable.addModel(location2);

        Collection<Location> locationCollection = locationSearchTable.getMatchingLocations("new");
        System.out.println(locationCollection);
        assertThat(locationCollection.contains(location1), is(true));
        assertThat(locationCollection.contains(location2), is(true));
        assertThat(locationCollection.size(), is(2));
    }
}
