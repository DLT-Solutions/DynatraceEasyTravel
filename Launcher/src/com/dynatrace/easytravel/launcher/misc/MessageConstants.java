package com.dynatrace.easytravel.launcher.misc;

public class MessageConstants {

    public static final String BUTTON_STOP = "Stop";
    public static final String BUTTON_ADD = "Add";

    public static final String COMMANDLINE_LAUNCHER = "CmdLauncher";

    public static final String PORT_SEEMS_TO_BE_IN_USE_PREFIX = "The ports {0} are required by easyTravel, but are blocked by some other application.\n\n" +
            "Because of this some easyTravel applications might not start correctly, either configure different ports or stop the application that is blocking these ports.\n\n" +
            "Note: If .NET Applications are started in IIS, warnings about these ports can be ignored.";
    public static final String PORT_ALREADY_IN_USE = "Port(s) already in use";

	public static final String LAUNCHER_INCOMPATIBILITY = "easyTravel Console incompatibility";

	public static final String REMOTE_LAUNCHER_VERSION_DETECTION = "Differences between easyTravel versions in distributed environments were detected. To fix this problem install the same version of application on every host.\n\n{0}";

	public static final String REMOTE_LAUNCHER_VERSION = "Remote Launcher: Host {0}, Version {1}";

	public static final String NO_CMD_LAUNCHER = "Remote Launcher is not available on host {0}";

	public static final String MASTER_LAUNCHER_VERSION = "Master Launcher: Host {0}, Version {1}";

    public static final String DOT_NET_AGENT_SETUP = ".NET Agent Setup";
    public static final String COR_PROFILER_NOT_SET = "The environment variable ''COR_PROFILER'' is not defined in the system wide settings and not in the easyTravel Configuration.\n\n" +
            "Usually this is set to define which dynaTrace .NET Agent is used. If you are running .NET procedures from IIS or have set up the UltiDevCassini procedures in the .NET Agent Config Tool, this might be ok.";

    public static final String DOT_NET_PROCEDURES_MIGHT_NOT_WORK_TITLE = "Problematic version of Windows detected";
    public static final String DOT_NET_PROCEDURES_MIGHT_NOT_WORK = "easyTravel has a known problem with the version of Windows that you are running (Windows XP or Windows Server 2003)\n" +
            "which may cause the .NET applications to not start correctly.\n\n" +
            "Processing data will still work normally, but the .NET procedures may not be reported as fully started."
            /* + "\n\n" +
            "Do you want to disable .NET procedures and start them manually instead?"*/;

    public static final String COULD_NOT_READ_PLUGIN_STATE_TITLE = "Could not read plugin state from ''{0}'': {1}";
    public static final String COULD_NOT_READ_PLUGIN_STATE_DESC = "Although the Backend was started, plugin state could not be read: {0}";

    public static final String DEVTEAM_SCENARIO_UNITTESTING_TITLE = "Unit/Integration Testing";
    public static final String DEVTEAM_SCENARIO_WEBAPITESTING_TITLE = "Web API Testing";
    public static final String DEVTEAM_SCENARIO_UNITTESTING_DESC = "You have unit tests and would like to quickly see how they behave? Overall execution time of the unit test suite decreased significantly, but which actual code change caused it?\n\nSee how unit test results and timings are automatically recorded, combined, reported, alerted and aggregated using the Test Center Edition features in Dynatrace.";
    public static final String DEVTEAM_SCENARIO_WEBAPITESTING_DESC = "How does the response size of my SOAP or REST APIs change with different parameters? Is the API sending a correct response code when parameters are missing or something goes wrong?\n\nThis scenario also catch architectural regression by providing insight into the server-side processing: How many calls to a database or to an internal web service are made per API call? How many exceptions are thrown? How many log messages are written? By monitoring these numbers over time you can ensure that your API meets our performance goals and catch potential problems early in the development lifecycle.";
    public static final String DEVTEAM_SCENARIO_FUNCTIONALTESTING_TITLE = "Functional Website Testing";
    public static final String DEVTEAM_SCENARIO_FUNCTIONALTESTING_DESC = "You think it too much hassle to do functional testing of your web application? Think again how hard it is to fix issues when users are already hitting the site! Wouldn't it be nice to \"know\" that the Website will still work after the latest fixes?\n\nThis scenario executes a set of functional web tests and shows how dynaTrace will not only alert you on broken tests, but also when tests run slower than expected. Where dynaTrace even learns itself, what \"expected\" is!";
    public static final String DEVTEAM_SCENARIO_LOADTESTING_TITLE = "Load Test Recording";
    public static final String DEVTEAM_SCENARIO_LOADTESTING_DESC = "Do you record your application performance while handling a certain load? Can you predict the behavior of your system when deploying to the production environment? A diary to look-up your progress on scalability can be priceless when your task is to give a comprehensive report about scalability progress.";
    public static final String DEVTEAM_SCENARIO_BUILDBREAK_TITLE = "Who broke the build?";
    public static final String DEVTEAM_SCENARIO_BUILDBREAK_DESC = "How does your development team handle the blame-game? Tests are failing, but how do you find out for sure where to look and who to talk to?\n\nRun this scenario to see how dynaTrace can collect various attributes related to test runs and thus will allow you to see, which change by whom is to blame.";
    public static final String DEVTEAM_SCENARIO_PERFREG_TITLE = "Performance Regression";
    public static final String DEVTEAM_SCENARIO_PERFREG_DESC = "You did a hard days work to get the release done. Now it only needs to pass some final tests before shipping and earning money can begin. But wait, the performance test shows a big drop in transaction throughput, how can that be? And why didn't we see that earlier?\n\nUse this scenario to see how dynaTrace helps you to run performance tests regularly and how to monitor their execution time automatically to inform you about degradations as soon as they are introduced.";

    public static final String EASYTRAVEL_ABSTRACT = "Browse trips to many exciting destinations and book your vacation with the easyTravel demo web application. \n" +
            "There are always some interesting offers available, so don't hesitate to step on board and fly one of those.\n" +
            "Behind the scenes it uses various technologies to demonstrate how dynaTrace helps your business and your customers. \n" +
            "It includes multiple application tiers across Browser, Java Web Application Server, Microsoft .NET web application, Backend servers, Database access and a native application \n" +
            "as well as some specialized procedures in some of the scenarios, e.g. Cassandra, PHP, ....";
	public static final String EASYTRAVEL_LINK_BROWSER = "Web:";
    public static final String EASYTRAVEL_LINK_DOTNET = "Business2Business Frontend (.NET):";
	public static final String EASYTRAVEL_LINK_MOBILE_NATIVE = "Mobile App:";
	public static final String EASYTRAVEL_LINK_MOBILE_BROWSER = "Mobile Web:";
    public static final String EASYTRAVEL_INSTALL_SYSTEMPROFILE = "Install System Profile";
    public static final String EASYTRAVEL_PLUGINNAME = "easyTravel-plugin.dtp";
    public static final String EASYTRAVEL_PLUGINNAME_55 = "easyTravel-plugin-for-5.5.0.dtp";

    public static final String PLUGIN_NOT_FOUND_TITLE = "Cannot install system profile";
    public static final String PLUGIN_NOT_FOUND = "The plugin for system profile and dashboards cannot be found at the expected location ''{0}''. \n\nCannot automatically install the system profile.";

    public static final String SHUTDOWN_WEBLAUNCHER_TITLE = "Shut down";
    public static final String SHUTDOWN_WEBLAUNCHER = "Please note that if you shut down the launcher application you need to access to the system console in order to run easyTravel again, are you sure you want to do that?";

    public static final String PLUGIN_MANUAL_INSTALL_TITLE = "Cannot automatically install system profile";
    public static final String PLUGIN_MANUAL_INSTALL = "Cannot automatically install the system profile and dashboards on this platform.\n\nPlease install the plugin manually.\n\nThe location of the plugin was copied into the system clipboard, you can now install it in the dynaTrace Client via Tools -> Manage Plugins -> Install Plugin.";

    public static final String PLUGIN_REST_INSTALL_TITLE = "System profile installation";
    public static final String PLUGIN_REST_INSTALL = "Installation of the system profile plugin was started in the running dynaTrace Client, please finish installation there by following the additional instructions.";

    public static final String PLUGIN_REST_OPEN_DASHBOARD_TITLE = "Open Dashboard";
    public static final String PLUGIN_REST_OPEN_DASHBOARD_ERROR = "The REST call for opening the Dashboard in the dynaTrace Client reported additional information: \n\n{0}";

    public static final String EXIT = "Shutdown";
	public static final String LOGOUT = "Logout";

	public static final String USER_LOGED_IN = "User: {0}";

    // this is the name of the logfiles!
    public static final String LAUNCHER = "Launcher";
    public static final String WEBLAUNCHER = "WebLauncher";
    public static final String CMD_LAUNCHER = "CmdLauncher";

    public static final String MENU_ITEM_NOT_AVAILABLE = "This feature is not applicable to your set up. Learn more about what this feature can do <a>here</a>.";

    public static final String MODULE_DERBY_DATABASE_MANAGEMENT_SYSTEM = "Derby Database";
    public static final String MODULE_MYSQL_DATABASE_MANAGEMENT_SYSTEM = "MySQL Database";
    public static final String MODULE_DATABASE_CONTENT_CREATOR = "Database Content Creator";
    public static final String MODULE_COUCHDB_CONTENT_CREATOR = "CouchDB Content Creator";
    public static final String MODULE_COUCHDB_CONTROLLER = "CouchDB";
    public static final String MOUDLE_MYSQL_CONTENT_CREATOR = "MySQL Content Creator";
    public static final String MODULE_BUSINESS_BACKEND = "Business Backend (Java)";
	public static final String MODULE_CUSTOMER_FRONTEND = "Customer Frontend (Java)";
	public static final String MODULE_ANGULAR_FRONTEND = "Angular Frontend REST Service (Java)";
    public static final String MODULE_CREDITCARD_AUTHORIZATION = "Credit Card Auth. (C++)";
    public static final String MODULE_PAYMENT_BACKEND = "Payment Backend (.NET)";
    public static final String MODULE_B2B_FRONTEND = "B2B Frontend (.NET)";
    public static final String MODULE_PATTERN_ANT = "Ant Script {0}";
    public static final String MODULE_CANOO_LOAD_TESTS = "Canoo Load Test (Ant)";
    public static final String MODULE_JUNIT_TESTS = "JUnit Tests (Ant)";
    public static final String MODULE_WEBAPI_TESTS = "WebAPI Tests (Ant)";
    public static final String MODULE_WEBDRIVER_TESTS_ONCE = "Run WebDriver Tests (Ant)";
    public static final String MODULE_APACHE_HTTPD = "Apache Webserver";
    public static final String MODULE_NGINX = "Nginx Webserver";
    public static final String MODULE_UNHOOKABLE_HTTPD = "Unhookable Webserver";
    public static final String MODULE_CASSANDRA = "Cassandra";
    public static final String MODULE_MONGO_DB = "MongoDB";
	public static final String MODULE_HBASE = "HBase";
    public static final String MOULE_APACHE_HTTPD_PHP = "Apache Webserver with PHP";
    public static final String MODULE_THIRDPARTY_SERVER = "Third Party Content Server";
    public static final String MODULE_BROWSER = "Web Browser";
    public static final String MODULE_WEBSERVER_AGENT_RESTART = "Web Server Agent Control";
    public static final String MODULE_HOST_AGENT_RESTART = "Host Agent Control";
    public static final String MODULE_PLUGIN_SERVICE = "Plugin Service";
	public static final String MODULE_PREPARE_VMWARE = "Prepare VMware";
	public static final String MODULE_VMOTION = "vMotion";
	public static final String MODULE_VAGRANT = "Vagrant";

    public static final String NO_PLUGIN_DESCRIPTION_AVAILABLE = "Plugin description not available";

    public static final String NO_PLUGINS_AVAILABLE_TITLE = "Plugins not available";
    public static final String NO_PLUGINS_AVAILABLE_DESC = "Plugins are only available if applications are started";

    public static final String NOT_AVAILABLE = "N/A";

    public static final String PRODUCTION_SCENARIO_STANDARD_TITLE = "Standard";
    public static final String PRODUCTION_SCENARIO_STANDARD_DESC = "Business is running fine, users are visiting the web site and are booking trips and everything seems to be ok? Are you sure? Or are some of your users actually heading off to other sites because they are dissatisfied?\n\nChoose this scenario to run the demo application with its default settings to show how dynaTrace gives you a quick overview of an application and how you can monitor its health.";
    public static final String PRODUCTION_SCENARIO_UPDATE_REGRESSION_TITLE = "Update Regression";
    public static final String PRODUCTION_SCENARIO_UPDATE_REGRESSION_DESC = "Your development just finished the next release and you are going to put this into test/production. How do you find out if there are regressions? In performance or functionality?\n\nStart this scenario and explore how dynaTrace visualizes problem patterns for you.";
    public static final String PRODUCTION_SCENARIO_BLACK_FRIDAY_TITLE = "Black Friday";
    public static final String PRODUCTION_SCENARIO_BLACK_FRIDAY_DESC = "There are days during the year where load is exceptionally high on your servers. How do you find out if your server is able to cope?\n\nRun this scenario to bring the application to its limits and see how dynaTrace allows you to drill down to the root problems.";
    public static final String PRODUCTION_SCENARIO_CASSANDRA_TITLE = "Cassandra";
	public static final String PRODUCTION_SCENARIO_CASSANDRA_DESC = "Find out whether introducing a Cassandra solution can make a competitive advantage for your company. This easyTravel Cassandra scenario reveals the benefits.";
    public static final String PRODUCTION_SCENARIO_MONGODB_TITLE = "MongoDB";
    public static final String PRODUCTION_SCENARIO_MONGODB_DESC = "Find out whether introducing a MongoDB solution can make a competitive advantage for your company. This easyTravel MongoDB scenario reveals the benefits.";
	public static final String PRODUCTION_SCENARIO_HBASE_TITLE = "HBase";
	public static final String PRODUCTION_SCENARIO_HBASE_DESC = "Find out whether HBase is the appropriate datastore for the purposes of your company. HBase is an open source, non-relational, distributed database modeled after Google's BigTable. Among other big companies, Facebook uses HBase for its Messaging Platform.\n\nAttention: You need to manually start the HBase datastore as it is not started by the scenario.";
    public static final String PRODUCTION_SCENARIO_JMS_TITLE = "Messaging";
    public static final String PRODUCTION_SCENARIO_JMS_DESC = "Your application is heavily relying on Queuing services? Are your service providers telling you they do not see the messages on the receiving end, but your developers assure you that they are sending the correct data? Are you concerned about the size of messages going back and forth?\n\nRun this scenario to use JMS messaging to get additional 'Hot Deals' whenever searches are performed. Use the Messaging Dashlet to get an overview of how much data you send on which queue and Drill Down to see the actual data that was sent.";
    public static final String PRODUCTION_SCENARIO_RMI_TITLE = "Remoting";
    public static final String PRODUCTION_SCENARIO_RMI_DESC = "Your application is heavily using RMI to call remote procedures? Are you struggling to find out why the call never reaches the server? Are you concerned about the number of remote invocations?\n\nUse this scenario to enable Java RMI remoting to get additional 'Hot Deals' whenever searches are performed. Use the Remoting Dashlet to get an overview of the remoting usage in your application.";
    public static final String PRODUCTION_SCENARIO_DCRUM_TITLE = "DC-RUM";
    public static final String PRODUCTION_SCENARIO_DCRUM_DESC = "You want to integrate dynaTrace with a DC-RUM appliance and want to know how it works. Using this scenario you can easily emulate a DC-RUM appliance by easyTravel.";

    public static final String DEMO_STORY_BOARD_SCENARIO_TITLE = "Best Practices";
    public static final String DEMO_STORY_BOARD_SCENARIO_DESCRIPTION = "Users are visiting the web site from all over the world and want to book trips. Is everything running fine in your application? Use this scenario in combination with the DemoStoryBoard to walk through the most important use-cases.";

    public static final String UEM_SCENARIO_MINIMAL_TITLE = "Lightweight";
    public static final String UEM_SCENARIO_MINIMAL_DESC = "Backend application monitoring shows \"all green\", but how can you monitor how well visitors to your websites are served?\n\nUse this scenario when running on machines with low processing power and memory to simulate visitors to your website from all over the world with varying response times depending on geographical location. Let dynaTrace show you from where visitors come to your site and how satisified they will be.";
    public static final String UEM_SCENARIO_DEFAULT_TITLE = "Standard";
    public static final String UEM_WITH_REST_SCENARIO_DEFAULT_TITLE = "Standard with REST Service and Angular2 frontend";
    public static final String UEM_WITH_REST_AND_PHP_SCENARIO_DEFAULT_TITLE = "Standard with REST Service, PHP and Angular2 frontend";
    public static final String UEM_WITH_NGINX_SCENARIO_DEFAULT_TITLE = "Standard with Nginx";
    public static final String UEM_SCENARIO_DEFAULT_DESC = "Backend application monitoring shows \"all green\", but how can you monitor how well visitors to your websites are served?\n\nUse this scenario to simulate visitors to your website from all over the world with varying response times depending on geographical location. Let dynaTrace show you from where visitors come to your site and how satisified they will be.";
    public static final String UEM_WITH_REST_SCENARIO_DEFAULT_DESC = "This scenario works like the standard UEM scenario. Additionally a second frontend server is started (port 9080 per default) which serves a fancy Angular2 frontend.";
    public static final String UEM_WITH_REST_AND_PHP_SCENARIO_DEFAULT_DESC = "This scenario works like Angular scenario with addition of PHP on Apache and MySQL DB.";
    public static final String PHP_SCENARIO_DEFAULT_TITLE = "UEM with PHP";
    public static final String PHP_SCENARIO_DEFAULT_DESC = "Your web application uses a combination of Java, .NET as well as PHP and you are looking for a way to troubleshoot all of them?\n\nUse this scenario to add functionality for user-rating journeys in easyTravel via a PHP application. This shows you how seamlessly information from the PHP agent is available with dynaTrace in an application landscape where PHP is part of the 'technology mix'.";
    public static final String UEM_SCENARIO_WEBSERVER_TITLE = "Via Apache Webserver";
    public static final String UEM_SCENARIO_WEBSERVER_DESC = "This scenario works like the standard UEM scenario, but it additionally starts an Apache HTTP webserver as load balancer.";
    public static final String UEM_SCANARIO_BASELINING_DEMO = "Baselining Demo";
    public static final String UEM_SCANARIO_BASELINING_DEMO_DESC = "How do you distinguish between variations in load or response time which are ok compared to high response times which are a problem. Just looking at averages does not work in this case.\n\nUse this scenario to show how dynaTrace uses baselining technology to alert when things start behaving incorrectly. In this scenario the \"SlowUserLogin\" problem pattern will get activated 12 minutes after startup of the Businessbackend, staying on for 3 minute (repeating this sequence in an endless loop). We expect some \"Failure Rates\" baselining incidents.\n\nThe problem pattern \"JourneyUpdateSlow\" and \"DisableHibernateCache\" will be activated 15 minutes after start, staying on for 5 minutes (repeating this sequence in an endless loop).  We expect some \"Response Time\" and \"Slow Response Time\" baselining incidents.\n\nBe aware that this scenario consumes significantly more memory than the default UEM scenario. Thus, the B2B frontend is disabled by default.";
    public static final String UEM_SCENARIO_UEM_ONLY_TITLE = "UEM Only";
    public static final String UEM_SCENARIO_UEM_ONLY_DESC = "You need to know about the user experience of your customers? How long does it take to load a page for your end users? Which browsers, tablets or smartphones are your customers using for accessing your page and which impact has the performance to your conversion rate? \n\nUse this scenario for an UEM Only setup. No Java and .NET Agents are used, only the web server agents and UEM is running.";
    public static final String UEM_SCANARIO_PROXY_INJECTION = "Proxy Injection via Apache Web Server";
    public static final String UEM_SCANARIO_PROXY_INJECTION_DESC = "With this scenario UEM can be tested on web sites where dynaTrace UEM is not installed yet. An apache web server is started, which acts as a proxy injecting the JavaScript agent. Further a browser is started with corresponding proxy settings.\n\nThis scenario supports browsers using the system proxy configuration (e.g. Internet Explorer or Chrome).";
    public static final String UEM_SCENARIO_COUCHDB_TITLE = "CouchDB";
    public static final String UEM_SCENARIO_COUCHDB_DESC = "Standard scenario, but with CouchDB used to serve some of the images.";

    public static final String SCENARIO_GROUP_PRODCUTION_TITLE = "Production";
    public static final String SCENARIO_GROUP_TESTCENTER_TITLE = "Test Center";
    public static final String SCENARIO_GROUP_DEVTEAM_TITLE = "Development Team";
    public static final String SCENARIO_GROUP_UEM_TITLE = "UEM";
    public static final String SCENARIO_GROUP_MAINFRAME_TITLE = "Mainframe";
    public static final String SCENARIO_GROUP_PLUGINS = "Problem Patterns"; // note: tab is last tab group tab

    public static final String STATE_FINISHED = "finished";
    public static final String STATE_NOT_RUNNING = "not running";
    public static final String STATE_OPERATING = "operating";
    public static final String STATE_RUNNING = "running";
    public static final String STATE_STARTING = "starting";
    public static final String STATE_STOPPING = "stopping";
    public static final String STATE_UNKNOWN = "unknown state";
    public static final String STATE_FAILED = "failed";
    public static final String STATE_ACCESS_DENIED = "access denied";
    public static final String STATE_TIMEOUT = "startup timeout";

    public static final String TESTCENTER_SCENARIO_PERFORMANCE_TITLE = "Performance Regression";
    public static final String TESTCENTER_SCENARIO_PERFORMANCE_DESC = "Users are complaining about unbearable slowness? Is it a real problem or just their network connection. Is it making you feel comfortable to rely on users calling you instead of heading off to visit some other site?\n\nEnable some performance problems in different areas here which shows how dynaTrace can visualize slow transactions before the user even knows it.";
    public static final String TESTCENTER_SCENARIO_MEMORY_TITLE = "Running out of Memory";
    public static final String TESTCENTER_SCENARIO_MEMORY_DESC = "Your application is slowly using more and more memory and nobody can figure out? Your only remedy is to restart the application with downtime and user complaints following en-suite?\n\nThis scenario causes a small memory leak in the business backend application and allows to show how dynaTrace can track memory usage over time and how the sophisticated compare functionality allows to drill down to the root cause quickly.";
    public static final String TESTCENTER_SCENARIO_SCALABILITY_TITLE = "Scalability Flaws";
    public static final String TESTCENTER_SCENARIO_SCALABILITY_DESC = "When you started everything was fine? Your servers happily coped with the load the few users caused. But with more users things quickly become 'complicated, more transactions, more load. The system seems to be at it's limit, but CPU and I/O are not maxed out? Looks like you have poor scalability in your application. So where do you begin? How do you investigate where scalability is lost?\n\nRun the load tests included in this scenario to investigate the scalability problems of the demo application in dynaTrace.";
    public static final String TESTCENTER_SCENARIO_INCREASING_LOAD_TITLE = "Increasing Load";
    public static final String TESTCENTER_SCENARIO_INCREASING_LOAD_DESC = "In your work as QA manager, you get questions like 'How much load can we put onto the system? Can you give us numbers?'. How do you find out where scalability reaches its limit and throughput actually decreases if you further increase the load?\n\nSee how your servers cope with an increasing number of visits. This scenario will ramp up load over time to make the web application busier, until scalability limits are reached. Open the Dashboard 'easyTravel Performance Overview' and the 'Analyze WebRequests' item in Start Center under 'Analyze Performance' and watch and analyze by comparing performance related numbers like throughput, response time and other metrics over time.";

    public static final String MAINFRAME_SCENARIO_WSMQ_TITLE = "Credit Card Verification via WebSphere MQ";
    public static final String MAINFRAME_SCENARIO_WSMQ_DESC = "This scenario uses Websphere MQ for credit card verification. This can either be handled by using a WebSphere MessageBroker message flow or a CICS transaction server.";
    public static final String MAINFRAME_SCENARIO_CICS_TITLE = "Credit Card Verification via CICS Transaction Gateway";
    public static final String MAINFRAME_SCENARIO_CICS_DESC = "This scenario uses CTG for credit card verification. CTG is used to send a request to a CICS transaction server.";
    public static final String MAINFRAME_SCENARIO_IMS_TITLE = "Credit Card Verification via IMS";
    public static final String MAINFRAME_SCENARIO_IMS_DESC = "This scenario uses IMS for credit card verification. The request is sent using the IBM IMS TM Resource Adapter.";

    public static final String UNKNOWN = "unknown";
    public static final String UNKNOWN_VERSION = "unknown version";

    public static final String CONTEXT_START = "&Start";
    public static final String CONTEXT_START_REMOTE = "&Start";
    public static final String CONTEXT_STOP = "S&top";
    public static final String CONTEXT_START_STOP_6_HOURS = "Start and stop after 6 hours";
    public static final String CONTEXT_START_STOP_12_HOURS = "Start and stop after 12 hours";
    public static final String CONTEXT_START_STOP_1_DAY = "Start and stop after 1 day";
    public static final String CONTEXT_START_STOP_3_DAYS = "Start and stop after 3 days";
    public static final String CONTEXT_RESTART_ALL = "&Restart all";
    public static final String CONTEXT_RESTART = "&Restart";
    public static final String CONTEXT_OPEN_IN_BROWSER = "&Open in Browser";
    public static final String CONTEXT_NO_ACTION = "No action available";
    public static final String CONTEXT_LOGFILE = "Show &Logfile";
    public static final String CONTEXT_SCENARIO = "Show &Scenario File";
    public static final String CONTEXT_USER_SCENARIO = "Show &User Scenario File";
    public static final String CONTEXT_PROPERTIES = "Show &Properties";
    public static final String CONTEXT_LOCAL_PROPERTIES = "Show &Local Properties";
    public static final String CONTEXT_ADD = "&Add";
    public static final String CONTEXT_CONTINUOUSLY = "&Continuously";
    public static final String CONTEXT_ANT_SCRIPT = "Show &Ant Script";
    public static final String CONTEXT_PROBLEM_PATTERNS_SHOW = "Show &Problem Patterns";
	public static final String CONTEXT_PROBLEM_PATTERNS_HIDE = "Hide &Problem Patterns";
	public static final String CONTEXT_CONFIGURE_HOSTS_FILE = "&Configure www.easytravel.com";
    public static final String CONTEXT_STATUS = "Status pa&ge";
    public static final String CONTEXT_BALANCE_MANAGER = "&Balancer Manager";
    public static final String CONTEXT_DETAILS = "&Details";

    // Apache HPTTD related menu items
    public static final String CONTEXT_ACCESS_LOGFILE = "Show &access.log";
    public static final String CONTEXT_ERROR_LOGFILE = "Show &error.log";
    public static final String CONTEXT_HTTPD_CONF = "Show &httpd.conf";
    public static final String CONTEXT_PHP_INI = "Show &php.ini";

    // MySQL related menu items
    public static final String CONTEXT_MYSQL_INI = "Show &my.ini";
    public static final String CONTEXT_MYSQL_LOG = "Show &error.log";

    // copied from class WidgetUtil as we do not want to include rap-classes in Launcher directly
    public static final String CUSTOM_WIDGET_ID = "org.eclipse.rap.rwt.customWidgetId";

    // the following ids are set to some widgets to have HTML-IDs in the resulting web-pages for Configuration Web-UI
    // See APM-5912 for details
    public static final String ID_STOP_BUTTON = "id_stop_button";
    //public static final String ID_START_BUTTON = "id_start_button";
    //public static final String ID_RESTART_BUTTON = "id_restart_button";
    public static final String ID_PROC_COMPONENT = "id_proc_component";
    public static final String ID_CONFIG_BUTTON = "id_config_button";
    public static final String ID_PAGE_AREA = "id_page_area";
    public static final String ID_PAGE_AREA_SCROLL = "id_page_area_scroll";
    public static final String ID_DOT_NET_CHECK = "id_dot_net_check";
	public static final String ID_BROWSER_CHECK = "id_browser_check";
	public static final String ID_MOBILE_NATIVE_CHECK = "id_mobile_native_check";
	public static final String ID_MOBILE_BROWSER_CHECK = "id_mobile_browser_check";
    public static final String ID_TRAFFIC_SLIDER = "id_traffic_slider";
    //public static final String ID_BATCH_PANEL = "id_batch_panel";
    //public static final String ID_MENU_PAGE_PANEL = "id_menu_page_panel";
    //public static final String ID_PROBLEM_PATTERN_MENU_PAGE_PANEL = "id_problem_pattern_menu_page_panel";
    //public static final String ID_PAGE_SCROLL_CONTENT = "id_page_scroll_content";

    public static final String TRAFFIC = "Traffic";
    public static final String TAGGED_REQUESTS = "Synthetic Web Requests";
    public static final String TAGGED_REQUESTS_HINT = "If enabled, web requests will be tagged so they \n" +
                                                        "are recognized by dynaTrace as simulated requests, \n" +
                                                        "which allows to show CI related scenarios like load testing, ...";
    public static final String GENERATE_VISITS = "Generated visits";
    public static final String GENERATE_VISITS_HINT = "If enabled, visits are generated automatically \n" +
                                                        "where the rate of visits is based on the slider position.";
    public static final String CREATE_VISITS_IMMEDIATELY = "Manual visits";
    public static final String CREATE_VISITS_IMMEDIATELY_HINT = "If enabled, you can generate visits manually \n" +
                                                                "pressing the corresponding button";
	public static final String CREATE_CUSTOMER_VISITS_BUTTON = "{0} Browser {1}";
	public static final String CREATE_BROWSER_WEB_VISITS_BUTTON = "{0} Browser Web {1}";
    public static final String CREATE_B2B_VISITS_BUTTON = "{0} B2B {1}";
	public static final String CREATE_MOBILE_NATIVE_VISITS_BUTTON = "{0} Mobile Native {1}";
	public static final String CREATE_MOBILE_BROWSER_VISITS_BUTTON = "{0} Mobile Browser {1}";
	public static final String MOBILE_NATIVE_VISITS_SLIDERTOOLTIP = "Mobile App: {0} visits/min";
	public static final String MOBILE_BROWSER_VISITS_SLIDERTOOLTIP = "Mobile Web: {0} visits/min";
    public static final String VISITS_ID = "VISITS";

    public static final String DOTNET_CHECKBOX_HINT_TEXT = "Using this checkbox you can enable and disable all .NET procedures of easyTravel, which are not running with IIS.\n" +
                                                         "Be aware that .NET instances running out of IIS will be terminated!\n" +
                                                         "In any case on disable .NET procedures DummyPaymentService is used instead of DotNetPaymentService!";
	public static final String BROWSER_CHECKBOX_HINT_TEXT = "By using this checkbox you can enable and disable all desktop browser visits.";
	public static final String MOBILE_NATIVE_CHECKBOX_HINT_TEXT = "By using this checkbox you can enable and disable all mobile native visits.";
	public static final String MOBILE_BROWSER_CHECKBOX_HINT_TEXT = "By using this checkbox you can enable and disable all mobile browser visits.";

    public static final String SCENARIO_LABEL_TEXT = "Used scenario: {0}";
    public static final String STANDARD_LOAD_SCENARIO = "Standard Load";
    public static final String FIXED_LOAD_SCENARIO = "Constant Load";
    public static final String PREDICTABLE_LOAD_SCENARIO = "Predictable Load";

	public static String getAdaptedVisitString(int selected) {
		return (selected != 1) ? "visits" : "visit";
	}
}
