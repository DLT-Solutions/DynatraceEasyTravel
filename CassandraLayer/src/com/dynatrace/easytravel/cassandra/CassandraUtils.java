/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CassandraUtils.java
 * @date: 07.08.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.cassandra;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.Persistence;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;

import ch.qos.logback.classic.Logger;


/**
 *
 * @author stefan.moschinski
 */
public abstract class CassandraUtils {

	private static final Logger log = LoggerFactory.make();


	/**
	 * Checks whether the defined Cassandra node is available
	 *
	 * @param host host of the Cassandra node
	 * @param port port of the Cassandra node
	 * @return <code>true</code> if a connection could be established
	 * @author stefan.moschinski
	 */
	public static boolean isNodeAvailable(String host, int port) {
		TTransport transport = new TSocket(host, port);

		try {
			transport.open();
			return true;
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				String error = (e.getCause() == null) ? e.getMessage() : e.getCause().getMessage();
				log.debug(TextUtils.merge("Exception connecting to ''{0}:{1}''. Reason: {2}.", host,
						String.valueOf(port), error));
			}
		} finally {
			transport.close();
		}
		return false;
	}

	private static boolean isNodeAvailable(String address) {
		String[] split = address.split(BaseConstants.COLON);
		if (split.length == 2) {
			String host = split[0];
			int port = Integer.parseInt(split[1]);
			return isNodeAvailable(host, port);
		} else if (split.length == 1) {
			return isNodeAvailable(address, Persistence.Cassandra.DEFAULT_RPC_PORT);
		}
		throw new IllegalArgumentException(TextUtils.merge("The cassandra node address ''{0}'' cannot be validated", address));
	}

	/**
	 * Tries to connect to the specified Cassandra node as long as the timeout is not reached
	 *
	 * @param host host of the Cassandra node
	 * @param port port of the Cassandra node
	 * @param timeoutMs defines how long (in milliseconds) the method tries to connect the specified node
	 * @return <code>true</code> if a connection could be established within the timeout
	 * @author stefan.moschinski
	 */
	public static boolean waitUntilNodeRunning(String[] nodeAddresses, long timeoutMs, int noRequiredNodes) {
		Preconditions.checkArgument(noRequiredNodes <= nodeAddresses.length,
				"noRequiredNodes (%d) cannot be greater than the size of the passed nodeAdresses (%d)", noRequiredNodes,
				nodeAddresses.length);

		Stopwatch stopwatch = Stopwatch.createStarted();

		Set<String> availableNodes = Sets.newHashSetWithExpectedSize(nodeAddresses.length);
		while (stopwatch.elapsed(TimeUnit.MILLISECONDS) <= timeoutMs) {
			for (String address : nodeAddresses) {
				if (isNodeAvailable(address) && availableNodes.add(address)) {
					if (log.isDebugEnabled()) {
						log.debug(TextUtils.merge("Successfully connected to Cassandra node ''{0}:{1}''",
								address, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
					}
				}

				if (availableNodes.size() >= noRequiredNodes) {
					return true;
				}
			}

			// wait some time before trying again...
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.warn("Interrupted while sleeping!");
			}

		}

		log.warn(TextUtils.merge(
				"Waited for {0} ms, but there could not be established ''{1}'' connection(s) to the Cassandra node(s) ''{2}''",
				stopwatch.elapsed(TimeUnit.MILLISECONDS), noRequiredNodes, nodeAddresses));
		return false;
	}



}
