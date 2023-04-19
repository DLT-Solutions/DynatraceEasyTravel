package com.dynatrace.easytravel.launcher.engine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.procedures.*;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.vagrant.VagrantProcedure;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * Factory to create procedure instances.
 *
 * @author martin.wurzinger
 */
public class ProcedureFactory {

	private static final Logger LOGGER = Logger.getLogger(ProcedureFactory.class.getName());

	/**
	 * Create a procedure out of a {@link ProcedureMapping} instance.
	 *
	 * @param mapping
	 *            the mapping of a procedure to create
	 * @return the new instantiated procedure or <code>null</code> if procedure
	 *         could not be installed
	 * @author martin.wurzinger
	 */
	public Procedure create(ProcedureMapping mapping) {
		return create(mapping, true);
	}

	/**
	 * Create a procedure out of a {@link ProcedureMapping} instance.
	 *
	 * @param mapping
	 *            the mapping of a procedure to create
	 * @param allowRemote
	 *            defines if the procedure can be started on a remote host if
	 *            specified via system properties.
	 * @return the new instantiated procedure or <code>null</code> if procedure
	 *         could not be installed
	 * @author dominik.stadler
	 */
	public Procedure create(ProcedureMapping mapping, boolean allowRemote) {
		if (mapping == null) {
			LOGGER.warning("No procedure mappings specified. Factory is unable to create procedures.");
			return null;
		}

		String mappingId = mapping.getId();
		if (mappingId == null || mappingId.isEmpty()) {
			LOGGER.warning("Invalid ID of procedure mapping. Factory is unable to create procedures.");
			return null;
		}

		try {
			return createInternal(mapping, allowRemote);
		} catch (CorruptInstallationException e) {
			LOGGER.log(Level.SEVERE,
					TextUtils.merge(
							"The {0} installation is corrupt. Unable to create procedure for mapping ''{1}'': {2}.",
							BaseConstants.EASYTRAVEL, mappingId, e.getMessage()));
			return null;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, TextUtils.merge("Unable to instantiate {0}.", mapping.getId()), e);
			return null;
		}
	}

	private Procedure createInternal(ProcedureMapping mapping, boolean allowRemote)
			throws CorruptInstallationException, IOException {
		final String mappingId = mapping.getId();

		// create a remote procedure if the host is set and is different from
		// localhost
		if (isRemote(mapping) && allowRemote) {
			String host = mapping.getHost();
			if (host == null) {
				host = getHost(mapping);
			}

			LOGGER.info("Creating remote procedure for '" + mappingId + "' to run on host '" + host + "'");
			return new RemoteProcedure(mapping, host);
		}

		if (Constants.Procedures.BUSINESS_BACKEND_ID.equalsIgnoreCase(mappingId)) {
			return new BusinessBackendProcedure(mapping);
		} else if (Constants.Procedures.CUSTOMER_FRONTEND_ID.equalsIgnoreCase(mappingId)) {
			return new CustomerFrontendProcedure(mapping);
		} else if (Constants.Procedures.ANGULAR_FRONTEND_ID.equalsIgnoreCase(mappingId)) {
			return new AngularFrontendProcedure(mapping);
		} else if (Constants.Procedures.INPROCESS_DBMS_ID.equalsIgnoreCase(mappingId)) {
			return new DbmsProcedure(mapping);
		} else if (Constants.Procedures.INPROCESS_MYSQL_ID.equalsIgnoreCase(mappingId)) {
			return new MysqlProcedure(mapping);
		} else if (Constants.Procedures.DATABASE_CONTENT_CREATOR_ID.equalsIgnoreCase(mappingId)) {
			return new DatabaseContentCreationProcedure(mapping);
		} else if (Constants.Procedures.COUCHDB_CONTENT_CREATOR_ID.equalsIgnoreCase(mappingId)) {
			return new CouchDBContentCreationProcedure(mapping);
		} else if (Constants.Procedures.COUCHDB_ID.equalsIgnoreCase(mappingId)) {
			return new CouchDBProcedure(mapping);
		} else if (Constants.Procedures.MYSQL_CONTENT_CREATOR_ID.equalsIgnoreCase(mappingId)) {
			return new MysqlContentCreationProcedure(mapping);
		} else if (Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID.equalsIgnoreCase(mappingId)) {
			return new CreditCardAuthorizationProcedure(mapping);
		} else if (Constants.Procedures.PAYMENT_BACKEND_ID.equalsIgnoreCase(mappingId)) {
			return new PaymentBackendProcedure(mapping);
		} else if (Constants.Procedures.B2B_FRONTEND_ID.equalsIgnoreCase(mappingId)) {
			return new B2BFrontendProcedure(mapping);
		} else if (Constants.Procedures.ANT_ID.equalsIgnoreCase(mappingId)) {
			return new AntProcedure(mapping);
		} else if (Constants.Procedures.APACHE_HTTPD_ID.equalsIgnoreCase(mappingId)) {
			return new ApacheHttpdProcedure(mapping);
		} else if (Constants.Procedures.NGINX_WEBSERVER_ID.equalsIgnoreCase(mappingId)) {
			return new NginxWebserverProcedure(mapping);
		} else if (Constants.Procedures.CASSANDRA_ID.equalsIgnoreCase(mappingId)) {
			return new CassandraProcedure(mapping);
		} else if (Constants.Procedures.MONGO_DB_ID.equalsIgnoreCase(mappingId)) {
			return new MongoDbProcedure(mapping);
		} else if (Constants.Procedures.HBASE_ID.equalsIgnoreCase(mappingId)) {
			return new HbaseProcedure(mapping);
		} else if (Constants.Procedures.APACHE_HTTPD_PHP_ID.equalsIgnoreCase(mappingId)) {
			return new ApacheHttpdPhpProcedure(mapping);
		} else if (Constants.Procedures.THIRDPARTY_SERVER_ID.equalsIgnoreCase(mappingId)) {
			return new ThirdPartyContentServerProcedure(mapping);
		} else if (Constants.Procedures.BROWSER_ID.equalsIgnoreCase(mappingId)) {
			return new BrowserProcedure(mapping);
		} else if (Constants.Procedures.WEBSERVER_AGENT_RESTART_ID.equalsIgnoreCase(mappingId)) {
			return new WebserverAgentControlProcedure(mapping);
		} else if (Constants.Procedures.HOST_AGENT_RESTART_ID.equalsIgnoreCase(mappingId)) {
			return new HostAgentControlProcedure(mapping);
		} else if (Constants.Procedures.PLUGIN_SERVICE.equalsIgnoreCase(mappingId)) {
			return new PluginServiceProcedure(mapping);
		} else if (Constants.Procedures.PREPARE_VMWARE_ID.equalsIgnoreCase(mappingId)) {
			return new PrepareVMwareProcedure(mapping);
		} else if (Constants.Procedures.VMOTION_ID.equalsIgnoreCase(mappingId)) {
			return new VMotionProcedure(mapping);
		} else if (Constants.Procedures.VAGRANT_ID.equalsIgnoreCase(mappingId)) {
			return new VagrantProcedure(mapping);
		}

		return null;
	}

	/**
	 * Determine the name of the given procedure
	 *
	 * @param mapping
	 * @return
	 * @author dominik.stadler
	 */
	public static String getNameOfProcedure(ProcedureMapping mapping) {
		final String mappingId = mapping.getId();

		if (Constants.Procedures.BUSINESS_BACKEND_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_BUSINESS_BACKEND;
		} else if (Constants.Procedures.CUSTOMER_FRONTEND_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_CUSTOMER_FRONTEND;
		} else if (Constants.Procedures.ANGULAR_FRONTEND_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_ANGULAR_FRONTEND;
		} else if (Constants.Procedures.INPROCESS_DBMS_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_DERBY_DATABASE_MANAGEMENT_SYSTEM;
		} else if (Constants.Procedures.INPROCESS_MYSQL_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_MYSQL_DATABASE_MANAGEMENT_SYSTEM;
		} else if (Constants.Procedures.DATABASE_CONTENT_CREATOR_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_DATABASE_CONTENT_CREATOR;
		} else if (Constants.Procedures.COUCHDB_CONTENT_CREATOR_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_COUCHDB_CONTENT_CREATOR;
		} else if (Constants.Procedures.COUCHDB_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_COUCHDB_CONTROLLER;
		} else if (Constants.Procedures.MYSQL_CONTENT_CREATOR_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MOUDLE_MYSQL_CONTENT_CREATOR;
		} else if (Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_CREDITCARD_AUTHORIZATION;
		} else if (Constants.Procedures.PAYMENT_BACKEND_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_PAYMENT_BACKEND;
		} else if (Constants.Procedures.B2B_FRONTEND_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_B2B_FRONTEND;
		} else if (Constants.Procedures.ANT_ID.equalsIgnoreCase(mappingId)) {
			String target = mapping.getSettingValue(AntProcedure.TARGET);
			return AntProcedure.getTitle(mapping, target);
		} else if (Constants.Procedures.APACHE_HTTPD_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_APACHE_HTTPD;
		} else if (Constants.Procedures.NGINX_WEBSERVER_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_NGINX;
		} else if (Constants.Procedures.UNHOOKABLE_HTTP_SERVER_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_UNHOOKABLE_HTTPD;
		} else if (Constants.Procedures.CASSANDRA_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_CASSANDRA;
		} else if (Constants.Procedures.MONGO_DB_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_MONGO_DB;
		} else if (Constants.Procedures.HBASE_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_HBASE;
		} else if (Constants.Procedures.APACHE_HTTPD_PHP_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MOULE_APACHE_HTTPD_PHP;
		} else if (Constants.Procedures.THIRDPARTY_SERVER_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_THIRDPARTY_SERVER;
		} else if (Constants.Procedures.BROWSER_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_BROWSER;
		} else if (Constants.Procedures.WEBSERVER_AGENT_RESTART_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_WEBSERVER_AGENT_RESTART;
		} else if (Constants.Procedures.HOST_AGENT_RESTART_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_HOST_AGENT_RESTART;
		} else if (Constants.Procedures.PLUGIN_SERVICE.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_PLUGIN_SERVICE;
		} else if (Constants.Procedures.PREPARE_VMWARE_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_PREPARE_VMWARE;
		} else if (Constants.Procedures.VMOTION_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_VMOTION;
		} else if (Constants.Procedures.VAGRANT_ID.equalsIgnoreCase(mappingId)) {
			return MessageConstants.MODULE_VAGRANT;
		}

		LOGGER.warning("Unknown procedure mapping id encountered: " + mappingId);
		return MessageConstants.UNKNOWN;
	}

	/**
	 * Helper method which reads system properties to find out if we have a
	 * remote ip address for this procedure mapping
	 *
	 * Currently supported system properties are:
	 *
	 * "com.dynatrace.easytravel.host.business_backend",
	 * "com.dynatrace.easytravel.host.credit_card_authorization",
	 * "com.dynatrace.easytravel.host.customer_frontend",
	 * "com.dynatrace.easytravel.host.database_content_creator",
	 * "com.dynatrace.easytravel.host.inprocess_dbms",
	 * "com.dynatrace.easytravel.host.payment_backend",
	 * "com.dynatrace.easytravel.host.b2b_frontend",
	 * "com.dynatrace.easytravel.host.ant",
	 * "com.dynatrace.easytravel.host.uemload"
	 * "com.dynatrace.easytravel.host.apache_httpd"
	 * "com.dynatrace.easytravel.host.cassandra"
	 * "com.dynatrace.easytravel.host.nginx"
	 *
	 * @param mappingId
	 * @return
	 * @author dominik.stadler
	 */
	private static String getHost(String mappingId) {
		// no host without mapping id
		if (mappingId == null) {
			return null;
		}

		String propertyName = getSystemProperty(mappingId);

		LOGGER.fine("Checking if procedure should be started on a remote host, looking for system property '"
				+ propertyName + "'");
		String property = System.getProperty(propertyName);

		if (property != null) {
			property = property.toLowerCase();
			LOGGER.info("Found host '" + property + "' for procedure '" + mappingId + "' via host-property '"
					+ propertyName + "'");
		}

		return property;
	}

	/**
	 * Returns the system property that is queried for the given procedure id
	 * taken from {@link Constants.Procedures}
	 *
	 * @param mappingId
	 *            An id for one the available procedures.
	 * @return A system property key that is queried for the remote host for
	 *         this procedure type.
	 */
	public static String getSystemProperty(String mappingId) {
		return "com.dynatrace.easytravel.host." + mappingId.replace(" ", "_").toLowerCase();
	}

	/**
	 * Determines the host to execute the given mapping on.<br />
	 * <br />
	 * If the mapping itself has no host defined for execution,
	 * {@link #getHost(String)} will deliver the result based on System Property
	 * values.
	 *
	 * @param mapping
	 *            the procedure mapping to determine the host to execute it on
	 * @return the host to execute the procedure on or <tt>null</tt> if it is
	 *         supposed to get executed locally
	 *
	 * @author cwat-rpilz
	 */
	private static String getHost(ProcedureMapping mapping) {
		if (mapping == null) {
			return null;
		}
		String host = mapping.getHost();
		if (host != null) {
			return host;
		}
		return getHost(mapping.getId());
	}

	/**
	 * Returns a list of all known remote hosts where we can start procedures.
	 *
	 * @return
	 * @author dominik.stadler
	 */
	public static Collection<String> getAllRemoteHosts() {
		// use a Set to have each host only listed once, use TreeMap to get it
		// sorted by name
		Set<String> hosts = new TreeSet<String>();
		for (String proc : Constants.Procedures.ALL_REMOTE) {
			if (isRemote(proc)) {
				hosts.add(getHost(proc));
			}
		}

		// allow to specify other hosts via special system property
		String property = System.getProperty("com.dynatrace.easytravel.host.additional");
		if (property != null) {
			String[] properties = property.split(",");
			for (String host : properties) {
				hosts.add(host);
			}
		}

		// don't return empty strings
		hosts.remove(BaseConstants.EMPTY_STRING);

		return hosts;
	}

	/**
	 * Should this procedure be started on a remote machine?
	 *
	 * @param mappingId
	 * @return
	 * @author dominik.stadler
	 */
	public static boolean isRemote(String mappingId) {
		return isHostRemote(getHost(mappingId));
	}

	private static boolean isHostRemote(String host) {
		InetAddress localHost = getLocalHost(); // NOSONAR

		if (localHost == null) {
			return host != null && !"localhost".equals(host) && !"127.0.0.1".equals(host);
		}

		return host != null && !"localhost".equals(host) && !"127.0.0.1".equals(host)
				&& !localHost.getHostAddress().equals(host) && !localHost.getHostName().equalsIgnoreCase(host)
				&& !localHost.getCanonicalHostName().equalsIgnoreCase(host);
	}

	/**
	 * Get local host
	 *
	 * @return InetAddress
	 */
	private static InetAddress getLocalHost() {
		try {
			return InetAddress.getLocalHost(); // NOSONAR
		} catch (UnknownHostException e) {
			LOGGER.log(Level.SEVERE, "Cannot get localhost", e);
			return null;
		}
	}

	/**
	 * Determines if the given procedure is supposed to get executed on a remote
	 * machine or locally.
	 *
	 * @param mapping
	 *            the mapping for the procedure to check for remote execution
	 * @return <tt>true</tt> if this procedure is supposed to get launched on a
	 *         remote machine, <tt>false</tt> otherwise.
	 *
	 * @author cwat-rpilz
	 */
	public static boolean isRemote(ProcedureMapping mapping) {
		if (mapping == null) {
			return false;
		}
		String host = mapping.getHost();
		if (host != null) {
			return isHostRemote(host);
		}
		return isRemote(mapping.getId());
	}

	/**
	 * for a specific mapping get the host the procuedure runs at --> if mapping
	 * is not null host returned is guaranteed not to be null
	 *
	 * @param mappingId
	 * @return
	 * @author peter.kaiser
	 */
	public static String getHostOrLocal(String mappingId) {
		if (mappingId == null) {
			throw new IllegalArgumentException("mapping must not be null");
		}
		String host = getHost(mappingId);
		if (StringUtils.isEmpty(host)) {
			return "localhost";
		}
		return host;
	}

	/**
	 * Get the master-launcher host
	 *
	 * @return
	 * @author cwpl-rorzecho
	 */
	public static String getMasterLauncherHost() {
		String webLauncher = System.getProperty("config.internalDatabaseHost");
		if (webLauncher != null) {
			return webLauncher;
		} else {
			return "<not specified>";
		}
	}

}
