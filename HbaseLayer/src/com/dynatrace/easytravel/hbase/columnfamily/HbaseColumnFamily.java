/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: HbaseColumnFamily.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.columnfamily;

import static com.dynatrace.easytravel.hbase.HbaseDataController.NO_FILTER;
import static com.dynatrace.easytravel.hbase.HbaseDataController.NO_RESULT_LIMIT;
import static java.lang.String.format;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.util.Bytes;

import com.dynatrace.easytravel.hbase.HbaseDataController;
import com.dynatrace.easytravel.hbase.serializer.ColumnPrefix;
import com.dynatrace.easytravel.hbase.serializer.HbaseSerializer;
import com.dynatrace.easytravel.hbase.serializer.SerializerFactory;
import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.persistence.provider.EasyTravelPersistenceProvider;
import com.google.common.collect.Lists;

import ch.qos.logback.classic.Logger;
/**
 * 
 * @author stefan.moschinski
 */
public abstract class HbaseColumnFamily<OV extends Base> implements EasyTravelPersistenceProvider<OV> {

	private static final Logger log = LoggerFactory.make();

	protected final String columnFamilyName;
	protected final ColumnPrefix prefix;

	private final HbaseDataController controller;
	private final HbaseSerializer<OV> serializer;

	@SuppressWarnings("unchecked")
	HbaseColumnFamily(HbaseDataController controller, String columnFamilyName, String namespace) {
		this.columnFamilyName = columnFamilyName;
		this.controller = controller;
		this.prefix = ColumnPrefix.createPrefix(namespace);
		this.serializer = (HbaseSerializer<OV>) SerializerFactory.createSerializer(columnFamilyName,
				prefix);
	}

	@Override
	public OV add(OV value) {
		try {
			controller.addRecord(columnFamilyName, serializer.serialize(value));
		} catch (IOException e) {
			log.warn(format("Could not add '%s' to column family '%s'", value, columnFamilyName), e);
		}
		return value;
	}

	@Override
	public OV update(OV value) {
		return add(value);
	}

	protected OV getByKey(String key) {
		return getByKey(Bytes.toBytes(key));
	}

	protected OV getByKey(byte[] key) {
		try {
			Result result = controller.getByKey(key, columnFamilyName);

			if (result.isEmpty()) {
				log.info(format("Could not find a matching entry for key '%s' in column family '%s'", Bytes.toString(key),
						columnFamilyName));
				return null;
			}

			return serializer.deserialize(result);
		} catch (IOException e) {
			log.warn(format("Could not get value for key '%s' in column family '%s'", Bytes.toString(key), columnFamilyName), e);
		}

		return null;
	}

	protected Result getByKey(String column, byte[] key) {
		try {
			Result result = controller.getColumnByKey(key, columnFamilyName, column);

			if (result.isEmpty()) {
				log.info(format("Could not find a matching entry for key '%s' in column family '%s'", key, columnFamilyName));
				return null;
			}

			return result;
		} catch (IOException e) {
			log.warn(format("Could not get value for key '%s' in column '%s' of column family '%s'", Bytes.toString(key), column,
							columnFamilyName), e);
			return null;
		}
	}


	@Override
	public Collection<OV> getAll() {
		return getWithLimit(NO_RESULT_LIMIT);
	}

	@Override
	public Collection<OV> getWithLimit(int maxResults) {
		return getFiltered(NO_FILTER, maxResults);
	}

	Collection<OV> getFiltered(Filter filter) {
		return getFiltered(filter, NO_RESULT_LIMIT);
	}

	Collection<OV> getFiltered(Filter filter, int maxResults) {
		ResultScanner scanner = null;
		try {
			List<OV> results = Lists.newArrayListWithCapacity(maxResults == NO_RESULT_LIMIT ? 256 : maxResults);
			scanner = getScanner(filter);
			Iterator<Result> iterator = scanner.iterator();

			// the Hbase-0.95 SNAPSHOT has a native Scan::setMaxResultSize method --> use it in future
			for (int i = 0; i < maxResults && iterator.hasNext(); i++) {
				Result result = iterator.next();
				results.add(serializer.deserialize(result));
			}

			return results;
		} catch (IOException e) {
			log.warn(format("Could not get results for colum family '%s' - filter is: %s", columnFamilyName, filter), e);
			return Collections.emptyList();
		} finally {
			IOUtils.closeQuietly(scanner);
		}
	}

	public ResultScanner getScanner(Filter filter) throws IOException {
		return controller.getScanner(columnFamilyName, filter);
	}

	@Override
	public int getCount() {
		try {
			// trigger map reduce job
			controller.refreshCounter(columnFamilyName);

			return controller.getCount(columnFamilyName);
		} catch (Exception e) {
			log.warn(format("Cannot count row for column family '%s'", columnFamilyName), e);
		}
		return 0;
	}

	public void createColumnFamily() {
		controller.addColumnFamily(columnFamilyName);
	}

	public void deleteColumnFamily() {
		try {
			controller.deleteColumnFamilyContents(columnFamilyName);
		} catch (IOException e) {
			log.warn(format("Cannot delete column family '%s'", columnFamilyName), e);
		}
	}

	boolean deleteByRowKey(String rowKey) {
		try {
			controller.deleteByRowKey(rowKey, columnFamilyName);
			return true;
		} catch (IOException e) {
			log.warn(format("Could not delete row with key '%s' in column family '%s'", rowKey, columnFamilyName),
					e);
			return false;
		}
	}

	boolean deleteByRowKey(byte[] rowKey) {
		try {
			controller.deleteByRowKey(rowKey, columnFamilyName);
			return true;

		} catch (IOException e) {
			log.warn(format("Could not delete row with key '%s' in column family '%s'", rowKey, columnFamilyName),
					e);
		}
		return false;
	}

	@Override
	public void reset() {
		deleteColumnFamily();
		createColumnFamily();
	}
	
	void increment(String rowKey, String columnName, long amount) {
		try {
			controller.increment(rowKey, columnFamilyName, columnName, amount);
		} catch (IOException e) {
			log.warn(format("Could increment value for key '%s' in column '%s' of column family '%s'",
							rowKey,
							columnName,
							columnFamilyName), e);
		}
	}

}
