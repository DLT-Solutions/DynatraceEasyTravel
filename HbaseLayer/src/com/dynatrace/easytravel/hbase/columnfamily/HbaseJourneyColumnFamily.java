/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HbaseJourneyColumnFamily.java
 * @date: 28.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.columnfamily;

import static com.dynatrace.easytravel.jpa.business.Journey.*;
import static com.dynatrace.easytravel.jpa.business.Location.LOCATION_NAME;
import static com.dynatrace.easytravel.jpa.business.Tenant.TENANT_NAME;
import static java.lang.String.format;
import static org.apache.hadoop.hbase.util.Bytes.toBytes;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.util.Bytes;

import com.dynatrace.easytravel.hbase.HbaseDataController;
import com.dynatrace.easytravel.hbase.serializer.ColumnPrefix;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.persistence.provider.JourneyProvider;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import ch.qos.logback.classic.Logger;


/**
 *
 * @author stefan.moschinski
 */
public class HbaseJourneyColumnFamily extends HbaseColumnFamily<Journey> implements JourneyProvider {

	private static final Logger log = LoggerFactory.make();
	private static final String JOURNEY_COLUMN_FAMILY_PREFIX = "jou";
	public static final String JOURNEY_COLUMN_FAMILY_NAME = "JourneyColumnFamily";

	/**
	 * 
	 * @param controller
	 * @param columnFamilyName
	 * @param namespace
	 * @author stefan.moschinski
	 */
	public HbaseJourneyColumnFamily(HbaseDataController controller) {
		super(controller, JOURNEY_COLUMN_FAMILY_NAME, JOURNEY_COLUMN_FAMILY_PREFIX);
	}



	@Override
	public Journey add(Journey value) {
		if (value.getId() == 0) {
			value.setId(value.getName().hashCode() + 17 * value.getTenant().getName().hashCode());
		}

		return super.add(value);
	}

	@Override
	public Collection<Journey> findJourneys(String destination, Date fromDate, Date toDate, boolean normalize) {
		List<Filter> filters = Lists.newArrayListWithCapacity(3);

		SingleColumnValueFilter destFilter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.createPrefix(JOURNEY_COLUMN_FAMILY_PREFIX, JOURNEY_DESTINATION).getPrefixedColumnName(
						LOCATION_NAME)),
				CompareOp.EQUAL,
				new SubstringComparator(destination));
		destFilter.setFilterIfMissing(true);
		destFilter.setLatestVersionOnly(true);
		filters.add(destFilter);

		SingleColumnValueFilter fromDateFilter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.createPrefix(JOURNEY_COLUMN_FAMILY_PREFIX).getPrefixedColumnName(JOURNEY_FROM_DATE)),
				CompareOp.GREATER_OR_EQUAL,
				new BinaryComparator(toBytes(fromDate.getTime())));
		fromDateFilter.setFilterIfMissing(true);
		fromDateFilter.setLatestVersionOnly(true);
		filters.add(fromDateFilter);

		SingleColumnValueFilter toDateFilter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.createPrefix(JOURNEY_COLUMN_FAMILY_PREFIX).getPrefixedColumnName(JOURNEY_TO_DATE)),
				CompareOp.LESS_OR_EQUAL, // / REALLY?
				new BinaryComparator(toBytes(toDate.getTime())));
		toDateFilter.setFilterIfMissing(true);
		toDateFilter.setLatestVersionOnly(true);
		filters.add(toDateFilter);


		FilterList filterList = new FilterList(
				FilterList.Operator.MUST_PASS_ALL, filters);

		return getFiltered(filterList);
	}

	@Override
	public Journey getJourneyById(Integer id) {
		return getByKey(toBytes(id));
	}

	@Override
	public Collection<Journey> getJourneysByTenant(String tenantName) {
		return getJourneysByTenant(tenantName, 0, HbaseDataController.NO_RESULT_LIMIT);
	}

	@Override
	public Collection<Journey> getJourneysByTenant(String tenantName, int fromIdx, int count) {
		SingleColumnValueFilter tenantFilter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.createPrefix(JOURNEY_COLUMN_FAMILY_PREFIX, JOURNEY_TENANT).getPrefixedColumnName(
						TENANT_NAME)),
				CompareOp.EQUAL,
				new BinaryComparator(toBytes(tenantName)));
		tenantFilter.setFilterIfMissing(true);
		tenantFilter.setLatestVersionOnly(true);
		return getFiltered(tenantFilter, count);
	}

	@Override
	public int getJourneyCountByTenant(String tenantName) {
		SingleColumnValueFilter tenantFilter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.createPrefix(JOURNEY_COLUMN_FAMILY_PREFIX, JOURNEY_TENANT).getPrefixedColumnName(
						TENANT_NAME)),
				CompareOp.EQUAL,
				new BinaryComparator(toBytes(tenantName)));
		tenantFilter.setFilterIfMissing(true);
		tenantFilter.setLatestVersionOnly(true);
		return getFiltered(tenantFilter).size();
	}

	@Override
	public int getJourneyIndexByName(String tenantName, String journeyName) {
		List<Filter> filters = Lists.newArrayListWithCapacity(3);

		SingleColumnValueFilter tenantFilter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.createPrefix(JOURNEY_COLUMN_FAMILY_PREFIX, JOURNEY_TENANT).getPrefixedColumnName(
						TENANT_NAME)),
				CompareOp.EQUAL,
				new BinaryComparator(toBytes(tenantName)));
		tenantFilter.setFilterIfMissing(true);
		tenantFilter.setLatestVersionOnly(true);
		filters.add(tenantFilter);

		SingleColumnValueFilter filter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.createPrefix(JOURNEY_COLUMN_FAMILY_PREFIX).getPrefixedColumnName(JOURNEY_NAME)),
				CompareOp.EQUAL,
				new BinaryComparator(toBytes(journeyName)));
		filter.setFilterIfMissing(true);
		filter.setLatestVersionOnly(true);
		filters.add(filter);

		FilterList filterList = new FilterList(
				FilterList.Operator.MUST_PASS_ALL, filters);

		Collection<Journey> found = getFiltered(filterList, 1);
		if (found.isEmpty()) {
			log.info(format("No journey found matching journey name '%s' and tenant '%s'", journeyName, tenantName));
			return 0;
		}

		return found.iterator().next().getId();
	}

	@Override
	public Journey getJourneyByName(String journeyName) {
		SingleColumnValueFilter filter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.createPrefix(JOURNEY_COLUMN_FAMILY_PREFIX).getPrefixedColumnName(JOURNEY_NAME)),
				CompareOp.EQUAL,
				new BinaryComparator(toBytes(journeyName)));
		filter.setFilterIfMissing(true);
		filter.setLatestVersionOnly(true);

		Collection<Journey> result = getFiltered(filter);
		return result.isEmpty() ? null : result.iterator().next();
	}

	@Override
	public Collection<Integer> getAllJourneyIds() {
		return FluentIterable.from(getAll()).transform(new Function<Journey, Integer>() {

			@Override
			public Integer apply(Journey input) {
				return input.getId();
			}
		}).toList();
	}

	@Override
	public boolean isJourneyDestination(String locationName) {
		return !getMatchingJourneyDestinations(locationName, false).isEmpty();
	}

	@Override
	public boolean isJourneyStart(String locationName) {
		SingleColumnValueFilter filter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.createPrefix(JOURNEY_COLUMN_FAMILY_PREFIX, JOURNEY_START).getPrefixedColumnName(
						LOCATION_NAME)),
				CompareOp.EQUAL,
				new BinaryComparator(toBytes(locationName)));
		filter.setFilterIfMissing(true);
		filter.setLatestVersionOnly(true);
		Collection<Journey> filtered = getFiltered(filter);
		return !filtered.isEmpty();
	}


	@Override
	public Collection<Location> getMatchingJourneyDestinations(String name, boolean normalize) {
		SingleColumnValueFilter filter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.createPrefix(JOURNEY_COLUMN_FAMILY_PREFIX, JOURNEY_DESTINATION).getPrefixedColumnName(
						LOCATION_NAME)),
				CompareOp.EQUAL,
				new SubstringComparator(name));
		filter.setFilterIfMissing(true);
		filter.setLatestVersionOnly(true);
		Collection<Journey> filtered = getFiltered(filter);
//		ArrayList<Location> locations = Lists.newArrayList();

		return FluentIterable.from(filtered).transform(new Function<Journey, Location>() {

			@Override
			public Location apply(Journey input) {
				return input.getDestination();
			}

		}).toList();
	}

	@Override
	public void removeJourneyById(int id) {
		deleteByRowKey(Bytes.toBytes(id));
	}

	@Override
	public int refreshJourneys() {
		return 0;
	}



	@Override
	public Journey getJourneyByIdNormalize(Integer id, boolean normalize) {
		return getJourneyById(id);
	}

}
