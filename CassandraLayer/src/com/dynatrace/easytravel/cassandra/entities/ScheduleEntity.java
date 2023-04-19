package com.dynatrace.easytravel.cassandra.entities;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.ScheduleTable;
import com.dynatrace.easytravel.jpa.business.Schedule;

import java.util.Date;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
@Table(name = ScheduleTable.SCHEDULE_TABLE_NAME)
public class ScheduleEntity implements CassandraEntity<Schedule> {
    private String name;
    private Date lastExecution;
    private long period;

    public ScheduleEntity() {}

    public ScheduleEntity(Schedule schedule) {
        this.name = schedule.getName();
        this.lastExecution = schedule.getLastExecution();
        this.period = schedule.getPeriod();
    }

    @PartitionKey
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(Date lastExecution) {
        this.lastExecution = lastExecution;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    @Override
    public Schedule createModel() {
        Schedule schedule = new Schedule();
        schedule.setName(name);
        schedule.setLastExecution(lastExecution);
        schedule.setPeriod(period);
        return schedule;
    }
}
