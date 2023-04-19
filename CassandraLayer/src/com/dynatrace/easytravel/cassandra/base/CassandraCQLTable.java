package com.dynatrace.easytravel.cassandra.base;

public abstract class CassandraCQLTable extends CassandraObject{
    private final String tableName;

    public CassandraCQLTable(EtCluster cluster, String name) {
        super(cluster);
        this.tableName = name;
    }

    @Override
    public boolean isPresent() {
        return isTablePresent(tableName);
    }

    @Override
    public void delete(){
        dropTable(tableName);
    }
}
