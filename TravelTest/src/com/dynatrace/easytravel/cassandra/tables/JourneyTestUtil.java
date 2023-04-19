package com.dynatrace.easytravel.cassandra.tables;

import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.jpa.business.Tenant;

import java.util.*;

public class JourneyTestUtil extends CassandraTableTestUtil {
    protected Journey journey1;
    protected Journey journey2;
    protected Journey journey3;
    protected Date end;
    protected Date begin;

    protected void createJournyes() {
        Calendar calendar = new GregorianCalendar(2017, 11, 17);
        begin = calendar.getTime();
        calendar.set(2017, 11, 31);
        end = calendar.getTime();

        journey1 = getJourney("name", "loc1", "loc2", begin,
                end, "tname");

        journey2 = getJourney("name1", "loc3", "loc2", begin,
                end, "tname1");

        journey3 = getJourney("name2", "loc3", "loc4", begin,
                end, "tname1");
    }

    public List<Journey> createJourneys(int count, String tenant) {
        Calendar calendar = new GregorianCalendar(2017, 11, 17);
        Date begin = calendar.getTime();
        calendar.set(2017, 11, 31);
        Date end = calendar.getTime();

        List<Journey> journeys = new ArrayList<>(count);

        for(int i=0; i<count; i++) {
            String idx =  getNameSuffix(i);
            String name = getJourneyName(tenant, i);
            String depLocation = "depLoc" + idx;
            String destLocation = "destLoc" +idx;
            String tenantName = (tenant == null ? "tenant" + idx : tenant);
            Journey journey = getJourney(name, depLocation, destLocation, begin, end, tenantName);
            journeys.add(journey);
        }
        return journeys;
    }

    protected String getJourneyName(String tenant, int i) {
        return "name" + tenant + "_" + getNameSuffix(i);
    }

    protected String getNameSuffix(int i) {
        return String.format("%03d", i);
    }

    public Journey getJourney(String name, String departure, String destination, Date begin, Date end, String tenantName) {
        Location depLocation = new Location(departure);
        Location destLocation = new Location(destination);
        Tenant tenant = new Tenant(tenantName, null, null);
        Journey journey = new Journey(name, depLocation, destLocation, tenant, begin, end, 56.3, null);
        journey.setId(journey.hashCode());
        return journey;
    }

}