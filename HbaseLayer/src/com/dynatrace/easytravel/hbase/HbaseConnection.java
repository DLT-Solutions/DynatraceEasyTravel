/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HbaseConnection.java
 * @date: 06.02.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase;

import static com.dynatrace.easytravel.constants.BaseConstants.COMMA;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;

import com.dynatrace.easytravel.hbase.util.OS;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.persistence.PersistenceStoreNotAvailableException;
import com.google.common.base.Joiner;

import ch.qos.logback.classic.Logger;



/**
 * Each instance of this class represents a connection to an HBase instance or cluster.
 *
 * @author stefan.moschinski
 */
public class HbaseConnection {

	private static final Logger log = LoggerFactory.make();

	/**
	 *
	 * @param addresses addresses of the HBase instances
	 * @return a new {@link HbaseConnection} if a connection to the passed address(es) is possible
	 * @throws PersistenceStoreNotAvailableException if no connection could be established
	 */
	public static HbaseConnection openConnection(String... addresses) throws PersistenceStoreNotAvailableException {
		Validate.notEmpty(addresses, "The passed HBase addresses must not be empty");

		try {
			return new HbaseConnection(addresses);
		} catch (Exception t) {
			throw new PersistenceStoreNotAvailableException("Could not connect to HBase using following address(es): " +
					Arrays.toString(addresses), t);
		}
	}

	private final HBaseAdmin admin;
	private final Configuration conf;

	private HbaseConnection(String[] addresses) throws IOException {
		log.info("Connecting to HBase: " + Arrays.toString(addresses));
		this.conf = configureHbase(addresses);
		this.admin = new HBaseAdmin(conf);
	}

	/**
	 * @return an {@link HBaseAdmin} instance to access HBase
	 */
	public HBaseAdmin getAdmin() {
		return admin;
	}

	/**
	 * Closes the connection to HBase
	 *
	 * @author stefan.moschinski
	 */
	public void closeConnection() {
		IOUtils.closeQuietly(admin);
	}

	/**
	 *
	 * @return the default easyTravel {@link Configuration} of HBase
	 */
	public Configuration getConf() {
		return conf;
	}

	private Configuration configureHbase(String[] hbaseQuorum) {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", Joiner.on(COMMA).join(hbaseQuorum));
		conf.setInt("hbase.client.retries.number", 5);
		conf.setLong("zookeeper.session.timeout", TimeUnit.SECONDS.toMillis(60));

		// prevent OOM errors in map reduce jobs:
		conf.setFloat("io.sort.record.percent", 0.01f);

		if (OS.isWinOs()) {
			// do not use linux paths
			conf.set("mapreduce.jobtracker.staging.root.dir", FileUtils.getTempDirectoryPath());
			conf.set("mapred.local.dir", FileUtils.getTempDirectoryPath());
			conf.set("hbase.tmp.dir", FileUtils.getTempDirectoryPath());
		}

		log.info("Using following HBase configuration: " + conf);
		return conf;
	}



}