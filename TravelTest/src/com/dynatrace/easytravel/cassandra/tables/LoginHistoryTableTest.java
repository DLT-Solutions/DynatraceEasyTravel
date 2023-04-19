package com.dynatrace.easytravel.cassandra.tables;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.cassandra.tables.cql.CountersTable;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.LoginHistoryTable;
import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.jpa.business.User;

@Ignore
public class LoginHistoryTableTest extends CassandraTableTestUtil {

    CountersTable countersTable = new CountersTable(getCluster());
    LoginHistoryTable loginHistoryTable = new LoginHistoryTable(getCluster(), countersTable);

    User user1 = new User("user1", "name1", "email1", "pw1");
    User user2 = new User("user2", "name2", "email2", "pw2");
    User user3 = new User("user3", "name3", "email3", "pw3");
    User notAdded = new User("user4", "name4", "email4", "pw4");

    public LoginHistoryTableTest()  {
        setCassandraObjects(loginHistoryTable, countersTable);
    }

    @Before
    public void setup() {
        assertThat(loginHistoryTable.getCount(), is(0));

        LoginHistory lh = new LoginHistory(user1, new Date());
        lh.setId(1);
        loginHistoryTable.addModel(lh);
        lh = new LoginHistory(user1, DateUtils.addDays(new Date(), 1));
        lh.setId(2);
        loginHistoryTable.addModel(lh);
        lh = new LoginHistory(user2, DateUtils.addDays(new Date(), 1));
        lh.setId(3);
        loginHistoryTable.addModel(lh);
        lh = new LoginHistory(user3, DateUtils.addDays(new Date(), 1));
        lh.setId(4);
        loginHistoryTable.addModel(lh);

        assertThat(loginHistoryTable.getCount(), is(4));
    }

    @Test
    public void getLoginCountForUserTest(){
        assertThat(loginHistoryTable.getLoginCountForUser("user1"), is(2));
        assertThat(loginHistoryTable.getLoginCountForUser("user2"), is(1));
        assertThat(loginHistoryTable.getLoginCountForUser("not-existing"), is(0));
    }

    @Test
    public void removeLoginHistoryByIdTest() {
        assertThat(loginHistoryTable.getModel(2), notNullValue() );
        loginHistoryTable.removeLoginHistoryById(2);
        assertThat(loginHistoryTable.getCount(), is(3));
        assertThat(loginHistoryTable.getModel(2), nullValue());
    }

    @Test
    public void getLoginCountExcludingUserTest() {
        assertThat(loginHistoryTable.getLoginCountExcludingUser(user1), is(2));
        assertThat(loginHistoryTable.getLoginCountExcludingUser(user2), is(3));
        assertThat(loginHistoryTable.getLoginCountExcludingUser(user3), is(3));
        assertThat(loginHistoryTable.getLoginCountExcludingUser(notAdded), is(4));
    }

    @Test
    public void getLoginIdsExcludingUserTest() {
        assertThat(loginHistoryTable.getLoginIdsExcludingUser(user1, 100), containsInAnyOrder(3,4));
        assertThat(loginHistoryTable.getLoginIdsExcludingUser(user2, 100), containsInAnyOrder(1,2,4));
        assertThat(loginHistoryTable.getLoginIdsExcludingUser(user3, 100), containsInAnyOrder(1,2,3));
        assertThat(loginHistoryTable.getLoginIdsExcludingUser(notAdded, 100), containsInAnyOrder(1,2,3,4));
    }

    @Test
    public void getLoginHistoryByIdTest() {
        assertThat(loginHistoryTable.getModel(1), notNullValue());
        assertThat(loginHistoryTable.getModel(2), notNullValue());
        assertThat(loginHistoryTable.getModel(3), notNullValue());
        assertThat(loginHistoryTable.getModel(4), notNullValue());
        assertThat(loginHistoryTable.getModel(5), nullValue());
    }
}
