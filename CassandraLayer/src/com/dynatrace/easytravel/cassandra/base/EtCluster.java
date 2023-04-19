package com.dynatrace.easytravel.cassandra.base;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.dynatrace.easytravel.config.EasyTravelConfig;

import org.apache.commons.lang3.Validate;

//TODO create singleton

public class EtCluster {
    private final String name;
    private final String[] addresses;
    private Cluster cluster;
    private Session session;
    private MappingManager manager;

    public EtCluster(String clusterName, String... addresses) {
        this.name = clusterName;
        this.addresses = Validate.notEmpty(addresses, "The Cassandra node addresses must no be empty.");
    }

    public void create() {

        cluster = Cluster.builder()
                .withClusterName(name)
                .addContactPoints(addresses)
                .withQueryOptions( getQueryOptions() )
                .build();
        session = cluster.connect();
        manager = new MappingManager(session);
    }

    private QueryOptions getQueryOptions() {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setConsistencyLevel(ConsistencyLevel.valueOf(EasyTravelConfig.read().cassandraReadConsistencyLevel.toUpperCase()));
        return queryOptions;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Session getSession() {
        return session;
    }

    public MappingManager getManager() {
        return manager;
    }

    public void close() {
        cluster.close();
    }
}
