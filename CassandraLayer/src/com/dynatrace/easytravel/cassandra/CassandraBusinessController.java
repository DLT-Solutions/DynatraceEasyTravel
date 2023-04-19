/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CassandraBusinessController.java
 * @date: 10.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.cassandra;

import com.dynatrace.easytravel.cassandra.base.EtCluster;
import com.dynatrace.easytravel.cassandra.base.EtKeySpace;
import com.dynatrace.easytravel.cassandra.tables.cql.CountersTable;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.BookingTable;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.JourneyTable;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.LocationNameTable;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.LocationSearchTable;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.LoginHistoryTable;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.ScheduleTable;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.TenantTable;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.UserTable;
import com.dynatrace.easytravel.persistence.MandatoryPersistencePreparation;
import com.dynatrace.easytravel.persistence.controller.AbstractBusinessDatabaseController;
import com.dynatrace.easytravel.persistence.provider.BookingProvider;
import com.dynatrace.easytravel.persistence.provider.JourneyProvider;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;
import com.dynatrace.easytravel.persistence.provider.LoginHistoryProvider;
import com.dynatrace.easytravel.persistence.provider.ScheduleProvider;
import com.dynatrace.easytravel.persistence.provider.TenantProvider;
import com.dynatrace.easytravel.persistence.provider.UserProvider;


/**
 *
 * @author stefan.moschinski
 */
public class CassandraBusinessController extends AbstractBusinessDatabaseController implements MandatoryPersistencePreparation {

	private UserTable userTable;
	private JourneyTable journeyTable;
	private BookingTable bookingTable;
	private LocationNameTable locationNameTable;
	private LocationSearchTable locationSearchTable;
	private LoginHistoryTable loginHistoryTable;
	private ScheduleTable scheduleTable;
	private TenantTable tenantTable;
	
	private CountersTable countersTable;
	
	private final EtKeySpace businessKeyspace;
	private final EtCluster cluster;

	protected CassandraBusinessController(CassandraController controller) {
		super(controller);
		businessKeyspace = controller.getKeyspace();
		if(!businessKeyspace.isPresent()) {
			businessKeyspace.create();
		}
		businessKeyspace.useKeyspace();
		cluster = controller.getEtCluster();
	}

	@Override
	public void createSchema() {
		new CountersTable(cluster).create();
		
		new JourneyTable(cluster).create();;
		new LocationNameTable(cluster).create();
		new LocationSearchTable(cluster).create();
		new LoginHistoryTable(cluster, countersTable).create();
		new ScheduleTable(cluster).create();
		new TenantTable(cluster).create();
		new UserTable(cluster, countersTable).create();
		new BookingTable(cluster, countersTable).create();
	}

	@Override
	protected UserProvider createUserProviderInternal() {
		return new CassandraUserProvider(getUserTable());
	}

	@Override
	protected JourneyProvider createJourneyProviderInternal() {
		return new CassandraJourneyProvider(getJourneyTable(), getLocationSearchTable());
	}

	@Override
	protected BookingProvider createBookingProviderInternal() {
		return new CassandraBookingProvider(getBookingTable());
	}

	@Override
	protected LocationProvider createLocationProviderInternal() {
		return new CassandraLocationProvider(getLocationNameTable(), getLocationSearchTable());
	}

	@Override
	protected LoginHistoryProvider createLoginHistoryProviderInternal() {
		return new CassandraLoginHistoryProvider(getLoginHistoryTable());
	}

	@Override
	protected ScheduleProvider createScheduleProviderInternal() {
		return new CassandraScheduleProvider(getScheduleTable());
	}

	@Override
	protected TenantProvider createTenantProviderInternal() {
		return new CassandraTenantProvider(getTenantTable());
	}
	
	public CountersTable createCountersTable() {
		return new CountersTable(cluster);
	}
	
	private CountersTable getCountersTable() {
		if(countersTable == null) {
			countersTable = new CountersTable(cluster);
		}
		return countersTable;
	}
	
	private UserTable getUserTable() {
		if(userTable == null) {
			userTable = new UserTable(cluster, getCountersTable());
			userTable.init();
		}
		return userTable;
	}
	
	private JourneyTable getJourneyTable() {
		if(journeyTable == null) {
			journeyTable = new JourneyTable(cluster);
			journeyTable.init();
		}
		return journeyTable;
	}
	
	private BookingTable getBookingTable() {
		if(bookingTable == null) {
			bookingTable = new BookingTable(cluster, getCountersTable());
			bookingTable.init();
		}
		return bookingTable;
	}
	
	private LocationSearchTable getLocationSearchTable() {
		if(locationSearchTable == null) {
			locationSearchTable = new LocationSearchTable(cluster);
			locationSearchTable.init();
		}
		return locationSearchTable;
	}
	
	private LocationNameTable getLocationNameTable() {
		if(locationNameTable == null) {
			locationNameTable = new LocationNameTable(cluster);
			locationNameTable.init();
		}
		return locationNameTable;
	}
	
	private LoginHistoryTable getLoginHistoryTable() {
		if(loginHistoryTable == null) {
			loginHistoryTable = new LoginHistoryTable(cluster, getCountersTable());
			loginHistoryTable.init();
		}
		return loginHistoryTable;
	}
	
	private ScheduleTable getScheduleTable() {
		if(scheduleTable == null) {
			scheduleTable = new ScheduleTable(cluster);
			scheduleTable.init();
		}
		return scheduleTable;
	}
	
	private TenantTable getTenantTable() {
		if(tenantTable == null) {
			tenantTable = new TenantTable(cluster);
			tenantTable.init();
		}
		return tenantTable;
	}

}
