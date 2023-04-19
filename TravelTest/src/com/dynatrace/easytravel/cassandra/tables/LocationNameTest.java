package com.dynatrace.easytravel.cassandra.tables;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.cassandra.tables.objectmapper.LocationNameTable;
import com.dynatrace.easytravel.jpa.business.Location;

@Ignore
public class LocationNameTest extends CassandraTableTestUtil {
    LocationNameTable locationNameTable = new LocationNameTable(getCluster());

    public LocationNameTest() {
        setCassandraObjects(locationNameTable);
    }

    @Test
    public void test() {
        assertThat(locationNameTable.getCount(), is(0));
        locationNameTable.addModel(new Location("new location"));
        assertThat(locationNameTable.getCount(), is(1));
        assertThat(locationNameTable.getAllModels().size(), is(1));

        locationNameTable.addModel(new Location("New York"));
        assertThat(locationNameTable.getCount(), is(2));
        assertThat(locationNameTable.getAllModels().size(), is(2));
    }

    @Test
    public void testGetLocationsFromTo() {
        assertThat(locationNameTable.getCount(), is(0));
        for(char c ='z'; c>='a'; c--) {
            locationNameTable.addModel(new Location("location_" + c));
        }
        assertThat(locationNameTable.getCount(), is('z'-'a'+1));

        Collection<Location> locations = locationNameTable.getLocations(0, 1);
        assertThat(locations.size(), is(1));
        assertThat(locations.iterator().next().getName(), is("location_a"));

        int cidx = 'c' - 'a';
        locations = locationNameTable.getLocations(cidx, 10);
        assertThat(locations.size(), is(10));

        Iterator<Location> iterator = locations.iterator();
        for(char c = 'c'; c<= cidx+10; c++){
            assertThat(iterator.next().getName(), is("location_" + c));
        }
    }
}
