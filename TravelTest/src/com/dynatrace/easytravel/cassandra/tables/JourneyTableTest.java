package com.dynatrace.easytravel.cassandra.tables;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.cassandra.tables.objectmapper.JourneyTable;
import com.dynatrace.easytravel.jpa.business.Journey;

@Ignore
public class JourneyTableTest extends JourneyTestUtil {

    JourneyTable journeyTable = new JourneyTable(getCluster());

    public JourneyTableTest() {
        setCassandraObjects(journeyTable);
    }

    @Before
    public void addJourneys(){
        assertThat(journeyTable.getCount(), equalTo(0));
        createJournyes();
        journeyTable.addModel(journey1);
        journeyTable.addModel(journey2);
        journey3.setId(JourneyTable.JOURNEY_ID_NOT_SET);
        journeyTable.addModel(journey3);
        assertThat(journeyTable.getCount(), is(3));
    }

    @Test
    public void testFindJourneys() {
        Collection<Journey> journeys = journeyTable.findJourneys("loc2", begin, end, false);
        assertThat(journeys.size(), is(2));

        journeys = journeyTable.findJourneys("loc1", begin, end, false);
        assertThat(journeys.size(), is(0));

        Calendar calendar = new GregorianCalendar(2017, 10, 10 );
        journeys = journeyTable.findJourneys("loc2", calendar.getTime(), calendar.getTime(), false);
        assertThat(journeys.size(), is(0));
    }

    @Test
    public void testGetJourneyById() {
        assertThat(journeyTable.getJourneyById(journey1.getId()), equalTo(journey1));
        assertThat(journeyTable.getJourneyByIdNormalize(journey1.getId(),true), equalTo(journey1));
        assertThat(journeyTable.getJourneyByIdNormalize(journey2.getId(),false), equalTo(journey2));
        assertThat(journeyTable.getJourneyByIdNormalize(journey3.hashCode(),false), equalTo(journey3));
    }

    @Test
    public void getJourneyByName() {
        assertThat(journeyTable.getJourneyByName(journey1.getName()), equalTo(journey1));
        assertThat(journeyTable.getJourneyByName(journey2.getName()), equalTo(journey2));
        assertThat(journeyTable.getJourneyByName(journey3.getName()), equalTo(journey3));
    }

    @Test
    public void getJourneyIds() {
    	Collection<Integer> list = journeyTable.getAllJourneyIds();
    	assertThat(list.size(), is(3));
    	assertThat(list, hasItems(
    			journey1.getId(),
    			journey2.getId(),
    			journey3.getId()
    	));
    }

    @Test
    public void getJourneysByTenant() {
        assertThat(journeyTable.getJourneysByTenant("tname1").size(), is(2));
        assertThat(journeyTable.getJourneysByTenant("tname2").size(), is(0));
        assertThat(journeyTable.getJourneysByTenant("tname").size(), is(1));
        Optional<Journey> journey = journeyTable.getJourneysByTenant("tname").stream().findFirst();
        System.out.println(journey);
    }

    @Test
    public void getJourneysByTenantFromTo() {
        journeyTable.deleteModel(journey1);
        journeyTable.deleteModel(journey2);
        journeyTable.deleteModel(journey3);
        assertThat(journeyTable.getCount(), is(0));

        List<Journey> journeys = createJourneys(30, "tenant");
        journeys.stream().forEach( journey -> journeyTable.addModel(journey));
        assertThat(journeyTable.getCount(), is(30));

        int fromIdx = 0;
        Collection<Journey> tenantJourneys = journeyTable.getJourneysByTenant("tenant", fromIdx, 5);
        tenantJourneys.stream().forEach(System.out::println);
        verifyJourneys(tenantJourneys, 0, 5, "tenant");
    }

    private void verifyJourneys(Collection<Journey> journeys, int fromIdx, int count, String tenant) {
        journeys.stream().forEach(System.out::println);
        assertThat(journeys.size(), is(count));
        int id = fromIdx;
        for(Journey journey : journeys) {
            String name = getJourneyName(tenant, id++);
            assertThat(journey.getName(), is(name));
            assertThat(journey.getTenant().getName(), is(tenant));
        }
    }

    @Test
    public void getJourneysCountByTenant() {
        assertThat(journeyTable.getJourneyCountByTenant("tname1"), is(2));
        assertThat(journeyTable.getJourneyCountByTenant("tname2"), is(0));
        assertThat(journeyTable.getJourneyCountByTenant("tname"), is(1));
    }

    @Test
    public void getJouryneyIndexByName() {
        assertThat(journeyTable.getJourneyIndexByName("tname", "name"), is(journey1.hashCode()));
        assertThat(journeyTable.getJourneyIndexByName("tname1", "name2"), is(journey3.hashCode()));
        assertThat(journeyTable.getJourneyIndexByName("tname1", "name3"), is(0));
        assertThat(journeyTable.getJourneyIndexByName("tname1", "name1"), is(journey2.hashCode()));
    }

    @Test
    public void testDeleteJourney() {
        assertThat(journeyTable.getCount(), is(3));
        journeyTable.deleteModel(journey1);
        journeyTable.deleteModel(journey2);
        journeyTable.deleteModel(journey3);
        assertThat(journeyTable.getCount(), is(0));
    }

    @Test
    public void testDeleteTime() {
        long time = System.currentTimeMillis();
        journeyTable.delete();
        System.out.println(System.currentTimeMillis() - time);
    }

    @Test
    public void testGetJourneyIdsByDeparture() {
    	Collection<Integer> list = journeyTable.getJourneyIdsForDeparture("loc1");
    	assertThat(list, hasItems(
    			journey1.getId()
    	));
    	assertThat(list.size(), is(1));

    	list = journeyTable.getJourneyIdsForDeparture("loc3");
    	assertThat(list, hasItems(
    			journey2.getId(),
    			journey3.getId()
    	));
    	assertThat(list.size(), is(2));
    }

    @Test
    public void testGetJourneyIdsByDestination() {
    	Collection<Integer> list = journeyTable.getJourneyIdsForDestination("loc4");
    	assertThat(list, hasItems(
    			journey3.getId()
    	));
    	assertThat(list.size(), is(1));

    	list = journeyTable.getJourneyIdsForDestination("loc2");
    	assertThat(list, hasItems(
    			journey1.getId(),
    			journey2.getId()
    	));
    	assertThat(list.size(), is(2));
    }

}