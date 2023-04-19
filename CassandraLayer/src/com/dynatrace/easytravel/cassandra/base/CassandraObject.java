package com.dynatrace.easytravel.cassandra.base;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

public abstract class CassandraObject {
	
	private static final Logger LOGGER = LoggerFactory.make();

    public static final int DEFAULT_DROP_TIMEOUT = 65000;

    protected final EtCluster cluster;

    public CassandraObject(EtCluster cluster) {
        this.cluster = cluster;
    }

    public abstract void create();

    public abstract void delete();

    public abstract boolean isPresent();

    public void reset() {
        delete();
        create();
    }

    protected ResultSet executeQuery(String query) {
    	logQuery(query);
        return executeQuery(new SimpleStatement(query));
    }

    protected ResultSet executeQuery(String query, int timeoutInMs) {
    	logQuery(query);
        return executeQuery(new SimpleStatement(query).setReadTimeoutMillis(timeoutInMs));
    }

    protected ResultSet executeQuery(Statement statement) {
        logQuery(statement.toString());
        return getSession().execute(statement);
    }

    protected Session getSession() {
        return cluster.getSession();
    }

    private void logQuery(String query) {
        LOGGER.trace(query);
    }

    protected boolean isTablePresent(String tableName){
        String loggedKeyspace = getSession().getLoggedKeyspace();
        return cluster.getCluster().getMetadata().getKeyspace(loggedKeyspace).getTable(tableName) != null;
    }

    protected void dropTable(String tableName) {
        //TODO ADD IF EXISTS
        if(isPresent()) {
            executeQuery(TextUtils.merge("DROP TABLE {0}", tableName), DEFAULT_DROP_TIMEOUT);
        }
    }
}
