/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HbaseLocationColumnProvider.java
 * @date: 25.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.columnfamily;

import java.util.Collection;

import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;

import com.dynatrace.easytravel.hbase.HbaseDataController;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.persistence.provider.LocationProvider;


/**
 *
 * @author stefan.moschinski
 */
public class HbaseLocationColumnFamily extends HbaseColumnFamily<Location> implements LocationProvider {

	public static final String LOCATION_COLUMN_FAMILY_NAME = "LocationColumnFamily";


	public HbaseLocationColumnFamily(HbaseDataController controller) {
		super(controller, LOCATION_COLUMN_FAMILY_NAME, "loc");
	}

	@Override
	public boolean deleteLocation(String name) {
		if (getLocationByName(name) == null) {
			return false; // emulate missing boolean operation
		}

		return deleteByRowKey(name.toLowerCase());
	}

	@Override
	public Collection<Location> getLocations(int fromIdx, int count) {
		// ignores actual start
		return getWithLimit(count);
	}

	@Override
	public Location getLocationByName(String locationName) {
		return getByKey(locationName.toLowerCase());
	}

	@Override
	public Collection<Location> getMatchingLocations(String locationNamePart) {
		return getFiltered(new RowFilter(CompareFilter.CompareOp.EQUAL,
				new SubstringComparator(locationNamePart.toLowerCase())));
	}

	@Override
	public void verifyLocation(int sleepTime) {
	}

}
