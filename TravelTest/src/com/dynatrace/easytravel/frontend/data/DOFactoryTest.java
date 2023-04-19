package com.dynatrace.easytravel.frontend.data;

import static com.dynatrace.easytravel.MiscConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.dynatrace.easytravel.jpa.business.xsd.Journey;
import com.dynatrace.easytravel.jpa.business.xsd.Location;
import com.dynatrace.easytravel.jpa.business.xsd.Tenant;
import com.dynatrace.easytravel.jpa.business.xsd.User;
import com.dynatrace.easytravel.spring.SpringTestBase;


public class DOFactoryTest extends SpringTestBase {




    @Test
    public void testGetWrappers() {
        Journey journey1 = Journey.Factory.newInstance();
        Journey journey2 = Journey.Factory.newInstance();
        Location location1 = Location.Factory.newInstance();
        Location location2 = Location.Factory.newInstance();
        Tenant tenant1 = Tenant.Factory.newInstance();
        Tenant tenant2 = Tenant.Factory.newInstance();

        location1.setName(LOCATION_NAME1);
        location2.setName(LOCATION_NAME2);

        tenant1.setName(TENANT_NAME1);
        tenant2.setName(TENANT_NAME2);

        journey1.setId(JOURNEY_ID1);
        journey1.setStart(location1);
        journey1.setDestination(location2);
        journey1.setAmount(AMOUNT1);
        journey1.setFromDate(FROM_DATE1);
        journey1.setToDate(TO_DATE1);
        journey1.setTenant(tenant1);
        journey1.setPicture(PICTURE1);

        journey2.setId(JOURNEY_ID2);
        journey2.setStart(location2);
        journey2.setDestination(location1);
        journey2.setAmount(AMOUNT2);
        journey2.setFromDate(FROM_DATE2);
        journey2.setToDate(TO_DATE2);
        journey2.setTenant(tenant2);
        journey2.setPicture(PICTURE2);

        JourneyDO[] journeys = DOFactory.getWrappers(new Journey[] {journey1, journey2});

        JourneyDO j1 = journeys[0];
        assertEquals(JOURNEY_ID1, j1.getId());
        assertEquals(LOCATION_NAME1, j1.getStart());
        assertEquals(LOCATION_NAME2, j1.getDestination());
        assertEquals(AMOUNT1, j1.getAmount(), DELTA);
//        assertEquals(FROM_DATE1, j1.getFromDate());
//        assertEquals(TO_DATE1, j1.getToDate());
        assertEquals(TENANT_NAME1, j1.getTenant());
        assertTrue(j1.isHasPicture());
        assertTrue(Arrays.equals(PICTURE1, j1.getPicture()));

        JourneyDO j2 = journeys[1];
        assertEquals(JOURNEY_ID2, j2.getId());
        assertEquals(LOCATION_NAME2, j2.getStart());
        assertEquals(LOCATION_NAME1, j2.getDestination());
        assertEquals(AMOUNT2, j2.getAmount(), DELTA);
//        assertEquals(FROM_DATE2, j2.getFromDate());
//        assertEquals(TO_DATE2, j2.getToDate());
        assertEquals(TENANT_NAME2, j2.getTenant());
        assertTrue(j2.isHasPicture());
        assertTrue(Arrays.equals(PICTURE2, j2.getPicture()));

        LocationDO[] locations = DOFactory.getWrappers(new Location[] {location1, location2});
        assertEquals(LOCATION_NAME1, locations[0].getName());
        assertEquals(LOCATION_NAME2, locations[1].getName());

        User user1 = User.Factory.newInstance();
        user1.setName(USER_NAME1);
        user1.setPassword(USER_PASSWORD1);

        User user2 = User.Factory.newInstance();
        user2.setName(USER_NAME2);
        user2.setPassword(USER_PASSWORD2);

        UserDO[] users = DOFactory.getWrappers(new User[] {user1, user2});
        assertEquals(USER_NAME1, users[0].getName());
        assertEquals(USER_PASSWORD1, users[0].getPassword());
        assertEquals(USER_NAME2, users[1].getName());
        assertEquals(USER_PASSWORD2, users[1].getPassword());
    }

}