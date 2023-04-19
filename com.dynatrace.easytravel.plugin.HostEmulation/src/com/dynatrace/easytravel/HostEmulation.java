package com.dynatrace.easytravel;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

public class HostEmulation extends AbstractGenericPlugin {

	private static Logger log = LoggerFactory.make();

	private static final String DEFAULT_HOST_NAME_PATTERN = "fake-host";
	
	private static final int DEFAULT_NUMBER_HOSTS = 50; 
	private static final int CONNECTING_THREAD_COUNT = 10;

	private static final AtomicInteger counter = new AtomicInteger(0);
	private static final CopyOnWriteArraySet<Future<Process>> processes = new CopyOnWriteArraySet<>();

	/**
	 * we use 1/4 of the original agent buffer size (100,000) to reduce the memory footprint of the host agents
	 * with this setting each 32bit host agent requires about 10MB RAM 
	 */
	private static final int BUFFER_SIZE = 10_000;

	@Override
	public Object doExecute(String location, Object... context) {
		if (PluginConstants.LIFECYCLE_PLUGIN_ENABLE.equals(location)) {
			EasyTravelConfig config = EasyTravelConfig.read();
			File hostAgent = lookUpHostAgent(config.agent);
			if (hostAgent == null) {
				if (config.agent != null
						&& new File(config.agent).getParent() != null) {
					log.warn(String.format(
							"Could not find host agent in folder '%s'",
							new File(config.agent).getParent()));
				} else {
					log.warn("Could not find host agent, because set 'config.agent' property is invalid");
				}
				return null;
			}
			int noHostAgents = getNoHosts(config.noHostAgents);
			List<String> hostNamePatterns = getHostNamePatterns(config.hostNamePatterns);
			processes.addAll(startHosts(hostAgent, noHostAgents, hostNamePatterns, config.dtServer, config.dtServerPort));
		} else if (PluginConstants.LIFECYCLE_PLUGIN_DISABLE.equals(location)
				|| PluginConstants.LIFECYCLE_BACKEND_SHUTDOWN.equals(location)) {
			shutdonwnHosts(processes);
		}
		return null;
	}

	
	private List<String> getHostNamePatterns(String[] hostNamePatterns) {
		if (hostNamePatterns == null || hostNamePatterns.length == 0) {
			return Arrays.asList(DEFAULT_HOST_NAME_PATTERN);
		}		
		return Arrays.asList(hostNamePatterns);
	}

	private int getNoHosts(int noHostAgents) {
		return noHostAgents > 0 ? noHostAgents : DEFAULT_NUMBER_HOSTS;
	}

	private Collection<Future<Process>> startHosts(final File hostAgent, int noHostAgents, final List<String> hostNamePatterns, String dtServer, String dtServerPort) {
		final String hostName = getHostName();
		final File outputFile;
		try {
			outputFile = File.createTempFile("fake_hostagents_", ".log");
			outputFile.deleteOnExit();
		} catch (IOException e) {
			log.warn("Failed to create temp file - no hosts will be started", e);
			return null;
		}
		final String collectorAddress = getCollectorAddress(dtServer, dtServerPort);
		ExecutorService executor = createExecutor();
		final List<Future<Process>> processes = new ArrayList<>();

		log.info(String.format("Staring '%s' host agents that will connect to '%s'", noHostAgents, collectorAddress));
		for (int i = 0; i < noHostAgents; i++) {
			processes.add(executor.submit(new Callable<Process>() {
				@Override
				public Process call() throws Exception {
					ProcessBuilder pb = new ProcessBuilder(
							hostAgent.getAbsolutePath(), 
							overrideHostName(hostName), 
							"server=" + collectorAddress,
							"buffers=" + BUFFER_SIZE)
							.redirectError(outputFile);
					return pb.start();
				}

				private String overrideHostName(final String hostName) {
					String fakeHostName = getHostName(hostNamePatterns, counter.getAndIncrement());
					return "overridehostname=" + fakeHostName;
				}

			}));
		}
		executor.shutdown();
		return processes;
	}
	
	static String getHostName(List<String> hostNamePatterns, int no) {
		int index = no % hostNamePatterns.size();
		return String.format("%s.%03d", hostNamePatterns.get(index), no + 1);
	}
	
	private String getCollectorAddress(String dtServer, String dtServerPort) {
        int port = dtServerPort == null || dtServerPort.isEmpty() || BaseConstants.AUTO.equalsIgnoreCase(dtServerPort) ? -1 : Integer.parseInt(dtServerPort); 
        if (port > 0) {
        	return dtServer + BaseConstants.COLON + port;
        }
		return dtServer;
	}

	private ExecutorService createExecutor() {
		ThreadPoolExecutor executor = new ThreadPoolExecutor(CONNECTING_THREAD_COUNT,
				CONNECTING_THREAD_COUNT, 60L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
		executor.allowCoreThreadTimeOut(true);
		return executor;
	}

	private void shutdonwnHosts(
			CopyOnWriteArraySet<Future<Process>> hostProcesses) {
		for (Future<Process> future : hostProcesses) {
			try {
				future.get().destroy();
				hostProcesses.remove(future);
			} catch (InterruptedException e) {
				// ignore
			} catch (ExecutionException e) {
				log.warn("Failed to get host process",
						e.getCause());
			}
		}
	}

	private String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "localhost";
		}
	}

	final File lookUpHostAgent(String agent) {
		File agentPath = new File(agent);
		if (agentPath.exists()) {
			if (agentPath.getParent().endsWith("64")) {
				// to reduce the memory footprint we prefer to use the 32bit host agent
				File lib32 = new File(agentPath.getParentFile().getParentFile(), "lib");
				File hostAgent32 = lookUpHostAgentInDir(lib32);
				if (hostAgent32 != null) {
					return hostAgent32;
				}
			}
			File hostAgent = lookUpHostAgentInDir(agentPath.getParentFile());
			if (hostAgent != null) {
				return hostAgent;
			}
		}
		return null;
	}

	private File lookUpHostAgentInDir(File agentLib) {
		File hostAgent = new File(agentLib, "dthostagent"
				+ (isWinOs() ? ".exe" : ""));
		if (hostAgent.exists()) {
			return hostAgent;
		}
		return null;
	}

	private static final String OS_NAME = System.getProperty("os.name", "")
			.toLowerCase();

	public static boolean isWinOs() {
		return OS_NAME.contains("windows");
	}

}
