/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HBaseCounterUpdater.java
 * @date: 05.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase;

import java.io.IOException;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;

import com.dynatrace.easytravel.hbase.columnfamily.HbaseCounterColumnFamily;


/**
 * A simple mapper and reducer needed for the local map reduce job
 * @author stefan.moschinski
 */
public class HBaseCounterUpdater {

    public static class RowCunterMapper extends TableMapper<ImmutableBytesWritable, IntWritable> {

        private static final IntWritable one = new IntWritable(1);

        @Override
        public void map(ImmutableBytesWritable row, Result values, Context context) throws IOException {
            // extract userKey from the compositeKey (userId + counter)
            ImmutableBytesWritable userKey = new ImmutableBytesWritable(row.get(), 0, Bytes.SIZEOF_INT);
            try {
                context.write(userKey, one);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    public static class RowCounterReducer extends TableReducer<ImmutableBytesWritable, IntWritable, ImmutableBytesWritable> {

        @Override
		public void reduce(ImmutableBytesWritable key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }

            Put put = new Put(key.get());
			put.add(Bytes.toBytes(HbaseCounterColumnFamily.COUNTER_COLUMN_FAMILY_NAME),
					Bytes.toBytes("count"),
					Bytes.toBytes(sum));
            context.write(key, put);
        }
    }
}
