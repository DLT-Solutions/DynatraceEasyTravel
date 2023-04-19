package com.dynatrace.easytravel.spring;

/**
 * Collection of extension points and their locations.
 * The concept is a hierarchical nesting of extension points and their locations.
 *
 * @author philipp.grasboeck
 */
public class PluginConstants {

    // === backend tier ===
    public static final String BACKEND = "backend";

    // extension point: backend.authenticationservice
	public static final String BACKEND_AUTHENTICATION_SERVICE = BACKEND + ".authenticationservice";
    public static final String BACKEND_AUTHENTICATE = BACKEND_AUTHENTICATION_SERVICE + ".authenticate";
    public static final String BACKEND_AUTHENTICATE_GETUSER = BACKEND_AUTHENTICATE + ".getuser";
    public static final String BACKEND_TENANT_AUTHENTICATE = BACKEND_AUTHENTICATION_SERVICE + ".tenant.authenticate"; // UNUSED
    public static final String BACKEND_USER_ROLES = BACKEND_AUTHENTICATION_SERVICE + ".user.roles"; // UNUSED
    public static final String BACKEND_USER_PASSWORD = BACKEND_AUTHENTICATION_SERVICE + ".user.password"; // UNUSED
    public static final String BACKEND_USER_ALL = BACKEND_AUTHENTICATION_SERVICE + ".user.getall"; // UNUSED
    public static final String BACKEND_TENANT_ALL = BACKEND_AUTHENTICATION_SERVICE + ".tenant.getall"; // UNUSED

    // extension point: backend.bookingservice
	public static final String BACKEND_BOOKING_SERVICE = BACKEND + ".bookingservice";
    public static final String BACKEND_BOOKING_STORE_BEFORE = BACKEND_BOOKING_SERVICE + ".booking.store.before";
    public static final String BACKEND_BOOKING_STORE = BACKEND_BOOKING_SERVICE + ".booking.store";
    public static final String BACKEND_BOOKING_BY_TENANT_PAGE = BACKEND_BOOKING_SERVICE + ".booking.bytenant.page"; // UNUSED

    // extension point: backend.journeyservice
	public static final String BACKEND_JOURNEY_SERVICE = BACKEND + ".journeyservice";
    public static final String BACKEND_JOURNEY_ADD = BACKEND_JOURNEY_SERVICE + ".journey.add";
    public static final String BACKEND_LOCATION_SEARCH_BEFORE = BACKEND_JOURNEY_SERVICE + ".location.search.before";
    public static final String BACKEND_LOCATION_SEARCH = BACKEND_JOURNEY_SERVICE + ".location.search";
    public static final String BACKEND_LOCATION_MATCHING = BACKEND_JOURNEY_SERVICE + ".location.matching";
    public static final String BACKEND_JOURNEY_SEARCH = BACKEND_JOURNEY_SERVICE + ".journey.search";
    public static final String BACKEND_LOCATION_ACTION_BEFORE = BACKEND_JOURNEY_SERVICE + ".location.action.before";
    public static final String BACKEND_LOCATION_ADD = BACKEND_JOURNEY_SERVICE + ".location.add"; // UNUSED
    public static final String BACKEND_LOCATION_DELETE = BACKEND_JOURNEY_SERVICE + ".location.delete"; // UNUSED
    public static final String BACKEND_JOURNEY_DELETE = BACKEND_JOURNEY_SERVICE + ".journey.delete"; // UNUSED
    public static final String BACKEND_JOURNEY_BY_TENANT = BACKEND_JOURNEY_SERVICE + ".journey.get.bytenant"; // /UNUSED
    public static final String BACKEND_JOURNEY_BY_TENANT_PAGE = BACKEND_JOURNEY_SERVICE + ".journey.get.bytenant.page"; // UNUSED
    public static final String BACKEND_LOCATION_ALL_PAGE = BACKEND_JOURNEY_SERVICE + ".location.all.page"; // UNUSED
    public static final String BACKEND_LOCATION_ALL = BACKEND_JOURNEY_SERVICE + ".location.getall"; // UNUSED
    public static final String BACKEND_JOURNEY_GETALL = BACKEND_JOURNEY_SERVICE + ".journey.getall"; // UNUSED
    public static final String BACKEND_JOURNEY_GETALLNAMES = BACKEND_JOURNEY_SERVICE + ".journey.getallnames"; // UNUSED
    public static final String BACKEND_JOURNEY_GETINDEX = BACKEND_JOURNEY_SERVICE + ".journey.getindex";
    public static final String BACKEND_DESTINATION_CHECK = BACKEND_JOURNEY_SERVICE + ".destination.check";
    public static final String BACKEND_JOURNEY_VALIDATENAME = BACKEND_JOURNEY_SERVICE + ".validatename";
   
    // === dataaccess tier ===

    // extension point: dataaccess
    public static final String DATAACCESS = "dataaccess";
    public static final String DATAACESS_INTERCEPT_QUERY = DATAACCESS + ".query.intercept";

    // === nativeapp tier ===

    // extension point: nativeapp
    public static final String NATIVEAPP = "nativeapp";
    public static final String NATIVEAPP_SENDANDRECEIVE = NATIVEAPP + ".sendAndReceive";

    // === periodics ===
    public static final String PERIODIC = "periodic";

    // extension point: periodic.execute
    public static final String PERIODIC_EXECUTE = PERIODIC + ".execute";


    // === frontend tier ===
	public static final String FRONTEND = "frontend";

    // extension point: frontend.loginlogic
    public static final String FRONTEND_LOGIN_LOGIC = FRONTEND + ".loginlogic";
    public static final String FRONTEND_LOGIN = FRONTEND_LOGIN_LOGIC + ".login";
    public static final String FRONTEND_PASSWORD = FRONTEND_LOGIN_LOGIC + ".password"; // UNUSED

    // extension point: frontend.dataprovider
    public static final String FRONTEND_DATA_PROVIDER = FRONTEND + ".dataprovider";
    public static final String FRONTEND_JOURNEY_SEARCH = FRONTEND_DATA_PROVIDER + ".journey.search";
    public static final String FRONTEND_JOURNEY_SEARCH_AFTER = FRONTEND_DATA_PROVIDER + ".journey.search.after";
    //public static final String FRONTEND_DESTINATION_CHECK = FRONTEND_DATA_PROVIDER + ".destination.check";

    // extension point: frontend.socialmedia
    public static final String FRONTEND_SOCIALMEDIA = FRONTEND + ".socialmedia";
    public static final String FRONTEND_SOCIALMEDIA_FOOTER = FRONTEND_SOCIALMEDIA + ".footer";

    // extension point: frontend.search
    public static final String FRONTEND_SEARCH = FRONTEND + ".search";
    public static final String FRONTEND_SEARCH_JOURNEY_PAGE = FRONTEND_SEARCH + ".journey.page";

    // extension point: frontend.tripdetails.page
    public static final String FRONTEND_TRIPDETAILS = FRONTEND + ".tripdetails";
    public static final String FRONTEND_TRIPDETAILS_PAGE = FRONTEND_TRIPDETAILS + ".page";
    public static final String FRONTEND_TRIPDETAILS_WEATHER_FORECAST_LINK = FRONTEND_TRIPDETAILS_PAGE + ".weatherforecast";

    // extension point: frontend.results.after/before
    public static final String FRONTEND_RESULTS = FRONTEND + ".results";
    public static final String FRONTEND_RESULTS_BEFORE = FRONTEND_RESULTS + ".before";
    public static final String FRONTEND_RESULTS_AFTER = FRONTEND_RESULTS + ".after";

    // extension point: frontend.page
    public static final String FRONTEND_PAGE = FRONTEND + ".page";
    public static final String FRONTEND_PAGE_EXECUTE = FRONTEND_PAGE + ".execute";
    public static final String FRONTEND_PAGE_RESOURCE = FRONTEND_PAGE + ".resource";
    public static final String FRONTEND_PAGE_HEADINJECTION = FRONTEND_PAGE + ".headinjection";
    public static final String FRONTEND_PAGE_HEADER = FRONTEND_PAGE + ".header";
    public static final String FRONTEND_PAGE_FOOTER = FRONTEND_PAGE + ".footer";
    public static final String FRONTEND_PAGE_FOOTER_SCRIPT = FRONTEND_PAGE + ".footerscript";
    public static final String FRONTEND_PAGE_CONTENT_FINISH = FRONTEND_PAGE + ".content.finish";

    // extension point: frontend.travellers.account
    public static final String FRONTEND_TRAVELLERS_ACCOUNT = FRONTEND + ".travellers.account";

    // extension point: frontend.javascript
    public static final String FRONTEND_JAVASCRIPT = FRONTEND + ".javascript";
	public static final String FRONTEND_JAVASCRIPT_BOOTSTRAP = FRONTEND + ".javascript.bootstrapagent";

    // extension point: frontend.booking
    public static final String FRONTEND_BOOKING = FRONTEND + ".booking";
    public static final String FRONTEND_BOOKING_VALIDATE_PAYMENT = FRONTEND_BOOKING + ".validatepayment";
    public static final String FRONTEND_BOOKING_PERFORM_BOOKING = FRONTEND_BOOKING + ".performbooking";

    // extension point: frontend.promotion
    public static final String FRONTEND_PROMOTION = FRONTEND + ".promotion";
    public static final String FRONTEND_PROMOTION_CLICK = FRONTEND_PROMOTION + ".click";

    public static final String FRONTEND_RESOURCE = FRONTEND + ".resource";
    public static final String FRONTEND_RESOURCE_CACHING = FRONTEND_RESOURCE + ".caching";

    public static final String FRONTEND_JQUERY = FRONTEND + ".jquery";
    public static final String FRONTEND_JQUERY_PATHS = FRONTEND_JQUERY + ".paths";

    public static final String FRONTEND_THIRDPARTYADSERVER = FRONTEND + ".adserver";

    // extension point: frontend.webfonts
    public static final String FRONTEND_WEBFONTS = FRONTEND + ".webfonts";

    // extension point: frontend.couchDB
    public static final String FRONTEND_IMAGEDB = FRONTEND + ".couchdb";

    // extension point: frontend.resources
    public static final String FRONTEND_RESOURCES = FRONTEND + ".resources";

    // extension point: WSMQ_NATIVEAPP
    public static final String WSMQ_NATIVEAPP_SENDANDRECEIVE = "wsmqNativeapp.sendAndReceive";

    // extension point: frontend.media
    public static final String FRONTEND_MEDIA = FRONTEND + ".media";
    public static final String FRONTEND_MEDIA_HOMEPAGE = FRONTEND_MEDIA + ".homepage";
    public static final String FRONTEND_MEDIA_RESOURCE = FRONTEND_MEDIA + ".resource";

    // extension point: uem.load
    public static final String UEM_LOAD = "extension.point.uem.load";
    public static final String UEM_LOAD_DCRUM = UEM_LOAD + ".dcrum";
    public static final String UEM_LOAD_WORLDMAP = UEM_LOAD + ".worldmap";
    public static final String UEM_LOAD_MOBILE = UEM_LOAD + ".mobile";

    /**
     * Life cycle extension points.
     * These plugin points are executed no matter if they are enabled or not,
     * provided they are interested in the corresponding lifecycle extension point.
     */
    public static final String LIFECYCLE = "lifecycle";
    public static final String LIFECYCLE_BACKEND_START = LIFECYCLE + ".backend.start";
    public static final String LIFECYCLE_FRONTEND_START = LIFECYCLE + ".frontend.start";
    public static final String LIFECYCLE_BACKEND_SHUTDOWN = LIFECYCLE + ".backend.shutdown";
    public static final String LIFECYCLE_FRONTEND_SHUTDOWN = LIFECYCLE + ".frontend.shutdown";
    public static final String LIFECYCLE_PLUGIN_ENABLE = LIFECYCLE + ".plugin.enable";
    public static final String LIFECYCLE_PLUGIN_DISABLE = LIFECYCLE + ".plugin.disable";

    // for web.xml
    public static final String EXTENSION_POINT_START = "extension.point.start";
    public static final String EXTENSION_POINT_SHUTDOWN = "extension.point.shutdown";
}
