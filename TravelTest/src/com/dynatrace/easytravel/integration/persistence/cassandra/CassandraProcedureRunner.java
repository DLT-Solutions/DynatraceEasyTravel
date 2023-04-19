package com.dynatrace.easytravel.integration.persistence.cassandra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dynatrace.easytravel.integration.StartProcedure;
import com.dynatrace.easytravel.integration.persistence.ProcedureRunner;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.procedures.CassandraProcedure;


/**
 * Base class for running a database based test. It will
 * ensure that the database is started.
 *
 */
public class CassandraProcedureRunner {

	public static Iterable<CassandraProcedure> startCassandraNode(int nodeNo) throws IOException, CorruptInstallationException,
			InterruptedException {
		ProcedureRunner.configure();

		List<CassandraProcedure> list = new ArrayList<CassandraProcedure>(nodeNo);
		for (int i = 0; i < nodeNo; i++) {
			CassandraProcedure node1 = StartProcedure.newCassandraProcedure();
			node1.run();
			list.add(node1);
		}

		return list;
	}

}
