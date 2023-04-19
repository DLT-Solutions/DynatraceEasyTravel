package com.dynatrace.easytravel.cassandra.base;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.util.TextUtils;

import java.text.MessageFormat;

public class EtKeySpace extends CassandraObject{

    public static final String KEYSPACE_NAME = "easyTravelBusiness";

    private final String name;

    public EtKeySpace(EtCluster cluster) {
        this(KEYSPACE_NAME, cluster);
    }

    public EtKeySpace(String name, EtCluster cluster) {
        super(cluster);
        this.name = name;
    }

    public void create() {
        if (!isPresent()) {
            String query = MessageFormat.format("CREATE KEYSPACE {0} " + "WITH replication = '{''class'':" +
                            " ''SimpleStrategy'',''replication_factor'': '{1}'};",
                    name, getReplicationFactor());
            executeQuery(query);
        }
    }

    @Override
    public boolean isPresent() {
        return getSession().getCluster().getMetadata().getKeyspace(name) != null;
    }

    @Override
    public void delete() {
        if (isPresent()) {
            executeQuery(TextUtils.merge("DROP KEYSPACE {0};", name), DEFAULT_DROP_TIMEOUT);
        }
    }

    private int getReplicationFactor() {
        return EasyTravelConfig.read().cassandraReplicationFactor;
    }

    public void useKeyspace() {
        executeQuery(TextUtils.merge("use {0};", name));
    }
}
