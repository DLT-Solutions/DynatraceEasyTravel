package com.dynatrace.easytravel.cassandra.tables;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.cassandra.tables.cql.CountersTable;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.UserTable;
import com.dynatrace.easytravel.jpa.business.User;
import com.google.common.collect.Maps;

@Ignore
public class UserTableTest extends CassandraTableTestUtil {

    CountersTable countersTable = new CountersTable(getCluster());
    UserTable userTable = new UserTable(getCluster(),countersTable);

    public UserTableTest() {
        setCassandraObjects(countersTable, userTable);
    }

    @Test
    public void testGetUserByName() {
        User user = new User("name1", "fullName1", "email1", "pw1");
        userTable.addModel(user);

        User user2 = new User("name2", "fullName2", "email2", "pw1");
        userTable.addModel(user2);

        assertThat(userTable.getCount(), is(2));
        assertThat(userTable.getUserByName("name1"), is(user));
        assertThat(userTable.getUserByName("name2"), is(user2));

        List<User> users = userTable.getAllModels();
        Map<String, User> userMap = Maps.uniqueIndex(users, u -> u.getName());

        assertThat(userMap.get("name1"), is(user));
        assertThat(userMap.get("name2"), is(user2));
    }

    @Test
    public void testUserCount() {
        User user = new User("name1", "fullName1", "email1", "pw1");
        userTable.addModel(user);

        assertThat(countersTable.getLoginCount("name1"), is(0));
        countersTable.incrementLoginCountForUser("name1");
        countersTable.incrementLoginCountForUser("name1");
        countersTable.incrementLoginCountForUser("name1");
        assertThat(countersTable.getLoginCount("name1"), is(3));
    }

    @Test
    public void testLastLoginTime() {
        User user = new User("name1", "fullName1", "email1", "pw1");
        userTable.addModel(user);

        assertThat(userTable.getUserByName("name1").getLastLogin(), is(nullValue()));

        Date loginTime = new Date();

        user.setLastLogin(loginTime);
        userTable.updateModel(user);

        assertThat(userTable.getUserByName("name1").getLastLogin(), is(loginTime));
    }
}