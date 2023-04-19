/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CassandraPersistence.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.cassandra;

import java.util.concurrent.TimeUnit;

import com.dynatrace.easytravel.cassandra.base.EtCluster;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.persistence.AbstractDatabase;
import com.dynatrace.easytravel.persistence.MandatoryPersistencePreparation;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class CassandraDatabase extends AbstractDatabase {

	private EtCluster cluster;

	/**
	 * Creates a new {@link CassandraDatabase} instance
	 */
	public CassandraDatabase() {
		super("Cassandra");
	}

	/**
	 * Const' especially for testing purposes - allows you to pass a
	 *
	 * @param cluster an {@link EtCluster}
	 * @author stefan.moschinski
	 */
	public CassandraDatabase(EtCluster cluster) {
		super("Cassandra");
		this.cluster = cluster;
	}

	@Override
	public BusinessDatabaseController createNewBusinessController() {
		return createNewBusinessController("easyTravelBusiness");
	}

	public BusinessDatabaseController createNewBusinessController(String clusterName) {
		return new CassandraBusinessController(new CassandraController(getCluster(), clusterName));
	}

	@Override
	public void setupPersistenceLayer() {
		getCluster().create();
		((MandatoryPersistencePreparation) getBusinessController()).createSchema();
	}


	private synchronized EtCluster getCluster() {
		if (cluster == null) {
			cluster = createNewCluster();
		}
		return cluster;
	}

	private EtCluster createNewCluster() {
		EasyTravelConfig cfg = EasyTravelConfig.read();
		CassandraUtils.waitUntilNodeRunning(cfg.cassandraNodeAddresses, TimeUnit.SECONDS.toMillis(45),
				cfg.cassandraNodeAddresses.length);
		EtCluster etCluster = new EtCluster("easyTravelCluster", cfg.cassandraNodeAddresses);
		etCluster.create();
		return etCluster;
	}

	@Override
	public synchronized void closeConnection() {
		if (cluster != null) {
			cluster.close();
		}
	}


}
