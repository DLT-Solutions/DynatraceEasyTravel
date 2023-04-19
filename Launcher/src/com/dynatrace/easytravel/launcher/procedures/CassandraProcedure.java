package com.dynatrace.easytravel.launcher.procedures;

import static java.lang.String.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.cassandra.CassandraUtils;
import com.dynatrace.easytravel.config.CassandraReservation;
import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.CassandraArgument;
import com.dynatrace.easytravel.constants.BaseConstants.Persistence.Cassandra;
import com.dynatrace.easytravel.constants.BaseConstants.SubDirectories;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.DynamicPortDtAgentConfig;
import com.dynatrace.easytravel.launcher.engine.*;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.sync.Predicate;
import com.dynatrace.easytravel.launcher.sync.PredicateMatcher;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class CassandraProcedure extends AbstractJavaProcedure implements WebProcedure {

	private static final EasyTravelConfig CONFIG = EasyTravelConfig.read();
	private static final String PLACEHOLDER_LISTEN_ADDRESS = "PLACEHOLDER_listen_address";
	private static final String PLACEHOLDER_RPC_ADRESS = "PLACEHOLDER_RPC_ADRESS";

	private static final Logger log = Logger.getLogger(CassandraProcedure.class.getName());

	private static final String LOG4J_CFG = "log4j-server.properties";
	private static final String CASSANDRA_CFG = "cassandra.yaml";

	private static final String PLACEHOLDER_DATA_FILE_DIRECTORIES = "PLACEHOLDER_data_file_directories";
	private static final String PLACEHOLDER_COMMITLOG_DIRECTORY = "PLACEHOLDER_commitlog_directory";
	private static final String PLACEHOLDER_SAVE_CACHES_DIRECTORY = "PLACEHOLDER_saved_caches_directory";
	private static final String PLACEHOLDER_INITIAL_TOKEN = "PLACEHOLDER_initial_token";
	private static final String PLACEHOLDER_LOG_FILE_PATH = "PLACEHOLDER_LOG_FILE_PATH";
	private static final String PLACEHOLDER_HINT_FILE_DIRECTORIES = "PLACEHOLDER_hints_directory";
	private static final String PLACEHOLDER_CDC_RAW_FILE_DIRECTORIES = "PLACEHOLDER_cdc_raw_directory";
	private static final String PLACEHOLDER_SEED = "PLACEHOLDER_SEED";

	private static int CASSANDRA_TIMEOUT_MS = (int) TimeUnit.MINUTES.toMillis(3); // cassandra nodes may take a quite long time to startup

	private static final File BASE_CASSANDRA_CFG_FILE = new File(Directories.getInstallDir().getAbsolutePath() + File.separator +
			SubDirectories.CASSANDRA, CASSANDRA_CFG);
	private static final File BASE_LOG4J_CFG_FILE = new File(Directories.getInstallDir().getAbsolutePath() + File.separator +
			SubDirectories.CASSANDRA, LOG4J_CFG);

	private static final String EASYTRAVEL_CONFIG_PATH = Directories.getConfigDir().getAbsolutePath();

	private static final CassandraToken token = new CassandraToken(CassandraProcedure.CONFIG.cassandraNodeAddresses);

	private String logPath;
	protected String host;
	protected int port;

	public String adaptedconf;
	private String nodeName;
	private int nodeNo;
	private volatile CassandraReservation reservation;

	private void configure() {
		try {
			copyAndAdaptCfg(BASE_CASSANDRA_CFG_FILE);
			copyAndAdaptLogCfg(BASE_LOG4J_CFG_FILE);
		} catch (ConfigurationException e) {
			throw new IllegalStateException("Could not properly configure Cassandra node", e);
		}
	}

	private void copyAndAdaptLogCfg(File cfg) throws ConfigurationException {
		try {
			String orgLogCfg = FileUtils.readFileToString(cfg, BaseConstants.UTF8);
			String adaptedLogCfg = orgLogCfg
					.replace(
							PLACEHOLDER_LOG_FILE_PATH,
							/* forward slashes are required: */logPath.replace(File.separator, BaseConstants.FORWARD_SLASH));
			FileUtils.write(new File(EASYTRAVEL_CONFIG_PATH, getLog4jPropFileName()),
					adaptedLogCfg, BaseConstants.UTF8);
		} catch (FileNotFoundException e) {
			throw new ConfigurationException("Could not adapt cassandra log config", e);
		} catch (IOException e) {
			throw new ConfigurationException("Could not adapt cassandra log config", e);
		}
	}

	private String getLog4jPropFileName() {
		return TextUtils.merge("log4j-server-{0}.properties", nodeName);
	}

	private void copyAndAdaptCfg(File cfg) throws ConfigurationException {
		try {
			String orgCfg = FileUtils.readFileToString(cfg, BaseConstants.UTF8);
			String adaptedCfg = orgCfg
					.replace(
							PLACEHOLDER_DATA_FILE_DIRECTORIES,
							Directories.getExistingDatabaseDir().getAbsolutePath() + File.separator + nodeName)
					.replace(
							PLACEHOLDER_COMMITLOG_DIRECTORY,
							Directories.getExistingDatabaseDir().getAbsolutePath() + File.separator + nodeName + File.separator +
									"commitlog")
					.replace(
							PLACEHOLDER_HINT_FILE_DIRECTORIES,
							Directories.getExistingDatabaseDir().getAbsolutePath() + File.separator + nodeName + File.separator +
									"hints")
					.replace(
							PLACEHOLDER_CDC_RAW_FILE_DIRECTORIES,
							Directories.getExistingDatabaseDir().getAbsolutePath() + File.separator + nodeName + File.separator +
									"cdc")
					.replace(PLACEHOLDER_SAVE_CACHES_DIRECTORY,
							Directories.getExistingDatabaseDir().getAbsolutePath() + File.separator + nodeName + File.separator +
									"saved_caches")
					.replace(PLACEHOLDER_SEED,
							getSeedIp())
					.replace(PLACEHOLDER_LISTEN_ADDRESS,
							host)
					.replace(PLACEHOLDER_RPC_ADRESS,
							host)
					.replace(PLACEHOLDER_INITIAL_TOKEN, token.getTokenForHost(host));

			FileUtils.write(new File(EASYTRAVEL_CONFIG_PATH, adaptedconf), adaptedCfg, BaseConstants.UTF8);
		} catch (FileNotFoundException e) {
			throw new ConfigurationException("Could not adapt cassandra config", e);
		} catch (IOException e) {
			throw new ConfigurationException("Could not adapt cassandra config", e);
		}
	}

	private CharSequence getSeedIp() throws ConfigurationException {
		String[] nodes = CONFIG.cassandraNodeAddresses;
		if (ArrayUtils.isEmpty(nodes)) {
			throw new ConfigurationException(
					"You must provide at least one cassandra node address in the easyTravelConfig! (property name: cassandraNodeAddresses)");
		}

		String seed = nodes[0];
		try {
			String seedIp = UrlUtils.resolveAddress(seed);
			if (log.isLoggable(Level.FINE)) {
				log.fine("Found following Cassandra seed: " + seed);
			}
			return seedIp;
		} catch (UnknownHostException e) {
			throw new ConfigurationException(
					TextUtils.merge(
							"The seed address (the first element in ''cassandraNodeAddresses'', values) is not valid, it must be either an IP address or a valid host name",
							nodes[0]));
		}

	}

	public CassandraProcedure(final ProcedureMapping mapping) throws CorruptInstallationException {
		super(mapping);
		configure();
	}

	@Override
	protected String[] getJavaOpts() {
		if (reservation == null) {
			try {
				this.reservation = CassandraReservation.reserveLocalHost();
			} catch (IOException e) {
				throw new IllegalStateException("Could not reserve a local host for the Cassandra instance", e);
			}

			this.nodeNo = getNodeNo(reservation);
			this.nodeName = "node" + nodeNo;


			if (shouldUseExternalIp(CONFIG)) {
				this.host = getHost(CONFIG);
			} else {
				// if several nodes are running on a single machine
				this.host = reservation.getLocalHostIp();
			}

			this.adaptedconf = TextUtils.merge("cassandra_{0}.yaml", nodeName);
			this.logPath = Joiner.on(File.separator).join(
					Directories.getLogDir().getAbsolutePath(),
					"cassandra_" + nodeName + ".log");
		}
		String[] javaopts = CONFIG.javaopts;
		CassandraArgBuilder argBuilder = new CassandraArgBuilder()
				.add(CONFIG.cassandraJavaopts)
				.add(javaopts)
				.add("cassandra.config", TextUtils.merge("file:///{0}", new File(EASYTRAVEL_CONFIG_PATH,
						adaptedconf).getAbsolutePath()))
				.add("log4j.configuration", TextUtils.merge("file:///{0}",
						new File(EASYTRAVEL_CONFIG_PATH,
						getLog4jPropFileName()
						).getAbsolutePath()))
				.add("cassandra.rpc_port", 9160)
				.add("cassandra.storage_port", 7000)
				.add("com.sun.management.jmxremote.port", 7198 + nodeNo)
				.add("com.sun.management.jmxremote.local.only", false)
				.add("com.sun.management.jmxremote.authenticate", false)
				.add("com.sun.management.jmxremote.ssl", false)
				.add("cassandra.start_rpc", true);

		log.info(format("Starting cassandra node '%d' with following properties: %s", nodeNo, argBuilder));
		return argBuilder.build();
	}

	// we calculate the node number by the last character of its IP, e.g. if the node has the assigned IP 127.0.0.2 it gets
	// the node no 2
	private int getNodeNo(CassandraReservation reservation) {
		return Character.getNumericValue(reservation.getLocalHostIp().charAt(reservation.getLocalHostIp().length() - 1));
	}

	private boolean shouldUseExternalIp(final EasyTravelConfig config) {
		return reservation.isDefaultHostIp()
				&& !Lists.newArrayList(config.cassandraNodeAddresses).contains(CassandraReservation.DEFAULT_HOST);
	}

	private void releaseLocalhost() {
		if (reservation == null) {
			log.warning("Trying to release localhost IP that have not been reserved.");
			return;
		}

		// release reserved ports
		reservation.release();
	}

	private String getHost(EasyTravelConfig config) {
		if (getMapping().getSettingValue(CassandraArgument.HOST) != null) {
			host = getMapping().getSettingValue(CassandraArgument.HOST);
			if (log.isLoggable(Level.FINE))
				log.fine(TextUtils.merge("Found custom settings for Cassandra node - host: {0}; port: {1}", host,
						host));
			return host;
		}

		try {
			return InetAddress.getLocalHost().getHostAddress();	// NOSONAR - we don't care too much about multi-home machines here
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	protected DtAgentConfig getAgentConfig() {
		return new DynamicPortDtAgentConfig(this, CONFIG.cassandraSystemProfile, CONFIG.cassandraAgent, CONFIG.cassandraAgentOptions,
				CONFIG.cassandraEnvArgs);
	}

	@Override
	public Feedback run() {
		Feedback feedback = super.run();
		// wait until the Cassandra node can handle request, so we enforce that the Cassandra nodes are started step by step -
		// Cassandra does not like concurrent startup
		if (!feedback.isOk() || !waitUntilCanHandleRequests(CASSANDRA_TIMEOUT_MS, CONFIG.processRunningCheckInterval)) {
			log.log(Level.SEVERE, "Waiting for Cassandra Node on host " + host + " timed out, process startup reported: " + feedback + ".");
			return Feedback.Failure;
		}

		return Feedback.Success;
	}


	@Override
	public Feedback stop() {
		try {
			log.log(Level.INFO, "Stopping procedures. Stopping Cassandra.");
			Feedback feedback = super.stop();
			log.log(Level.INFO, "Stopping procedures. Cassandra stopped.");
			return feedback;
		} finally {
			releaseLocalhost();
		}
	}

	private boolean waitUntilCanHandleRequests(int timeoutMs, int intervalMs) {
		return new PredicateMatcher<CassandraProcedure>(this, timeoutMs, intervalMs).waitForMatch(new Predicate<CassandraProcedure>() {

			@Override
			public boolean eval(CassandraProcedure procedure) {
				return procedure.isOperating();
			}

			@Override
			public boolean shouldStop() {
				return false;
			}
		});
	}


	@Override
	public boolean isOperatingCheckSupported() {
		return true;
	}

	@Override
	public boolean isOperating() {
		return CassandraUtils.isNodeAvailable(host, Cassandra.DEFAULT_RPC_PORT);
	}

	@Override
	public boolean hasLogfile() {
		return true;
	}

	@Override
	public String getLogfile() {
		return logPath;
	}

	@Override
	protected String getModuleJar() {
		return Constants.Modules.CASSANDRA;
	}

	@Override
	protected String getWorkingDir() {
		return BaseConstants.SubDirectories.CASSANDRA;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public String getPortPropertyName() {
		return CassandraArgument.PORT;
	}

	@Override
	public StopMode getStopMode() {
		return StopMode.PARALLEL;
	}

	private class CassandraArgBuilder {

		private List<String> arguments = Lists.newArrayListWithExpectedSize(12);

		CassandraArgBuilder add(String[] arguments) {
			this.arguments.addAll(Arrays.asList(arguments));
			return this;
		}

		CassandraArgBuilder add(String name, Object value) {
			arguments.add(asArgumentStr(name, value));
			return this;
		}

		String[] build() {
			return arguments.toArray(new String[arguments.size()]);
		}

		private String asArgumentStr(String name, Object value) {
			return TextUtils.merge("-D{0}={1}", name, String.valueOf(value));
		}

		@Override
		public String toString() {
			return "Cassandra Arguments: " + arguments;
		}
	}

	/**
	 * To prevent long lasting token synchronization between the nodes and token collision, we use
	 * precomputed initial tokens.
	 *
	 * @author stefan.moschinski
	 */
	static class CassandraToken {

		private static final String ZERO = "0";
		private static final Map<Integer, List<String>> NODE_NO_TO_TOKEN_MAPPING;

		static {
			NODE_NO_TO_TOKEN_MAPPING = Maps.newHashMapWithExpectedSize(6);

			NODE_NO_TO_TOKEN_MAPPING.put(1, Lists.newArrayList(ZERO));

			NODE_NO_TO_TOKEN_MAPPING.put(2, Lists.newArrayList(ZERO,
					"85070591730234615865843651857942052864"));

			NODE_NO_TO_TOKEN_MAPPING.put(3, Lists.newArrayList(ZERO,
					"56713727820156410577229101238628035242",
					"113427455640312821154458202477256070485"));

			NODE_NO_TO_TOKEN_MAPPING.put(4, Lists.newArrayList(ZERO,
					"42535295865117307932921825928971026432",
					"85070591730234615865843651857942052864",
					"127605887595351923798765477786913079296"));

			NODE_NO_TO_TOKEN_MAPPING.put(5, Lists.newArrayList(ZERO,
					"34028236692093846346337460743176821145",
					"68056473384187692692674921486353642291",
					"102084710076281539039012382229530463436",
					"136112946768375385385349842972707284582"));

			NODE_NO_TO_TOKEN_MAPPING.put(6, Lists.newArrayList(ZERO,
					"28356863910078205288614550619314017621",
					"56713727820156410577229101238628035242",
					"85070591730234615865843651857942052864",
					"113427455640312821154458202477256070485",
					"141784319550391026443072753096570088106"));

		}

		private final Map<String, String> hostToTokenMapping;

		CassandraToken(String... cassandraNodes) {
			String[] nodes = cassandraNodes == null ? new String[] {} : cassandraNodes;
			hostToTokenMapping = mapNodesToTokens(NODE_NO_TO_TOKEN_MAPPING, nodes);
		}

		private HashMap<String, String> mapNodesToTokens(Map<Integer, List<String>> mapping, String[] nodes) {
			HashMap<String, String> hostToToken = Maps.newHashMapWithExpectedSize(nodes.length);
			if (nodes.length == 0 || nodes.length > NODE_NO_TO_TOKEN_MAPPING.size()) {
				log.warning(format("The given Cassandra node number '%d' is not supported - using empty initial tokens",
						nodes.length));
				return hostToToken;
			}

			Iterator<String> token = mapping.get(nodes.length).iterator();
			for (String node : nodes) {
				hostToToken.put(node, token.next());
			}
			return hostToToken;
		}

		String getTokenForHost(String host) {
			String token = hostToTokenMapping.get(host);
			return token == null ? BaseConstants.EMPTY_STRING : token;
		}



	}

	/**
	 * Allow to reduce wait time for testing
	 *
	 * @param timeoutMS
	 */
	@TestOnly
	public static void setStartupTimeout(int timeoutMS) {
		CASSANDRA_TIMEOUT_MS = timeoutMS;
	}
}
