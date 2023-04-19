package com.dynatrace.easytravel.cassandra.tables;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.dynatrace.easytravel.cassandra.base.EtCluster;

public class CassandraConnection {
    private EtCluster cluster;
    public void connect(String... connectionPoints) {
        cluster = new EtCluster("etCluster", "127.0.0.1");
        cluster.create();
    }

    public EtCluster getCluster() {
        return cluster;
    }

    public Session getSession() {
        return cluster.getSession();
    }

    public MappingManager getMappingManager() {
        return cluster.getManager();
    }

    public void close() {
        cluster.close();
    }
}
