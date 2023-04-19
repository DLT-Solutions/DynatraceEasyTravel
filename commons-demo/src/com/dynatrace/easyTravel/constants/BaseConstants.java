package com.dynatrace.easytravel.constants;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;


public class BaseConstants {

    public static final String BACK_SLASH = "\\";
    public static final String FSLASH = "/";
    public static final String BRACKET_LEFT = "[";
    public static final String BRACKET_RIGHT = "]";
    public static final String COLON = ":";
    public static final String COMMA = ",";
    public static final String COMMA_WS = ", ";
    public static final String CRLF = "\n";
    public static final String CRLF_OS_INDEPENDENT = System.getProperty("line.separator");
    public static final String DOT = ".";
    public static final String DQUOTE = "\"";
    public static final String EMPTY_STRING = "";
    public static final String EQUAL = "=";
    public static final String FORWARD_SLASH = "/";
    public static final String MINUS = "-";
    public static final String SQUOTE = "'";
    public static final String DSQUOTE = "''";
    public static final String WS = " ";
    public static final String FORMAT_STRING_PERCENT_20 = "%20";
    public static final String DBSLASH = "\\\\";
    public static final String AMP = "&"; // CharacterConstants.AMP;
    public static final String PLUS = "+";
    public static final String FTICK = "\u00B4"; // Should be char "U+00B4", must not be "U+0092"(like in some codepages)
    public static final String BTICK = "\u0060";
    public static final String DWS = "  "; // double whitespace
    public static final String DOTDOTDOT = "...";
	public static final String SCOLON = ";";
	public static final String SCOLON_WS = "; ";
	public static final String QMARK = "?";
	public static final String HASH = "#";
	public static final String PIPE = "|";
	public static final String delimiter_h = "h";

    /** * round brackets ** */
    public static final String WS_LRBRA = " (";
    public static final String LRBRA = "(";
    public static final String RRBRA = ")";
    public static final String RRBRA_WS = ") ";

    /** * angle brackets ** */
    public static final String RABRA_WS = "> ";
    public static final String WS_LABRA = " <";
    public static final String RABRA = ">";
    public static final String LABRA = "<";
    /** * curly brackets ** */
    public static final String RCBRA_WS = "} ";
    public static final String WS_LCBRA = " {";
    public static final String RCBRA = "}";
    public static final String LCBRA = "{";
    /** * square brackets ** */
    public static final String RSBRA_WS = "] ";
    public static final String WS_LSBRA = " [";
    public static final String RSBRA = "]";
    public static final String LSBRA = "[";

    public static final String AUTO = "auto";
    public static final String NONE = "none";
    public static final String CREDITCARD_PIPE_CHANNEL = "easyTravelPipe";
    public static final String DYNATRACE = "dynaTrace";
    public static final String COMPUWARE = "Compuware";
    public static final String DYNATRACE_AGENT = "dynaTrace Agent";
    public static final String DT_INSTRUMENTATION_SERVER = "server";
    public static final String DT_INSTRUMENTATION_SYSTEM_PROFILE = "name";
    public static final String DT_INSTRUMENTATION_LOGPATH = "logpath";
    public static final String EASYTRAVEL = "easyTravel";
    public static final String LOG_FILENAME_PATTERN = "{0}_%i.log";
    public static final String LOG_SIZE_SYSTEM_PROPERTY_NAME = "logSizeMB";
    public static final String PACKAGE_COM_DYNATRACE = "com.dynatrace";
    public static final String PACKAGE_COM_DYNATRACE_EASYTRAVEL = PACKAGE_COM_DYNATRACE + ".easytravel";
    public static final String PIPE_PREFIX = SystemUtils.IS_OS_WINDOWS ? "\\\\.\\pipe\\" : "/tmp/";
    public static final String TOMCAT_SHUTDOWN = "SHUTDOWN-TOMCAT";
    public static final String LOCALHOST = "localhost";
    public static final String UNDERSCORE = "_";
    public static final String UNKNOWN = "unknown";
    public static final String UTF8 = "UTF-8";
	public static final String LINK_HREF = "<a href=\"{0}\" target=\"_blank\">{1}</a>";
	public static final String LOGOUT_LINK_HREF = "<a href=\"{0}\">{1}</a>";
	public static final String CONFIGURATION_SERVICE = "ConfigurationService";
	public static final String UEM_LOAD_SHUTDOWN_PORT = "uemLoadShutdownPort";
	public static final String UEM_LOAD_THREAD = "Uem-Load-Thread";
	public static final String UEM_LOAD_HOST_AVAILABILITY_THREAD = "Uem-Load-Host-Availability-Thread";
	public static final String UEM_LOAD_HOST_PLUGIN_ENABLEMENT_WATCHER_THREAD = "Uem-Load-Plugin-Enablement-Watcher-Thread";
	public static final String AUTOMATIC_PROCEDURE_SHUTDOWN_THREAD = "Automatic-Procedure-Shutdown-Thread";

	/** * CONNECTION ** */
	public static final String CONNECTION_COLON = COLON;
	public static final String CONNECTION_SLASHSLASH = "//";
	public static final String CONNECTION_SLASH = "/";

	/** * EXTENSIONS / FILE TYPES ** */
	public static final String EXTENSION_JS = ".js";
	public static final String EXTENSION_PNG = ".png";
	public static final String EXTENSION_JPG = ".jpg";
	public static final String EXTENSION_JPEG = ".jpeg";

    public static class SystemProperties {

        /* derby system properties: http://db.apache.org/derby/docs/10.2/tuning -> Derby properties */
    	public static final String DERBY_PORT_NUMBER = "derby.drda.portNumber";
    	public static final String DERBY_HOST = "derby.drda.host";
        public static final String DERBY_LOGGER_METHOD = "derby.stream.error.method";
        public static final String DERBY_SYSTEM_HOME = "derby.system.home";

        public static final String JAVA_HOME = "java.home";
        public static final String OS_ARCH = "os.arch";
        public static final String OS_VERSION = "os.version";
        public static final String OS_NAME = "os.name";
        public static final String USER_DIR = "user.dir"; // current working directory
        public static final String USER_HOME = "user.home"; // home directory of current user
		public static final String OVERRIDE_USER_HOME = "com.dynatrace.easytravel.override.user.home"; // home directory of
// current user

        /** Enables to manipulation the installation directory. Useful if launcher application is running from IDE. */
        public static final String INSTALL_DIR_CORRECTION = PACKAGE_COM_DYNATRACE_EASYTRAVEL + ".install.dir.correction";

		/**
		 * Allows to specify a different home-directory, i.e. the "easyTravel" part under C:\Users\<user>\.dynaTrace\easyTravel
		 * 2.0.0
		 */
        public static final String HOME_DIR_CORRECTION = PACKAGE_COM_DYNATRACE_EASYTRAVEL + ".home.dir.correction";

        /**
         * Enables to set the directory to look for dynaTrace Agents. Useful in development stage
         * because the demo application is not located within the dynaTrace programs directory in
         * most cases.
         */
        public static final String AGENT_LOOKUP_DIR = PACKAGE_COM_DYNATRACE_EASYTRAVEL + ".agent.lookup.dir";

        /**
         * Property to select the agent lookup directory for developers.
         */
        public static final String AGENT_LOOKUP_DIR_DEV = AGENT_LOOKUP_DIR + ".dev";
		public static final String PERSISTENCE_MODE = "com.dynatrace.easytravel.persistence.mode";
        public static final String INSTALLATION_MODE = "com.dynatrace.easytravel.installation.mode";
        public static final String DONT_USE_LOCAL_ET_PROPERTIES = "com.dynatrace.easytravel.dont_use_local_et_properties";
        public static final String DONT_USE_PRIVATE_ET_PROPERTIES = "com.dynatrace.easytravel.dont_use_private_et_properties";
        public static final String DONT_USE_WAR_ET_PROPERTIES = "com.dynatrace.easytravel.dont_use_war_et_properties";
        public static final String DISABLE_PORT_CHECK_VIA_NETSTAT = "disablePortCheckViaNetstat";
    }

    public static class SubDirectories {

        public static final String AGENT = "agent";
        public static final String BUSINESS = "business";
		public static final String CASSANDRA = "cassandra";
        public static final String CANOO = "canoo";
        public static final String CONFIG = "config";
        public static final String CUSTOMER = "customer";
        public static final String ANGULAR = "angular";
        public static final String DATABASE = "database";
        public static final String EASYTRAVEL_BLOG = "easyTravelBlog";
        public static final String DYNATRACE_HOME = DOT + DYNATRACE;
        public static final String LOG = "log";
        public static final String SELENIUM = "selenium";
        public static final String TEMP = "tmp";
        public static final String TEST = "test";
        public static final String TESTS = "tests";
        public static final String THIRDPARTY = "thirdparty";
        public static final String JUNIT = "junit";
        public static final String WEBAPI = "webapi";
        public static final String CURRENT = ".";
        public static final String WEB_APP = "weblauncher" + File.separator + "webapp";
        public static final String COUCHDB = "couchdb";
        public static final String PHP = "php";
        public static final String MySQL = "mysql";
		public static final String JRE_BIN = "jre/bin";
		public static final String RESOURCES = "resources";
		public static final String HBASE = "hbase";
		public static final String MONGODB = "mongodb";
		public static final String LIB = "lib";
		public static final String CHROME = "chrome";
	}

    public static class CustomerFrontendArguments {

		public static final String ROUTEPREFIX = "routeprefix";
        public static final String PORT = "port";
        public static final String SHUTDOWN_PORT = "shutdownport";
        public static final String AJP_PORT = "ajpport";
        public static final String CONTEXT_ROOT = "contextroot";
        public static final String WEBAPP_BASE = "webappbase";
    }

	public static class CassandraArgument {

		public static final String PORT = "port";
		public static final String HOST = "host";
	}

	public static class BusinessBackend {

		public static class Persistence {

			public static final String JPA = "jpa";
			public static final String CASSANDRA = "cassandra";
			public static final String MONGODB = "mongodb";
			public static final String HBASE = "hbase";
		}
	}


    public static class Browser {

        public static final String REMOTE_OPEN_URL = " -remote openURL(";
        public static final String FILE_PROTOCOL = "file:///";
        public static final String PLATFORM_WIN32 = "win32";
        public static final String PLATFORM_RAP = "rap";
        // the artificial "title" is needed here in order to avoid problems, see e.g. JLT-48197
        // for some reason Windows requires to have this "title" and it also requires the separate quotes!!
		public static final String[] CMC_C_START_ARRAY = { "cmd", "/c", "start", "\"title\"" };
        public static final String PLATFORM_CARBON = "carbon";
        public static final String USR_BIN_OPEN = "/usr/bin/open ";
        public static final String STRING_FILE = "file:";

        /** * BROWSERS ** */
        public static final String BROWSER_NETSCAPE = "netscape";
        public static final String BROWSER_MOZILLA = "mozilla";
        public static final String BROWSER_FIREFOX = "firefox";
        public static final String BROWSER_GNOME_OPEN = "gnome-open";
        public static final String BROWSER_KDE_OPEN = "kde-open";

        // ordered list of choices that are checked when trying to open a file
		public static final String[] BROWSER_CHOICES = { BROWSER_GNOME_OPEN, BROWSER_KDE_OPEN, BROWSER_FIREFOX, BROWSER_MOZILLA,
				BROWSER_NETSCAPE };
    }

    public static class CmdArguments {

        public static final String PROPERTY_FILE = "propertyfile";
		public static final String PERSISTENCE_MODE = "persistencemode";
        public static final String INSTALLATION_MODE = "installationmode";
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!! USE OF THESE CONSTANTS IS DEPRECATED !!!!!!!!!!!!
    // FOR ANY FUTURE NEEDS, USE CONSTANTS FROM PluginChangeMonitor
    public static class Plugins {

		public static final String DC_RUM_EMULATOR = "DCRUMEmulationPlugin";
		public static final String TABLET_CRASHES = "TabletCrashes";
		public static final String MOBILE_CRASHES_PEAK = "MobileCrashesPeak";
		public static final String MOBILE_ERRORS = "MobileErrors";
		public static final String SLOW_TRANSACTION_FOR_PHP_BLOG = "SlowTransactionForPHPBlog";
		public static final String PHP_ENABLEMENT_PLUGIN = "PHPEnablementPlugin";
		public static final String ADS_ENABLEMENT_PLUGIN = "HTTPErrorForBlogAds";
		public static final String SLOW_APACHE_WEBSERVER = "SlowApacheWebserver";
		public static final String COUCHDB = "CouchDB";
    }
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	public static class PluginsDir {
		public static final String PLUGINS_BACKEND = "plugins-backend";
		public static final String PLUGINS_FRONTEND = "plugins-frontend";
		public static final String PLUGINS_SHARED = "plugins-shared";
		public static final String PLUGINS_UNUSED = "plugins-unused";
	}

    public static class LoggerNames {

        public static final String BUSINESS_BACKEND = "BusinessBackend";
        public static final String CUSTOMER_FRONTEND = "CustomerFrontend";
        public static final String ANGULAR_FRONTEND = "AngularFrontend";
        public static final String PAYMENT_BACKEND = "PaymentBackend";
        public static final String B2B_FRONTEND = "B2BFrontend";
        public static final String UEM_LOAD = "uemload";
        public static final String ANT = "Ant";
        public static final String CREDIT_CARD_AUTHORIZATION = "CreditCardAuthorization";
        public static final String THIRDPARTY_CONTENT = "ThirdPartyContent";
        public static final String PLUGIN_SERVICE = "PluginService";
    }

    public static class VisitNames {
    	public static final String DEFAULT = "Default Visit";
    	public static final String AMP = "AMP Visit";
    	public static final String MAGENTO = "Magento Shop";
    	public static final String WORDPRESS = "WordPress Shop";
    	public static final String ANGULAR_JS = "AngularJS Image Gallery";

    	public static final String HEADLESS_ALMOST_CONVERTED_VISIT = "Headless Almost Converted Visit";
    	public static final String HEADLESS_BOUNCE_VISIT = "Headless Bounce Visit";
    	public static final String HEADLESS_CONVERTED_VISIT = "Headless Converted Visit";
    	public static final String HEADLESS_IMAGE_GALLERY_VISIT = "Headless Image Gallery Visit";
    	public static final String HEADLESS_MAGENTO_SHOP_VISIT ="Headless Magento Shop Visit";
    	public static final String HEADLESS_PAGE_WANDERER_VISIT = "Headless Page Wanderer Visit";
    	public static final String HEADLESS_SEARCH_VISIT = "Headless Search Visit";
    	public static final String HEADLESS_SELECT_MENU_OPTIONS_VISIT = "HeadlessSelectMenuOptionsVisit";
    	public static final String HEADLESS_OVERLOAD_VISIT = "Headless Overload Visit";

    	public static final String HEADLESS_ONLINE_BOUTIQUE_BUY_VISIT = "Headless Online Boutique Buy Visit";
    	
    	public static final String HEADLESS_B2B_SIMPLE_VISIT = "Headless B2B Simple Visit";
    	public static final String HEADLESS_B2B_ALL_MENU_OPTIONS_VISIT = "Headless B2B All Menu Options Visit";
    	
    	public static final String EASYTRAVEL_PAGE_WANDERER = "Page Wanderer";
    	public static final String EASYTRAVEL_BOUNCE = "Bounce";
    	public static final String EASYTRAVEL_SPECIAL_OFFERS = "Special Offers";
    	public static final String EASYTRAVEL_SEARCH = "Search";
    	public static final String EASYTRAVEL_SEO = "Seo";
    	public static final String EASYTRAVEL_ALMOST_CONVERT = "Almost Convert";
    	public static final String EASYTRAVEL_CONVERT = "Convert";
    	public static final String EASYTRAVEL_B2B = "B2B";

    	public static final String MOBILE_BOUNCE = "Mobile App - Bounce";
    	public static final String MOBILE_SEARCH = "Mobile App - Search Journey";
    	public static final String MOBILE_BOOKING_AND_CRASH = "Mobile App - Booking Journey and Crash";
    	public static final String MOBILE_MANY_ACTION_SESSION = "Mobile App - Many Action Session";
    	public static final String MOBILE_SPECIAL_OFFERS = "Mobile App - Special Offers";

    	public static final String ANGULAR_ALMOST_CONVERTED_VISIT = "Angular Almost Converted Visit";
    	public static final String ANGULAR_BOUNCE_VISIT = "Angular Bounce Visit";
    	public static final String ANGULAR_CONVERTED_VISIT = "Angular Converted Visit";
    	public static final String ANGULAR_SEARCH_VISIT = "Angular Search Visit";
    	public static final String ANGULAR_SELECT_MENU_OPTIONS_VISIT = "Angular Select Menu Options Visit";
    	public static final String ANGULAR_PAGE_WANDERER_VISIT = "Angular Page Wanderer Visit";
    	public static final String ANGULAR_SPECIAL_OFFERS_CONVERT_VISIT = "Angular Special Offers Convert Visit";
    	public static final String ANGULAR_OVERLOAD_VISIT = "Angular Overload Visit";
    }

	public static class VisitExtras {
		public static class OnlineBoutique {
			public static enum SelectQuantity {
				ONE("1"),
				TWO("2"),
				THREE("3"),
				FOUR("4"),
				FIVE("5"),
				TEN("10"),
				;
				public static final List<SelectQuantity> values = cacheValues();
				private final String value;
				
				private SelectQuantity (String value){
					this.value = value;
				}

				private static List<SelectQuantity> cacheValues(){
					return List.of(SelectQuantity.values());
				}
				
				public String get(){
					return value;
				}
			}
		}
	}
    
    public static class OpenkitLogs{
    	public static String LOG_FILE_COUNT_PROPERTY_NAME = "openkitLogFileCount";
    	public static String FILE_PREFIX = "Openkit";
    }

    public static class ScenarioNames {
    	private ScenarioNames() {
    		throw new IllegalStateException("Utility class");
    	}
    	public static final String HEADLESS_CUSTOMER = "Headless Customer Scenario";
    	public static final String HEADLESS_ANGULAR = "Headless Angular Scenario";
    	public static final String HEADLESS_ANGULAR_MOBILE = "Headless Angular Mobile Scenario";
    	public static final String HEADLESS_ANGULAR_MOUSE_VISIT = "Headless Angular Mouse Visit";
    	public static final String HEADLESS_ANGULAR_SPECIAL_USER_VISIT = "Headless Angular Special User Visit";
    	public static final String HEADLESS_B2B = "Headless B2B Scenario";
    	public static final String HEADLESS_ONLINE_BOUTIQUE = "Headless Online Boutique";
    }
    
    public enum B2BAccount {
    	PERSONAL_TRAVEL_INC("Personal Travel Inc.", "pti"),
    	SPEED_TRAVEL_AGENCY("Speed Travel Agency", "sta")
    	;
    	
    	private final String login;
    	private final String password;
    	
    	B2BAccount(String login, String password){
    		this.login = login;
    		this.password = password;
    	}
    	
    	public String getLogin() {
    		return login;
    	}
    	
    	public String getPassword() {
    		return password;
    	}
    }

    public interface DtHeader {

    	String X_DYNATRACE = "X-dynaTrace";
    	String X_DYNATRACE_MOBILE_ONEAGENT = "x-dynatrace";
    	String VIRTUAL_USER_ID = "VU";
    	String TIMER_NAME = "NA";
    	String PAGE_CONTEXT = "PC";
    	String SOURCE_ID = "SI";
    	String GEOGRAPHIC_REGION = "GR";
    	String AGENT_NAME = "AN";
    	String SCRIPT_NAME = "SN";
    	String TEST_NAME = "TE";

    	interface TimerName {

    		String START_PAGE = "EasyTravel Start Page";
    		String LOGIN = "Login";
    		String SEARCH = "Search";
    		String BOOKING_REVIEW = "Booking Review";
    		String PAYMENT = "Payment";
    		String PURCHASE = "Purchase";
    		String TERMS = "Terms";
    		String PRIVACY = "Privacy";
    		String CONTACT = "Contact";
    		String ABOUT = "About";
    		String SEO = "SEO";
    		String LOGOUT = "Logout";
    		String FINISH_BOOKING = "Finish Booking Procedure";
    		String B2B_HOME = "B2B Home";
    		String B2B_LOGIN = "B2B Login";
    		String B2B_LOGOUT = "B2B Logout";
    		String B2B_JOURNEYS = "B2B Journeys";
    		String B2B_REPORT = "B2B Report";
    		String B2B_BOOKING = "B2B Booking";
    		String TRIPDET = "Trip details";
    		String BLOGDET = "Blog - Post";
    		String WEATHERFORECAST = "Weather forecast";
    		String IMAGE_GALLERY = "Image Gallery";
			String SPECIAL = "Special offers";
			String MAGENTO_SHOP_START_PAGE = "Magento Shop Start Page";
			String MAGENTO_SHOP_TRAVEL_GEAR_PAGE = "Magento Bags and Luggage - Accessories";
			String MAGENTO_SHOP_SALE_PAGE = "Magento Sale";
			String MAGENTO_SHOP_ACCESSORIES_PAGE = "Magento Accessories";
			String MAGENTO_SHOP_WOMEN_PAGE = "Magento Women";
			String MAGENTO_SHOP_MEN_PAGE = "Magento Men";
			String MAGENTO_SHOP_HOME_DECOR_PAGE = "Magento Home & Decor";
			String MAGENTO_SHOP_VIP_PAGE = "Magento VIP";

			String WORDPRESS_SHOP_START_PAGE = "DynaShop - Just another WordPress site";
			String WORDPRESS_SHOP_BLOG = "Blog - DynaShop";
			String WORDPRESS_SHOP_ACCESSORIES = "Accessories - DynaShop";
			String WORDPRESS_SHOP_CLOTHING = "Clothing - DynaShop";
			String WORDPRESS_SHOP_DECOR = "Decor - DynaShop";
			String WORDPRESS_SHOP_HOODIES = "Hoodies - DynaShop";
			String WORDPRESS_SHOP_MUSIC = "Music - DynaShop";
			String WORDPRESS_SHOP_TSHIRTS = "Tshirts - DynaShop";
			String WORDPRESS_SHOP_PRODUCT = "Product - DynaShop";

			String AMP_WEBSITE = "AMP Website";
    	}

    	interface PageContext {

    		String CUSTOMER_FRONTEND = "CustomerFrontend";
    		String B2B_FRONTEND = "B2BFrontend";
    		String MAGENTO = "Magento";
    		String WORDPRESS = "WordPress";
    	}

    	interface ScriptName {

    		String BOOKING_JOURNEY = "Booking journey";
    		String TERMS_CONTRACT = "Requesting terms of contract";
    		String PRIVACY_AGREEMENT = "Requesting privacy agreement";
    		String ABOUT_INFORMATION = "Requesting about information";
    		String SEO_INFORMATION = "Requesting Search Engine Optimization information";
    		String LOGOUT_INFORMATION = "Logging out";
    		String CONTACT_INFORMATION = "Requesting contact information";
    		String ADMINISTRATION = "Administration";
    		String TRIPDETAILS = "Trip details for journeys";
    		String BLOGDETAILS = "Blog posts";
    		String WEATHERFORECAST = "Weather forecast";
    		String IMAGE_GALLERY = "Image Gallery";
			String SECIAL = "Special offers";
			String MAGENTO_SHOP = "Magento shop";
			String WORDPRESS_SHOP = "WordPress Shop";
			String AMP_WEBSITE = "AMP Website";
    	}

    	interface Source {

    		String WEB_LOAD_TESTING = "WLT";
    	}
    }


	public interface Uem {

		String DT_MONITOR = "dynaTraceMonitor";
		String DT_MONITOR_PATH = "nonblocked/" + Uem.DT_MONITOR;
		String CONNECTION_TYPE_WIFI = "WIFI";
		String CONNECTION_TYPE_MOBILE = "Mobile";

		interface Url {

			String START = "orange.jsf";
			String REVIEW = "orange-booking-review.jsf";
			String PAYMENT = "orange-booking-payment.jsf";
			String PURCHASE = "orange-booking-finish.jsf";
			String TERMS = "legal-orange.jsf";
			String PRIVACY = "privacy-orange.jsf";
			String CONTACT = "contact-orange.jsf";
			String SEO = "seo";
            String SEO_ABOUT = "about";
            String SEO_CONTACT = "contact";
            String ABOUT = "about-orange.jsf";
			String LOGOUT = "j_invalidate_session";
			String HOME = BaseConstants.EMPTY_STRING;
			String LOGIN = "Account/LogOn";
			String B2BLOGOUT = "Account/LogOff";
			String JOURNEY = "Journey";
			String REPORT = "Report";
			String BOOKING = "Booking";
			String MOBILE_LOGIN = "services/AuthenticationService/authenticate";
			String MOBILE_BOOKING = "services/BookingService/storeBooking";
			String MOBILE_SEARCH = "services/JourneyService/findJourneys";
			String MOBILE_SEARCH_AS_YOU_TYPE = "services/JourneyService/findLocations";
			String TRIPDETAILS = "orange-trip-details.jsf";
			String BLOGDETAILS = "blog/";
			String MOBILE_CALCULATE_RECOMMENDATIONS = "CalculateRecommendations";
			String MOBILE_CONTACT = "contact-orange-mobile.jsf";
			String MOBILE_TERMS = "legal-orange-mobile.jsf";
			String MOBILE_PRIVACY = "privacy-orange-mobile.jsf";
			String WEATHERFORECAST = "forecast";
			String IMAGE_GALLERY = "image_gallery";
			String SPECIAL = "special-offers.jsp";

			String WORDPRESS_SHOP_START_PAGE = "";
			String WORDPRESS_SHOP_BLOG = "index.php/blog/";
			String WORDPRESS_SHOP_ACCESSORIES = "index.php/product-category/accessories-clothing/";
			String WORDPRESS_SHOP_CLOTHING = "index.php/product-category/clothing/";
			String WORDPRESS_SHOP_DECOR = "index.php/product-category/decor/";
			String WORDPRESS_SHOP_HOODIES = "index.php/product-category/hoodies-clothing/";
			String WORDPRESS_SHOP_MUSIC = "index.php/product-category/music/";
			String WORDPRESS_SHOP_TSHIRTS = "index.php/product-category/tshirts-clothing/";
			String WORDPRESS_SHOP_PRODUCT = "index.php/product/";

			String AMP = "/";
		}

		interface Title {

			String START = "easyTravel - One step to happiness";
			String REVIEW = "easyTravel - Booking - Your Journey";
			String PAYMENT = "easyTravel - Booking - Payment";
			String PURCHASE = "easyTravel - Booking - Finish";
			String TERMS = "easyTravel - Terms of Use";
			String PRIVACY = "easyTravel - Privacy Policy";
			String CONTACT = "easyTravel - Contact";
			String ABOUT = "easyTravel - About";
			String SEO = "easyTravel - SEO";
			String LOGOUT = "easyTravel - Logout";
			String HOME = "easyTravel B2B Site";
			String JOURNEY = HOME;
			String LOGIN = HOME;
			String B2BLOGOUT = HOME;
			String REPORT = HOME;
			String BOOKING = HOME;
			String TRIPDETAILS = "easyTravel - Your Journey";
			String BLOGDETAILS = "Blog posts";
			String WEATHERFORECAST = "Weather Station";
			String IMAGE_GALLERY = "Angular JS Image Gallery";
			String SPECIAL = "easyTravel - Special Offers";

			String WORDPRESS_SHOP_START_PAGE = "DynaShop - Just another WordPress site";
			String WORDPRESS_SHOP_BLOG = "Blog - DynaShop";
			String WORDPRESS_SHOP_ACCESSORIES = "Accessories - DynaShop";
			String WORDPRESS_SHOP_CLOTHING = "Clothing - DynaShop";
			String WORDPRESS_SHOP_DECOR = "Decor - DynaShop";
			String WORDPRESS_SHOP_HOODIES = "Hoodies - DynaShop";
			String WORDPRESS_SHOP_MUSIC = "Music - DynaShop";
			String WORDPRESS_SHOP_TSHIRTS = "Tshirts - DynaShop";
			String WORDPRESS_SHOP_PRODUCT = "Product - DynaShop";

			String AMP_TITLE = "easyTravel AMP website";
		}


		interface Action {

			String LOGIN = "Login";
			String SEARCH = "Search";
			String BOOK = "Book Now";
			String DETAIL= "Image";
			String BLOG= "Blog";
			String NEXT = "Next";
			String FINISH = "Finish";
            String SEO = "Seo";
            String ABOUT = "About";
            String CONTACT = "Contact";
            String NEW_SEARCH = "New Search";
            String BACK = "Back";
            String CLEAR = "[Clear]";
            String MOBILE_BOOK = "bookJourney";
			String MOBILE_BOOK_SOAP = "SoapCall_storeBooking";
			String MOBILE_LOGIN_ANDROID = "DoLogin";
			String MOBILE_LOGIN_IOS = "performLogin";
			String MOBILE_LOGIN_IOS_ANIMATE = "animateLogin";
			String MOBILE_LOGIN_SOAP = "SoapCall_authenticate";
			String MOBILE_SEARCH_ANDROID = "searchJourney";
			String MOBILE_SEARCH_SOAP = "SoapCall_findJourneys";
			String MOBILE_SEARCH_IOS = "performSearch";
			String MAGENTO_SHOP_START_PAGE = "magentoShopStart";
			String MAGENTO_SHOP_TRAVEL_GEAR_PAGE = "magentoShopTravelGear";
			String MAGENTO_SHOP_SALE_PAGE = "magentoShopSalePage";
			String MAGENTO_SHOP_ACCESSORIES_PAGE = "magentoShopAccessoriesPage";
    	}

		interface Argument {

			String JOURNEY_ID = "journeyId";
			String USER_NAME = "userName";
			String PASSWORD = "password";
			String CREDIT_CARD = "creditCard";
			String AMOUNT = "amount";
			String AUTHENTICATE = "authenticate";
			String DESTINATION = "destination";
			String FROM_DATE = "fromDate";
			String TO_DATE = "toDate";
			String BOOKING_PRICE = "BookingPrice";
			String NAME = "name";
			String CHECK_FOR_JOURNEYS = "checkForJourneys";
			String MAX_RESULT_SIZE = "maxResultSize";
		}

		interface Response {

			String BOOKING_ID = "booking ID";
			String ERROR = "Error";
			String PAYMENT_SUCCESS = "<redirect url=\"/orange-booking-finish.jsf?journeyId";
			String JOURNEY_FOUND = "JourneysFound";
			String NO_JOURNEY_FOUND = "NoJourneysFound";
			String BOOK_JOURNEY_AMOUNT = "bookJourneyAmount";
			String BOOK_JOURNEY_DESTINATION = "bookJourneyDestination";
			String BOOKING_FAILED = "bookingFailed";
			String LOGIN_SUCCESSFUL = "LoginSuccessful";
			String LOGIN_FAILED = "LoginFailed";
			String SESSION_EXPIRED = "Session has expired";
		}

		interface DtCookieName {
			String DT_COOKIE = "dtCookie";
			String DT_TIMER_COOKIE = "dtTC";
			String DT_PAGE_COOKIE = "dtPC";
			String DT_LATENCY_COOKIE = "dtLatC";
		}

	}

	public interface Apache {
		final String PROXY				= "Proxy";
		final String BALANCER_MEMBER	= "BalancerMember";
		final String AJP				= "ajp";
		final String ROUTE				= "route";
		final String CONNECTION_TIMEOUT	= "connectiontimeout";
		final String RETRY				= "retry";

	}

	public interface Http {

		public static final String PROTOCOL = "http";

		public interface Headers {

			String X_FORWARDED_FOR = "x-forwarded-for";
			String USER_AGENT = "user-agent";
			String REFERER = "Referer";
			String CONTENT_TYPE = "Content-Type";
			String FACES_REQUEST = "Faces-Request";
			String SET_COOKIE = "Set-Cookie";
			String CACHE_CONTROL = "Cache-Control";
		}

		public interface Method {

			String GET = "GET";
			String POST = "POST";
		}
	}

	public interface Persistence {

		interface Cassandra {

			int DEFAULT_RPC_PORT = 9160;
		}
	}

	public enum UrlType {
		APACHE_JAVA_FRONTEND,
        NGINX_JAVA_FRONTEND,
		APACHE_B2B_FRONTEND,
		NGINX_B2B_FRONTEND,
		APACHE_PROXY,
		APACHE_BUSINESS_BACKEND,
		APACHE_ANGULAR_FRONTEND,
		NGINX_ANGULAR_FRONTEND;
	}

	public enum ProcedureId {
		CUSTOMER_FRONTEND,
		BUSINESS_BACKEND,
		APACHE_HTTPD_WEBSERVER,
		B2B_FRONTEND,
		PAYMENT_BACKEND,
		UNKNOWN;
	}

	public interface Version {
		String VERSION_MAJOR = "version.major";
		String VERSION_MINOR = "version.minor";
		String VERSION_REVISION = "version.revision";
		String VERSION_BUILDNUMBER = "version.buildnumber";

		// which version do we use as default in some places, e.g. WindowsService, HeaderPanel
		public static final String DEFAULT_DYNATRACE_VERSION = "6.5";
	}

    public static class Images {
	    public static final String HEADER_EASY_TRAVEL = "/images/headerEasyTravel.png";
	    public static final String HEADER_APM_EASY_TRAVEL = "/images/headerAPMEasyTravel.png";
	    public static final String THIRDPARTY_ADVERTISMENT_IMAGE_TEMPLATE = "<img class=\"iceGphImg\" src=\"{0}\" alt=\"promotion\"></img>";
    }

	public static class Labels {

		public static final String CLASSIC_SERVER = "AppMon Server";
		public static final String APM_SERVER = "Dynatrace";

	}

	public static interface Security {
		// JAAS
		String JAAS_LOGIN_MODULE_CONFIG_FILE = "login-module.config";
		String JAAS_LOGIN_MODULE_CONFIG_SYSTEM_PROPERTY = "java.security.auth.login.config";
		String JAAS_LOGIN_CONTEXT_NAME = "WebLauncher";

        // users
		String WEB_LAUNCHER_USERS = "webLauncherUsers.txt";

		// HttpServlet names
		String LOGIN_SERVLET = "login";
		String LOGOUT_SERVLET = "logout";

		String MAIN_SERVLET = "main";
		String SERVICE_LOGIN_SERVLET = "ServiceLogin";

		// HttpSession attributes
		String SUBJECT_SESSION_ATTRIBUTE = "javax.security.auth.subject";

		// jsp pages
		String JSP_LOGIN_PAGE = "login.jsp";

	}
}
