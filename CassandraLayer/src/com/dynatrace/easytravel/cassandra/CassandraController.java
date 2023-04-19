/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CassandraController.java
 * @date: 07.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.cassandra;

import com.dynatrace.easytravel.cassandra.base.EtCluster;
import com.dynatrace.easytravel.cassandra.base.EtKeySpace;
import com.dynatrace.easytravel.persistence.controller.TransactionlessController;


/**
 *
 * @author stefan.moschinski
 */
public class CassandraController extends TransactionlessController {

	private final EtCluster cluster;
	private final String keyspaceName;

	/**
	 * 
	 * @param cluster
	 * @author stefan.moschinski
	 * @param keyspaceName
	 */
	public CassandraController(EtCluster cluster, String keyspaceName) {
		this.cluster = cluster;
		this.keyspaceName = keyspaceName;
	}

	@Override
	public void close() {
		// currently closing single connections are not supported
	}

	@Override
	public void dropContents() {
	}

	EtKeySpace getKeyspace() {
		return new EtKeySpace(keyspaceName, cluster);
	}
	
	public EtCluster getEtCluster() {
		return cluster;
	}


}
