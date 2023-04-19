package com.dynatrace.easytravel.cassandra.tables.objectmapper;

import com.dynatrace.easytravel.cassandra.base.CassandraModelTable;
import com.dynatrace.easytravel.cassandra.base.EtCluster;
import com.dynatrace.easytravel.cassandra.entities.UserEntity;
import com.dynatrace.easytravel.cassandra.tables.cql.CountersTable;
import com.dynatrace.easytravel.jpa.business.User;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
public class UserTable extends CassandraModelTable<UserEntity, User> {
    public static final String USER_TABLE = "UserTable";
    //TODO remove from here and create a new class UserProvider
    private final CountersTable loginCounterTable;

    public UserTable(EtCluster cluster, CountersTable countersTable) {
        super(cluster, UserEntity.class);
        loginCounterTable = countersTable;
    }

    @Override
    public void create() {
        executeQuery("CREATE TABLE IF NOT EXISTS " + USER_TABLE + " (name text PRIMARY KEY, password text, email text, loyaltyStatus text, fullName text, lastLogin timestamp);");
    }

    @Override
    protected UserEntity getEntity(User user) {
        return new UserEntity(user);
    }

    public User getUserByName(String name) {
        return getModel(name);
    }
    
    public int getLoginCountForUser(User user) {
        return loginCounterTable.getLoginCount(user.getName());
    }
}
