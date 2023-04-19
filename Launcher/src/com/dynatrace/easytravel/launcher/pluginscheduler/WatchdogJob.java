package com.dynatrace.easytravel.launcher.pluginscheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import com.dynatrace.diagnostics.uemload.http.base.HostAvailability;
import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.launcher.engine.Batch;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * Job for monitoring if specified easyTravel components
 * are available if not Batch restart is performed
 *
 * @author cwpl-rorzecho
 */

public class WatchdogJob implements Job {
	private static Logger LOGGER = Logger.getLogger(WatchdogJob.class.getName());

	public static HostAvailability hostAvailability = HostAvailability.INSTANCE;

	public static final String UNAVAILABLE_HOSTS = "unavailableHosts";

	private JobExecutionContext context;

	public static Batch batch = null;

	@Override
	public void execute(JobExecutionContext context) {
		this.context = context;
		LOGGER.info("Host Unavailability Check...");

		batch = getBatch();

		switch (getBatchState()) {
			case STOPPING:
			case STARTING:
			case STOPPED:
				break;
			case OPERATING:
				loadUnexpectedUnavailableHosts();
				checkAvailability();
				break;
			case FAILED:
			case ACCESS_DENIED:
			case TIMEOUT:
				restartBatch();
				break;
		}
	}

	/**
	 * Load hosts for unexpected unavailable
	 *
	 * @return Collection<String>
	 */
	public Collection<String> loadUnexpectedUnavailableHosts() {
		JobDataMap contextJobDataMap = context.getJobDetail().getJobDataMap();
		Collection<String> unavailableHosts = Collections.emptyList();

		if (contextJobDataMap.containsKey(UNAVAILABLE_HOSTS)) {
			unavailableHosts = getUnanavailableHosts(contextJobDataMap);
			if (!isUnavailableHostsLoaded(unavailableHosts)) {
				hostAvailability.informUnexpectedUnavailable(unavailableHosts);

				LOGGER.log(Level.INFO, TextUtils.merge("Hosts monitored for unanavailability: {0}", unavailableHosts.toString()));
			}
		}

		return unavailableHosts;
	}

	/**
	 * Get unavailable hosts from JobDataMap object specified in XML definiton
	 * Entry contains:
	 * key: unavailableHosts
	 * value: comma separated host urls to be checked for unanavailability
	 *
	 * For instance:
	 * <job-data-map>
	 *	<entry>
	 *		<key>unavailableHosts</key>
	 *		<value>http://172.18.0.1:7777,http://172.18.0.2:8888,http://172.18.0.3:9999</value>
	 *	</entry>
	 *</job-data-map>
	 *
	 * @param contextJobDataMap
	 * @return
	 */
	private Collection<String> getUnanavailableHosts(JobDataMap contextJobDataMap) {
		StringTokenizer hostsTokens = new StringTokenizer(contextJobDataMap.getString(UNAVAILABLE_HOSTS), ",");
		Collection<String> unavailableHosts = new ArrayList<String>();

		while (hostsTokens.hasMoreElements()) {
			unavailableHosts.add(hostsTokens.nextToken());
		}
		return unavailableHosts;
	}

	/**
	 * Check if all defined unavailabile hosts are set to HostAvailability instance
	 * @param unavailableHosts
	 * @return
	 */
	private boolean isUnavailableHostsLoaded(Collection<String> unavailableHosts) {
		return hostAvailability.getUnexpectedHostUvavailable().containsAll(unavailableHosts);
	}

	/**
	 * If there is any host anavailibility the Batch will be restarted
	 */
	private void checkAvailability() {
		if (hostAvailability.isAnyUnexpectedUnavailable()) {
			restartBatch();

			LOGGER.log(Level.INFO, TextUtils.merge("Batch was restarted beceause of host {0} was unexpectedly unavailable",
					hostAvailability.getUnexpectedHostUvavailable().toString()));
		}
	}

	public Batch getBatch() {
		return batch == null ? getRunningBatch(): batch;
	}

	public State getBatchState() {
		return batch != null ? batch.getState() : State.STOPPED;
	}

	private Batch getRunningBatch() {
		return LaunchEngine.getRunningBatch() != null ? LaunchEngine.getRunningBatch(): null;
	}

	/**
	 * Stop running Batch
	 */
	private void stopBatch() {
		LOGGER.log(Level.WARNING, "Stopping Batch...");
		try {
			batch.stop();
		} catch (IllegalStateException e) {
			LOGGER.log(Level.SEVERE, "Batch cannot be stopped now", e);
		}
	}

	/**
	 * Start running Batch
	 */
	private void startBatch() {
		LOGGER.log(Level.WARNING, "Starting Batch...");
		try {
			batch.start();
		} catch (IllegalStateException e) {
			LOGGER.log(Level.SEVERE, "Batch cannot be started now", e);
		}
	}

	/**
	 * Restart running Batch
	 */
	private void restartBatch() {
		stopBatch();
		startBatch();
	}

	/**
	 * For tests only
	 *
	 * @param hostAvailability
	 */
	@TestOnly
	public void setHostAvailability(HostAvailability hostAvailability) {
		WatchdogJob.hostAvailability = hostAvailability;
	}

	/**
	 * For test only
	 * @param batch
	 */
	@TestOnly
	public void setBatch(Batch batch) {
		WatchdogJob.batch = batch;
	}

	public HostAvailability getHostAvailability() {
		return hostAvailability;
	}
}
