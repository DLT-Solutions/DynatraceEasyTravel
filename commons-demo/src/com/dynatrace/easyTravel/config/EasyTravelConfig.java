package com.dynatrace.easytravel.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.SystemProperties;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.ConfigurationProvider;
import com.dynatrace.easytravel.util.MvelUtils;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.MoreObjects;

import ch.qos.logback.classic.Logger;

/**
 * The EasyTravel central configuration comes here.
 *
 * @author philipp.grasboeck
 *
 */
public class EasyTravelConfig implements UEMLoadConfig {

	private static final Logger LOGGER = LoggerFactory.make();

	private static final String PROPERTIES_SUFFIX = ".properties";
	private static final String NAMESPACE = "config";
	public static final String PROPERTIES_FILE = "easyTravelConfig";
	public static final String LOCAL_PROPERTIES_FILE = "easyTravelLocal.properties";
	public static final String PRIVATE_PROPERTIES_FILE = "easyTravelPrivate.properties";
	public static final String WAR_PROPERTIES_FILE = "easyTravelWarConfig.properties";

	public static final Map<?, ?> NONE_CUSTOM_FIELD_TO_LOAD = null;

	private static final AtomicReference<EasyTravelConfig> SINGLETON = new AtomicReference<EasyTravelConfig>(null);
	private static final AtomicReference<EasyTravelConfig> OUTDATED_SINGLETON = new AtomicReference<EasyTravelConfig>(null);

	private static boolean localConfigFileLogged = false;
	private static boolean privateConfigFileLogged = false;
	private static boolean warConfigFileLogged = false;

	public String agent;		// NOSONAR - public on purpose
	public String autostart;		// NOSONAR - public on purpose
	public String autostartGroup;		// NOSONAR - public on purpose

	public String clusterNode;		// NOSONAR - public on purpose

	public String frontendAgent;		// NOSONAR - public on purpose
	public String frontendSystemProfile;		// NOSONAR - public on purpose
	public String[] frontendAgentOptions = {};		// NOSONAR - public on purpose
	public String[] frontendJavaopts;		// NOSONAR - public on purpose
	public String[] frontendEnvArgs = {};		// NOSONAR - public on purpose

	public String angularFrontendAgent;		// NOSONAR - public on purpose
	public String angularFrontendSystemProfile;		// NOSONAR - public on purpose
	public String[] angularFrontendAgentOptions = {};		// NOSONAR - public on purpose
	public String[] angularFrontendJavaopts;		// NOSONAR - public on purpose
	public String[] angularFrontendEnvArgs = {};		// NOSONAR - public on purpose

	public String antAgent;		// NOSONAR - public on purpose
	public String antSystemProfilePrefix;		// NOSONAR - public on purpose
	public String[] antAgentOptions = {};		// NOSONAR - public on purpose
	public int antForkHttpService;		// NOSONAR - public on purpose
	public String[] antJavaopts = {};		// NOSONAR - public on purpose
	public String[] antEnvArgs = {};		// NOSONAR - public on purpose

	public int backendPort;		// NOSONAR - public on purpose
	public String backendHost;		// NOSONAR - public on purpose
	public int backendShutdownPort;		// NOSONAR - public on purpose
	public String backendAgent;		// NOSONAR - public on purpose
	public String backendSystemProfile;		// NOSONAR - public on purpose
	public String[] backendAgentOptions = {};		// NOSONAR - public on purpose
	public String[] backendJavaopts;		// NOSONAR - public on purpose
	public String[] backendEnvArgs = {};		// NOSONAR - public on purpose
	public String webServiceBaseDir;		// NOSONAR - public on purpose
	public String thirdPartyWebserviceUri;		// NOSONAR - public on purpose
	public long backendCPUCycleTime;		// NOSONAR - public on purpose
	public long cpuLoadJourneyServiceWaitTime;		// NOSONAR - public on purpose
	public double CPUCalibration;	// NOSONAR - public on purpose

	public int thirdpartyPort;		// NOSONAR - public on purpose
	public String thirdpartyHost;		// NOSONAR - public on purpose
	public int thirdpartyShutdownPort;		// NOSONAR - public on purpose
	public String thirdpartyWebappBase;		// NOSONAR - public on purpose
	public String[] thirdpartyJavaopts;		// NOSONAR - public on purpose
	public String[] thirdpartyEnvArgs = {};		// NOSONAR - public on purpose
	public String thirdpartyUrl;		// NOSONAR - public on purpose
	public String thirdpartyContextRoot;		// NOSONAR - public on purpose

	// settings for PP: Unhealthy Third Party Service
	public boolean thirdpartyCaching;		// NOSONAR - public on purpose
	public String thirdpartyCdnHost;		// NOSONAR - public on purpose
	public String thirdpartyCdnUrl;		// NOSONAR - public on purpose
	public String thirdpartySocialMediaHost;		// NOSONAR - public on purpose
	public String thirdpartySocialMediaUrl;		// NOSONAR - public on purpose

	public String creditCardAuthorizationAgent;		// NOSONAR - public on purpose
	public String creditCardAuthorizationSystemProfile;		// NOSONAR - public on purpose
	public String[] creditCardAuthorizationAgentOptions = {};		// NOSONAR - public on purpose
	public String[] creditCardAuthorizationEnvArgs = {};		// NOSONAR - public on purpose
	public int creditCardAuthorizationSocketPort = 1101;		// NOSONAR - public on purpose // need to pass ET_PORT to native binary if this is changed!
	public String creditCardAuthorizationHost;		// NOSONAR - public on purpose

	public String paymentBackendServer;		// NOSONAR - public on purpose
	public String paymentBackendServerIIS;		// NOSONAR - public on purpose
	public String paymentBackendDir;		// NOSONAR - public on purpose
	public String paymentBackendSystemProfile;		// NOSONAR - public on purpose
	public String paymentBackendPageToIdentify;		// NOSONAR - public on purpose
	public String[] paymentBackendAgentOptions;		// NOSONAR - public on purpose
	public String[] paymentBackendEnvArgs;		// NOSONAR - public on purpose
	public String paymentBackendHost;		// NOSONAR - public on purpose
	public int paymentBackendPort;		// NOSONAR - public on purpose

	public String b2bFrontendServer;		// NOSONAR - public on purpose
	public String b2bFrontendServerIIS;		// NOSONAR - public on purpose
	public String b2bFrontendDir;		// NOSONAR - public on purpose
	public String b2bFrontendSystemProfile;		// NOSONAR - public on purpose
	public String b2bFrontendPageToIdentify;		// NOSONAR - public on purpose
	public String[] b2bFrontendAgentOptions;		// NOSONAR - public on purpose
	public String[] b2bFrontendEnvArgs;		// NOSONAR - public on purpose
	public int b2bFrontendPortRangeStart;		// NOSONAR - public on purpose
	public int b2bFrontendPortRangeEnd;		// NOSONAR - public on purpose
	public String b2bFrontendPublicUrl;		// NOSONAR - public on purpose

	public String webappBase;		// NOSONAR - public on purpose

	public int frontendPortRangeStart;		// NOSONAR - public on purpose
	public int frontendPortRangeEnd;		// NOSONAR - public on purpose
	public int frontendShutdownPortRangeStart;		// NOSONAR - public on purpose
	public int frontendShutdownPortRangeEnd;		// NOSONAR - public on purpose
	public int frontendAjpPortRangeStart;		// NOSONAR - public on purpose
	public int frontendAjpPortRangeEnd;		// NOSONAR - public on purpose
	public String frontendPublicUrl;		// NOSONAR - public on purpose
	public String frontendContextRoot;		// NOSONAR - public on purpose

	public int angularFrontendPortRangeStart;		// NOSONAR - public on purpose
	public int angularFrontendPortRangeEnd;		// NOSONAR - public on purpose
	public int angularFrontendShutdownPortRangeStart;		// NOSONAR - public on purpose
	public int angularFrontendShutdownPortRangeEnd;		// NOSONAR - public on purpose
	public int angularFrontendAjpPortRangeStart;		// NOSONAR - public on purpose
	public int angularFrontendAjpPortRangeEnd;		// NOSONAR - public on purpose
	public int angularFrontendApachePort;		// NOSONAR - public on purpose
	public String angularFrontendPublicUrl;		// NOSONAR - public on purpose
	public String angularFrontendContextRoot;		// NOSONAR - public on purpose
	public String angularOpenTelemetryForwardUrl;	// NOSONAR - public on purpose

	public boolean backendMultiEnabled;		// NOSONAR - public on purpose
	public int backendPortRangeStart;		// NOSONAR - public on purpose
	public int backendPortRangeEnd;		// NOSONAR - public on purpose
	public int backendShutdownPortRangeStart;		// NOSONAR - public on purpose
	public int backendShutdownPortRangeEnd;		// NOSONAR - public on purpose
	public int backendAjpPortRangeStart;		// NOSONAR - public on purpose
	public int backendAjpPortRangeEnd;		// NOSONAR - public on purpose

	public String backendContextRoot;		// NOSONAR - public on purpose

	public long minSessionLifetimeInMillis;		// NOSONAR - public on purpose

	public boolean internalDatabaseEnabled;		// NOSONAR - public on purpose
	public int internalDatabasePort;		// NOSONAR - public on purpose
	public String internalDatabaseHost;		// NOSONAR - public on purpose

	public String databaseDriver;		// NOSONAR - public on purpose
	public String databaseUrl;		// NOSONAR - public on purpose
	public String databaseUser;		// NOSONAR - public on purpose
	public String databasePassword;		// NOSONAR - public on purpose

	public int mysqlPort;		// NOSONAR - public on purpose
	public String mysqlDriver;		// NOSONAR - public on purpose
	public String mysqlHost;		// NOSONAR - public on purpose
	public String mysqlUrl;		// NOSONAR - public on purpose
	public String mysqlUser;		// NOSONAR - public on purpose
	public String mysqlPassword;		// NOSONAR - public on purpose
	public String mysqlServer;		// NOSONAR - public on purpose
	public boolean mysqlGeneratedMyIniConfig;		// NOSONAR - public on purpose

	public String dtServer;		// NOSONAR - public on purpose
	public String dtServerWebPort;		// NOSONAR - public on purpose
	public String dtServerPort;		// NOSONAR - public on purpose
	public String dtServerWebURL;		// NOSONAR - public on purpose
	public String dtClientWebURL;		// NOSONAR - public on purpose
	public String dtServerUsername;		// NOSONAR - public on purpose
	public String dtServerPassword;		// NOSONAR - public on purpose

	public String apmServerHost;		// NOSONAR - public on purpose
	public String apmServerWebPort;		// NOSONAR - public on purpose
	public String apmServerPort;		// NOSONAR - public on purpose
	public String apmServerUsername;		// NOSONAR - public on purpose
	public String apmServerPassword;		// NOSONAR - public on purpose
	public String apmServerWebURL;		// NOSONAR - public on purpose
	public String apmServerProtocol;    // NOSONAR - public on purpose
	public String apmTenant; 			// NOSONAR - public on purpose
	public String apmTenantToken;		// NOSONAR - public on purpose

	public boolean openKitTrustAllCertificates; // NOSONAR - public on purpose
	public String iotBeaconUrl;			// NOSONAR - public on purpose
	public String iotRentalCarsAppId;				// NOSONAR - public on purpose

	public String mobileBeaconUrl;			// NOSONAR - public on purpose
	public String etMobileAppId;				// NOSONAR - public on purpose
	public boolean openKitMobileLoadGenerator;				// NOSONAR - public on purpose
	public boolean openKitDisableCrashReports;

	public InstallationType apmServerDefault = InstallationType.APM;		// NOSONAR - public on purpose

	public String[] javaopts = {};		// NOSONAR - public on purpose

	public String dotNetBackendWebServiceBaseDir;		// NOSONAR - public on purpose

	public ServiceStubStrategy serviceStubStrategy;		// NOSONAR - public on purpose
	public boolean cacheJourneyPictures;		// NOSONAR - public on purpose

	public boolean memoryCacheEager;		// NOSONAR - public on purpose
	public boolean memoryCacheSingleton;		// NOSONAR - public on purpose

	public String[] bootPlugins = {};		// NOSONAR - public on purpose
	public int launcherHttpPort;		// NOSONAR - public on purpose

	public int weblauncherPort;		// NOSONAR - public on purpose
	public int weblauncherShutdownPort;		// NOSONAR - public on purpose
	public String weblauncherContextRoot;		// NOSONAR - public on purpose

	public boolean disableFQDN;		// NOSONAR - public on purpose

	public int apacheWebServerPort;		// NOSONAR - public on purpose
	public int apacheWebServerBackendPort;		// NOSONAR - public on purpose
	public int apacheWebServerB2bPort;		// NOSONAR - public on purpose
	public int apacheWebServerStatusPort;		// NOSONAR - public on purpose
	public String apacheWebServerAgent;		// NOSONAR - public on purpose
	public String[] apacheWebServerEnvArgs = {};		// NOSONAR - public on purpose
	public String apacheWebServerHost;		// NOSONAR - public on purpose
	public String apacheWebServerB2bHost;		// NOSONAR - public on purpose
	public boolean apacheWebServerUsesGeneratedHttpdConfig;		// NOSONAR - public on purpose
	public String apacheWebServerHttpdConfig;	// NOSONAR - public on purpose
	public String apacheWebServerSslHost;	// NOSONAR - public on purpose
	public int apacheWebServerSslPort;		// NOSONAR - public on purpose
	public boolean disableApacheCertificateGeneration; // NOSONAR - public on purpose

	public boolean apacheWebServerSimulatesFirewall;		// NOSONAR - public on purpose
	public boolean apacheWebServerEnableVirtualIp;		// NOSONAR - public on purpose
	public String apacheWebServerVirtualIp;		// NOSONAR - public on purpose
	public String apacheWebServerB2bVirtualIp;		// NOSONAR - public on purpose
	public String apacheWebServerWaitTime;		// NOSONAR - public on purpose

	public boolean apacheWebServerUsesGeneratedPhpIni;		// NOSONAR - public on purpose
	public String phpAgent;		// NOSONAR - public on purpose
	public String[] phpEnvArgs;		// NOSONAR - public on purpose

	public String apacheFrontendPublicUrl;		// NOSONAR - public on purpose
	public String apacheB2BFrontendPublicUrl;		// NOSONAR - public on purpose

	public double baseLoadB2BRatio;		// NOSONAR - public on purpose
	public double baseLoadCustomerRatio;		// NOSONAR - public on purpose
	public double baseLoadMobileNativeRatio;		// NOSONAR - public on purpose
	public double baseLoadMobileBrowserRatio;		// NOSONAR - public on purpose
	public double baseLoadHotDealServiceRatio;		// NOSONAR - public on purpose
	public double baseLoadIotDevicesRatio;		// NOSONAR - public on purpose
	public double baseLoadHeadlessCustomerRatio;		// NOSONAR - public on purpose
	public double baseLoadHeadlessAngularRatio; // NOSONAR - public on purpose
	public double baseLoadHeadlessMobileAngularRatio; // NOSONAR - public on purpose
	public double baseLoadHeadlessB2BRatio; // NOSONAR - public on purpose

	public boolean showHeadlessBrowser;				// NOSONAR - public on purpose
	public int maximumChromeDrivers; 				// NOSONAR - public on purpose
	public int maximumChromeDriversMobile; 				// NOSONAR - public on purpose
	public int reUseChromeDriverFrequency; 			// NOSONAR - public on purpose
	public String[] chromeDriverOpts = {};	        // NOSONAR - public on purpose	
	public String chromeBinary; 					// NOSONAR - public on purpose
	public String chromeDriverBinary;				// NOSONAR - public on purpose
	public boolean headlessThrottlingEnabled; 		// NOSONAR - public on purpose
	public int overloadActionPause; 				// NOSONAR - public on purpose
	public int overloadActionsPerVisit;				// NOSONAR - public on purpose
	public HeadlessTrafficScenarioEnum headlessLoadScenario;		// NOSONAR - public on purpose
	public int headlessAngularJsErrorRate; // NOSONAR - public on purpose
	public int headlessAngularUsabilityClickPayFailRate; // NOSONAR - public on purpose
 

	public int baseLoadDefault;		// NOSONAR - public on purpose
	public int baseLoadIncreased;		// NOSONAR - public on purpose

	public int b2bFrontendStartLoad;		// NOSONAR - public on purpose
	public int customerFrontendStartLoad;		// NOSONAR - public on purpose
	public int mobileNativeStartLoad;		// NOSONAR - public on purpose
	public int mobileBrowserStartLoad;		// NOSONAR - public on purpose
	public int b2bFrontendIncreasePerMinute;		// NOSONAR - public on purpose
	public int customerFrontendIncreasePerMinute;		// NOSONAR - public on purpose
	public int mobileNativeIncreasePerMinute;		// NOSONAR - public on purpose
	public int mobileBrowserIncreasePerMinute;		// NOSONAR - public on purpose
	public int b2bFrontendMaximumLoad;		// NOSONAR - public on purpose
	public int customerFrontendMaximumLoad;		// NOSONAR - public on purpose
	public int mobileNativeMaximumLoad;		// NOSONAR - public on purpose
	public int mobileBrowserMaximumLoad;		// NOSONAR - public on purpose

	public CustomerTrafficScenarioEnum customerLoadScenario;		// NOSONAR - public on purpose
	public double predictableCustomerLoadBounce = 0.1;		// NOSONAR - public on purpose
	public double predictableCustomerLoadSearch = 0.2;		// NOSONAR - public on purpose
	public double predictableCustomerLoadAlmostConvert = 0.1;		// NOSONAR - public on purpose
	public double predictableCustomerLoadConvert = 0.6;		// NOSONAR - public on purpose
	/**
	 * The max amount of time we let the process execute before stopping it.
	 */
	public int syncProcessTimeoutMs = 30 * 1000;		// NOSONAR - public on purpose // 30 sec
	public int shutdownTimeoutMs = 30 * 1000;		// NOSONAR - public on purpose // 30 sec
	public int softShutdownTimeoutMs = 2 * 1000;		// NOSONAR - public on purpose // 10 sec
	public int processRunningCheckInterval = 200;		// NOSONAR - public on purpose // 100 ms
	public int processOperatingCheckIntervalMs = 1000;		// NOSONAR - public on purpose // 1 sec
	public long isRunningCheckIntervalMs = 200;		// NOSONAR - public on purpose // 200 ms

	public String filePath;		// NOSONAR - public on purpose

	public String memcachedServerHost = "localhost";		// NOSONAR - public on purpose
	public int memcachedServerPort = 11211;		// NOSONAR - public on purpose

	public int dcRumMaxDataRecords = 2500;		// NOSONAR - public on purpose
	public boolean xDynaTraceHeaders = false;		// NOSONAR - public on purpose

	public String lastPortWarning;		// NOSONAR - public on purpose

	public boolean enableMainframeDemo;		// NOSONAR - public on purpose
	public String wsmqClasspath[];		// NOSONAR - public on purpose
	public boolean wsmqJms;		// NOSONAR - public on purpose
	public boolean wsmqSetJmsCompliantTargetClient;		// NOSONAR - public on purpose
	public String wsmqHostName;		// NOSONAR - public on purpose
	public int wsmqPort;		// NOSONAR - public on purpose
	public String wsmqChannel;		// NOSONAR - public on purpose
	public String wsmqQueueManagerName;		// NOSONAR - public on purpose
	public String wsmqUserId;		// NOSONAR - public on purpose
	public String wsmqPassword;		// NOSONAR - public on purpose
	public String wsmqPutQueueName;		// NOSONAR - public on purpose
	public String wsmqGetQueueName;		// NOSONAR - public on purpose

	public String ctgHostName;		// NOSONAR - public on purpose
	public int ctgPort;		// NOSONAR - public on purpose
	public String ctgServer;		// NOSONAR - public on purpose
	public String ctgProgram;		// NOSONAR - public on purpose
	public String ctgTransaction;		// NOSONAR - public on purpose
	public String ctgUserId;		// NOSONAR - public on purpose
	public String ctgPassword;		// NOSONAR - public on purpose

	public String imsClasspath[];		// NOSONAR - public on purpose
	public String imsDataStoreName;		// NOSONAR - public on purpose
	public String imsHostName;		// NOSONAR - public on purpose
	public int imsPort;		// NOSONAR - public on purpose
	public String imsTranCode;		// NOSONAR - public on purpose

	public int apacheWebServerProxyPort;		// NOSONAR - public on purpose
	public String browserPath;		// NOSONAR - public on purpose

	public String proxyHost;		// NOSONAR - public on purpose
	public int proxyPort;		// NOSONAR - public on purpose
	public String[] proxiedSites;		// NOSONAR - public on purpose

	public String[] cassandraJavaopts = {};		// NOSONAR - public on purpose
	public String cassandraSystemProfile;		// NOSONAR - public on purpose
	public String cassandraAgent;		// NOSONAR - public on purpose
	public String[] cassandraAgentOptions;		// NOSONAR - public on purpose
	public String[] cassandraEnvArgs = {};		// NOSONAR - public on purpose
	public String[] cassandraNodeAddresses;		// NOSONAR - public on purpose
	public String cassandraReadConsistencyLevel;		// NOSONAR - public on purpose
	public String cassandraWriteConsistencyLevel;		// NOSONAR - public on purpose
	public int cassandraReplicationFactor;		// NOSONAR - public on purpose

	public String[] mongoDbInstances;		// NOSONAR - public on purpose

	public String officialHost;		// NOSONAR - public on purpose
	public boolean enableRecommendationBean;		// NOSONAR - public on purpose

	public String amdVersion;		// NOSONAR - public on purpose

	public boolean shortHostDisplay;		// NOSONAR - public on purpose

	/** The host which replaces the backend as holder for plugins */
	public String pluginServiceHost;		// NOSONAR - public on purpose
	/** The port to access the plugin service */
	public int pluginServicePort;		// NOSONAR - public on purpose
	public int pluginServiceShutdownPort;		// NOSONAR - public on purpose
	public String pluginServiceContextRoot;		// NOSONAR - public on purpose

	/**
	 * Configuration of VMotion
	 */
	public String vCenterHost;		// NOSONAR - public on purpose
	public String vCenterUser;		// NOSONAR - public on purpose
	public String vCenterPassword;		// NOSONAR - public on purpose
	public String fromHost;		// NOSONAR - public on purpose
	public String toHost;		// NOSONAR - public on purpose
	public String resPool;		// NOSONAR - public on purpose
	public String vmName;		// NOSONAR - public on purpose

	/** Enable WebLauncher authentication and SSL encryption */
	public boolean isWebLauncherAuthEnabled;	// NOSONAR - public on purpose
	public int sslPort;		// NOSONAR - public on purpose

	/** Additional delay added to getUsers method in {@link AuthenticationService} class. Used if DBSpammingAuth problem pattern is enabled. Value in milliseconds.*/
	public int authServiceGetUserDelay = 1;	// NOSONAR - public on purpose
	/** Number of db queries performed when DBSpammingAuth problem pattern is enabled */
	public int authServiceSpamSize = 32;	// NOSONAR - public on purpose
	/** enable spamming in all AuthenticationService methods */
	public boolean isFullAuthServiceSpammingEnabled = true; // NOSONAR - public on purpose

    /**
     * Delay value - in ms - for database slowdown
     */
    public int databaseSlowdownDelay; // NOSONAR - public on purpose

    public boolean enableDBSlowdown; // NOSONAR - public on purpose

    /**
     * CPULoad values - in seconds
     */
    // Total time to run. -1 means forever, but you cannot pass -1 to CPULoad, so pass nothing, and it will run forever
    public int	NativeCPULoadTotalTime=-1; // NOSONAR - public on purpose
    // Length of active times. -1 means forever, but you cannot pass -1 to CPULoad, so pass nothing, and it will run forever
    public int	NativeCPULoadActiveTime=-1; // NOSONAR - public on purpose
    // Length of quiet times.
    public int	NativeCPULoadQuietTime=0; // NOSONAR - public on purpose
    // Priority to run CPULoad at:
    // 0 - normal
    public int	NativeCPULoadPri=0;

	/** node.js parameters */
	public String nodejsHost; // NOSONAR - public on purpose
	public int nodejsPort;    // NOSONAR - public on purpose
	public String nodejsURL;  // NOSONAR - public on purpose

	public String wordpressBlogUrl;  // NOSONAR - public on purpose
	public boolean PHPEnabledForAngularOnly;

	public String[] thirdPartyDomains; // NOSONAR - public on purpose
	public String[] thirdPartyUrls; // NOSONAR - public on purpose

	public String syntheticTestDefinitionId; // NOSONAR - public on purpose
	public String syntheticTestVuControllerIds; // NOSONAR - public on purpose

	public int noHostAgents; // NOSONAR - public on purpose
	public String[] hostNamePatterns; // NOSONAR - public on purpose

    /**
     * CouchDB values
     */
    // host address and port
	public String couchDBHost;	// NOSONAR - public on purpose
	public int couchDBPort;		// NOSONAR - public on purpose
	public int couchDBShutdownPort;		// NOSONAR - public on purpose
	// database name
	public String couchDBName;	// NOSONAR - public on purpose
	public String couchDBAdminUser;
	public String couchDBAdminPassword;

    /**
     * NIGINX configuration
     */
    public String nginxWebServerHost;   // NOSONAR - public on purpose
    public int nginxWebServerPort;  // NOSONAR - public on purpose

    public String nginxFrontendPublicUrl;   // NOSONAR - public on purpose
    public String nginxWebServerAgent;      // NOSONAR - public on purpose
    public String[] nginxWebServerEnvArgs;    // NOSONAR - public on purpose
    public String nginxWebServerB2bHost;    // NOSONAR - public on purpose
    public int nginxWebServerB2bPort;    // NOSONAR - public on purpose
    public String nginxB2BFrontendPublicUrl;    // NOSONAR - public on purpose
    public String nginxWebServerAngularHost; // NOSONAR - public on purpose 
    public int nginxWebServerAngularPort;    // NOSONAR - public on purpose
    public String nginxAngularFrontendPublicUrl;    // NOSONAR - public on purpose

	public boolean nginxWebServerUsesGeneratedHttpdConfig;		// NOSONAR - public on purpose

    /**
     * Enable {@link com.codahale.metrics.CsvReporter} to store measurements in {@link com.dynatrace.easytravel.config.Directories.getTempDir()}
     * The {@link com.dynatrace.easytravel.metrics.Metrics} class performs CsvReporter initialization.
     *
     * Use for debug purposes. Long running CsvReporter may fill in HDD
     * By intention, this property is not included in easyTravelConfig.properties
     */
    public boolean enableMetricsReporter = false;	// NOSONAR - public on purpose

	/**
	 * EasyTravelMonitor is a fake application deployed on backend. It is used to simulate war deployment events.
	 * Below configuration contains application parameters.
	 */
	public String backendEasyTravelMonitorContextRoot = "/easyTravelMonitor"; 	// NOSONAR - public on purpose
	public String backendEasyTravelMonitorWar = "easyTravelMonitor.war"; 			// NOSONAR - public on purpose

	//private static long lastConfigReadTime = 0;
	private Map<?, ?> customFieldSettintgs;

	/**
	 * Configuration for PluginScheduler
	 */
	public boolean pluginSchedulerEnabled;	// NOSONAR - public on purpose

    /**
     * Sudo password for executing Linux commands
     */
    public String sudoPassword;	// NOSONAR - public on purpose

    /**
     * Iptables rules
     */
    public String[] iptablesRules;	// NOSONAR - public on purpose

    /**
     * Should the start/stop session recording target be skipped when running test-related scenarios based on Ant script.
     */
    public boolean skipSessionRecording;	// NOSONAR - public on purpose

    /*
     * Configuration for Magento demo shop
     */
    public String magentoShopUrl; // NOSONAR - public on purpose

    public int asyncContextTimeout; // NOSONAR - public on purpose

    /*
     * Configuration for Mesos + Marathon
     */
    public String marathonHost; // NOSONAR - public on purpose
    public String marathonPort; // NOSONAR - public on purpose
    public String marathonUser; // NOSONAR - public on purpose
    public String marathonPassword; // NOSONAR - public on purpose
    public String marathonURI; // NOSONAR - public on purpose
    public String[] marathonMicroservices; // NOSONAR - public on purpose
    public int marathonScaledNumberOfMicroservices; // NOSONAR - public on purpose
    public int marathonDefaultNumberOfMicroservices; // NOSONAR - public on purpose

    public String mongoDbUser; // NOSONAR - public on purpose
    public String mongoDbPassword; // NOSONAR - public on purpose
    public String mongoDbAuthDatabase; // NOSONAR - public on purpose


	/** PluginAgent parameters */
	public String pluginAgentURLCF;  // NOSONAR - public on purpose
	public String pluginAgentURLBB;  // NOSONAR - public on purpose
	public String pluginAgentContext;  // NOSONAR - public on purpose


	/*
     * Vagrant configuration
     */
    public String vagrantBinaryLocation; // NOSONAR - public on purpose
    public String vagrantBoxProtocol; // NOSONAR - public on purpose
    public String vagrantWorkingDir; // NOSONAR - public on purpose

    /*
     * Robots and Synthetic browser visits configuration
     */
    public double baseDynatraceSyntheticLoad; // NOSONAR - public on purpose
    public double baseSyntheticLoad; // NOSONAR - public on purpose
    public double baseRobotLoad; // NOSONAR - public on purpose

    /*
     * AMP application ID
     */
    public String ampApplicationID; // NOSONAR - public on purpose
    public String ampBfProtocol; // NOSONAR - public on purpose
    public String ampBfEnvironment; // NOSONAR - public on purpose
    public String ampBfPort; // NOSONAR - public on purpose
    public String ampBfTenant; // NOSONAR - public on purpose

    /***
     * Azure queue
     */
    public String azureServiceBusConnectionString; // NOSONAR - public on purpose
    public String azureRequestQueue; // NOSONAR - public on purpose
    public String azureResponseQueue; // NOSONAR - public on purpose
    
    /**
     * MemoryLeakWithGC plugin settings 
     */
    public int memoryLeakWithGCDelay = 60;
    public int memoryLeakWithGCMaxCacheSize = 10000;
    public int memoryLeakWithGCCacheGrowSize = 5;
    
    //GarbageCollectionTrigger plugin settings
    public int garbageCollectionTriggerGCDelay = 10;
    public int garbageCollectionTriggerFinalizeDelay = 500;
    public int garbageCollectionTriggerMaxObjects = 500000;
    
    public boolean disableJavaScriptAgent = true;
    
    public String dtReleaseStage = "demo";

	private static final Set<ConfigChangeListener> listeners = new HashSet<ConfigChangeListener>(8, 1F);

	/**
	 * Provide a current instance of the configuration, usually via an internal singleton.
	 *
	 * @return An instance of EasyTravelConfig which has all members populated from the
	 * 				EasyTravelConfig.properties file.
	 */

	public static EasyTravelConfig read() {
		return read(NONE_CUSTOM_FIELD_TO_LOAD);
	}

	/**
	 *
	 * @return an instance of {@link EasyTravelConfig}, not <code>null</code>
	 */
	static EasyTravelConfig read(Map<?, ?> customFieldsToLoad) {
		final EasyTravelConfig singleton = SINGLETON.get();
		boolean enforcedReload = customFieldsToLoad != NONE_CUSTOM_FIELD_TO_LOAD;

		// re-read singleton every 10 seconds (disabled as it causes trouble, see JLT-41104)
		if (!enforcedReload && singleton != null /*&& (lastConfigReadTime + 10000) >= System.currentTimeMillis()*/) {
			return singleton;
		}

		// use double checked synchronization to avoid synchronizing each read()
		// call after the config is created once
		synchronized (EasyTravelConfig.class) {
			if (enforcedReload || SINGLETON.get() == null /*|| lastConfigReadTime + 10000 < System.currentTimeMillis()*/) {
				String path = null;
				if (SINGLETON.get() != null) {
					path = SINGLETON.get().filePath;
				}

				// reset to avoid error about having a singleton already
				resetSingleton();

				// now read the config, either from the given config file or the default file if none is specified
				String cfgPath = path == null ? PROPERTIES_FILE : path;
				createSingleton(cfgPath, customFieldsToLoad);
			}
			//lastConfigReadTime = System.currentTimeMillis();

			return SINGLETON.get();
		}
	}


	/**
	 * Create and set a new singleton.
	 *
	 * @param propertiesFile
	 * @author martin.wurzinger
	 */
	public synchronized static void createSingleton(String propertiesFilePath) {
		createSingleton(propertiesFilePath, null);
	}

	/**
	 *
	 * @param propertiesFilePath
	 * @param customFields if the given map is null, the settings of the last {@link EasyTravelConfig} are applied
	 *        otherwise, the settings of the given map are applied
	 * @author stefan.moschinski
	 */
	private synchronized static void createSingleton(String propertiesFilePath, Map<?, ?> customFields) {
		// create new singleton
		EasyTravelConfig newConfig = create(propertiesFilePath);

		// replace it in an atomic operation
		if (SINGLETON.compareAndSet(null, newConfig)) {
			if (customFields == NONE_CUSTOM_FIELD_TO_LOAD) {
				// if the custom field properties have not been overridden, we use the old ones
				copyCustomFieldSettings(OUTDATED_SINGLETON.get(), SINGLETON.get());

			} else {
				// set the new custom properties
				SINGLETON.get().setCustomSettings(customFields);
			}
			notifyListenersIfRequired(OUTDATED_SINGLETON.get(), SINGLETON.get());
			return;
		}

		throw new IllegalStateException(TextUtils.merge("The {0} singleton must not be set a second time.",
				EasyTravelConfig.class.getSimpleName()));
	}

	private static void copyCustomFieldSettings(EasyTravelConfig oldCfg, EasyTravelConfig newCfg) {
		if (oldCfg == null || oldCfg.customFieldSettintgs == null) {
			return;
		}
		newCfg.setCustomSettings(oldCfg.customFieldSettintgs);
	}

	private static void notifyListenersIfRequired(EasyTravelConfig oldCfg, EasyTravelConfig newCfg) {
		for (ConfigChangeListener listener : listeners) {
			listener.notifyConfigLoaded(oldCfg, newCfg);
		}
	}

	/**
	 * Delete the current singleton. This also invalidates any custom filePath that was set
	 * and does <b>NOT</b> read the values stored in the temporary file!
	 *
	 * It also removes a custom property file provided via commandline-arguments, so
	 * only use this method in special cases, i.e. mostly in test-cases.
	 *
	 * @author martin.wurzinger
	 */
	public synchronized static void resetSingleton() {
		OUTDATED_SINGLETON.set(SINGLETON.get());
		SINGLETON.set(null);
	}

	public static EasyTravelConfig create(String propertiesFilePath) {
		MvelUtils.registerEnumType(InstallationType.class);
		MvelUtils.registerEnumType(ServiceStubStrategy.class);
		MvelUtils.registerEnumType(CustomerTrafficScenarioEnum.class);
		MvelUtils.registerEnumType(HeadlessTrafficScenarioEnum.class);

		String configfilePath = getAbsolutePropertiesFilePath(propertiesFilePath,true);
		EasyTravelConfig config = ConfigurationProvider.createPropertyBean(EasyTravelConfig.class, configfilePath, NAMESPACE);

		// override with local settings if found
		loadLocalPropertiesFile(config);
		loadPrivatePropertiesFile(config);
		loadWarPropertiesFile(config);

		adjustPathsAndURLs(config);

		//
		if (config.frontendPublicUrl == null || config.frontendPublicUrl.isEmpty()) {
			config.frontendPublicUrl = System.getProperty("com.dynatrace.easytravel.host.customer_frontend.public");
		}
		if (config.angularFrontendPublicUrl == null || config.angularFrontendPublicUrl.isEmpty()) {
			config.angularFrontendPublicUrl = System.getProperty("com.dynatrace.easytravel.host.angular_frontend.public");
		}
		if (config.b2bFrontendPublicUrl == null || config.b2bFrontendPublicUrl.isEmpty()) {
			config.b2bFrontendPublicUrl = System.getProperty("com.dynatrace.easytravel.host.b2b_frontend.public");
		}
		if (config.apacheFrontendPublicUrl == null || config.apacheFrontendPublicUrl.isEmpty()) {
			config.apacheFrontendPublicUrl = System.getProperty("com.dynatrace.easytravel.host.apache_customer.public");
        }
		if (config.nginxFrontendPublicUrl == null || config.nginxFrontendPublicUrl.isEmpty()) {
			config.nginxFrontendPublicUrl = System.getProperty("com.dynatrace.easytravel.host.nginx_customer.public");
		}
		if (config.apacheB2BFrontendPublicUrl == null || config.apacheB2BFrontendPublicUrl.isEmpty()) {
			config.apacheB2BFrontendPublicUrl = System.getProperty("com.dynatrace.easytravel.host.apache_b2b.public");
		}
		if (config.nginxAngularFrontendPublicUrl == null || config.nginxAngularFrontendPublicUrl.isEmpty()) {
			config.nginxAngularFrontendPublicUrl = System.getProperty("com.dynatrace.easytravel.host.nginx_angular.public");
		}

		// verify that none of the ranges is invalid (i.e. "End" lower than "Start"
		checkRange(config.b2bFrontendPortRangeStart, config.b2bFrontendPortRangeEnd);
		checkRange(config.frontendPortRangeStart, config.frontendPortRangeEnd);
		checkRange(config.frontendShutdownPortRangeStart, config.frontendShutdownPortRangeEnd);
		checkRange(config.frontendAjpPortRangeStart, config.frontendAjpPortRangeEnd);

		// set the path of the properties file the config was loaded from
		config.filePath = configfilePath;

		return config;
	}

	private static void loadLocalPropertiesFile(EasyTravelConfig config) {
		if (isUseLocalEasyTravelEnvironmentFile()) {
			localConfigFileLogged = loadPropertiesFile(EasyTravelConfig.getEasyTravelLocalPropertiesFile(), localConfigFileLogged, config);
		}
	}

	private static void loadPrivatePropertiesFile(EasyTravelConfig config) {
		if (isUsePrivateEasyTravelEnvironmentFile()) {
			privateConfigFileLogged = loadPropertiesFile(EasyTravelConfig.getEasyTravelPrivatePropertiesFile(), privateConfigFileLogged, config);
		}
	}

	private static void loadWarPropertiesFile(EasyTravelConfig config) {
		if (isUseWarEasyTravelEnvironmentFile()) {
			warConfigFileLogged = loadPropertiesFile(EasyTravelConfig.getEasyTravelWarPropertiesFile(), warConfigFileLogged, config);
		}
	}

	private static boolean loadPropertiesFile(File file, boolean logMessage, EasyTravelConfig config) {
		try {
			if (file.exists()) {
				// log this out only once on info-loglevel to not spam the logfile every 10 seconds
				if (LOGGER.isDebugEnabled() || logMessage) {
					LOGGER.info("Loading override-properties from file: " + file);
				}
				Properties props = ConfigurationProvider.readPropertyFile(file.getAbsolutePath());
				MvelUtils.injectProperties(config, props, NAMESPACE);
				return true;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new IllegalArgumentException(e);
		}
		return false;
	}

	protected static void adjustPathsAndURLs(EasyTravelConfig config) {
		config.agent = config.adaptPath(config.agent);

		// adjust for global agent if necessary
		config.frontendAgent = config.adjustForGlobalAgent(config.frontendAgent);
		config.backendAgent = config.adjustForGlobalAgent(config.backendAgent);
		config.creditCardAuthorizationAgent = config.adjustForGlobalAgent(config.creditCardAuthorizationAgent);
		config.antAgent = config.adjustForGlobalAgent(config.antAgent);

		// JLT-76536: do not auto-adapt path for Apache WebServer and PHP Agent as we run them
		// with 32-bit agents always!
		config.apacheWebServerAgent = config.adaptPath(config.apacheWebServerAgent);
		config.phpAgent = config.adaptPath(config.phpAgent);

		// URL-items should always have a trailing slash to not need to check in the code later
		config.dtServerWebURL = TextUtils.appendTrailingSlash(config.dtServerWebURL);

		config.dtClientWebURL = TextUtils.appendTrailingSlash(config.dtClientWebURL);
		config.frontendContextRoot = TextUtils.appendTrailingSlash(config.frontendContextRoot);
		config.weblauncherContextRoot = TextUtils.appendTrailingSlash(config.weblauncherContextRoot);
		config.backendContextRoot = TextUtils.appendTrailingSlash(config.backendContextRoot);
		config.webServiceBaseDir = TextUtils.appendTrailingSlash(config.webServiceBaseDir);
		config.dotNetBackendWebServiceBaseDir = TextUtils.appendTrailingSlash(config.dotNetBackendWebServiceBaseDir);
	}

	/**
	 *
	 * @param frontendAjpPortRangeStart2
	 * @param frontendAjpPortRangeEnd2
	 * @author dominik.stadler
	 */
	private static void checkRange(int rangeStart, int rangeEnd) {
		if (rangeEnd < rangeStart) {
			throw new IllegalStateException(TextUtils.merge(
					"Range-values have lower ''End'' than ''Start'': {0,number,#} and {1,number,#}.", rangeEnd, rangeStart));
		}
	}

	/**
	 * Return true if easyTravel is running an a local machine.
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	public boolean isLocalEnvironment() {
		return frontendPublicUrl == null;
	}

	/**
	 * Allows to override/add more settings
	 *
	 * @param properties
	 * @author dominik.stadler
	 */
	public void enhance(Properties properties) {
		ConfigurationProvider.enhancePropertyBean(this, properties, NAMESPACE);

		// ensure that we adjust paths and URLs here again, e.g. when we transport agent-pathes
		// from Windows to Linux, they should be set back to have slashes instead of backslashes again
		adjustPathsAndURLs(this);
	}

	private static final HashSet<String> MANUAL_PROPERTIES = new HashSet<String>();

	static {
		// all properties which are not listed in the easyTravelConfig.properties and thus need to be read specifically
		MANUAL_PROPERTIES.add("config.officialHost");

		// env args for java/native procedures
		MANUAL_PROPERTIES.add("config.antEnvArgs");
		MANUAL_PROPERTIES.add("config.backendEnvArgs");
		MANUAL_PROPERTIES.add("config.cassandraEnvArgs");
		MANUAL_PROPERTIES.add("config.frontendEnvArgs");
		MANUAL_PROPERTIES.add("config.phpEnvArgs");
		MANUAL_PROPERTIES.add("config.thirdpartyEnvArgs");

		// public urls
		MANUAL_PROPERTIES.add("config.apacheB2BFrontendPublicUrl");
		MANUAL_PROPERTIES.add("config.apacheFrontendPublicUrl");
		MANUAL_PROPERTIES.add("config.b2bFrontendPublicUrl");
		MANUAL_PROPERTIES.add("config.frontendPublicUrl");

		// others
		MANUAL_PROPERTIES.add("config.creditCardAuthorizationSocketPort");
		MANUAL_PROPERTIES.add("config.pluginServiceHost");
		MANUAL_PROPERTIES.add("config.proxiedSites");
		MANUAL_PROPERTIES.add("config.proxyHost");
		MANUAL_PROPERTIES.add("config.shortHostDisplay");
		MANUAL_PROPERTIES.add("config.shutdownTimeoutMs");
		MANUAL_PROPERTIES.add("config.softShutdownTimeoutMs");
		MANUAL_PROPERTIES.add("config.noHostAgents");
		MANUAL_PROPERTIES.add("config.hostNamePatterns");
	}

	public void store(File location) throws IOException {
		store(location, MANUAL_PROPERTIES);
	}

	public void store(File location, Set<String> importantPropertyNames) throws IOException {
		// find out which properties we support by looking at the original property file
		String configfilePath = getAbsolutePropertiesFilePath(PROPERTIES_FILE,true);
		Properties config = ConfigurationProvider.readPropertyFile(configfilePath);

		// both, the properties in the existing local file and the
		// names which are important for the caller should be evaluated
		HashSet<String> propertyNames = new HashSet<String>();
		propertyNames.addAll(config.stringPropertyNames());
		propertyNames.addAll(importantPropertyNames);

		// save all properties in the new location
		ConfigurationProvider.store(this, propertyNames, location, NAMESPACE);
	}

	public File storeInTempFile() {
		// write the properties to a file and pass this as setting to the procedures
		try {
			// get a unique file name
			File file = File.createTempFile(EasyTravelConfig.PROPERTIES_FILE, ".properties", Directories.getExistingTempDir());

			// remember that we should remove this file when the process stops
			file.deleteOnExit();

			// store the properties to the file
			store(file);

			return file;
		} catch (IOException e) {
			LOGGER.warn("Could not create temporary file for custom properties in directory " + Directories.getTempDir());
		}

		return null;
	}

	protected static String getAbsolutePropertiesFilePath(String fileName, boolean dontSearchForWarConfig) {
		if (!fileName.endsWith(PROPERTIES_SUFFIX)) {
			fileName += PROPERTIES_SUFFIX;
		}

		URL url = ConfigurationProvider.getResource(fileName);
		if (url == null) {
			if(dontSearchForWarConfig){
			throw new IllegalArgumentException("Resource not found: " + fileName);
			}
			else return WAR_PROPERTIES_FILE;
		}

		try {
			return new File(url.toURI()).getAbsolutePath();
		} catch (URISyntaxException e) {
			return new File(fileName).getAbsolutePath();
		}
	}

	/**
	 * Let the global agent step in if the specific one is "auto".
	 *
	 * @param local
	 * @return
	 * @author dominik.stadler
	 */
	private String adjustForGlobalAgent(String local) {
		if (BaseConstants.AUTO.equalsIgnoreCase(local) || local == null) {
			return agent;
		}
		return adaptPath(local);
	}

	private String adaptPath(String inputPath) {
		boolean netStart = false;
		final String netStartWindows = "\\\\";
		final String netStartLinux = "//";
		if (inputPath.startsWith(netStartWindows) || inputPath.startsWith(netStartLinux)) {
			netStart = true;
		}
		String pathSeparator = "[/\\\\]+";
		String clearedPath = inputPath.replaceAll(pathSeparator, "/");
		// we cannot use File.separator directly in the replaceAll invocation,
		// it will cause OutOfBoundsException if the separator is \\
		clearedPath = clearedPath.replace("/", File.separator);
		return netStart ? File.separator + clearedPath : clearedPath;
	}

	/**
	 * Allows to write a config property to the local config file, the singleton
	 * in EasyTravelConfig is NOT reset afterwards to ensure that we still use
	 * the existing file (which can have been set to a different file!).
	 *
	 * @param key
	 * @param value
	 * @throws IOException
	 * @author dominik.stadler
	 */
	public static void writeLocalSetting(String key, String value) throws IOException {
		final Properties props = new Properties();
		final StringBuilder comments = new StringBuilder();

		// if the file exists already, read it in
		File file = EasyTravelConfig.getEasyTravelLocalPropertiesFile();
		if (file.exists()) {
			LOGGER.info("Loading override-properties from file: " + file);
			InputStream in = new FileInputStream(file);
			try {
				props.load(in);
			} finally {
				in.close();
			}

			// Properties throws away comments, however I would like to keep them here, therefore read them in and
			// keep them
			List<String> lines = FileUtils.readLines(file);
			for (String line : lines) {
				// handle all comments
				if (line.startsWith("#")) {
					comments.append(line).append("\n");
				}
			}
		}

		// set the value
		props.setProperty(key, value);

		// write it back out
		FileOutputStream out = new FileOutputStream(file);
		try {
			props.store(out, comments.toString());
		} finally {
			out.close();
		}

		// re-read config to use the current config settings
		// don't do this, breaks Training Mode: EasyTravelConfig.resetSingleton();
	}


	/**
	 * Using this method, you can override the default configuration values of instances
	 * of this class
	 *
	 * @param customSettings settings that override the loaded default values
	 * @author stefan.moschinski
	 */
	public static EasyTravelConfig applyCustomSettings(Map<?, ?> customSettings) {
		return read(customSettings);
	}


	@Override
	protected EasyTravelConfig clone() throws CloneNotSupportedException {
		return (EasyTravelConfig) super.clone();
	}

	private void setCustomSettings(Map<?, ?> customSettings) {
		customFieldSettintgs = new HashMap<Object, Object>(customSettings);
		setFieldValues(customFieldSettintgs);
	}

	private void setFieldValues(Map<?, ?> fieldVals) {
		MvelUtils.injectProperties(this, fieldVals, NAMESPACE);
	}

	/*private boolean hasCustomSettings() {
		return customFieldSettintgs != null && !customFieldSettintgs.isEmpty();
	}*/

	/**
	 * Refresh the EasyTravel Config from the file that was last used for reading, i.e.
	 * either the default easyTravelConfig.properties or the file specified via the
	 * commandline option "propertyfile",
	 *
	 * @author dominik.stadler
	 */
	public synchronized static void reload() {
		String filePath = read().filePath;
		LOGGER.info("Reloading properties from file: '" + filePath);
		resetSingleton();
		createSingleton(filePath, null);
	}

	/**
	 * Returns true if the current configuration is for the default Derby Database.
	 *
	 * Returns false if some other RDBMS is configured.
	 *
	 * @return
	 */
	public static boolean isDerbyDatabase() {
		return read().databaseDriver.contains("derby");
	}

	/**
	 * Returns true if the current configuration is for the Oracle database.
	 * @return
	 */
	public static boolean isOracleDatabase() {
		return read().databaseDriver.contains("oracle.jdbc.OracleDriver");
	}

	/**
	 * Returns true if the current configuration is for the MS SQL database.
	 * @return
	 */
	public static boolean isMssqlDatabase() {
		return (read().databaseDriver.contains("com.microsoft.sqlserver.jdbc.SQLServerDriver") || read().databaseDriver.contains("net.sourceforge.jtds.jdbc.Driver"));
	}

	/**
	 * Returns true if the current configuration is for the MySQL database.
	 * @return
	 */
	public static boolean isMySqlDatabase() {
		return (read().databaseDriver.contains("com.mysql.jdbc.Driver"));
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, /* excludes: */filePath);
	}

	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o, /* excludes: */filePath);
	}

	public static void addConfigChangeListener(ConfigChangeListener listener) {
		listeners.add(listener);
	}

	public static void removeConfigChangeListener(ConfigChangeListener listener) {
		listeners.remove(listener);
	}

	@Override
	public boolean hasDifferentLoadSettingsThan(UEMLoadConfig otherCfg) {
		return otherCfg == null
				|| otherCfg.getDefaultLoad() != baseLoadDefault
				|| otherCfg.getB2bRatio() != baseLoadB2BRatio
				|| otherCfg.getCustomerRatio() != baseLoadCustomerRatio
				|| otherCfg.getMobileNativeRatio() != baseLoadMobileNativeRatio
				|| otherCfg.getMobileBrowserRatio() != baseLoadMobileBrowserRatio
				|| otherCfg.getHotDealServiceRatio() != baseLoadHotDealServiceRatio
				|| otherCfg.getHeadlessCustomerRatio() != baseLoadHeadlessCustomerRatio
				|| otherCfg.getHeadlessAngularRatio() != baseLoadHeadlessAngularRatio
				|| otherCfg.getHeadlessMobileAngularRatio() != baseLoadHeadlessMobileAngularRatio
				|| otherCfg.getHeadlessB2BRatio() != baseLoadHeadlessB2BRatio;
	}

	@Override
	public boolean hasDifferentCustomerLoad(UEMLoadConfig otherConfig) {
		return otherConfig == null || otherConfig.getCustomerTrafficScenario() != getCustomerTrafficScenario();
	}

	@Override
	public int getDefaultLoad() {
		return baseLoadDefault;
	}

	@Override
	public double getB2bRatio() {
		return baseLoadB2BRatio;
	}

	@Override
	public double getMobileNativeRatio() {
		return baseLoadMobileNativeRatio;
	}

	@Override
	public double getMobileBrowserRatio() {
		return baseLoadMobileBrowserRatio;
	}

	@Override
	public double getHotDealServiceRatio() {
		return baseLoadHotDealServiceRatio;
	}

	@Override
	public double getHeadlessCustomerRatio() {
		return baseLoadHeadlessCustomerRatio ;
	}

	@Override
	public double getHeadlessAngularRatio() {
		return baseLoadHeadlessAngularRatio;
	}

	@Override
	public double getHeadlessMobileAngularRatio() {
		return baseLoadHeadlessMobileAngularRatio;
	}

	@Override
	public double getCustomerRatio() {
		return baseLoadCustomerRatio;
	}
	
	@Override
	public double getHeadlessB2BRatio() {
		return baseLoadHeadlessB2BRatio;
	}

	/**
	 * @return traffic scenario for customer traffic. Default value: CustomerTrafficScenarioEnum.EasyTravel
	 */
	@Override
	public CustomerTrafficScenarioEnum getCustomerTrafficScenario() {
		return (customerLoadScenario == null ? CustomerTrafficScenarioEnum.EasyTravel : customerLoadScenario);
	}

	/**
	 * @return traffic scenario for headless traffic. Default value: HeadlessTrafficScenarioEnum.StandardHeadlessTraffic
	 */
	@Override
	public HeadlessTrafficScenarioEnum getHeadlessTrafficScenario() {
		return (headlessLoadScenario == null ? HeadlessTrafficScenarioEnum.StandardHeadlessTraffic : headlessLoadScenario);
	}


	public static enum ServiceStubStrategy
	{
		/**
		 * Always create new Stub objects.
		 */
		alwaysCreate,

		/**
		 * Keep stubs in a global cache.
		 * Note this is very deadlock-prone. Only here for demo.
		 */
		globalCache,

		/**
		 * Cache stub objects in a thread-local cache.
		 */
		threadLocalCache,

		/**
		 * A cache per-thread but globally managed.
		 */
		threadGlobalCache,

		/**
		 * Manage stub objects with a global pool.
		 */
		globalPool;
	}

	@Override
	public String getUemLoadConfigAsString() {
		return MoreObjects.toStringHelper(this)
				.add("config.baseLoadDefault", baseLoadDefault)
				.add("config.baseLoadCustomerRatio", baseLoadCustomerRatio)
				.add("config.baseLoadB2BRatio", baseLoadB2BRatio)
				.add("config.baseLoadMobileNativeRatio", baseLoadMobileNativeRatio)
				.add("config.baseLoadMobileBrowserRatio", baseLoadMobileBrowserRatio)
				.add("config.baseLoadHotDealServiceRatio", baseLoadHotDealServiceRatio)
				.add("config.baseLoadHeadlessRatio", baseLoadHeadlessCustomerRatio)
				.add("config.customerLoadScenario", customerLoadScenario)
				.add("config.headlessLoadScenario", headlessLoadScenario)
				.add("config.baseLoaddHeadlessB2BRatio", baseLoadHeadlessB2BRatio)
				.toString();
	}

	public static File getEasyTravelLocalPropertiesFile() {
		return new File(Directories.getConfigDir(), EasyTravelConfig.LOCAL_PROPERTIES_FILE);
	}

	public static File getEasyTravelPrivatePropertiesFile() {
		return new File(Directories.getConfigDir(), EasyTravelConfig.PRIVATE_PROPERTIES_FILE);
	}

	public static File getEasyTravelWarPropertiesFile() {
		return new File(getAbsolutePropertiesFilePath(EasyTravelConfig.WAR_PROPERTIES_FILE,false));
	}

	/**
	 * Just gets a string which describes the current situation about the easyTravelLocal properties file.<br/>
	 * e.g. if it is available and if it's usable.
	 */
	public static String buildLocalETPropertiesFileSituationLogEntry() {
		boolean useFile = isUseLocalEasyTravelEnvironmentFile();
		return buildETPropertiesFileSituationLogEntry(EasyTravelConfig.LOCAL_PROPERTIES_FILE, useFile);
	}

	/**
	 * Just gets a string which describes the current situation about the easyTravelPrivate properties file.<br/>
	 * e.g. if it is available and if it's usable.
	 */
	public static String buildPrivateETPropertiesFileSituationLogEntry() {
		boolean useFile = isUsePrivateEasyTravelEnvironmentFile();
		return buildETPropertiesFileSituationLogEntry(EasyTravelConfig.PRIVATE_PROPERTIES_FILE, useFile);
	}

	private static String buildETPropertiesFileSituationLogEntry(String fileName, boolean useFile) {
		File file = new File(Directories.getConfigDir(), fileName);

		return String.format("local: %s (Exists: %b). Is also used: %b\n", file, file.exists(), useFile);
	}

	private static boolean isUseLocalEasyTravelEnvironmentFile() {
		return !Boolean.getBoolean(SystemProperties.DONT_USE_LOCAL_ET_PROPERTIES);
	}

	private static boolean isUsePrivateEasyTravelEnvironmentFile() {
		return !Boolean.getBoolean(SystemProperties.DONT_USE_PRIVATE_ET_PROPERTIES);
	}
	private static boolean isUseWarEasyTravelEnvironmentFile() {
		return !Boolean.getBoolean(SystemProperties.DONT_USE_WAR_ET_PROPERTIES);
	}


}
