package com.dynatrace.easytravel.cassandra.tables.objectmapper;

import com.dynatrace.easytravel.cassandra.base.CassandraModelTable;
import com.dynatrace.easytravel.cassandra.base.EtCluster;
import com.dynatrace.easytravel.cassandra.entities.ScheduleEntity;
import com.dynatrace.easytravel.jpa.business.Schedule;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
public class ScheduleTable extends CassandraModelTable<ScheduleEntity, Schedule> {
    public static final String SCHEDULE_TABLE_NAME = "ScheduleTable";

    public ScheduleTable(EtCluster cluster) {
        super(cluster, ScheduleEntity.class);
    }

    @Override
    protected ScheduleEntity getEntity(Schedule model) {
        return new ScheduleEntity(model);
    }

    @Override
    public void create() {
        executeQuery("CREATE TABLE IF NOT EXISTS " + SCHEDULE_TABLE_NAME + " (name text PRIMARY KEY, lastExecution timestamp, period bigint);");
    }

    public Schedule getByName(String name) {
        return getModel(name);
    }
}
