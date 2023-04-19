package com.dynatrace.easytravel.launcher.misc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.misc.RESTConstants;


public class Constants { // NOPMD

	public static class Modules {

        public static final String LAUNCHER = "com.dynatrace.easytravel.launcher.jar";
        public static final String BUSINESS_BACKEND = "com.dynatrace.easytravel.business.backend.jar";
        public static final String CUSTOMER_FRONTEND = "com.dynatrace.easytravel.customer.frontend.jar";
        public static final String CUSTOMER_FRONTEND_REST = "com.dynatrace.easytravel.customer.frontend.rest.jar";
        public static final String THIRDPARTY_CONTENT_SERVER = "com.dynatrace.easytravel.thirdpartycontent.server.jar";
		public static final String CASSANDRA = "com.dynatrace.easytravel.cassandra.jar";
        public static final String CREDITCARD_AUTHORIZATION = "CreditCardAuthorization";
        public static final String CREDITCARD_AUTHORIZATION_64BIT = "CreditCardAuthorization64";
        public static final String CREDITCARD_AUTHORIZATION_S = "CreditCardAuthorizationS";
        public static final String CREDITCARD_AUTHORIZATION_64BIT_S = "CreditCardAuthorizationS64";
        public static final String PAYMENT_BACKEND = "PaymentBackend";
        public static final String B2B_BACKEND = "B2BFrontend";
        public static final String UEMLOAD = "uemload.jar";
        public static final String PLUGIN_SERVICE = "com.dynatrace.easytravel.pluginservice.jar";
    }

    public static class Colors {
        public static final Color WHITE = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
        public static final Color BLACK = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);

        public static final Color BLUE = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
        public static final Color CYAN = Display.getCurrent().getSystemColor(SWT.COLOR_CYAN);
        public static final Color DARK_BLUE = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);
        public static final Color DARK_CYAN = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN);
        public static final Color DARK_GRAY = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
        public static final Color DARK_GREEN = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);
        public static final Color DARK_MAGENTA = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA);
        public static final Color DARK_RED = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);
        public static final Color DARK_YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW);
        public static final Color GRAY = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
        public static final Color GREEN = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
        public static final Color MAGENTA = Display.getCurrent().getSystemColor(SWT.COLOR_MAGENTA);
        public static final Color RED = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
        public static final Color YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
    }

    public static class Images {

        public static final String STATE_ON = "/images/on.png";
        public static final String STATE_OFF = "/images/off.png";

        public static final String PROCEDURE_STOP = "/images/stop_red.gif";

        public static final String PROCEDURE_OK = "/images/procedure_ok.png";
        public static final String PROCEDURE_OK_IIS = "/images/procedure_ok_iis.png";
        public static final String PROCEDURE_OK_GRAY = "/images/procedure_ok_gray.png";
        public static final String PROCEDURE_STOPPED = "/images/procedure_stopped.png";
        public static final String PROCEDURE_STOPPED_GRAY = "/images/procedure_stopped_gray.png";
        public static final String PROCEDURE_PROGRESS = "/images/procedure_progress.png";
        public static final String PROCEDURE_PROGRESS_ORANGE = "/images/procedure_progress_orange.png";
        public static final String PROCEDURE_POWER = "/images/procedure_power.png";
        public static final String PROCEDURE_OK_NGINX = "/images/procedure_ok_nginx.png";
        public static final String PROCEDURE_PROGRESS_NGINX = "/images/procedure_progress_nginx.png";
        public static final String PROCEDURE_STOPPED_NGINX = "/images/procedure_stopped_nginx.png";

        public static final String PROCEDURE_AGENT_FOUND = "/images/agent_online.png";
		public static final String PROCEDURE_AGENT_NOT_FOUND = "/images/agent_offline.png";

		public static final String SEARCH_CLOSE_NORMAL = "/images/search_close_n.gif";
		public static final String SEARCH_CLOSE_HIGHLIGHT = "/images/search_close_h.gif";

		public static final String MENU_ON = "/images/menu_on.png";

        private static final String IMG_FANCYMENU = "/images/fancymenu/";

        public static final String FANCY_MENU_BUTTON_BG_SELECTED = IMG_FANCYMENU + "button_bg_selected_full.png";
        public static final String FANCY_MENU_BUTTON_BG_SHADOW = IMG_FANCYMENU + "button_bg_shadow_full.png";
        public static final String FANCY_MENU_BUTTON_BG = IMG_FANCYMENU + "button_bg_full.png";
        public static final String FANCY_MENU_BUTTON_BG_SELECTED_HOVER = IMG_FANCYMENU + "button_bg_selected_full_hover.png";
        public static final String FANCY_MENU_BUTTON_BG_SHADOW_HOVER = IMG_FANCYMENU + "button_bg_shadow_full_hover.png";
        public static final String FANCY_MENU_BUTTON_BG_HOVER = IMG_FANCYMENU + "button_bg_full_hover.png";

        public static final String FANCY_MENU_LAST_BT_BG_FULL = IMG_FANCYMENU + "last_bt_bg_full.png";
        public static final String FANCY_MENU_LAST_BT_BG_SHADOW_FULL = IMG_FANCYMENU + "last_bt_bg_shadow_full.png";
        public static final String FANCY_MENU_LAST_BT_BG = IMG_FANCYMENU + "last_bt_bg.png";
        public static final String FANCY_MENU_PAGE_BG_TOP = IMG_FANCYMENU + "page_bg_top.png";
        public static final String FANCY_MENU_PAGE_BG_TOP_RIGHT = IMG_FANCYMENU + "page_bg_top_right.png";
        public static final String FANCY_MENU_PAGE_BG_RIGHT = IMG_FANCYMENU + "page_bg_right.png";
        public static final String FANCY_MENU_PAGE_BG_BOTTOM = IMG_FANCYMENU + "page_bg_bottom.png";
        public static final String FANCY_MENU_PAGE_BG_BOTTOM_LEFT = IMG_FANCYMENU + "page_bg_bottom_left.png";
        public static final String FANCY_MENU_PAGE_BG_BOTTOM_RIGHT = IMG_FANCYMENU + "page_bg_bottom_right.png";

        public static final String IMG_COGWHEEL = "/images/zahnrad-th.png";
    }

    public static class InternalMessages {
        public static final String MENU_ACTION_WAS_NULL = "Action for MenuItem ({0}) was NULL!";
    }

    public static class Html {
        public static final String BEGIN_BOLD = "<b>";
        public static final String BEGIN_LINK = "<a>";
        public static final String BREAK = "<br>";
        public static final String END_BOLD = "</b>";
        public static final String END_LINK = "</a>";
        public static final String BEGIN_ITALIC = "<i>";
        public static final String END_ITALIC = "</i>";
        public static final String LINK = "<A name=\"{0}\" href=\"{1}\">{2}</A>";
        public static final String BEGIN_TABLE = "<table>";
        public static final String END_TABLE = "</table>";
        public static final String BEGIN_TABLE_ROW = "<tr>";
        public static final String END_TABLE_ROW = "</tr>";
        public static final String BEGIN_TABLE_DATA = "<td>";
        public static final String END_TABLE_DATA = "</td>";
    }

    public static class Cursors {
        public static final Cursor ARROW = Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW);
        public static final Cursor HAND = Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND);
        public static final Cursor WAIT = Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT);
    }

    public static class Misc {

        public static final String DERBY_LOG_FILE = "Derby.log";
        public static final String SCENARIOS_FILE = "scenarios.xml";
        public static final String USER_SCENARIOS_FILE = "userScenarios.xml";


        public static final String JAVA_COMMAND = "java";
        public static final String JAR_CLASSPATH = "-cp";
        public static final String JAR_OPTION = "-jar";
        public static final String JAVA_AGENT_PATH = "-agentpath";
        public static final String JAVA_BIN = "bin";

        public static final String CMD_PARAM_ADK_AGENT_NAME = "--dt_agentname";
        public static final String CMD_PARAM_ADK_RUXIT_AGENT_NAME = "--ruxit_agentname";

        public static final String CMD_PARAM_ADK_AGENT_LIB = "--dt_agentlibrary";
        public static final String CMD_PARAM_ADK_RUXIT_AGENT_LIB = "--ruxit_agentlibrary";

        public static final String CMPD_PARAM_ADK_SERVER = "--dt_server";
        public static final String CMPD_PARAM_ADK_RUXIT_SERVER = "--ruxit_connection_point";

        public static final String ENV_VAR_ADK_AGENT_NAME = "DT_AGENTNAME";
		public static final String ENV_VAR_ADK_RUXIT_AGENT_NAME = "RUXIT_AGENTNAME";

        public static final String ENV_VAR_ADK_AGENT_LIB = "DT_AGENTLIBRARY";
        public static final String ENV_VAR_ADK_RUXIT_AGENT_LIB = "RUXIT_AGENTLIBRARY";

		public static final String ENV_VAR_ADK_SERVER = "DT_SERVER";
		public static final String ENV_VAR_ADK_RUXIT_CONNECTION_POINT = "RUXIT_CONNECTION_POINT";

        public static final String ENV_VAR_DOTNET_20_AGENT_NAME = "DT_AGENTNAME";
        public static final String ENV_VAR_DOTNET_20_RUXIT_AGENT_NAME = "RUXIT_AGENTNAME";

        public static final String ENV_VAR_DOTNET_20_AGENT_ACTIVE = "DT_AGENTACTIVE";
        public static final String ENV_VAR_DOTNET_20_RUXIT_AGENT_ACTIVE = "RUXIT_AGENTACTIVE";

        //public static final String ENV_VAR_DOTNET_20_AGENT_LIB = "DT_AGENTLIBRARY";
        //public static final String ENV_VAR_DOTNET_20_RUXIT_AGENT_LIB = "RUXIT_AGENTLIBRARY";

        public static final String ENV_VAR_DOTNET_20_SERVER = "DT_SERVER";
        public static final String ENV_VAR_DOTNET_20_RUXIT_CONNECTION_POINT = "RUXIT_CONNECTION_POINT";

        public static final String ENV_VAR_COR_PROFILER = "COR_PROFILER";
        public static final String ENV_VAR_COR_ENABLE_PROFILING = "COR_ENABLE_PROFILING";

        public static final String ENV_VAR_WEBSERVER_LIBRARY_PATH = "LD_LIBRARY_PATH";
        public static final String ENV_VAR_MYSQL_LIBRARY_PATH = "LD_LIBRARY_PATH";

        public static final String ENV_VAR_NGINX_LIBRARY_PATH = "LD_PRELOAD";

        public static final String SETTING_ENABLED = "enabled";
        public static final String SETTING_VALUE_ON = "on";
        public static final String SETTING_VALUE_OFF = "off";
		public static final String SETTING_PERSISTENCE_MODE = "persistenceMode";
        public static final String SETTING_INSTALLATION_MODE = "installationMode";
		public static final String SETTING_COPY_DERBY_DATA_FROM = "copyDerbyDataFrom";
		public static final String SETTING_TYPE_PLUGIN_HOSTS = "pluginhosts";
	    public static final String SETTING_TYPE_PLUGIN = "plugin";

        public static final String WIN_PROGRAMS_DIR_X86_EXTENSION = " (x86)";
		public static final int AUTOMATIC_PLUGIN_ON_OFF_DISABLED = -1;

    }

    public static class Procedures {
    	// TODO: replace this with an enum!

        public static final String BUSINESS_BACKEND_ID = "business backend";
        public static final String CREDIT_CARD_AUTH_UNIT_ID = "credit card authorization";
        public static final String CUSTOMER_FRONTEND_ID = "customer frontend";
        public static final String ANGULAR_FRONTEND_ID = "customer frontend rest";
        public static final String DATABASE_CONTENT_CREATOR_ID = "database content creator";
        public static final String COUCHDB_ID = "couchdb";
        public static final String COUCHDB_CONTENT_CREATOR_ID = "couchdb content creator";
        public static final String MYSQL_CONTENT_CREATOR_ID = "MySQL content creator";
        public static final String INPROCESS_DBMS_ID = "inprocess DBMS";
        public static final String INPROCESS_MYSQL_ID = "inprocess MYSQL";
        public static final String PAYMENT_BACKEND_ID = "payment backend";
        public static final String B2B_FRONTEND_ID = "b2b frontend";
        public static final String ANT_ID = "ant";
        public static final String APACHE_HTTPD_ID = "apache_httpd";
        public static final String NGINX_WEBSERVER_ID = "nginx";
		public static final String CASSANDRA_ID = "cassandra";
		public static final String MONGO_DB_ID = "mongodb";
		public static final String HBASE_ID = "hbase";
        public static final String APACHE_HTTPD_PHP_ID = "apache_httpd_php";
		public static final String INCREASING_LOAD_ID = "increasing_load";
		public static final String THIRDPARTY_SERVER_ID = "third party server";
		public static final String UNHOOKABLE_HTTP_SERVER_ID = "unhookable http server";
		public static final String BROWSER_ID = "browser";
		public static final String WEBSERVER_AGENT_RESTART_ID = "webserver agent control";
		public static final String HOST_AGENT_RESTART_ID = "host agent control";
		public static final String PLUGIN_SERVICE = "plugin service";
		public static final String PREPARE_VMWARE_ID = "prepare vmware";
		public static final String VMOTION_ID = "vmotion";
		public static final String VAGRANT_ID = "vagrant";

		// all Procedures that can be started remotely
		public static final String[] ALL_REMOTE = new String[] {
			APACHE_HTTPD_ID,
			APACHE_HTTPD_PHP_ID,
            NGINX_WEBSERVER_ID,
			CUSTOMER_FRONTEND_ID,
			B2B_FRONTEND_ID,
	        BUSINESS_BACKEND_ID,
	        PAYMENT_BACKEND_ID,
	        CREDIT_CARD_AUTH_UNIT_ID,
			THIRDPARTY_SERVER_ID,
			CASSANDRA_ID,
			INPROCESS_MYSQL_ID,
			MYSQL_CONTENT_CREATOR_ID,
			WEBSERVER_AGENT_RESTART_ID,
			HOST_AGENT_RESTART_ID,
			PLUGIN_SERVICE,
			VAGRANT_ID
	        //DATABASE_CONTENT_CREATOR_ID,
	        //INPROCESS_DBMS_ID,
	        //ANT_ID,
	        // not used any more: UEMLOAD_ID,
			// not a real scenario: INCREASING_LOAD_ID
			// multiple MongoDB instances require more work to enable replication: MONGO_DB_ID
		};

		// all Procedures that can be started locally or remotely
		public static final String[] ALL = new String[] {
	        BUSINESS_BACKEND_ID,
	        CREDIT_CARD_AUTH_UNIT_ID,
	        CUSTOMER_FRONTEND_ID,
	        DATABASE_CONTENT_CREATOR_ID,
	        COUCHDB_ID,
	        COUCHDB_CONTENT_CREATOR_ID,
	        MYSQL_CONTENT_CREATOR_ID,
	        INPROCESS_DBMS_ID,
	        INPROCESS_MYSQL_ID,
	        PAYMENT_BACKEND_ID,
	        B2B_FRONTEND_ID,
	        // this procedure needs special properties in the scenario to start: ANT_ID,
	        APACHE_HTTPD_ID,
	        NGINX_WEBSERVER_ID,
			CASSANDRA_ID,
			MONGO_DB_ID,
			HBASE_ID,
	        APACHE_HTTPD_PHP_ID,
			// not a real procedure: INCREASING_LOAD_ID,
			THIRDPARTY_SERVER_ID,
			UNHOOKABLE_HTTP_SERVER_ID,
			BROWSER_ID,
			WEBSERVER_AGENT_RESTART_ID,
			HOST_AGENT_RESTART_ID,
			PLUGIN_SERVICE,
			PREPARE_VMWARE_ID,
			VMOTION_ID,
		};
    }

    public static class ConfigurationXml {

        public static final String ATTRIBUTE_ENABLED = "enabled";
        public static final String ATTRIBUTE_ID = "id";
        public static final String ATTRIBUTE_NAME = "name";
        public static final String ATTRIBUTE_COMPATIBILITY = "compatibility";
        public static final String ATTRIBUTE_TITLE = "title";
        public static final String ATTRIBUTE_TYPE = "type";
        public static final String ATTRIBUTE_VALUE = "value";
        public static final String ATTRIBUTE_HOST = "host";
        public static final String ATTRIBUTE_APM_TENANT = "apmTenant";
        public static final String ATTRIBUTE_STAY_OFF_DURATION = "stayoffduration";
        public static final String ATTRIBUTE_STAY_ON_DURATION = "stayonduration";
        public static final String NODE_TECHNOLOGY_PROPERTIES = "TechnologyProperties";
	    public static final String NODE_USER_INTERFACE_AVAILABILITY_PROPERTIES= "UIAvailabilityProperties";
    }

    public static class CmdArguments {

        public static final String PROPERTY_FILE = BaseConstants.CmdArguments.PROPERTY_FILE;
        public static final String SCENARIO_FILE = "scenariofile";
        public static final String START_SCENARIO = "startscenario";
        public static final String START_GROUP = "startgroup";
        public static final String NO_AUTOSTART = "noautostart";
        public static final String MANUAL_START = "manualstart";
        public static final String HELP = "help";
    }

    public static class REST {
    	public static final String SHUTDOWN = RESTConstants.SHUTDOWN;
    	public static final String PING = RESTConstants.PING;
    	public static final String PREPARE = RESTConstants.PREPARE;
    	public static final String START = RESTConstants.START;
    	public static final String STOP = RESTConstants.STOP;
    	public static final String STOP_ALL = RESTConstants.STOP_ALL;
    	public static final String STATUS = RESTConstants.STATUS;
    	public static final String STATUS_ALL = RESTConstants.STATUS_ALL;
    	public static final String LOG = RESTConstants.LOG;
    	public static final String DETAILS = RESTConstants.DETAILS;
    	public static final String URI = RESTConstants.URI;
    	public static final String CONTINUOUSLY = RESTConstants.CONTINUOUSLY;
    	public static final String TECHNOLOGY = RESTConstants.TECHNOLOGY;
    	public static final String AGENT_FOUND = RESTConstants.AGENT_FOUND;
    	public static final String IS_INSTRUMENTATION_SUPPORTED = RESTConstants.IS_INSTRUMENTATION_SUPPORTED;
    	public static final String DCRUM_AVAILABILITY_SERVLET = RESTConstants.DCRUM_AVAILABILITY_SERVLET;
    	public static final String DCRUM_SERVLET = RESTConstants.DCRUM_SERVLET;
    	public static final String VERSION = RESTConstants.VERSION;
    	public static final String DOT_NET_ENABLED = RESTConstants.DOT_NET_ENABLED;
    	public static final String IS_RUNNING_ON_IIS = RESTConstants.IS_RUNNING_ON_IIS;

    	public static final String PROPERTY_DELIMITER = RESTConstants.PROPERTY_DELIMITER;

    	public static final String MANAGEMENT_CLIENT_ACTIVATE = RESTConstants.MANAGEMENT_CLIENT_ACTIVATE;
    	public static final String MANAGEMENT_CLIENT_OPEN_DASHBOARD = RESTConstants.MANAGEMENT_CLIENT_OPEN_DASHBOARD;

    	public static final String SYNTHETIC_REQUESTS = RESTConstants.SYNTHETIC_REQUESTS;
    	public static final String MANUAL_VISITS  = RESTConstants.MANUAL_VISITS;
    	public static final String LOAD_VALUE  = RESTConstants.LOAD_VALUE;
    }

	/** --- all that are enabled / disabled from scenario config --- */
    public static class Plugin {
    	public static final String BadCacheSynchronization = "BadCacheSynchronization";
    	public static final String BookingErrorAsHttp500 = "BookingErrorAsHttp500";
    	public static final String CPULoad = "CPULoad";
    	public static final String CreditCardCheckError500 = "CreditCardCheckError500";
    	public static final String DBSpamming = "DBSpamming";
    	public static final String DatabaseAccessExceedPoolNonThreaded = "DatabaseAccessExceedPoolNonThreaded";
    	public static final String DatabaseAccessExceedPoolThreaded = "DatabaseAccessExceedPoolThreaded";
    	public static final String DatabaseAccessFromFrontend = "DatabaseAccessFromFrontend";
    	public static final String DatabaseAccessFromFrontendHeavy = "DatabaseAccessFromFrontendHeavy";
    	public static final String DatabaseAccessPoolContention = "DatabaseAccessPoolContention";
    	public static final String DatabaseAccessPoolContentionSync = "DatabaseAccessPoolContentionSync";
    	public static final String DatabaseCleanup = "DatabaseCleanup";
    	public static final String DatabaseReducePoolSize = "DatabaseReducePoolSize";
    	public static final String DeadlockInFrontend = "DeadlockInFrontend";
    	public static final String DisableHibernateCache = "DisableHibernateCache";
    	public static final String DotNetPaymentService = "DotNetPaymentService";
    	public static final String DummyNativeApplication = "DummyNativeApplication";
    	public static final String DummyNativeApplication_NET = "DummyNativeApplication.NET";
    	public static final String DummyPagePlugin = "DummyPagePlugin";
    	public static final String DummyPaymentService = "DummyPaymentService";
    	public static final String EnableCaching = "EnableCaching";
    	public static final String FetchSizeTooSmall = "FetchSizeTooSmall";
    	public static final String FindJourneysSqlServerQueryOverride = "FindJourneysSqlServerQueryOverride";
    	public static final String GarbageCollectionEvery10Seconds = "GarbageCollectionEvery10Seconds";
    	public static final String HiddenIframeAmazonTracking = "HiddenIframeAmazonTracking";
    	public static final String HotDealClientJMS = "HotDealClientJMS";
    	public static final String HotDealClientRMI = "HotDealClientRMI";
    	public static final String HotDealServerJMS = "HotDealServerJMS";
    	public static final String HotDealServerRMI = "HotDealServerRMI";
    	public static final String HttpRedirect = "HttpRedirect";
    	public static final String AdsExtension = "AdsExtension";
    	public static final String IncludeSocialMedia = "IncludeSocialMedia";
    	public static final String IncludeSocialMedia_NET = "IncludeSocialMedia.NET";
    	public static final String InfiniteLoopInFrontend = "InfiniteLoopInFrontend";
    	public static final String JQueryEffectsOptimized = "JQueryEffectsOptimized";
    	public static final String JQueryEffectsStandard = "JQueryEffectsStandard";
    	public static final String JQueryEffectsCloudflare = "JQueryEffectsCloudflare";
    	public static final String JavascriptError = "JavascriptError";
    	public static final String JavascriptTagging = "JavascriptTagging";
		public static final String JavascriptBootstrapAgent = "JavascriptBootstrapAgent";
    	public static final String JourneySearchError404 = "JourneySearchError404";
    	public static final String JourneySearchError500 = "JourneySearchError500";
    	public static final String JourneyUpdateFast = "JourneyUpdateFast";
    	public static final String JourneyUpdateSlow = "JourneyUpdateSlow";
    	public static final String LargeMemoryLeak = "LargeMemoryLeak";
    	public static final String WebFontGoogle = "WebFontGoogle";
    	public static final String LoginProblems = "LoginProblems";
    	public static final String MediumMemoryLeak = "MediumMemoryLeak";
    	public static final String Memcached_NET = "Memcached.NET";
    	public static final String MissingServletError404 = "MissingServletError404";
    	public static final String NamedPipeNativeApplication = "NamedPipeNativeApplication";
    	public static final String NamedPipeNativeApplication_NET = "NamedPipeNativeApplication.NET";
    	public static final String Print = "Print";
    	public static final String PrintClusterNode = "PrintClusterNode";
    	public static final String SimpleMemoryCache = "SimpleMemoryCache";
    	public static final String SimpleMemoryCache_NET = "SimpleMemoryCache.NET";
    	public static final String SlowAuthentication = "SlowAuthentication";
    	public static final String SlowImages = "SlowImages";
    	public static final String SlowThirdPartyAdvertisements = "SlowThirdPartyAdvertisements";
    	public static final String SlowUserLogin = "SlowUserLogin";
    	public static final String SmallMemoryLeak = "SmallMemoryLeak";
    	public static final String SocketNativeApplication = "SocketNativeApplication";
    	public static final String ThirdPartyAdvertisements = "ThirdPartyAdvertisements";
    	public static final String ThirdPartyContent = "ThirdPartyContent";
    	public static final String TravellersOptionBox = "TravellersOptionBox";
    	public static final String UseFinanceServiceWCF_NET = "UseFinanceServiceWCF.NET";
    	public static final String UseLocationCache = "UseLocationCache";
    	public static final String WPOPagePlugin = "WPOPagePlugin";

    	public static final String CTGNativeApplication = "CTGNativeApplication";
    	public static final String WSMQNativeApplication = "WSMQNativeApplication";
    	public static final String IMSNativeApplication = "IMSNativeApplication";

    	public static final String CouchDB = "CouchDB";
    }
}
