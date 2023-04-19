/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: HBaseBusinessController.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase;

import com.dynatrace.easytravel.hbase.columnfamily.HbaseBookingColumnFamily;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseColumnFamily;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseCounterColumnFamily;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseJourneyColumnFamily;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseLocationColumnFamily;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseLoginHistoryColumnFamily;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseTenantColumnFamily;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseUserColumnFamily;
import com.dynatrace.easytravel.persistence.MandatoryPersistencePreparation;
import com.dynatrace.easytravel.persistence.controller.AbstractBusinessDatabaseController;
import com.dynatrace.easytravel.persistence.provider.BookingProvider;
import com.dynatrace.easytravel.persistence.provider.JourneyProvider;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;
import com.dynatrace.easytravel.persistence.provider.LoginHistoryProvider;
import com.dynatrace.easytravel.persistence.provider.ScheduleProvider;
import com.dynatrace.easytravel.persistence.provider.TenantProvider;
import com.dynatrace.easytravel.persistence.provider.UserProvider;
import com.dynatrace.easytravel.persistence.provider.util.NullScheduleProvider;


/**
 * 
 * @author stefan.moschinski
 */
public class HbaseBusinessController extends AbstractBusinessDatabaseController implements
		MandatoryPersistencePreparation {

	private HbaseDataController controller;
	private HbaseCounterColumnFamily counterColumnFamily;

	/**
	 * 
	 * @param controller
	 * @author stefan.moschinski
	 */
	protected HbaseBusinessController(HbaseDataController controller) {
		super(controller);
		this.controller = controller;
	}

	@Override
	protected UserProvider createUserProviderInternal() {
		return new HbaseUserColumnFamily(controller);
	}

	@Override
	protected JourneyProvider createJourneyProviderInternal() {
		return new HbaseJourneyColumnFamily(controller);
	}

	@Override
	protected TenantProvider createTenantProviderInternal() {
		return new HbaseTenantColumnFamily(controller);
	}

	@Override
	protected BookingProvider createBookingProviderInternal() {
		return new HbaseBookingColumnFamily(controller, getHbaseCounterColumnFamily());
	}

	@Override
	protected LocationProvider createLocationProviderInternal() {
		return new HbaseLocationColumnFamily(controller);
	}

	@Override
	protected LoginHistoryProvider createLoginHistoryProviderInternal() {
		return new HbaseLoginHistoryColumnFamily(controller, getHbaseCounterColumnFamily());
	}

	@Override
	protected ScheduleProvider createScheduleProviderInternal() {
		return new NullScheduleProvider();
	}


	protected synchronized HbaseCounterColumnFamily getHbaseCounterColumnFamily() {
		if (counterColumnFamily == null) {
			counterColumnFamily = new HbaseCounterColumnFamily(controller);
		}

		return counterColumnFamily;
	}

	@Override
	public void createSchema() {
		getHbaseCounterColumnFamily().createColumnFamily();

		((HbaseColumnFamily<?>) getBookingProvider()).createColumnFamily();
		((HbaseColumnFamily<?>) getUserProvider()).createColumnFamily();
		((HbaseColumnFamily<?>) getLocationProvider()).createColumnFamily();
		((HbaseColumnFamily<?>) getJourneyProvider()).createColumnFamily();
		((HbaseColumnFamily<?>) getTenantProvider()).createColumnFamily();
		((HbaseColumnFamily<?>) getLoginHistoryProvider()).createColumnFamily();
	}


}
