package com.dynatrace.easytravel.cassandra.tables.objectmapper;

import com.datastax.driver.core.ResultSet;
import com.dynatrace.easytravel.cassandra.base.CassandraModelTable;
import com.dynatrace.easytravel.cassandra.base.EtCluster;
import com.dynatrace.easytravel.cassandra.entities.LoginHistoryEntity;
import com.dynatrace.easytravel.cassandra.tables.cql.CountersTable;
import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.util.TextUtils;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
public class LoginHistoryTable extends CassandraModelTable<LoginHistoryEntity, LoginHistory> {
    public static final String LOGINHISTORY_TABLE_NAME = "LoginHistoryTable";
    private final static String LOGIN_COUNTER = "loginCnt";
    private final static String TOTAL_USER_LOGINS = "totalUserLogins";

    private CountersTable countersTable;

    public LoginHistoryTable(EtCluster cluster, CountersTable countersTable) {
        super(cluster, LoginHistoryEntity.class);
        this.countersTable = countersTable;
    }

    @Override
    protected LoginHistoryEntity getEntity(LoginHistory model) {
        return new LoginHistoryEntity(model);
    }

    @Override
    public void create() {
        executeQuery(TextUtils.merge("CREATE TABLE IF NOT EXISTS {0} (id int , userName text , loginDate timestamp, PRIMARY KEY (id, userName));", LOGINHISTORY_TABLE_NAME));
    }

    public int getLoginCountForUser(String userName){
        return countersTable.getCount(userName, LOGIN_COUNTER);
    }

    public void removeLoginHistoryById(Integer id) {
        LoginHistory model = getModel(id);
        if(model != null) {
            deleteModel(model);
        }
    }

    public Collection<Integer> getLoginIdsExcludingUser(User userToExclude, int maxResults){
        final String toExcludeName = userToExclude.getName();
        int resultCount = maxResults + countersTable.getCount(
                toExcludeName, LOGIN_COUNTER); // add the number of rows that are excluded

        String cql = TextUtils.merge("SELECT id, userName FROM {0} LIMIT {1};", LOGINHISTORY_TABLE_NAME, resultCount);
        ResultSet rows = executeQuery(cql);

        List<Integer> result = rows.all()
                .stream()
                .filter(row -> !userToExclude.getName().equals(row.getString("userName")))
                .limit(resultCount)
                .map(row -> row.getInt("id"))
                .collect(toList());
        return result;
    }

    @Override
    public void addModel(LoginHistory model) {
        super.addModel(model);
        countersTable.incrementByOne(model.getUser().getName(), LOGIN_COUNTER);
        countersTable.incrementByOne(TOTAL_USER_LOGINS, LOGIN_COUNTER);
    }

    @Override
    public void deleteModel(LoginHistory model) {
        String name = model.getUser().getName();
        countersTable.decrementByOne(name, LOGIN_COUNTER);
        countersTable.decrementByOne(TOTAL_USER_LOGINS, LOGIN_COUNTER);
        super.deleteModel(model);
    }

    public int getLoginCountExcludingUser(User userToExclude) {
        return (countersTable.getCount(TOTAL_USER_LOGINS, LOGIN_COUNTER) - countersTable.getCount(
                userToExclude.getName(), LOGIN_COUNTER));
    }
}
