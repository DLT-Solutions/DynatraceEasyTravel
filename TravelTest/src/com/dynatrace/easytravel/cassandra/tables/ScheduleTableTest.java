package com.dynatrace.easytravel.cassandra.tables;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.cassandra.tables.objectmapper.ScheduleTable;
import com.dynatrace.easytravel.jpa.business.Schedule;

@Ignore
public class ScheduleTableTest extends CassandraTableTestUtil{

    ScheduleTable scheduleTable = new ScheduleTable(getCluster());

    public ScheduleTableTest() {
        setCassandraObjects(scheduleTable);
    }

    @Test
    public void testGetByName() {
        assertThat(scheduleTable.getCount(), is(0));

        Schedule schedule1 = new Schedule("schedule1", 10);
        Schedule schedule2 = new Schedule("schedule2", 20);

        scheduleTable.addModel(schedule1);
        scheduleTable.addModel(schedule2);

        assertThat(scheduleTable.getCount(), is(2));

        //TODO
        assertThat(scheduleTable.getByName("schedule1").getName(), is(schedule1.getName()));
        assertThat(scheduleTable.getByName("schedule1").getLastExecution(), is(schedule1.getLastExecution()));
        assertThat(scheduleTable.getByName("schedule1").getPeriod(), is(schedule1.getPeriod()));
        assertThat(scheduleTable.getByName("schedule2").getName(), is(schedule2.getName()));
        assertThat(scheduleTable.getByName("schedule2").getLastExecution(), is(schedule2.getLastExecution()));
        assertThat(scheduleTable.getByName("schedule2").getPeriod(), is(schedule2.getPeriod()));
    }
}
