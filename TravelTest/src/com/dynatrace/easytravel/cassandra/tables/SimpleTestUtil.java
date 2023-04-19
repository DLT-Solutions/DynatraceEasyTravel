package com.dynatrace.easytravel.cassandra.tables;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.dynatrace.easytravel.integration.persistence.cassandra.CassandraProcedureRunner;
import com.dynatrace.easytravel.launcher.procedures.CassandraProcedure;

public class SimpleTestUtil {
	private static Iterable<CassandraProcedure> cassandraNodes;
    
    @BeforeClass
	public static void setupClass() throws Exception {
		CassandraTableTestUtil.deleteDirectoryWithPrefix(CassandraTableTestUtil.HOME_DIRECTORY,
				CassandraTableTestUtil.DIRECTORY1, CassandraTableTestUtil.PREFIX);
		CassandraTableTestUtil.deleteDirectoryWithPrefix(CassandraTableTestUtil.HOME_DIRECTORY,
				CassandraTableTestUtil.DIRECTORY1, CassandraTableTestUtil.PREFIX);
		cassandraNodes = CassandraProcedureRunner.startCassandraNode(1);
	}
    
    @AfterClass
    public static void tearDownClass() {
        for (CassandraProcedure node : cassandraNodes) {
			node.stop();
		}
    }
}
