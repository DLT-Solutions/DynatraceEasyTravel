/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HBaseDataController.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase;

import static java.lang.String.format;
import static org.apache.hadoop.hbase.util.Bytes.toBytes;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.coprocessor.example.RowCountEndpoint;
import org.apache.hadoop.hbase.coprocessor.example.generated.ExampleProtos;
import org.apache.hadoop.hbase.coprocessor.example.generated.ExampleProtos.CountResponse;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.ipc.BlockingRpcCallback;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;

import com.dynatrace.easytravel.hbase.HBaseCounterUpdater.RowCounterReducer;
import com.dynatrace.easytravel.hbase.HBaseCounterUpdater.RowCunterMapper;
import com.dynatrace.easytravel.hbase.columnfamily.HbaseCounterColumnFamily;
import com.dynatrace.easytravel.hbase.serializer.PersistableHbaseObject;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.persistence.PersistenceStoreNotAvailableException;
import com.dynatrace.easytravel.persistence.controller.DatabaseController;

import ch.qos.logback.classic.Logger;

/**
 *
 * @author stefan.moschinski
 */
public class HbaseDataController implements DatabaseController {

	public static final int NO_RESULT_LIMIT = Integer.MAX_VALUE;
	public static final Filter NO_FILTER = null;


	private static final Logger log = LoggerFactory.make();

	private final String tableName;
	private final HTable table;

	private final HBaseAdmin admin;
	private final Configuration conf;

	/**
	 * Creates a new controller using the passed tableName
	 *
	 * @param tableName
	 * @throws PersistenceStoreNotAvailableException if an exception happens during setup of the HBase controller
	 */
	public HbaseDataController(HbaseConnection hbaseConnection, String tableName) throws PersistenceStoreNotAvailableException {
		this.tableName = tableName;
		this.admin = hbaseConnection.getAdmin();
		this.conf = hbaseConnection.getConf();

		try {
			this.table = createTable(admin, tableName);
		} catch (IOException e) {
			throw new PersistenceStoreNotAvailableException("Could not create HBase table " + tableName, e);
		}
	}

	private HTable createTable(HBaseAdmin admin, String tableName) throws IOException {
		try {
			if (!admin.tableExists(tableName)) {
				createNewHbaseTable(admin, tableName);
			} else {
				ensureTableHasCoprocessor(admin, tableName);
			}
		} finally {
			ensureTableIsEnabled();
		}
		return new HTable(conf, tableName);
	}


	private void createNewHbaseTable(HBaseAdmin admin, String tableName) throws IOException {
		HTableDescriptor desc = new HTableDescriptor(tableName);
		addCoprocessor(desc);
		admin.createTable(desc);
	}

	private void ensureTableHasCoprocessor(HBaseAdmin admin, String tableName) throws TableNotFoundException, IOException {
		HTableDescriptor desc = admin.getTableDescriptor(toBytes(tableName));
		if (!desc.hasCoprocessor(RowCountEndpoint.class.getName())) {
			admin.disableTable(tableName);
			addCoprocessor(desc);
			admin.modifyTable(toBytes(tableName), desc);
		}
	}

	private void addCoprocessor(HTableDescriptor desc) throws IOException {
		desc.addCoprocessor(RowCountEndpoint.class.getName());
	}

	public void addColumnFamily(String columnFamilyName) {
		ensureTableIsEnabled();

		try {
			if (table.getTableDescriptor().hasFamily(toBytes(columnFamilyName))) {
				return;
			}

			admin.disableTable(tableName);
			HColumnDescriptor columnFamDesc = new HColumnDescriptor(toBytes(columnFamilyName));
			columnFamDesc.setMaxVersions(1); // we need only the most recent version

			admin.addColumn(toBytes(tableName), columnFamDesc);
		} catch (IOException ioe) {
			throw new IllegalStateException(format("Could not create column family '%s' in table '%s'", columnFamilyName,
					tableName), ioe);
		} finally {
			ensureTableIsEnabled();
		}
	}

	public void deleteColumnFamilyContents(String columnFamilyName) throws IOException {
		if (!table.getTableDescriptor().hasFamily(toBytes(columnFamilyName))) {
			return;
		}

		startTransaction();

		ResultScanner scanner = getScanner(columnFamilyName);
		try {
			for (Result result : scanner) {
				deleteByRowKey(result.getRow(), columnFamilyName);
			}
		} finally {
			IOUtils.closeQuietly(scanner);
		}

		commitTransaction();
	}


	public void ensureTableIsEnabled() {
		try {
			if (admin.isTableDisabled(tableName)) {
				admin.enableTable(tableName);
			}
		} catch (IOException e) {
			log.warn(format("Cannot enable HBase table '%s'", tableName), e);
		}
	}

	@Override
	public void close() {
		IOUtils.closeQuietly(table);
		IOUtils.closeQuietly(admin);
	}

	@Override
	public void dropContents() {
		try {
			admin.disableTable(tableName);
			admin.deleteTable(tableName);
		} catch (IOException e) {
			log.warn(format("Cannot drop contents of HBase table '%s'", tableName), e);
		}
	}

	public void addRecord(String columnFamily, PersistableHbaseObject objectToStore) throws IOException {
		Put put = new Put(objectToStore.getKey());
		for (Entry<byte[], byte[]> columnPair : objectToStore) {
			put.add(Bytes.toBytes(columnFamily),
					columnPair.getKey(), columnPair.getValue());
		}

		table.put(put);
	}

	/**
	 * <b>Attention</b>: the client is responsible for <b>closing</b> the returned scanner
	 *
	 * @param columnFamilyName
	 * @return
	 * @throws IOException
	 * @author stefan.moschinski
	 */
	public ResultScanner getScanner(String columnFamilyName) throws IOException {
		Scan scan = new Scan();
		scan.setMaxVersions(1);
		scan.addFamily(Bytes.toBytes(columnFamilyName));
		return table.getScanner(scan);
	}

	public ResultScanner getScanner(String columnFamilyName, Filter filter) throws IOException {
		Scan scan = new Scan();
		if (filter != NO_FILTER) {
			scan.setFilter(filter);
		}
		scan.setMaxVersions(1);
		scan.addFamily(Bytes.toBytes(columnFamilyName));
		return table.getScanner(scan);
	}

	public int getCount(String columnFamilyName) throws IOException {
		Configuration configuration = HBaseConfiguration.create(conf);

		configuration.setLong("hbase.rpc.timeout", 600000);
		// Default is 1, set to a higher value for faster scanner.next(..)
		configuration.setLong("hbase.client.scanner.caching", 1000);

		
		try {
			final ExampleProtos.CountRequest request = ExampleProtos.CountRequest.getDefaultInstance();
			Map<byte[], Object> results = table.coprocessorService(
					ExampleProtos.RowCountService.class, // the protocol interface we're invoking
					null, null,                          // start and end row keys
					new Batch.Call<ExampleProtos.RowCountService, Object>() {
						public Long call(ExampleProtos.RowCountService counter) throws IOException {
							BlockingRpcCallback<CountResponse> rpcCallback =
									new BlockingRpcCallback<CountResponse>();
							counter.getRowCount(null, request, rpcCallback);
							ExampleProtos.CountResponse response = rpcCallback.get();
							return response.hasCount() ? response.getCount() : 0;
						}
					});

			if (!results.isEmpty()) {
				Object object = results.values().iterator().next();
				if (object != null && object instanceof Number) {
					return ((Number) object).intValue();
				}
			}
			log.error(format("Could not get the row count for column family '%s'. Returned map was %s", columnFamilyName, results));
			return 0;
		} catch (Throwable t) {	// NOPMD - on purpose here as rowCount() throws Throwable itself?!
			log.error(format("Could not get the row count for column family '%s'", columnFamilyName), t);
			return 0;
		}
	}

	@Override
	public void flushAndClear() {
		flush();
	}


	@Override
	public void flush() {
		try
		{
			table.flushCommits();
			table.setAutoFlush(true);
		} catch (IOException e)
		{
			log.warn("Could not flush HBase commits", e);
		}
	}

	public void deleteByRowKey(String rowKey, String columnFamilyName) throws IOException {
		deleteByRowKey(toBytes(rowKey), columnFamilyName);
	}


	public void deleteByRowKey(byte[] rowKey, String columnFamilyName) throws IOException {
		Delete delete = new Delete(rowKey);
		delete.deleteFamily(toBytes(columnFamilyName));
		table.delete(delete);
	}


	/**
	 *
	 * @param key
	 * @author stefan.moschinski
	 * @return
	 * @throws IOException
	 */
	public Result getByKey(byte[] key, String columnFamily) throws IOException {
		Get get = new Get(key);
		get.addFamily(toBytes(columnFamily));
		return table.get(get);
	}

	public Result getColumnByKey(byte[] key, String columnFamily, String column) throws IOException {
		Get get = new Get(key);
		get.addColumn(toBytes(columnFamily), toBytes(column));
		return table.get(get);
	}


	@Override
	public void startTransaction() {
		table.setAutoFlush(false);
	}



	@Override
	public void commitTransaction() {
		flush();
	}

	@Override
	public void rollbackTransaction() {
		log.info("rollback of transactions is not supported by HBase");
	}


	public void increment(String rowKey, String columnFamily, String columnName, long amount) throws IOException {
		table.incrementColumnValue(toBytes(rowKey),
				toBytes(columnFamily), toBytes(columnName), amount);
	}


	public void refreshCounter(String columnFamilyName) throws IOException, InterruptedException, ClassNotFoundException {
		Configuration config = HBaseConfiguration.create(conf);

		// make sure counter column family exists
		addColumnFamily(HbaseCounterColumnFamily.COUNTER_COLUMN_FAMILY_NAME);

		Job job = new Job(config, "Hbase_FreqCounter1");
		job.setJarByClass(HBaseCounterUpdater.class);

		Scan scan = new Scan();
		scan.setCaching(500); // 1 is the default in Scan, which will be bad for MapReduce jobs
		scan.setCacheBlocks(false); // don't set to true for MR jobs

		scan.addFamily(toBytes(columnFamilyName));

		TableMapReduceUtil.initTableMapperJob(tableName,
				scan,
				RowCunterMapper.class,
				ImmutableBytesWritable.class,
				IntWritable.class,
				job);

		TableMapReduceUtil.initTableReducerJob(tableName, RowCounterReducer.class, job);
		job.setNumReduceTasks(1);

		job.submit(); // start job asynchronously
//		job.waitForCompletion(true);
	}



}
